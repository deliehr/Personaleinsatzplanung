<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*" %>
<%@ page import="java.sql.*" %>

<%
    try {
        int id = Integer.parseInt(request.getParameter("id"));

        try {
            // connection
            Connection connection = Database.Connection.getNewSQLConnection();

            // read store
            Statement sqlStatement = connection.createStatement();
            String query = String.format("DELETE FROM AdditionalRestriction WHERE id = %d", id);
            sqlStatement.executeUpdate(query);
            out.println(1);
        } catch (Exception e) {
            System.out.println("Error: " + e.toString());
            out.println(0);
        }
    } catch (Exception e) {
        System.out.println("Error (ajax/remove_manual_constraint.jsp): " + e.toString());
        out.println(0);
    }
%>