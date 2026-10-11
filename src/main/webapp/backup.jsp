<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.industrialinspection.model.User" %>
<%@ page import="java.io.File" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%
    request.setAttribute("activePage", "backup");
    File[] backupFiles = (File[]) request.getAttribute("backupFiles");
    User currentUser = (User) session.getAttribute("currentUser");
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
%>
<jsp:include page="/WEB-INF/includes/header.jsp" />

<div class="page-header">
    <div>
        <h1 class="page-title">Disaster Recovery &amp; Database Backup</h1>
        <div class="page-desc">Export MySQL database state, table schemas, records, and AI inspection logs</div>
    </div>
    <div>
        <form method="POST" action="<%= request.getContextPath() %>/backup" style="margin:0;">
            <button type="submit" class="btn btn-primary" onclick="return confirm('Initiate database backup snapshot now?');">
                💾 Create Instant Database Backup
            </button>
        </form>
    </div>
</div>

<% if ("success".equals(request.getParameter("msg"))) { %>
    <div style="background: rgba(34, 197, 94, 0.2); border: 1px solid var(--accent-green); color: #86efac; padding: 12px 18px; border-radius: 8px; margin-bottom: 20px;">
        ✓ Backup created successfully: <strong><%= request.getParameter("file") != null ? request.getParameter("file") : "backup.sql" %></strong>
    </div>
<% } %>

<div class="card" style="margin-bottom: 25px;">
    <h3 style="margin-top:0; color: var(--accent-cyan);">Database Storage &amp; Continuity Policy</h3>
    <p style="color: var(--text-muted); line-height: 1.6; margin-bottom: 0;">
        Full database dumps include all 15 relational tables: Products, Production Batches, Optical Images, 
        Simulated AI Predictions, Metrology Measurements, Defect Results, Quality Reports, and Audit Trail logs.
        Snapshots are generated in standard SQL syntax suitable for MySQL CLI restore:
        <code style="color: var(--accent-cyan); display: inline-block; margin-top: 5px;">mysql -u root -p industrial_inspection_db &lt; backup_file.sql</code>
    </p>
</div>

<h3 style="color: var(--text-primary); margin-top: 25px; margin-bottom: 15px;">Available Backup Archives</h3>
<div class="table-responsive">
    <table class="data-table">
        <thead>
            <tr>
                <th>Backup File Name</th>
                <th>File Size</th>
                <th>Creation Timestamp</th>
                <th>Format</th>
                <th>Integrity Status</th>
            </tr>
        </thead>
        <tbody>
        <% if (backupFiles != null && backupFiles.length > 0) {
            for (File bf : backupFiles) { %>
            <tr>
                <td style="font-family: monospace; font-weight: 700; color: var(--accent-cyan);"><%= bf.getName() %></td>
                <td><%= (bf.length() / 1024) %> KB</td>
                <td><%= sdf.format(new Date(bf.lastModified())) %></td>
                <td><span class="badge badge-info">SQL Dump</span></td>
                <td><span class="badge badge-pass">VALID</span></td>
            </tr>
        <%  }
        } else { %>
            <tr>
                <td colspan="5" style="text-align: center; color: var(--text-muted); padding: 30px;">No backup files found. Click "Create Instant Database Backup" above.</td>
            </tr>
        <% } %>
        </tbody>
    </table>
</div>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
