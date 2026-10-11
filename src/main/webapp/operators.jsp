<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.industrialinspection.model.Operator" %>
<%@ page import="com.industrialinspection.model.User" %>
<%@ page import="com.industrialinspection.model.enums.Shift" %>
<%@ page import="com.industrialinspection.model.enums.OperatorStatus" %>
<%@ page import="com.industrialinspection.util.DateTimeUtil" %>
<%@ page import="java.util.List" %>
<%
    request.setAttribute("activePage", "operators");
    List<Operator> operators = (List<Operator>) request.getAttribute("operators");
    User currentUser = (User) session.getAttribute("currentUser");
%>
<jsp:include page="/WEB-INF/includes/header.jsp" />

<div class="page-header">
    <div>
        <h1 class="page-title">Operator Directory &amp; Shift Assignment</h1>
        <div class="page-desc">Shift staffing, line deployment, and certified optical inspection workforce</div>
    </div>
    <div>
        <% if (currentUser != null && currentUser.canManageOperators()) { %>
            <button onclick="document.getElementById('opModal').style.display='block'" class="btn btn-primary">+ Register Operator</button>
        <% } %>
    </div>
</div>

<div class="table-responsive">
    <table class="data-table">
        <thead>
            <tr>
                <th>Employee Code</th>
                <th>Full Name</th>
                <th>Shift</th>
                <th>Department</th>
                <th>Contact</th>
                <th>Status</th>
                <% if (currentUser != null && currentUser.canManageOperators()) { %>
                <th>Toggle Status</th>
                <% } %>
            </tr>
        </thead>
        <tbody>
        <% if (operators != null && !operators.isEmpty()) {
            for (Operator op : operators) { %>
            <tr>
                <td style="font-family: monospace; font-weight: 700; color: var(--accent-cyan);"><%= op.getEmployeeCode() %></td>
                <td style="font-weight: 600;"><%= op.getName() %></td>
                <td><span class="badge badge-info"><%= op.getShift() != null ? op.getShift().name() : "GENERAL" %></span></td>
                <td><%= op.getDepartment() != null ? op.getDepartment() : "Quality Metrology" %></td>
                <td><%= op.getContact() != null ? op.getContact() : "+91-9876543210" %></td>
                <td>
                    <span class="badge <%= "ACTIVE".equalsIgnoreCase(op.getStatus().name()) ? "badge-pass" : "badge-fail" %>">
                        <%= op.getStatus().name() %>
                    </span>
                </td>
                <% if (currentUser != null && currentUser.canManageOperators()) { %>
                <td>
                    <% if ("ACTIVE".equalsIgnoreCase(op.getStatus().name())) { %>
                        <a href="<%= request.getContextPath() %>/operators?action=status&id=<%= op.getOperatorId() %>&status=INACTIVE" class="btn btn-secondary btn-sm">Set Inactive</a>
                    <% } else { %>
                        <a href="<%= request.getContextPath() %>/operators?action=status&id=<%= op.getOperatorId() %>&status=ACTIVE" class="btn btn-secondary btn-sm">Set Active</a>
                    <% } %>
                </td>
                <% } %>
            </tr>
        <%  }
        } else { %>
            <tr>
                <td colspan="7" style="text-align: center; color: var(--text-muted); padding: 30px;">No operators registered.</td>
            </tr>
        <% } %>
        </tbody>
    </table>
</div>

<% if (currentUser != null && currentUser.canManageOperators()) { %>
<div id="opModal" class="modal">
    <div class="modal-content">
        <h3 style="margin-top:0; color: var(--accent-cyan);">Register Manufacturing Operator</h3>
        <form method="POST" action="<%= request.getContextPath() %>/operators">
            <div class="form-group">
                <label>Employee Code</label>
                <input type="text" name="employeeCode" required placeholder="e.g. OP-104" class="form-control" />
            </div>
            <div class="form-group">
                <label>Full Name</label>
                <input type="text" name="name" required placeholder="e.g. Suresh Patel" class="form-control" />
            </div>
            <div class="form-group">
                <label>Assigned Shift</label>
                <select name="shift" class="form-control">
                    <option value="MORNING">Morning (06:00 - 14:00)</option>
                    <option value="EVENING">Evening (14:00 - 22:00)</option>
                    <option value="NIGHT">Night (22:00 - 06:00)</option>
                    <option value="GENERAL">General Day Shift</option>
                </select>
            </div>
            <div class="form-group">
                <label>Department</label>
                <input type="text" name="department" required placeholder="e.g. CNC Optical Inspection" class="form-control" />
            </div>
            <div class="form-group">
                <label>Contact Phone</label>
                <input type="text" name="contact" required placeholder="e.g. +91 98765 43210" class="form-control" />
            </div>
            <div style="display:flex; justify-content: flex-end; gap: 10px; margin-top: 20px;">
                <button type="button" onclick="document.getElementById('opModal').style.display='none'" class="btn btn-secondary">Cancel</button>
                <button type="submit" class="btn btn-primary">Save Operator</button>
            </div>
        </form>
    </div>
</div>
<% } %>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
