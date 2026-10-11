<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.industrialinspection.model.SystemSettings" %>
<%@ page import="com.industrialinspection.model.User" %>
<%@ page import="com.industrialinspection.util.DateTimeUtil" %>
<%@ page import="java.util.List" %>
<%
    request.setAttribute("activePage", "settings");
    List<SystemSettings> settings = (List<SystemSettings>) request.getAttribute("settings");
    User currentUser = (User) session.getAttribute("currentUser");
%>
<jsp:include page="/WEB-INF/includes/header.jsp" />

<div class="page-header">
    <div>
        <h1 class="page-title">Enterprise System Configuration</h1>
        <div class="page-desc">Inspection thresholds, AI inference tolerances, shift definitions, and plant parameters</div>
    </div>
</div>

<% if ("updated".equals(request.getParameter("msg"))) { %>
    <div style="background: rgba(34, 197, 94, 0.2); border: 1px solid var(--accent-green); color: #86efac; padding: 12px 18px; border-radius: 8px; margin-bottom: 20px;">
        ✓ System configuration setting updated successfully.
    </div>
<% } %>

<div class="table-responsive">
    <table class="data-table">
        <thead>
            <tr>
                <th>Configuration Key</th>
                <th>Current Value</th>
                <th>Group</th>
                <th>Description</th>
                <th>Last Updated</th>
                <th>Action</th>
            </tr>
        </thead>
        <tbody>
        <% if (settings != null && !settings.isEmpty()) {
            for (SystemSettings s : settings) { %>
            <tr>
                <td style="font-family: monospace; font-weight: 700; color: var(--accent-cyan);"><%= s.getSettingKey() %></td>
                <td>
                    <span style="font-family: monospace; background: var(--bg-surface); padding: 4px 8px; border-radius: 4px; border: 1px solid var(--border-color); color: #fff;">
                        <%= s.getSettingValue() %>
                    </span>
                </td>
                <td><span class="badge badge-info"><%= s.getSettingGroup() %></span></td>
                <td style="font-size: 13px; color: var(--text-muted);"><%= s.getDescription() != null ? s.getDescription() : "N/A" %></td>
                <td><%= s.getUpdatedAt() != null ? DateTimeUtil.formatTimestamp(s.getUpdatedAt()) : "System Default" %></td>
                <td>
                    <button type="button" 
                            onclick="editSetting('<%= s.getSettingKey() %>', '<%= s.getSettingValue().replace("'", "\\'") %>', '<%= s.getSettingGroup() %>', '<%= s.getDescription() != null ? s.getDescription().replace("'", "\\'") : "" %>')" 
                            class="btn btn-secondary btn-sm">Edit</button>
                </td>
            </tr>
        <%  }
        } else { %>
            <tr>
                <td colspan="6" style="text-align: center; color: var(--text-muted); padding: 30px;">No system parameters configured.</td>
            </tr>
        <% } %>
        </tbody>
    </table>
</div>

<div id="settingModal" class="modal">
    <div class="modal-content">
        <h3 style="margin-top:0; color: var(--accent-cyan);">Update Configuration Parameter</h3>
        <form method="POST" action="<%= request.getContextPath() %>/settings">
            <div class="form-group">
                <label>Setting Key</label>
                <input type="text" id="mKey" name="settingKey" readonly class="form-control" style="background: var(--bg-surface); color: var(--text-muted);" />
            </div>
            <div class="form-group">
                <label>Setting Value</label>
                <input type="text" id="mVal" name="settingValue" required class="form-control" />
            </div>
            <div class="form-group">
                <label>Group</label>
                <input type="text" id="mGrp" name="settingGroup" required class="form-control" />
            </div>
            <div class="form-group">
                <label>Description</label>
                <input type="text" id="mDesc" name="description" class="form-control" />
            </div>
            <div style="display:flex; justify-content: flex-end; gap: 10px; margin-top: 20px;">
                <button type="button" onclick="document.getElementById('settingModal').style.display='none'" class="btn btn-secondary">Cancel</button>
                <button type="submit" class="btn btn-primary">Save Changes</button>
            </div>
        </form>
    </div>
</div>

<script>
function editSetting(key, val, grp, desc) {
    document.getElementById('mKey').value = key;
    document.getElementById('mVal').value = val;
    document.getElementById('mGrp').value = grp;
    document.getElementById('mDesc').value = desc;
    document.getElementById('settingModal').style.display = 'block';
}
</script>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
