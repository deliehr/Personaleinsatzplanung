<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.io.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.sql.*" %>

<%
    String id = request.getParameter("id");
    String wochenstunden = request.getParameter("wochenstunden");

    out.println("id: " + id);
    out.println("<br /> wochentunden: " + wochenstunden);
%>