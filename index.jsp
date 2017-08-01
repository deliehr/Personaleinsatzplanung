<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="Store.*" %>
<%@ page import="Model.*" %>
<%@ page import="Default.*" %>
<%@ page import="java.util.List" %>
<%@ page import="Schedule.Schedule" %>


<!DOCTYPE html>
<html>
    <head>
        <title>Personaleinsatzplanung</title>
        <script type="text/javascript" src="js/jquery-3.2.1.js"></script>
        <script type="text/javascript" src="js/functions.js"></script>
        <link rel="stylesheet" type="text/css" href="css/default.css">
    </head>

    <body>
        <div class="divHoverConstraint">Constraint added</div>

        <%@ include file="menu.html" %>

        <%
            session.setAttribute("store_id", 0);    // set store id

            Store store = new Store((int) session.getAttribute("store_id"));
            OptimizationModel model = new OptimizationModel(OptimizationModel.Format.AMPL);
            model.setBasePath(application.getRealPath("/").replace('\\', '/'));
            session.setAttribute("store", store);
        %>

        <%
            if(store.readStoreFromDatabase()) {
                // region variables
                // region decision variables
                // early and late shifts
                for(Employee e:store.getEmployeeList()) {
                    // early shifts
                    VariableGroup group = new VariableGroup(String.format("group_%s_early", e.getVariableShortcut()));
                    for(int d=1;d <= 6;d++) {
                        group.getVariables().add(new DecisionVariable(Variable.Type.BINARY, String.format("%s_%d_f", e.getVariableShortcut(), d), false));
                    }
                    model.getVariableGroups().add(group);

                    // late shifts
                    group = new VariableGroup(String.format("group_%s_late", e.getVariableShortcut()));
                    for(int d=1;d <= 6;d++) {
                        group.getVariables().add(new DecisionVariable(Variable.Type.BINARY, String.format("%s_%d_s", e.getVariableShortcut(), d), false));
                    }
                    model.getVariableGroups().add(group);
                }

                // alternating shifts for trainees
                VariableGroup alternatingShifts = new VariableGroup("alternatingShifts");
                for(Employee e:store.getAllTrainees()) {
                    alternatingShifts.getVariables().add(new DecisionVariable(Variable.Type.BINARY, String.format("%s_trainee_early", e.getVariableShortcut()), false));
                }
                model.getVariableGroups().add(alternatingShifts);
                // endregion

                // region error variables
                // total sum of all employees
                VariableGroup singleErrorVariables = new VariableGroup("single_errors");
                singleErrorVariables.getVariables().add(new ErrorVariable(Variable.Type.DECIMAL, String.format("e_%s", ErrorType.TOTAL_MAX_HOURS_LESS), false));
                model.getVariableGroups().add(singleErrorVariables);

                // less hours per week
                VariableGroup errorLessOursPerWeek = new VariableGroup("less_hours_per_week");
                for(Employee e:store.getEmployeeList()) {
                    errorLessOursPerWeek.getVariables().add(new ErrorVariable(Variable.Type.DECIMAL, String.format("e_%s_%s", ErrorType.MAX_HOURS_LESS, e.getVariableShortcut()), false));
                }
                model.getVariableGroups().add(errorLessOursPerWeek);
                // endregion
                // endregion

                // region parameters
                // hours for each employee
                for(Employee e:store.getEmployeeList()) {
                    ParameterGroup group = new ParameterGroup(String.format("group_%s", e.getVariableShortcut()));

                    for(int d=1;d <= 6;d++) {
                        group.getParameters().add(new Parameter(String.format("h_%s_%d", e.getVariableShortcut(), d), e.getHours()[d-1]));
                    }
                    model.getParameterGroups().add(group);
                }
                // endregion

                // region target function
                ObjectiveFunction function = new ObjectiveFunction(ObjectiveFunction.Type.MINIMIZE, "costs");
                model.setObjectiveFunction(function);
                function.addVariableGroupAsSummands(singleErrorVariables, true);
                function.addVariableGroupAsSummands(errorLessOursPerWeek, true);
                // endregion

                // region restrictions
                // region (restrictions) 1. early- xor late shifts
                RestrictionGroup rGroupEarlyOrLate = new RestrictionGroup("early_xor_late");
                for(Employee e:store.getEmployeeList()) {
                    for(int d=1;d <= 6;d++) {
                        Restriction r = new Restriction(String.format("r_early_xor_late_%s_%d", e.getVariableShortcut(), d), Restriction.Relation.LESS_OR_EQUAL);
                        r.getLeftSide().getSummands().add(new Restriction.Side.Summand(String.format("%s_%d_f", e.getVariableShortcut(), d)));
                        r.getLeftSide().getSummands().add(new Restriction.Side.Summand(String.format("%s_%d_s", e.getVariableShortcut(), d)));
                        r.getRightSide().getSummands().add(new Restriction.Side.Summand("1"));
                        rGroupEarlyOrLate.getRestrictions().add(r);
                    }
                }
                model.getRestrictionGroups().add(rGroupEarlyOrLate);
                // endregion

                // region (restrictions) 2. min one of them (StoreAdministration, FirstSubstituion, SecondSubstitution)
                RestrictionGroup groupOneAdmin = new RestrictionGroup("minOneAdmin");
                for(int d=1;d <= 6;d++) {
                    Restriction r = new Restriction(String.format("r_min_one_admin_%d", d), Restriction.Relation.GREATER_OR_EQUAL);

                    for(Employee e:store.getAllAdministrators()) {
                        r.getLeftSide().getSummands().add(new Restriction.Side.Summand(String.format("%s_%d_f", e.getVariableShortcut(), d)));
                        r.getLeftSide().getSummands().add(new Restriction.Side.Summand(String.format("%s_%d_s", e.getVariableShortcut(), d)));
                    }
                    r.getRightSide().getSummands().add(new Restriction.Side.Summand("1"));

                    groupOneAdmin.getRestrictions().add(r);
                }
                model.getRestrictionGroups().add(groupOneAdmin);
                // endregion

                // region (restrictions) 3. max weekly hours
                RestrictionGroup group = new RestrictionGroup("max_weekly_hours");
                for(Employee e:store.getEmployeeList()) {
                    Restriction r = new Restriction(String.format("r_max_weekly_hours_%s", e.getVariableShortcut()), Restriction.Relation.EQUAL);

                    // early- and late shift
                    for(int d=1;d <= 6;d++) {
                        r.getLeftSide().getSummands().add(new Restriction.Side.Summand(String.format("h_%s_%d * (%s_%d_f + %s_%d_s)", e.getVariableShortcut(), d, e.getVariableShortcut(), d, e.getVariableShortcut(), d)));
                    }

                    // error variable
                    r.getLeftSide().getSummands().add(new Restriction.Side.Summand(String.format("e_%s_%s", ErrorType.MAX_HOURS_LESS.name().toLowerCase(), e.getVariableShortcut())));
                    r.getRightSide().getSummands().add(new Restriction.Side.Summand(String.format("%d", e.getMaxHoursWeek())));

                    group.getRestrictions().add(r);
                }
                model.getRestrictionGroups().add(group);
                // endregion

                // region (restrictions) 4. shifts
                RestrictionGroup rGroupShifts = new RestrictionGroup("shifts");
                for(int d=1;d <= 6;d++) {
                    // early shifts
                    Restriction r1 = new Restriction(String.format("r_min_shifts_%d_f", d), Restriction.Relation.GREATER_OR_EQUAL);
                    for(Employee e:store.getEmployeeList()) {
                        r1.getLeftSide().getSummands().add(new Restriction.Side.Summand(String.format("%s_%d_f", e.getVariableShortcut(), d)));
                    }

                    // shifts
                    int shifts = 0;
                    switch (d) {
                        case 1:case 6: { shifts = 3; break; }
                        default: { shifts = 2; break; }
                    }
                    r1.getRightSide().getSummands().add(new Restriction.Side.Summand(String.format("%d", shifts)));

                    // late shifts
                    Restriction r2 = new Restriction(String.format("r_min_shifts_%d_s", d), Restriction.Relation.GREATER_OR_EQUAL);
                    for(Employee e:store.getEmployeeList()) {
                        r2.getLeftSide().getSummands().add(new Restriction.Side.Summand(String.format("%s_%d_s", e.getVariableShortcut(), d)));
                    }

                    shifts = 0;
                    switch (d) {
                        case 1:case 6: { shifts = 3; break; }
                        default: { shifts = 3; break; }
                    }
                    r2.getRightSide().getSummands().add(new Restriction.Side.Summand(String.format("%d", shifts)));

                    rGroupShifts.getRestrictions().add(r1);
                    rGroupShifts.getRestrictions().add(r2);
                }
                model.getRestrictionGroups().add(rGroupShifts);
                // endregion

                // region (restrictions) 5. trainee, only early- or late shift
                // min 2 traines ...
                RestrictionGroup rGroupEarlyTrainees = new RestrictionGroup("early_trainees");
                List<Employee> allTrainees = store.getAllTrainees();
                if(allTrainees.size() >= 2) {
                    // for each trainee, day overlap
                    for(int i=1;i <= allTrainees.size()-1;i++) {
                        Restriction r = new Restriction(String.format("r_early_trainee_%d", i), Restriction.Relation.EQUAL);
                        r.getLeftSide().getSummands().add(new Restriction.Side.Summand(String.format("%s_trainee_early", allTrainees.get(i-1).getVariableShortcut())));
                        r.getLeftSide().getSummands().add(new Restriction.Side.Summand(String.format("%s_trainee_early", allTrainees.get(i).getVariableShortcut())));
                        r.getRightSide().getSummands().add(new Restriction.Side.Summand("1"));
                        rGroupEarlyTrainees.getRestrictions().add(r);
                    }
                }

                // only early or late shift
                for(Employee e:allTrainees) {
                    for(int d=1;d <= 6;d++) {
                        Restriction r1 = new Restriction(String.format("r_early_trainee_%s_%d_s", e.getVariableShortcut(), d), Restriction.Relation.LESS_OR_EQUAL);
                        Restriction r2 = new Restriction(String.format("r_early_trainee_%s_%d_f", e.getVariableShortcut(), d), Restriction.Relation.LESS_OR_EQUAL);

                        r1.getLeftSide().getSummands().add(new Restriction.Side.Summand(String.format("%s_%d_s", e.getVariableShortcut(), d)));
                        r1.getRightSide().getSummands().add(new Restriction.Side.Summand("1"));
                        r1.getRightSide().getSummands().add(new Restriction.Side.Summand(String.format("%s_trainee_early", e.getVariableShortcut()), false));

                        r2.getLeftSide().getSummands().add(new Restriction.Side.Summand(String.format("%s_%d_f", e.getVariableShortcut(), d)));
                        r2.getRightSide().getSummands().add(new Restriction.Side.Summand(String.format("%s_trainee_early", e.getVariableShortcut())));

                        rGroupEarlyTrainees.getRestrictions().add(r1);
                        rGroupEarlyTrainees.getRestrictions().add(r2);
                    }
                }
                model.getRestrictionGroups().add(rGroupEarlyTrainees);
                // endregion

                // region (restrictions) 6. max hours per week of all employees
                RestrictionGroup groupMaxHoursAllEmployees = new RestrictionGroup("max_hours_per_week_of_all_employees");
                Restriction restrictionMaxHoursAll = new Restriction("r_hours_all_employees", Restriction.Relation.EQUAL);

                for(int d=1;d <= 6;d++) {
                    for(Employee e:store.getEmployeeList()) {
                        restrictionMaxHoursAll.getLeftSide().getSummands().add(new Restriction.Side.Summand(String.format("h_%s_%d * (%s_%d_f + %s_%d_s)", e.getVariableShortcut(), d, e.getVariableShortcut(), d, e.getVariableShortcut(), d)));
                    }
                }

                restrictionMaxHoursAll.getLeftSide().getSummands().add(new Restriction.Side.Summand(String.format("e_%s", ErrorType.TOTAL_MAX_HOURS_LESS.name().toLowerCase())));
                restrictionMaxHoursAll.getRightSide().getSummands().add(new Restriction.Side.Summand(String.format("%d", store.getScheduledWeekHours())));
                groupMaxHoursAllEmployees.getRestrictions().add(restrictionMaxHoursAll);
                model.getRestrictionGroups().add(groupMaxHoursAllEmployees);
                // endregion
                // endregion

                // try to solve
                // presolve, bfp, dual, first solution, set bb depth, bb depth
                model.integrateManualRestrictions(store);
                model.solve(true, true, true, true, false, 1);
                model.interpretProcessOutcome();
                model.setEmployeeShifts(store);
                model.setErrorVariables(store);
            } else {
                out.println("Store konnte nicht aus der Datenbank geladen werden");
            }
        %>



        <fieldset class="plan">
            <legend>Zeitplan</legend>

                <%
                    Schedule schedule = new Schedule();
                    schedule.setModel(model);
                    schedule.setStore(store);
                    out.println(schedule.displaySchedule());
                %>
        </fieldset>

        <fieldset>
            <legend>Einstellungen</legend>

            <div style="margin-top: 10px;">
                <%
                    out.println(schedule.displayManualConstraints((int) session.getAttribute("store_id")));
                %>
            </div>
        </fieldset>




        <!--
        <div style="flex-basis: 25%; font-family: Menlo; font-size: 12px;">

            <div style="display: flex; flex-direction: row; flex-wrap: wrap;">
                <%
                    // read model variable outcomes
                    for(VariableGroup group:model.getVariableGroups()) {
                        for(Variable variable:group.getVariables()) {
                            switch (variable.getType()) {
                                default:
                                case BINARY:
                                case INTEGER: {
                                    out.println(String.format("<div style='margin-right: 30px;'>%s -> %d (%s)</div>", variable.getName(), (int) variable.getOutcome(), variable.getType().name()));
                                    break;
                                }
                                case DECIMAL: {
                                    out.println(String.format("<div style='margin-right: 30px;'>%s -> %2.2f (%s)</div>", variable.getName(), (double) variable.getOutcome(), variable.getType().name()));
                                    break;
                                }
                            }
                        }
                    }
                %>
            </div>
        </div>
        -->

        <script type='text/javascript'>
            addHoverFunctions();
            addConstraintFunctions();
        </script>
    </body>
</html>