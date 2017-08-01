package Model;

import Default.ErrorType;
import Schedule.Planning;
import Store.Employee;
import Store.Store;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * New instance of an optimization model. Contains decision variables, parameters, a target function and restrictions
 */
public class OptimizationModel {
    // region object variables
    private List<VariableGroup> variableGroups = new ArrayList<VariableGroup>() {
        @Override
        public boolean add(VariableGroup group) {
            for(int i=0;i < this.size();i++) {
                if(this.get(i).getName().equals(group.getName())) return false;
            }

            return super.add(group);
        }
    };
    private List<ParameterGroup> parameterGroups = new ArrayList<ParameterGroup>() {
        @Override
        public boolean add(ParameterGroup parameterGroup) {
            for(int i=0;i < this.size();i++) {
                if(this.get(i).getName().equals(parameterGroup.getName())) return false;
            }

            return super.add(parameterGroup);
        }
    };
    private List<RestrictionGroup> restrictionGroups = new ArrayList<RestrictionGroup>() {
        @Override
        public boolean add(RestrictionGroup restrictionGroup) {
            for(int i=0;i < this.size();i++) {
                if(this.get(i).getName().equals(restrictionGroup.getName())) return false;
            }

            return super.add(restrictionGroup);
        }
    };
    private ObjectiveFunction objectiveFunction = null;
    private boolean solved = false;
    private Format modelFormat;
    private String basePath = "";
    private List<String> processOutcomeLines = new ArrayList<String>();
    private OutcomeType outcomeType;
    private double objectiveValue = 0.0;
    // endregion

    // region constructors
    public OptimizationModel(Format format) {
        this.setModelFormat(format);
    }

    public OptimizationModel(ObjectiveFunction function, Format format) {
        this.objectiveFunction = function;
        this.setModelFormat(format);
    }
    // endregion

    // region object methods
    /**
     * Method for solving the model.
     * Gets the model full text, stores it in a file and run the solver application.
     * The command line output will be interpreted afterwards.
     * @param presolve With presolving analysis.
     * @param withBFP Uses the basic factorisation package. Reduce time to solve.
     * @param dualSolution In the first and second approach: use the dual solver.
     * @param takeFirstSolution Use first found solution.
     */
    public boolean solve(boolean presolve, boolean withBFP, boolean dualSolution, boolean takeFirstSolution, boolean setBBDepth, int bBDepth) {
        if(this.isSolved()) {
            return false;
        }

        // model
        File modelFile = new File(this.getBasePath() + "model.mod");
        if(modelFile.exists()) {
            modelFile.delete();
        }

        try {
            // create new model file
            modelFile.createNewFile();

            // write model to file
            PrintWriter modelWriter = new PrintWriter(String.format("%smodel.mod", this.getBasePath()), "UTF-8");
            modelWriter.write(this.getModel());
            modelWriter.close();

            // read output
            try {
                // region create parameters
                List<String> parameterList = new ArrayList<String>();
                parameterList.add(String.format("%slp_solve", this.getBasePath()));

                // presolve
                if(presolve) {
                    parameterList.add("-presolve");
                }

                // basic factor... package
                if(withBFP) {
                    parameterList.add("-bfp");
                    parameterList.add(String.format("%slibbfp_LUSOL.so", this.getBasePath()));
                }

                // model format
                switch (this.getModelFormat()) {
                    default:
                    case AMPL: {
                        parameterList.add("-rxli");
                        parameterList.add(String.format("%slibxli_MathProg.so", this.getBasePath()));
                        break;
                    }
                }

                // model file name
                parameterList.add(String.format("%smodel.mod", this.getBasePath()));

                // take dual solver?
                if(dualSolution) {
                    parameterList.add("-dual");
                }

                // take first found solution?
                if(takeFirstSolution) {
                    parameterList.add("-f");
                }

                // set branch & bound depth?
                if(setBBDepth) {
                    parameterList.add("-depth");
                    parameterList.add(String.valueOf(bBDepth));
                }
                // endregion

                // convert to array
                String[] params = new String[parameterList.size()];
                params = parameterList.toArray(params);

                Runtime runtime = Runtime.getRuntime();
                Process process = runtime.exec(params);
                int exitCode = process.waitFor();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                String line = "";
                this.processOutcomeLines = new ArrayList<String>();
                while((line = reader.readLine()) != null) {
                    processOutcomeLines.add(line);
                }

                // this model was solved
                this.setSolved(true);
                return true;
            } catch (Exception e) {
                System.out.println("Error (OptimizationModel.java [2]): " + e.toString());
            }
        } catch (Exception e) {
            System.out.println("Error (OptimizationModel.java [1]): " + e.toString());
        }

        return false;
    }

    public boolean interpretProcessOutcome() {
        try {
            // check if model is infeasible
            if(this.getProcessOutcomeLines().size() == 1 && this.getProcessOutcomeLines().get(0).contains("infeasible")) {
                // model is infeasible
                // "This problem is infeasible"
                this.setOutcomeType(OutcomeType.INFEASIBLE);
                return true;
            }

            if(this.getProcessOutcomeLines().size() > 1) {
                boolean foundObjectiveValue = false, foundActualValuesLine = false, getSuboptimalSolution = false;

                for(String line:this.getProcessOutcomeLines()) {
                    if(!getSuboptimalSolution && line.toLowerCase().contains("suboptimal")) {
                        getSuboptimalSolution = true;
                        continue;
                    }

                    if(!foundObjectiveValue) {
                        // search for value
                        if(line.toLowerCase().contains("value")) {
                            line = line.replace(" ", "");
                            String[] lineParts = line.split(":");
                            this.setObjectiveValue(Double.valueOf(lineParts[1]));
                            foundObjectiveValue = true;
                        }
                        continue;
                    }

                    if(!foundActualValuesLine) {
                        // search of last line before variables
                        if(line.toLowerCase().contains("actual")) {
                            foundActualValuesLine = true;
                        }
                        continue;
                    }

                    // contains variable?
                    line = line.replaceFirst(" ", ":");
                    line = line.replace(" ", "");
                    String[] lineParts = line.split(":");
                    String variableName = lineParts[0];
                    Double variableOutcome = Double.valueOf(lineParts[1]);

                    if(line.toLowerCase().substring(0, 1).equals("x")) {
                        this.setDecisionVariable(variableName, variableOutcome);
                    }

                    if(line.toLowerCase().substring(0, 1).equals("e")) {
                        this.setErrorVariable(variableName, variableOutcome);
                    }
                }

                if(getSuboptimalSolution) {
                    this.setOutcomeType(OutcomeType.SUBOPTIMAL);
                } else {
                    this.setOutcomeType(OutcomeType.OPTIMAL);
                }
            }

            return true;
        } catch (Exception e) {
            System.out.println("Error (OptimizationModel.java [3]): " + e.toString());
        }

        return false;
    }

    public boolean setEmployeeShifts(Store store) {
        try {
            for(VariableGroup vg:this.getVariableGroups()) {
                for(Variable v:vg.getDecisionVariables()) {
                    String name = v.getName();
                    Object outcome = v.getOutcome();
                    String shortcut = v.getVariableShortcut();

                    for(Employee e:store.getEmployeeList()) {
                        if(shortcut.equals(e.getVariableShortcut())) {
                            if(NumberUtils.isDigits(name.split("_")[3])) {
                                int day = Integer.valueOf(name.split("_")[3]) - 1;
                                String shift = name.split("_")[4];

                                if(shift.equals("f")) {
                                    if((int) outcome == 1) {
                                        e.getEarlyShifts()[day] = true;
                                    }
                                }

                                if(shift.equals("s")) {
                                    if((int) outcome == 1) {
                                        e.getLateShifts()[day] = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            return true;
        } catch (Exception e) {
            System.out.println("Error (OptimizationModel.java [4]): " + e.toString());
        }

        return false;
    }

    public boolean setErrorVariables(Store store) {
        try {
            for(VariableGroup g:this.getVariableGroups()) {
                for(ErrorVariable v:g.getErrorVariables()) {
                    for(Employee e:store.getEmployeeList()) {
                        if(v.getName().contains(e.getVariableShortcut()) && ((double) v.getOutcome()) != 0.0) {
                            addErrorToEmployee(e, v);
                            continue;
                        }
                    }
                }
            }

            return true;
        } catch (Exception e) {
            System.out.println("Error (OptimizationModel.java [5]): " + e.toString());
        }

        return false;
    }

    private boolean addErrorToEmployee(Employee employee, ErrorVariable errorVariable) {
        try {
            if(errorVariable.getName().contains(ErrorType.MAX_HOURS_LESS.name().toLowerCase())) {
                employee.getErrors().add(String.format("<img class='error_image' src='img/error_hour_less.png' title='%s %s arbeitet %2.2f Std. zu wenig.' />", employee.getFirstname(), employee.getLastname(), (double) errorVariable.getOutcome()));
            }

            return true;
        } catch (Exception e) {
            System.out.println("Error (OptimizationModel.java [8]): " + e.toString());
        }

        return false;
    }

    private boolean setDecisionVariable(String name, double value) {
        try {
            // iterate each group, each variable
            for(VariableGroup group:this.getVariableGroups()) {
                for(Variable variable:group.getDecisionVariables()) {
                    if(variable.getName().equals(name)) {
                        variable.setOutcome(value);
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error (OptimizationModel.java [6]): " + e.toString());
        }

        return false;
    }

    private boolean setErrorVariable(String name, double value) {
        try {
            // iterate each group, each variable
            for(VariableGroup group:this.getVariableGroups()) {
                for(Variable variable:group.getErrorVariables()) {
                    if(variable.getName().equals(name)) {
                        variable.setOutcome(value);
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error (OptimizationModel.java [7]): " + e.toString());
        }

        return false;
    }

    public String getModel() {
        StringBuilder model = new StringBuilder();

        switch (this.getModelFormat()) {
            default:
            case AMPL: {
                // variables
                model.append("\n# ---------- variables ----------\n");
                for(VariableGroup g:this.getVariableGroups()) {
                    model.append(g.getVariableGroup(this.getModelFormat()));
                }

                // parameter
                model.append("\n# ---------- parameters ----------\n");
                for(ParameterGroup g:this.getParameterGroups()) {
                    model.append(g.getParameterGroup(this.getModelFormat()));
                }

                // target function
                model.append("\n# ---------- target function ----------\n");
                if(this.getObjectiveFunction() != null) {
                    model.append(this.getObjectiveFunction().getTargetFunction(this.getModelFormat()));
                }

                // restrictions
                model.append("\n# ---------- restrictions ----------\n");
                for(RestrictionGroup g:this.getRestrictionGroups()) {
                    model.append(g.getRestrictionGroup(this.getModelFormat()));
                }
            }
        }

        return model.toString();
    }

    public boolean integrateManualRestrictions(Store store) {
        try {
            Connection connection = Database.Connection.getNewSQLConnection();
            Statement sqlStatement = connection.createStatement();
            String query = String.format("SELECT * FROM AdditionalRestriction WHERE fk_store_id = %d", store.getDatabaseId());
            ResultSet result = sqlStatement.executeQuery(query);
            result.beforeFirst();

            // db fields
            String dvs = "", option = "";
            double optionValue = 0.0;
            int day = 0;

            // new restriction group
            RestrictionGroup g = new RestrictionGroup("externalRestrictions");

            while(result.next()) {
                dvs = result.getString("dvs");
                option = result.getString("option");
                optionValue = result.getDouble("option_value");
                day = result.getInt("day");

                // read planning
                Planning planning = Planning.valueOf(option);

                // set employee
                Employee e = store.getEmployeeByVariableShortcut(dvs);

                switch (planning) {
                    default:
                    case EARLY_SHIFT: {
                        Restriction r = new Restriction();
                        r.setName(String.format("r_ext_%s_%s_%d_f", option.toLowerCase(), dvs, (day + 1)));
                        r.setRelation(Restriction.Relation.EQUAL);
                        r.getLeftSide().getSummands().add(new Restriction.Side.Summand(String.format("%s_%d_f", dvs, (day + 1))));
                        r.getRightSide().getSummands().add(new Restriction.Side.Summand("1"));
                        g.getRestrictions().add(r);
                        break;
                    }
                    case EARLY_SHIFT_WEEK: {
                        Restriction r = new Restriction();
                        r.setName(String.format("r_ext_%s_%s_%d_wf", option.toLowerCase(), dvs, (day + 1)));
                        r.setRelation(Restriction.Relation.EQUAL);
                        r.getLeftSide().getSummands().add(new Restriction.Side.Summand(String.format("%s_%d_s", dvs, 1)));
                        r.getLeftSide().getSummands().add(new Restriction.Side.Summand(String.format("%s_%d_s", dvs, 2)));
                        r.getLeftSide().getSummands().add(new Restriction.Side.Summand(String.format("%s_%d_s", dvs, 3)));
                        r.getLeftSide().getSummands().add(new Restriction.Side.Summand(String.format("%s_%d_s", dvs, 4)));
                        r.getLeftSide().getSummands().add(new Restriction.Side.Summand(String.format("%s_%d_s", dvs, 5)));
                        r.getLeftSide().getSummands().add(new Restriction.Side.Summand(String.format("%s_%d_s", dvs, 6)));
                        r.getRightSide().getSummands().add(new Restriction.Side.Summand("0"));
                        g.getRestrictions().add(r);
                        break;
                    }
                    case LATE_SHIFT: {
                        Restriction r = new Restriction();
                        r.setName(String.format("r_ext_%s_%s_%d_s", option.toLowerCase(), dvs, (day + 1)));
                        r.setRelation(Restriction.Relation.EQUAL);
                        r.getLeftSide().getSummands().add(new Restriction.Side.Summand(String.format("%s_%d_s", dvs, (day + 1))));
                        r.getRightSide().getSummands().add(new Restriction.Side.Summand("1"));
                        g.getRestrictions().add(r);
                        break;
                    }
                    case LATE_SHIFT_WEEK: {
                        Restriction r = new Restriction();
                        r.setName(String.format("r_ext_%s_%s_%d_ws", option.toLowerCase(), dvs, (day + 1)));
                        r.setRelation(Restriction.Relation.EQUAL);
                        r.getLeftSide().getSummands().add(new Restriction.Side.Summand(String.format("%s_%d_f", dvs, 1)));
                        r.getLeftSide().getSummands().add(new Restriction.Side.Summand(String.format("%s_%d_f", dvs, 2)));
                        r.getLeftSide().getSummands().add(new Restriction.Side.Summand(String.format("%s_%d_f", dvs, 3)));
                        r.getLeftSide().getSummands().add(new Restriction.Side.Summand(String.format("%s_%d_f", dvs, 4)));
                        r.getLeftSide().getSummands().add(new Restriction.Side.Summand(String.format("%s_%d_f", dvs, 5)));
                        r.getLeftSide().getSummands().add(new Restriction.Side.Summand(String.format("%s_%d_f", dvs, 6)));
                        r.getRightSide().getSummands().add(new Restriction.Side.Summand("0"));
                        g.getRestrictions().add(r);
                        break;
                    }
                    case FREE: {
                        Restriction r = new Restriction();
                        r.setName(String.format("r_ext_%s_%s_%d_fs", option.toLowerCase(), dvs, (day + 1)));
                        r.setRelation(Restriction.Relation.EQUAL);
                        r.getLeftSide().getSummands().add(new Restriction.Side.Summand(String.format("%s_%d_s", dvs, (day + 1))));
                        r.getLeftSide().getSummands().add(new Restriction.Side.Summand(String.format("%s_%d_f", dvs, (day + 1))));
                        r.getRightSide().getSummands().add(new Restriction.Side.Summand("0"));
                        e.getFreeShifts()[day] = true;
                        g.getRestrictions().add(r);
                        break;
                    }
                    case VACATION: {
                        Restriction r = new Restriction();
                        r.setName(String.format("r_ext_%s_%s_%d_v", option.toLowerCase(), dvs, (day + 1)));
                        r.setRelation(Restriction.Relation.EQUAL);
                        r.getLeftSide().getSummands().add(new Restriction.Side.Summand(String.format("%s_%d_s", dvs, (day + 1))));
                        r.getLeftSide().getSummands().add(new Restriction.Side.Summand(String.format("%s_%d_f", dvs, (day + 1))));
                        r.getRightSide().getSummands().add(new Restriction.Side.Summand("0"));
                        e.getVacationShifts()[day] = true;
                        g.getRestrictions().add(r);
                        break;
                    }
                    case MANUAL_HOURS: {
                        // change parameter value
                        changeParameterValue(String.format("h_%s_%d", e.getVariableShortcut(), (day + 1)), optionValue);
                        e.getHours()[day] = optionValue;
                        break;
                    }
                }


                this.getRestrictionGroups().add(g);
            }

            return true;
        } catch (Exception e) {
            System.out.println("Error (OptimizationModel.java [9]): " + e.toString());
        }

        return false;
    }

    private boolean changeParameterValue(String parameterName, double parameterValue) {
        try {
            for(ParameterGroup g:this.getParameterGroups()) {
                for(Parameter p:g.getParameters()) {
                    if(p.getName().equals(parameterName)) {
                        p.setValue(parameterValue);
                        return true;
                    }
                }
            }
        } catch (Exception e) {

        }

        return false;
    }
    // endregion

    // region getters & setters
    public List<VariableGroup> getVariableGroups() {
        return variableGroups;
    }

    public List<ParameterGroup> getParameterGroups() {
        return parameterGroups;
    }

    public ObjectiveFunction getObjectiveFunction() {
        return objectiveFunction;
    }

    public void setObjectiveFunction(ObjectiveFunction objectiveFunction) {
        this.objectiveFunction = objectiveFunction;
    }

    public List<RestrictionGroup> getRestrictionGroups() {
        return restrictionGroups;
    }

    public Format getModelFormat() {
        return modelFormat;
    }

    public void setModelFormat(Format modelFormat) {
        this.modelFormat = modelFormat;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public List<String> getProcessOutcomeLines() {
        return processOutcomeLines;
    }

    public boolean isSolved() {
        return solved;
    }

    public OutcomeType getOutcomeType() {
        return outcomeType;
    }

    private void setOutcomeType(OutcomeType outcomeType) {
        this.outcomeType = outcomeType;
    }

    private void setSolved(boolean solved) {
        this.solved = solved;
    }

    public double getObjectiveValue() {
        return objectiveValue;
    }

    private void setObjectiveValue(double objectiveValue) {
        this.objectiveValue = objectiveValue;
    }

    // endregion

    // region public static enums
    public enum Format {
        AMPL
    }

    public enum OutcomeType {
        OPTIMAL,
        SUBOPTIMAL,
        INFEASIBLE
    }
    // endregion
}