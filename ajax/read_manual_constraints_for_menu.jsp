<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*" %>
<%@ page import="java.sql.*" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="java.io.StringWriter" %>
<%@ page import="org.json.JSONArray" %>
<%@ page import="org.json.JSONString" %>

<%
    // ajax parameters
    String dvs = request.getParameter("dvs");
    int dayIndex = Integer.valueOf(request.getParameter("dayIndex"));

    // json
    JSONObject object = new JSONObject();
    StringWriter writer = new StringWriter();

    try {
        // add restrictions to table, if not exist
        java.sql.Connection connection = Database.Connection.getNewSQLConnection();

        // read statements
        Statement sqlStatement = connection.createStatement();
        String query = String.format("SELECT * FROM AdditionalRestriction WHERE fk_store_id = %s AND dvs = '%s' AND day = %d", session.getAttribute("store_id"), dvs, dayIndex);
        ResultSet result = sqlStatement.executeQuery(query);
        result.beforeFirst();

        // new jsonarray
        JSONArray array = new JSONArray();

        while (result.next()) {
            JSONObject obj2 = new JSONObject();
            obj2.put("id", result.getInt("id"));
            obj2.put("dvs", result.getString("dvs"));
            obj2.put("option", result.getString("option"));
            obj2.put("optionValue", result.getDouble("option_value"));
            obj2.put("dayIndex", Integer.valueOf(result.getString("day")));

            array.put(obj2);
        }

        JSONObject restrictionObject = new JSONObject();
        restrictionObject.put("restriction", array);

        object.put("restrictions", restrictionObject);

        connection.close();
    } catch (Exception e) {
        System.out.println(e.toString());

        object = new JSONObject();
        object.put("null", "null");
    }

    object.write(writer);
    out.println(writer.toString());
%>