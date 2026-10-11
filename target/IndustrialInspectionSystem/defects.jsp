<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.industrialinspection.model.DefectResult" %>
<%@ page import="com.industrialinspection.model.DefectType" %>
<%@ page import="com.industrialinspection.model.User" %>
<%@ page import="java.util.List" %>
<%
    request.setAttribute("activePage", "defects");
    List<DefectType> defectTypes = (List<DefectType>) request.getAttribute("defectTypes");
    List<DefectResult> defectResults = (List<DefectResult>) request.getAttribute("defectResults");
    User currentUser = (User) session.getAttribute("currentUser");
%>
<jsp:include page="/WEB-INF/includes/header.jsp" />

<div class="page-header">
    <div>
        <h1 class="page-title">Defect Classification &amp; Master Taxonomy</h1>
        <div class="page-desc">Comprehensive visual defect catalog and logged non-conformance records</div>
    </div>
    <div style="display: flex; gap: 10px;">
        <a href="<%= request.getContextPath() %>/defects?action=export" class="btn btn-secondary">Export Defects CSV</a>
        <% if (currentUser != null && currentUser.canApproveInspection()) { %>
            <button onclick="document.getElementById('defectModal').style.display='block'" class="btn btn-primary">+ Add Master Defect Type</button>
        <% } %>
    </div>
</div>

<!-- Master Taxonomy Section -->
<div class="card">
    <h3 style="font-size: 16px; font-weight: 700; margin-bottom: 12px; color: var(--accent-cyan);">
        Master Defect Categories &amp; Recommended Remedies
    </h3>
    <div class="table-responsive">
        <table class="data-table">
            <thead>
                <tr>
                    <th>Defect Code</th>
                    <th>Defect Name</th>
                    <th>Category</th>
                    <th>Default Severity</th>
                    <th>Remedy / Corrective Action</th>
                </tr>
            </thead>
            <tbody>
            <% if (defectTypes != null) {
                for (DefectType dt : defectTypes) { %>
                <tr>
                    <td style="font-family: monospace; font-weight: 700; color: var(--accent-blue);"><%= dt.getDefectCode() %></td>
                    <td style="font-weight: 600;"><%= dt.getDefectName() %></td>
                    <td><%= dt.getCategory() %></td>
                    <td>
                        <span class="badge <%= "CRITICAL".equals(dt.getDefaultSeverity().name()) ? "badge-fail" : ("HIGH".equals(dt.getDefaultSeverity().name()) ? "badge-fail" : "badge-review") %>">
                            <%= dt.getDefaultSeverity().name() %>
                        </span>
                    </td>
                    <td style="color: var(--text-muted);"><%= dt.getStandardRemedy() %></td>
                </tr>
            <%  }
               } %>
            </tbody>
        </table>
    </div>
</div>

<!-- Logged Defect Results -->
<div class="card">
    <h3 style="font-size: 16px; font-weight: 700; margin-bottom: 12px; color: var(--accent-red);">
        Confirmed Defect Occurrences from Inspections
    </h3>
    <div class="table-responsive">
        <table class="data-table">
            <thead>
                <tr>
                    <th>Result ID</th>
                    <th>Insp #</th>
                    <th>Defect Classification</th>
                    <th>Severity</th>
                    <th>Region / Location</th>
                    <th>Inspector Comments</th>
                </tr>
            </thead>
            <tbody>
            <% if (defectResults != null && !defectResults.isEmpty()) {
                for (DefectResult dr : defectResults) { %>
                <tr>
                    <td><%= dr.getDefectResultId() %></td>
                    <td>#<%= dr.getInspectionId() %></td>
                    <td style="font-weight: 600; color: var(--accent-cyan);"><%= dr.getDefectName() != null ? dr.getDefectName() : ("Type #" + dr.getDefectTypeId()) %></td>
                    <td>
                        <span class="badge <%= "CRITICAL".equals(dr.getSeverity().name()) ? "badge-fail" : "badge-review" %>">
                            <%= dr.getSeverity().name() %>
                        </span>
                    </td>
                    <td><%= dr.getLocation().getLabel() %></td>
                    <td style="color: var(--text-muted);"><%= dr.getComments() %></td>
                </tr>
            <%  }
               } else { %>
                <tr>
                    <td colspan="6" style="text-align: center; color: var(--text-muted); padding: 18px;">No defect incidents logged.</td>
                </tr>
            <% } %>
            </tbody>
        </table>
    </div>
</div>

<!-- Add Modal -->
<div id="defectModal" style="display:none; position:fixed; z-index:999; left:0; top:0; width:100%; height:100%; background:rgba(0,0,0,0.7); overflow:auto;">
    <div class="card" style="width: 460px; margin: 60px auto; background: var(--bg-card);">
        <h2 style="font-size: 18px; margin-bottom: 14px;">Add Defect Classification</h2>
        <form action="<%= request.getContextPath() %>/defects" method="POST">
            <div class="form-group">
                <label class="form-label">Defect Code</label>
                <input type="text" name="defectCode" class="form-control" value="DEF-NEW-<%= System.currentTimeMillis() % 100 %>" required>
            </div>
            <div class="form-group">
                <label class="form-label">Defect Name</label>
                <input type="text" name="defectName" class="form-control" required placeholder="e.g. Porosity Micro-Void">
            </div>
            <div class="form-group">
                <label class="form-label">Category</label>
                <input type="text" name="category" class="form-control" value="Surface Flaw" required>
            </div>
            <div class="form-group">
                <label class="form-label">Default Severity</label>
                <select name="severity" class="form-control">
                    <option value="LOW">LOW</option>
                    <option value="MEDIUM" selected>MEDIUM</option>
                    <option value="HIGH">HIGH</option>
                    <option value="CRITICAL">CRITICAL</option>
                </select>
            </div>
            <div class="form-group">
                <label class="form-label">Recommended Remedy</label>
                <input type="text" name="remedy" class="form-control" placeholder="Corrective action recommendation...">
            </div>
            <div style="display: flex; justify-content: flex-end; gap: 8px; margin-top: 16px;">
                <button type="button" class="btn btn-secondary" onclick="document.getElementById('defectModal').style.display='none'">Cancel</button>
                <button type="submit" class="btn btn-primary">Save Defect Type</button>
            </div>
        </form>
    </div>
</div>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
