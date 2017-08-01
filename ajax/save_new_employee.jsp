<%@
page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"
import="java.io.*"
import="java.util.*"
import="java.sql.*"
%>

<%
    int storeDatabaseId = Integer.valueOf(request.getParameter("storeDbId"));
    String firstname = request.getParameter("firstname");
    String lastname = request.getParameter("lastname");
    String role = request.getParameter("role");
    int empl_order = Integer.valueOf(request.getParameter("empl_order"));
    int max_hours_week = Integer.valueOf(request.getParameter("max_hours_week"));
    int max_hours_month = Integer.valueOf(request.getParameter("max_hours_month"));

    // todo eingaben überprüfen?
    // todo cross site scripting?

    // try to insert into db
    try {
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection connection = DriverManager.getConnection("jdbc:mysql://debianmac.fritz.box:3306/pep", "dliehr", "sadom01");
        Statement sqlStatement = connection.createStatement();
        String query = String.format("INSERT INTO Employee (fk_store_id, first_name, last_name, role, empl_order, max_hours_week, max_hours_month) VALUES (%d, '%s', '%s', '%s', %d, %d, %d)", storeDatabaseId, firstname, lastname, role, empl_order, max_hours_week, max_hours_month);
        sqlStatement.executeUpdate(query.toString());
        connection.close();
        out.println("1");
    } catch (Exception e) {
        out.println(e.getMessage());
    }
%>