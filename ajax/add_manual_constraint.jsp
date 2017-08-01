<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*" %>
<%@ page import="java.sql.*" %>

<%
    String dvs = request.getParameter("dvs");
    String option = request.getParameter("option");
    String optionValue = request.getParameter("option_value");
    int day = Integer.valueOf(request.getParameter("day_index"));

    try {
        // add restrictions to table, if not exist
        java.sql.Connection connection = Database.Connection.getNewSQLConnection();

        // read statements
        Statement sqlStatement = connection.createStatement();
        String query = String.format("SELECT COUNT(id) AS count_restrictions FROM AdditionalRestriction WHERE fk_store_id = %s AND dvs = '%s' AND option = '%s' AND day = %d", session.getAttribute("store_id"), dvs, option, day);
        ResultSet result = sqlStatement.executeQuery(query);
        result.first();

        if(result.getInt("count_restrictions") > 0) {
            // already exist, do not insert
        } else {
            sqlStatement = connection.createStatement();
            query = String.format("INSERT INTO AdditionalRestriction (fk_store_id, dvs, option, option_value, day) VALUES (%s, '%s', '%s', %s, %d)", session.getAttribute("store_id"), dvs, option, optionValue, day);
            sqlStatement.executeUpdate(query);
        }

        connection.close();

        // return code
        out.println(1);
    } catch (Exception e) {
        System.out.println(e.toString());
        out.println(0);
    }
%>