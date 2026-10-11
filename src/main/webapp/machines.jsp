<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.industrialinspection.model.Machine" %>
<%@ page import="com.industrialinspection.model.User" %>
<%@ page import="com.industrialinspection.util.DateTimeUtil" %>
<%@ page import="java.util.List" %>
<%
    request.setAttribute("activePage", "machines");
    List<Machine> machines = (List<Machine>) request.getAttribute("machines");
    User currentUser = (User) session.getAttribute("currentUser");
%>
<jsp:include page="/WEB-INF/includes/header.jsp" />

<div class="page-header">
    <div>
        <h1 class="page-title">Machines &amp; Optical Metrology Cell Stations</h1>
        <div class="page-desc">Production equipment status, line location, and maintenance scheduling</div>
    </div>
    <div>
        <% if (currentUser != null && currentUser.canManageMachines()) { %>
            <button onclick="document.getElementById('machModal').style.display='block'" class="btn btn-primary">+ Register Machine</button>
        <% } %>
    </div>
</div>

<div class="table-responsive">
    <table class="data-table">
        <thead>
            <tr>
                <th>Machine Code</th>
                <th>Machine Name</th>
                <th>Machinery Type</th>
                <th>Production Line</th>
                <th>Status</th>
                <th>Installation Date</th>
                <th>Last Maintenance</th>
                <% if (currentUser != null && currentUser.canManageMachines()) { %>
                <th>Toggle Status</th>
                <% } %>
            </tr>
        </thead>
        <tbody>
        <% if (machines != null) {
            for (Machine m : machines) { %>
            <tr>
                <td style="font-family: monospace; font-weight: 700; color: var(--accent-cyan);"><%= m.getMachineCode() %></td>
                <td style="font-weight: 600;"><%= m.getMachineName() %></td>
                <td><%= m.getMachineType() %></td>
                <td><%= m.getProductionLine() %></td>
                <td>
                    <span class="badge <%= "ACTIVE".equals(m.getStatus().name()) ? "badge-pass" : ("MAINTENANCE".equals(m.getStatus().name()) ? "badge-review" : "badge-fail") %>">
                        <%= m.getStatus().name() %>
                    </span>
                </td>
                <td><%= DateTimeUtil.formatDate(m.getInstallationDate()) %></td>
                <td><%= m.getLastMaintenance() != null ? DateTimeUtil.formatDate(m.getLastMaintenance()) : "N/A" %></td>
                <% if (currentUser != null && currentUser.canManageMachines()) { %>
                <td>
                    <% if ("ACTIVE".equals(m.getStatus().name())) { %>
                        <a href="<%= request.getContextPath() %>/machines?action=status&id=<%= m.getMachineId() %>&status=MAINTENANCE" class="btn btn-secondary btn-sm">Set Maintenance</a>
                    <% } else { %>
                        <a href="<%= request.getContextPath() %>/machines?action=status&id=<%= m.getMachineId() %>&status=ACTIVE" class="btn btn-success btn-sm">Set Active</a>
                    <% } %>
                </td>
                <% } %>
            </tr>
        <%  }
           } %>
        </tbody>
    </table>
</div>

<!-- Modal Dialog -->
<div id="machModal" style="display:none; position:fixed; z-index:999; left:0; top:0; width:100%; height:100%; background:rgba(0,0,0,0.7); overflow:auto;">
    <div class="card" style="width: 450px; margin: 60px auto; background: var(--bg-card);">
        <h2 style="font-size: 18px; margin-bottom: 14px;">Register Machine / Station</h2>
        <form action="<%= request.getContextPath() %>/machines" method="POST">
            <div class="form-group">
                <label class="form-label">Machine Code</label>
                <input type="text" name="machineCode" class="form-control" value="CELL-00<%= System.currentTimeMillis() % 100 %>" required>
            </div>
            <div class="form-group">
                <label class="form-label">Machine Name</label>
                <input type="text" name="machineName" class="form-control" required placeholder="e.g. Mazak Multi-Tasking Center">
            </div>
            <div class="form-group">
                <label class="form-label">Type</label>
                <input type="text" name="machineType" class="form-control" value="CNC Milling Rig" required>
            </div>
            <div class="form-group">
                <label class="form-label">Production Line</label>
                <input type="text" name="productionLine" class="form-control" value="Aero-Line-1" required>
            </div>
            <div class="form-group">
                <label class="form-label">Initial Status</label>
                <select name="status" class="form-control">
                    <option value="ACTIVE">ACTIVE</option>
                    <option value="MAINTENANCE">MAINTENANCE</option>
                    <option value="INACTIVE">INACTIVE</option>
                </select>
            </div>
            <div style="display: flex; justify-content: flex-end; gap: 8px; margin-top: 16px;">
                <button type="button" class="btn btn-secondary" onclick="document.getElementById('machModal').style.display='none'">Cancel</button>
                <button type="submit" class="btn btn-primary">Save Machine</button>
            </div>
        </form>
    </div>
</div>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
