package Schedule;

import Model.OptimizationModel;
import Store.Employee;
import Store.Store;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class Schedule {
    private OptimizationModel model = null;
    private Store store = null;

    public Schedule() {
    }

    public Schedule(OptimizationModel model, Store store) {
        this.setModel(model);
        this.setStore(store);
    }

    public String displaySchedule() {
        StringBuilder schedule = new StringBuilder();
        schedule.append("<table>");

        // woche
        schedule.append(String.format("<tr><th>Woche:</th><th colspan='%d'>Filiale %d %s - %s</th></tr>", this.getStore().getEmployeeCount(), this.getStore().getStoreNumber(), this.getStore().getPlace(), this.getStore().getStreet()));

        // namen
        schedule.append(String.format("<tr><th>Name:</th>"));
        for(Employee e:this.getStore().getEmployeeList()) {
            schedule.append(String.format("<td>%s</td>", e.getLastname()));
        }
        schedule.append(String.format("</th>"));

        // tätigkeit
        schedule.append(String.format("<tr><th>T&auml;tigkeit:</th>"));
        for(Employee e:this.getStore().getEmployeeList()) {
            schedule.append(String.format("<td>%s</td>", e.getRole().name()));
        }
        schedule.append(String.format("</th>"));

        // wochen stunden
        schedule.append(String.format("<tr><th>Wochen-Std.:</th>"));
        for(Employee e:this.getStore().getEmployeeList()) {
            schedule.append(String.format("<td>%d</td>", e.getMaxHoursWeek()));
        }
        schedule.append(String.format("</th>"));

        // show each day
        for(int d=0;d <= 5;d++) {
            StringBuilder row = new StringBuilder();
            row.append("<tr>");

            switch (d) {
                case 0: row.append("<td class='td_day'>Montag</td>"); break;
                case 1: row.append("<td class='td_day'>Dienstag</td>"); break;
                case 2: row.append("<td class='td_day'>Mittwoch</td>"); break;
                case 3: row.append("<td class='td_day'>Donnerstag</td>"); break;
                case 4: row.append("<td class='td_day'>Freitag</td>"); break;
                default: row.append("<td class='td_day'>Samstag</td>"); break;
            }

            int countEarlyShifts = 0, countLateShifts = 0;
            for(Employee e:store.getEmployeeList()) {
                if(e.getEarlyShifts()[d]) {
                    row.append(this.generateCellContent(e, d, true, false));
                } else if(e.getLateShifts()[d]) {
                    row.append(this.generateCellContent(e, d, false, true));
                } else {
                    row.append(this.generateCellContent(e, d, false, false));
                }

                // count early- and late shifts
                if(e.getEarlyShifts()[d]) {
                    countEarlyShifts++;
                }

                if(e.getLateShifts()[d]) {
                    countLateShifts++;
                }
            }

            row.append(String.format("<td>F: %d<br />S: %d</td>", countEarlyShifts, countLateShifts));

            row.append("</tr>");
            schedule.append(row);
        }

        // gesamtstunden
        schedule.append("<tr><td>Gesamtstunden:</td>");
        int totalSum = 0;
        for(Employee e:store.getEmployeeList()) {
            int sum = 0;

            for(int i=0;i < 6;i++) {
                sum += e.getHours()[i] * (this.convertBoolToInt(e.getEarlyShifts()[i]) + this.convertBoolToInt(e.getLateShifts()[i]));
            }

            totalSum += sum;

            schedule.append(String.format("<td>%d</td>", sum));
        }
        schedule.append(String.format("<td>%d</td>", totalSum));
        schedule.append("</tr>");

        // fehler
        schedule.append("<tr><td></td>");
        for(Employee e:this.getStore().getEmployeeList()) {
            schedule.append("<td>");
            for (String error : e.getErrors()) {
                schedule.append(String.format("%s<br />", error));
            }
            schedule.append("</td>");
        }
        schedule.append("</tr>");

        schedule.append("</table>");

        schedule.append("");

        return schedule.toString();
    }

    public String displayManualConstraints(int storeId) {
        StringBuilder constraints = new StringBuilder();
        constraints.append("<div class='div_manual_constraints'>");

        try {
            Connection connection = Database.Connection.getNewSQLConnection();
            Statement statement = connection.createStatement();
            String query = String.format("SELECT * FROM AdditionalRestriction WHERE fk_store_id = %d ORDER BY id ASC;", storeId);
            ResultSet result = statement.executeQuery(query);
            result.beforeFirst();

            String dvs = "", option = "", dayName = "";
            Double optionValue = 0.0;
            int id = 0, day = 0;

            while (result.next()) {
                id = result.getInt("id");
                dvs = result.getString("dvs");
                option = result.getString("option");
                optionValue = result.getDouble("option_value");
                day = result.getInt("day");
                Planning planning = Planning.valueOf(option);

                // related employee
                Employee e = this.getStore().getEmployeeByVariableShortcut(dvs);

                // dayname
                switch (day) {
                    default:
                    case 0: dayName = "Montag"; break;
                    case 1: dayName = "Dienstag"; break;
                    case 2: dayName = "Mittwoch"; break;
                    case 3: dayName = "Donnerstag"; break;
                    case 4: dayName = "Freitag"; break;
                    case 5: dayName = "Samstag"; break;
                    case 6: dayName = "Sonntag"; break;
                }

                constraints.append("<div class='manual_constraint'>");

                switch (planning) {
                    default:
                    case EARLY_SHIFT: {
                        constraints.append(String.format("[Fr&uuml;hschicht,%s,%s]", e.getLastname(), dayName));
                        break;
                    }
                    case EARLY_SHIFT_WEEK: {
                        constraints.append(String.format("[Wochen-Fr&uuml;hschicht,%s,%s]", e.getLastname(), dayName));
                        break;
                    }
                    case LATE_SHIFT: {
                        constraints.append(String.format("[Sp&auml;tschicht,%s,%s]", e.getLastname(), dayName));
                        break;
                    }
                    case LATE_SHIFT_WEEK: {
                        constraints.append(String.format("[Wochen-Sp&auml;tschicht,%s,%s]", e.getLastname(), dayName));
                        break;
                    }
                    case FREE: {
                        constraints.append(String.format("[Frei,%s,%s]", e.getLastname(), dayName));
                        break;
                    }
                    case VACATION: {
                        constraints.append(String.format("[Urlaub,%s,%s]", e.getLastname(), dayName));
                        break;
                    }
                    case MANUAL_HOURS: {
                        constraints.append(String.format("[Stunden,%s,%s,%2.2f]", e.getLastname(), dayName, optionValue));
                        break;
                    }
                }

                constraints.append(String.format("<button data-id='%d'>X</button></div>", id));
            }
        } catch (Exception e) {
            System.out.println("Error (Schedule.java [1]): " + e.toString());
        }

        constraints.append("</div>");
        return constraints.toString();
    }

    private int convertBoolToInt(boolean value) {
        return value ? 1 : 0;
    }

    private String generateCellContent(Employee e, int day, boolean earlyShift, boolean lateShift) {
        StringBuilder cellContent = new StringBuilder();
        cellContent.append("<td>");

        // are there restrictions associated with this day?
        boolean foundAssociatedRestrictions = false;
        try {
            java.sql.Connection connection = Database.Connection.getNewSQLConnection();
            Statement sqlStatement = connection.createStatement();
            String query = String.format("SELECT * FROM AdditionalRestriction WHERE fk_store_id = %d AND day = %d AND dvs = '%s'", this.store.getDatabaseId(), day, e.getVariableShortcut());
            ResultSet result = sqlStatement.executeQuery(query);
            result.beforeFirst();

            int countRows = 0;
            while(result.next()) {
                countRows++;
            }

            if(countRows > 0) {
                foundAssociatedRestrictions = true;
            }

            connection.close();
        } catch (Exception exc) {
            System.out.println("Error (Schedule.java [2]): " + e.toString());
        }

        // switch shifts
        if(earlyShift && !lateShift) {
            if(foundAssociatedRestrictions) {
                cellContent.append("<div class='td_shift_early_r'");
            } else {
                cellContent.append("<div class='td_shift_early'");
            }
        }

        if(!earlyShift && lateShift) {
            if(foundAssociatedRestrictions) {
                cellContent.append("<div class='td_shift_late_r'");
            } else {
                cellContent.append("<div class='td_shift_late'");
            }
        }

        if(!earlyShift && !lateShift) {
            if(foundAssociatedRestrictions) {
                cellContent.append("<div class='td_shift_no_shift_r'");
            } else {
                cellContent.append("<div class='td_shift_no_shift'");
            }
        }

        if(e.getFreeShifts()[day]) {
            cellContent.append(String.format(" style='background-image: url(\"img/free.png\"); background-size: contain; background-repeat: no-repeat;' "));
        }

        if(e.getVacationShifts()[day]) {
            cellContent.append(String.format(" style='background-image: url(\"img/vacation.png\"); background-size: contain; background-repeat: no-repeat; "));
        }

        cellContent.append(String.format("data-firstName='%s' ", e.getFirstname()));
        cellContent.append(String.format("data-lastName='%s' ", e.getLastname()));
        cellContent.append(String.format("data-dayIndex='%d' ", day));
        cellContent.append(String.format("data-dvs='%s'>", e.getVariableShortcut()));

        if(earlyShift && !lateShift) {
            cellContent.append(String.format("Fr&uuml;h (%2.2f Std.)", e.getHours()[day]));
        }

        if(!earlyShift && lateShift) {
            cellContent.append(String.format("Sp&auml;t (%2.2f Std.)", e.getHours()[day]));
        }

        if(!earlyShift && !lateShift) {
            //cellContent.append("/");
        }

        cellContent.append("</div></td>");
        return cellContent.toString();
    }

    // region getters & setters
    private OptimizationModel getModel() {
        return model;
    }

    public void setModel(OptimizationModel model) {
        this.model = model;
    }

    private Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    // endregion
}