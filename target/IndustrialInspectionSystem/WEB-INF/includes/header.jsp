<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.industrialinspection.model.User" %>
<%
    User currentUser = (User) session.getAttribute("currentUser");
    String activePage = (String) request.getAttribute("activePage");
    if (activePage == null) activePage = "";
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ABC Precision - Visual Inspection &amp; Quality System</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css">
</head>
<body>
<div class="app-container">
    <header class="top-navbar">
        <div class="brand-wrapper">
            <span class="brand-badge">ABC</span>
            <div>
                <div class="brand-title">ABC PRECISION MANUFACTURING PVT. LTD.</div>
                <div class="brand-subtitle">AI-Powered Visual Inspection Platform | Plant 3 Pune</div>
            </div>
        </div>
        <div class="user-profile-bar">
            <% if (currentUser != null) { %>
                <span class="badge-role"><%= currentUser.getRole().getDisplayName() %></span>
                <span style="font-weight: 600; color: var(--text-main);"><%= currentUser.getFullName() %></span>
                <a href="<%= request.getContextPath() %>/logout" class="btn btn-secondary btn-sm">Sign Out</a>
            <% } else { %>
                <a href="<%= request.getContextPath() %>/login" class="btn btn-primary btn-sm">Login</a>
            <% } %>
        </div>
    </header>

    <div class="main-body">
        <% if (currentUser != null) { %>
        <aside class="sidebar">
            <div class="nav-section-title">Operations</div>
            <a href="<%= request.getContextPath() %>/dashboard" class="nav-link <%= "dashboard".equals(activePage) ? "active" : "" %>">📊 Quality Dashboard</a>
            <a href="<%= request.getContextPath() %>/inspections" class="nav-link <%= "inspections".equals(activePage) ? "active" : "" %>">🔬 Visual Inspections</a>
            <a href="<%= request.getContextPath() %>/upload-image" class="nav-link <%= "upload".equals(activePage) ? "active" : "" %>">📷 Optical Acquisition</a>
            <a href="<%= request.getContextPath() %>/capa" class="nav-link <%= "capa".equals(activePage) ? "active" : "" %>">⚡ Corrective Actions</a>
            <a href="<%= request.getContextPath() %>/defects" class="nav-link <%= "defects".equals(activePage) ? "active" : "" %>">🏷️ Defect Master</a>

            <div class="nav-section-title">Manufacturing</div>
            <a href="<%= request.getContextPath() %>/products" class="nav-link <%= "products".equals(activePage) ? "active" : "" %>">📦 Product Catalog</a>
            <a href="<%= request.getContextPath() %>/batches" class="nav-link <%= "batches".equals(activePage) ? "active" : "" %>">🏭 Production Batches</a>
            <a href="<%= request.getContextPath() %>/machines" class="nav-link <%= "machines".equals(activePage) ? "active" : "" %>">⚙️ Machines &amp; Cells</a>
            <a href="<%= request.getContextPath() %>/operators" class="nav-link <%= "operators".equals(activePage) ? "active" : "" %>">👷 Floor Operators</a>

            <div class="nav-section-title">Reports &amp; Admin</div>
            <a href="<%= request.getContextPath() %>/reports" class="nav-link <%= "reports".equals(activePage) ? "active" : "" %>">📄 Quality Reports (7)</a>
            <% if (currentUser.canManageUsers()) { %>
                <a href="<%= request.getContextPath() %>/users" class="nav-link <%= "users".equals(activePage) ? "active" : "" %>">👥 Users (RBAC)</a>
                <a href="<%= request.getContextPath() %>/settings" class="nav-link <%= "settings".equals(activePage) ? "active" : "" %>">🔧 System Settings</a>
                <a href="<%= request.getContextPath() %>/backup" class="nav-link <%= "backup".equals(activePage) ? "active" : "" %>">💾 Database Backup</a>
            <% } %>
        </aside>
        <% } %>
        <main class="content-area">
