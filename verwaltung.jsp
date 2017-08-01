<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="true" %>
<%@ page import="java.io.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.sql.*" %>
<%@ page import="Store.*" %>
<%@ page import="javax.xml.parsers.DocumentBuilderFactory" %>
<%@ page import="javax.xml.parsers.DocumentBuilder" %>
<%@ page import="org.w3c.dom.Document" %>
<%@ page import="org.w3c.dom.NodeList" %>
<%@ page import="org.w3c.dom.Node" %>
<%@ page import="Default.DefaultValuesReader" %>

<!DOCTYPE html>
<html>
    <head>
        <title>Verwaltung der Filiale</title>
        <script type="text/javascript" src="js/jquery-3.2.1.js"></script>
        <link rel="stylesheet" type="text/css" href="css/default.css">
    </head>

    <body>
        <%
            String basePath = application.getRealPath("/").replace('\\', '/');
            Store currentStore = new Store((int) session.getAttribute("store_id"));
        %>

        <%
            try {
                Class.forName("com.mysql.jdbc.Driver").newInstance();
                java.sql.Connection connection = DriverManager.getConnection("jdbc:mysql://debianvmware:3306/pep", "dliehr", "sadom01");

                Statement sqlStatement = connection.createStatement();
                ResultSet result = sqlStatement.executeQuery(String.format("SELECT * FROM Store WHERE id = %d", session.getAttribute("store_id")));
                result.first();

                currentStore.setDatabaseId(result.getInt("id"));
                currentStore.setStoreNumber(result.getInt("number"));
                currentStore.setPlace(result.getString("place"));
                currentStore.setStreet(result.getString("street"));
                currentStore.setScheduledWeekHours(result.getInt("scheduled_week_hours"));
                currentStore.setScheduledWeekVolume(result.getDouble("scheduled_week_volume"));

                connection.close();
            } catch (Exception e) {
                out.println("Fehler: " + e.getMessage());
            }
        %>

        <script type="text/javascript">
            function inputWochenstundenChanged() {
                var wochenstunden = $("#inputFilialWStunden").val();
                $.ajax({
                    method: "POST",
                    url: "ajax/save_filiale_wochenstunden.jsp",
                    data: { id: "5", wochenstunden: "6"}
                })
                    .done(function(data) {
                        $("#td_filial_changes").html("Änderungen gespeichert" + data);
                    })
                    .fail(function(data) {
                        $("#td_filial_changes").html("Fehler" + data);
                    });
            }

            function inputWochenumsatzChanged() {
                $("#buttonSaveChanges").show();
            }

            function buttonSaveNewEmpl() {
                // all entries made?
                // todo: eingaben überprüfen

                alert("<%=currentStore.getDatabaseId()%>, " + $("#inputNewEmplFirstName").val() + ", " + $("#inputNewEmplLastName").val() + ", " + $("#selectNewEmplRole").val() + ", " + $("#inputNewEmplOrder").val() + ", " + $("#inputNewEmplMaxWh").val() + ", " + $("#inputNewEmplMaxMh").val());

                $.ajax({
                    method: "POST",
                    url: "ajax/save_new_employee.jsp",
                    data: { storeDbId: <%=currentStore.getDatabaseId()%> , firstname: $("#inputNewEmplFirstName").val(), lastname: $("#inputNewEmplLastName").val(), role: $("#selectNewEmplRole").val(), empl_order: $("#inputNewEmplOrder").val(), max_hours_week: $("#inputNewEmplMaxWh").val(), max_hours_month: $("#inputNewEmplMaxMh").val()}
                })
                    .done(function(data) {
                        alert(data);
                        // todo show insert message
                    })
                    .fail(function(data) {
                        alert(data);
                    });
            }
        </script>

        <div id="changed"></div>

        <jsp:include page="menu.html" />



        <div style="margin-top: 50px;">
            <!--
            <fieldset>
                <legend>Filial-Informationen</legend>

                <div style="display: flex; flex-direction: row; padding: 5px;">
                    <div style="background-color: #DDDDDD; width: 20px; height: 20px;"></div>
                    <div>: nicht editierbar</div>
                </div>

                <table>
                    <tr>
                        <td>Filialnummer:</td><td><input type="text" name="inputFilialNummer" id="inputFilialNummer" readonly="readonly" value="<%=String.valueOf(currentStore.getStoreNumber())%>" /></td>
                        <td>Ort:</td><td><input type="text" name="inputFilialOrt" id="inputFilialOrt" readonly="readonly" value="<%=String.valueOf(currentStore.getPlace())%>" /></td>
                        <td>Stra&szlig;e:</td><td><input type="text" name="inputFilialStrasse" id="inputFilialStrasse" readonly="readonly" value="<%=String.valueOf(currentStore.getStreet())%>" /></td>
                        <td>Wochenstunden:</td><td><input type="number" name="inputFilialWStunden" id="inputFilialWStunden" value="<%=String.valueOf(currentStore.getScheduledWeekHours())%>" onchange="inputWochenstundenChanged()" /></td>
                        <td>Wochenumsatz:</td><td><input type="number" name="inputFilialWUmsatz" id="inputFilialWUmsatz" value="<%=String.valueOf(currentStore.getScheduledWeekVolume())%>" onchange="inputWochenumsatzChanged()" /></td>
                        <td id="td_filial_changes"></td>
                    </tr>
                </table>

            </fieldset>
            -->

            <fieldset>
                <legend>Standard-Daten </legend>

                <p>Hier können Sie Standard-Daten wie z.B. Wochenstunden festlegen.</p>

                <%
                    // read default values
                    DefaultValuesReader reader = new DefaultValuesReader(basePath + "default_values.xml");
                %>

                <table>
                    <tr>
                        <th>Position</th>
                        <th>Maximale w&ouml;. Stunden</th>
                        <th>Maximale mtl. Stunden</th>
                    </tr>

                    <tr>
                        <td>Marktleitung</td>
                        <td><input type="number" name="inputDefaultMaxWhMarktleitung" id="inputDefaultMaxWhMarktleitung" value="<%=reader.getDefaultValueByRole(EmployeeRole.StoreAdministration).getMaxHoursWeekly()%>" /></td>
                        <td><input type="number" name="inputDefaultMaxMhMarktleitung" id="inputDefaultMaxMhMarktleitung" value="<%=reader.getDefaultValueByRole(EmployeeRole.StoreAdministration).getMaxHoursMonthly()%>" /></td>
                    </tr>

                    <tr>
                        <td>1. Vertretung</td>
                        <td><input type="number" name="inputDefaultMaxWh1Vertretung" id="inputDefaultMaxWh1Vertretung" value="<%=reader.getDefaultValueByRole(EmployeeRole.FirstSubstitution).getMaxHoursWeekly()%>" /></td>
                        <td><input type="number" name="inputDefaultMaxMh1Vertretung" id="inputDefaultMaxMh1Vertretung" value="<%=reader.getDefaultValueByRole(EmployeeRole.FirstSubstitution).getMaxHoursMonthly()%>" /></td>
                    </tr>

                    <tr>
                        <td>2. Vertretung</td>
                        <td><input type="number" name="inputDefaultMaxWh2Vertretung" id="inputDefaultMaxWh2Vertretung" value="<%=reader.getDefaultValueByRole(EmployeeRole.SecondSubstitution).getMaxHoursWeekly()%>" /></td>
                        <td><input type="number" name="inputDefaultMaxMh2Vertretung" id="inputDefaultMaxMh2Vertretung" value="<%=reader.getDefaultValueByRole(EmployeeRole.SecondSubstitution).getMaxHoursMonthly()%>" /></td>
                    </tr>

                    <tr>
                        <td>Auszubildender (1. Lehrjahr)</td>
                        <td><input type="number" name="inputDefaultMaxWhAzubi1Lj" id="inputDefaultMaxWhAzubi1Lj" value="<%=reader.getDefaultValueByRole(EmployeeRole.Trainee1Y).getMaxHoursWeekly()%>" /></td>
                        <td><input type="number" name="inputDefaultMaxMhAzubi1Lj" id="inputDefaultMaxMhAzubi1Lj" value="<%=reader.getDefaultValueByRole(EmployeeRole.Trainee1Y).getMaxHoursMonthly()%>" /></td>
                    </tr>

                    <tr>
                        <td>Auszubildender (2. Lehrjahr)</td>
                        <td><input type="number" name="inputDefaultMaxWhAzubi2Lj" id="inputDefaultMaxWhAzubi2Lj" value="<%=reader.getDefaultValueByRole(EmployeeRole.Trainee2Y).getMaxHoursWeekly()%>" /></td>
                        <td><input type="number" name="inputDefaultMaxMhAzubi2Lj" id="inputDefaultMaxMhAzubi2Lj" value="<%=reader.getDefaultValueByRole(EmployeeRole.Trainee2Y).getMaxHoursMonthly()%>" /></td>
                    </tr>

                    <tr>
                        <td>Auszubildender (3. Lehrjahr)</td>
                        <td><input type="number" name="inputDefaultMaxWhAzubi3Lj" id="inputDefaultMaxWhAzubi3Lj" value="<%=reader.getDefaultValueByRole(EmployeeRole.Trainee3Y).getMaxHoursWeekly()%>" /></td>
                        <td><input type="number" name="inputDefaultMaxMhAzubi3Lj" id="inputDefaultMaxMhAzubi3Lj" value="<%=reader.getDefaultValueByRole(EmployeeRole.Trainee3Y).getMaxHoursMonthly()%>" /></td>
                    </tr>

                    <tr>
                        <td>Verkauf &amp; Kasse</td>
                        <td><input type="number" name="inputDefaultMaxWhVK" id="inputDefaultMaxWhVK" value="<%=reader.getDefaultValueByRole(EmployeeRole.SaleAndCash).getMaxHoursWeekly()%>" /></td>
                        <td><input type="number" name="inputDefaultMaxMhVK" id="inputDefaultMaxMhVK" value="<%=reader.getDefaultValueByRole(EmployeeRole.SaleAndCash).getMaxHoursMonthly()%>" /></td>
                    </tr>

                    <tr>
                        <td>GFB</td>
                        <td><input type="number" name="inputDefaultMaxWhGfb" id="inputDefaultMaxWhGfb" value="<%=reader.getDefaultValueByRole(EmployeeRole.MarginalEmployee).getMaxHoursWeekly()%>" /></td>
                        <td><input type="number" name="inputDefaultMaxMhGfb" id="inputDefaultMaxMhGfb" value="<%=reader.getDefaultValueByRole(EmployeeRole.MarginalEmployee).getMaxHoursMonthly()%>" /></td>
                    </tr>
                </table>
            </fieldset>

            <fieldset>
                <legend>Mitarbeiter der Filiale</legend>

                <p>In diesem Abschnitt können Sie Mitarbeiter zu der derzeitigen Filiale hinzufügen und auch wieder entfernen. Die Mitarbeiter werden in der Reihenfolge ihrer Definition (von links nach rechts) angezeigt.</p>

                <fieldset>
                    <legend>Neuer Mitarbeiter</legend>

                    <%
                        // get max employees
                        int maxEmployees = 1;
                        try {
                            java.sql.Connection connection = DriverManager.getConnection("jdbc:mysql://debianvmware:3306/pep", "dliehr", "sadom01");

                            Statement sqlStatement = connection.createStatement();
                            ResultSet result = sqlStatement.executeQuery(String.format("SELECT COUNT(id) AS count_empl FROM Employee WHERE fk_store_id = %d", currentStore.getDatabaseId()));
                            result.first();

                            maxEmployees = result.getInt("count_empl") + 1;

                            connection.close();
                        } catch (Exception e) {
                            out.println(e.getMessage());
                        }
                    %>

                    <div style="display: flex; flex-wrap: wrap;">
                        <div>Vorname: <input type="text" name="inputNewEmplFirstName" id="inputNewEmplFirstName" /></div>
                        <div>Nachname: <input type="text" name="inputNewEmplLastName" id="inputNewEmplLastName" /></div>
                        <div>Funktion:
                            <select name="selectNewEmplRole" id="selectNewEmplRole">
                                <option value="0">Marktleitung</option>
                                <option value="1">Erste Vertretung</option>
                                <option value="2">Zweite Vertretung</option>
                                <option value="3">Azubi (1. LJ)</option>
                                <option value="4">Azubi (2. LJ)</option>
                                <option value="5">Azubi (3. LJ)</option>
                                <option value="6">Verkauf &amp; Kasse</option>
                                <option value="7">Geringfügig Beschäftigter</option>
                            </select>
                        </div>
                        <div>Reihenfolge: <input type="number" name="inputNewEmplOrder" id="inputNewEmplOrder" value="<%=maxEmployees%>" /></div>
                        <div>Max. w&ouml;. Stunden: <input type="number" name="inputNewEmplMaxWh"  id="inputNewEmplMaxWh" /></div>
                        <div>Max mtl. Stunden: <input type="number" name="inputNewEmplMaxMh"  id="inputNewEmplMaxMh" /></div>
                        <div><button onclick="buttonSaveNewEmpl()">Hinzufügen</button></div>
                    </div>
                </fieldset>

                <table>
                    <tr>
                        <th>Vorname</th>
                        <th>Nachname</th>
                        <th>Funktion</th>
                        <th>Reihenfolge</th>
                        <th>Max. w&ouml;. Stunden</th>
                        <th>Max. mtl. Stunden</th>
                    </tr>

                <%
                    // show existing employee's
                    try {
                        //Class.forName("com.mysql.jdbc.Driver").newInstance();
                        java.sql.Connection connection = Database.Connection.getNewSQLConnection();

                        Statement sqlStatement = connection.createStatement();
                        ResultSet result = sqlStatement.executeQuery(String.format("SELECT * FROM Employee WHERE fk_store_id = %d ORDER BY empl_order ASC", currentStore.getDatabaseId()));
                        result.beforeFirst();

                        while(result.next()) {
                            // first name
                            String firstName = "";
                            if(result.getString("first_name") != null) {
                                firstName = result.getString("first_name");
                            }

                            // switch role
                            EmployeeRole role = EmployeeRole.values()[result.getInt("role")];
                            String roleAsString = "";
                            switch (role) {
                                case StoreAdministration: roleAsString = "Marktleitung"; break;
                                case FirstSubstitution: roleAsString = "Erste Vertretung"; break;
                                case SecondSubstitution: roleAsString = "Zweite Vertretung"; break;
                                case Trainee1Y: roleAsString = "Azubi (1. LJ)"; break;
                                case Trainee2Y: roleAsString = "Azubi (2. LJ)"; break;
                                case Trainee3Y: roleAsString = "Azubi (3. LJ)"; break;
                                case SaleAndCash: roleAsString = "Verkauf &amp; Kasse"; break;
                                default: roleAsString = "Geringfügig Beschäftigter"; break;
                            }

                            // swith monthly hours
                            int hoursMontly = result.getInt("max_hours_month");
                            String hoursMontlyAsString = "";

                            if(hoursMontly != -1) {
                                hoursMontlyAsString = String.valueOf(hoursMontly);
                            }

                            out.println(String.format("<tr><td>%s</td><td>%s</td><td>%s</td><td>%d</td><td>%d</td><td>%s</td><td><button>Entfernen</button></td></tr>", firstName, result.getString("last_name"), roleAsString, result.getInt("empl_order"), result.getInt("max_hours_week"), hoursMontlyAsString));
                        }

                        connection.close();
                    } catch (Exception e) {
                        out.println("Fehler: " + e.getMessage());
                    }
                %>

                </table>
            </fieldset>
        </div>
    </body>
</html>