<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.industrialinspection.model.CorrectiveAction" %>
<%@ page import="com.industrialinspection.model.InspectionRecord" %>
<%@ page import="com.industrialinspection.model.User" %>
<%@ page import="com.industrialinspection.util.DateTimeUtil" %>
<%@ page import="java.util.List" %>
<%
    request.setAttribute("activePage", "capa");
    List<CorrectiveAction> actions = (List<CorrectiveAction>) request.getAttribute("actions");
    List<InspectionRecord> inspections = (List<InspectionRecord>) request.getAttribute("inspections");
    User currentUser = (User) session.getAttribute("currentUser");
%>
<jsp:include page="/WEB-INF/includes/header.jsp" />

<div class="page-header">
    <div>
        <h1 class="page-title">Corrective &amp; Preventive Action (CAPA) Register</h1>
        <div class="page-desc">Closed-loop quality containment, root cause analysis, and remedy tracking</div>
    </div>
    <div>
        <% if (currentUser != null && currentUser.canCreateCorrectiveAction()) { %>
            <button onclick="document.getElementById('capaModal').style.display='block'" class="btn btn-primary">+ Issue New CAPA</button>
        <% } %>
    </div>
</div>

<!-- CAPA Register Table -->
<div class="table-responsive">
    <table class="data-table">
        <thead>
            <tr>
                <th>Action ID</th>
                <th>Insp #</th>
                <th>Root Cause Analysis</th>
                <th>Corrective Action Implemented</th>
                <th>Responsible Person</th>
                <th>Target Date</th>
                <th>Completed</th>
                <th>Status</th>
                <% if (currentUser != null && currentUser.canCreateCorrectiveAction()) { %>
                <th>Quick Action</th>
                <% } %>
            </tr>
        </thead>
        <tbody>
        <% if (actions != null && !actions.isEmpty()) {
            for (CorrectiveAction ca : actions) { %>
            <tr>
                <td><%= ca.getActionId() %></td>
                <td>#<%= ca.getInspectionId() %></td>
                <td style="max-width: 200px; color: var(--accent-amber); font-weight: 500;"><%= ca.getRootCause() %></td>
                <td style="max-width: 240px;"><%= ca.getCorrectiveAction() %></td>
                <td style="font-weight: 600;"><%= ca.getResponsiblePerson() %></td>
                <td><%= DateTimeUtil.formatDate(ca.getTargetDate()) %></td>
                <td><%= ca.getCompletionDate() != null ? DateTimeUtil.formatDate(ca.getCompletionDate()) : "<span style='color:var(--text-muted);'>PENDING</span>" %></td>
                <td>
                    <span class="badge <%= "VERIFIED".equals(ca.getStatus().name()) ? "badge-pass" : ("COMPLETED".equals(ca.getStatus().name()) ? "badge-pass" : "badge-review") %>">
                        <%= ca.getStatus().name() %>
                    </span>
                </td>
                <% if (currentUser != null && currentUser.canCreateCorrectiveAction()) { %>
                <td>
                    <% if (!"VERIFIED".equals(ca.getStatus().name())) { %>
                        <a href="<%= request.getContextPath() %>/capa?action=status&id=<%= ca.getActionId() %>&status=VERIFIED" class="btn btn-secondary btn-sm">Mark Verified</a>
                    <% } else { %>
                        <span style="font-size: 11px; color: var(--accent-green); font-weight: 600;">✓ Closed</span>
                    <% } %>
                </td>
                <% } %>
            </tr>
        <%  }
           } else { %>
            <tr>
                <td colspan="9" style="text-align: center; color: var(--text-muted); padding: 20px;">No CAPA incidents logged.</td>
            </tr>
        <% } %>
        </tbody>
    </table>
</div>

<!-- Modal Dialog -->
<div id="capaModal" style="display:none; position:fixed; z-index:999; left:0; top:0; width:100%; height:100%; background:rgba(0,0,0,0.7); overflow:auto;">
    <div class="card" style="width: 500px; margin: 50px auto; background: var(--bg-card);">
        <h2 style="font-size: 18px; margin-bottom: 14px;">Issue Corrective Action</h2>
        <form action="<%= request.getContextPath() %>/capa" method="POST">
            <div class="form-group">
                <label class="form-label">Related Failed Inspection</label>
                <select name="inspectionId" class="form-control" required>
                    <% if (inspections != null) {
                        for (InspectionRecord ir : inspections) { %>
                            <option value="<%= ir.getInspectionId() %>">Inspection #<%= ir.getInspectionId() %> - <%= ir.getProductName() %> (<%= ir.getFinalDecision() %>)</option>
                    <%  }
                       } %>
                </select>
            </div>
            <div class="form-group">
                <label class="form-label">Root Cause Analysis</label>
                <textarea name="rootCause" class="form-control" rows="2" required placeholder="Identify 5-Why or technical root cause..."></textarea>
            </div>
            <div class="form-group">
                <label class="form-label">Corrective &amp; Containment Action</label>
                <textarea name="correctiveAction" class="form-control" rows="2" required placeholder="Specific remedial tooling or operational changes..."></textarea>
            </div>
            <div class="form-group">
                <label class="form-label">Responsible Engineer / Supervisor</label>
                <input type="text" name="responsiblePerson" class="form-control" value="<%= currentUser != null ? currentUser.getFullName() : "" %>" required>
            </div>
            <div style="display: flex; justify-content: flex-end; gap: 8px; margin-top: 16px;">
                <button type="button" class="btn btn-secondary" onclick="document.getElementById('capaModal').style.display='none'">Cancel</button>
                <button type="submit" class="btn btn-primary">Issue CAPA</button>
            </div>
        </form>
    </div>
</div>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
