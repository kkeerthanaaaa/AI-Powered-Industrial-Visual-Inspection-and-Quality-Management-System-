<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.industrialinspection.model.User" %>
<%@ page import="com.industrialinspection.model.enums.UserRole" %>
<%@ page import="com.industrialinspection.util.DateTimeUtil" %>
<%@ page import="java.util.List" %>
<%
    request.setAttribute("activePage", "users");
    List<User> users = (List<User>) request.getAttribute("users");
    UserRole[] roles = (UserRole[]) request.getAttribute("roles");
    User currentUser = (User) session.getAttribute("currentUser");
%>
<jsp:include page="/WEB-INF/includes/header.jsp" />

<div class="page-header">
    <div>
        <h1 class="page-title">RBAC User &amp; Credentials Security</h1>
        <div class="page-desc">Role-based access control, cryptographic hash credentials, and account provisioning</div>
    </div>
    <div>
        <button onclick="document.getElementById('userModal').style.display='block'" class="btn btn-primary">+ Create User Account</button>
    </div>
</div>

<div class="table-responsive">
    <table class="data-table">
        <thead>
            <tr>
                <th>User ID</th>
                <th>Full Name</th>
                <th>Username</th>
                <th>Role</th>
                <th>Department</th>
                <th>Email</th>
                <th>Status</th>
                <th>Last Login</th>
                <th>Actions</th>
            </tr>
        </thead>
        <tbody>
        <% if (users != null && !users.isEmpty()) {
            for (User u : users) { %>
            <tr>
                <td style="font-family: monospace; font-weight: 700;">#USR-<%= u.getUserId() %></td>
                <td style="font-weight: 600;"><%= u.getFullName() %></td>
                <td style="color: var(--accent-cyan);"><%= u.getUsername() %></td>
                <td>
                    <span class="badge <%= "ADMINISTRATOR".equals(u.getRole().name()) ? "badge-fail" : ("QUALITY_ENGINEER".equals(u.getRole().name()) ? "badge-info" : "badge-pass") %>">
                        <%= u.getRole().name() %>
                    </span>
                </td>
                <td><%= u.getDepartment() != null ? u.getDepartment() : "General" %></td>
                <td style="font-size: 12px; color: var(--text-muted);"><%= u.getEmail() %></td>
                <td>
                    <span class="badge <%= u.isActive() ? "badge-pass" : "badge-fail" %>">
                        <%= u.isActive() ? "ACTIVE" : "DISABLED" %>
                    </span>
                </td>
                <td><%= u.getLastLogin() != null ? DateTimeUtil.formatTimestamp(u.getLastLogin()) : "Never" %></td>
                <td>
                    <% if (currentUser != null && currentUser.getUserId() != u.getUserId()) { %>
                        <a href="<%= request.getContextPath() %>/users?action=delete&id=<%= u.getUserId() %>" 
                           onclick="return confirm('Are you sure you want to deactivate/delete user <%= u.getUsername() %>?');" 
                           class="btn btn-danger btn-sm">Delete</a>
                    <% } else { %>
                        <span style="font-size: 11px; color: var(--text-muted); font-style: italic;">Current Account</span>
                    <% } %>
                </td>
            </tr>
        <%  }
        } else { %>
            <tr>
                <td colspan="9" style="text-align: center; color: var(--text-muted); padding: 30px;">No user records found.</td>
            </tr>
        <% } %>
        </tbody>
    </table>
</div>

<div id="userModal" class="modal">
    <div class="modal-content">
        <h3 style="margin-top:0; color: var(--accent-cyan);">Provision New User Account</h3>
        <form method="POST" action="<%= request.getContextPath() %>/users">
            <div class="form-group">
                <label>Full Name</label>
                <input type="text" name="name" required placeholder="e.g. Anand Sharma" class="form-control" />
            </div>
            <div class="form-group">
                <label>Username</label>
                <input type="text" name="username" required placeholder="e.g. asharma" class="form-control" />
            </div>
            <div class="form-group">
                <label>Password (Salted PBKDF2 Hashed)</label>
                <input type="password" name="password" required placeholder="Minimum 6 characters" class="form-control" />
            </div>
            <div class="form-group">
                <label>Email Address</label>
                <input type="email" name="email" required placeholder="e.g. asharma@abc-precision.com" class="form-control" />
            </div>
            <div class="form-group">
                <label>System Role</label>
                <select name="role" class="form-control">
                    <% if (roles != null) {
                        for (UserRole r : roles) { %>
                        <option value="<%= r.name() %>"><%= r.name() %></option>
                    <%  }
                    } %>
                </select>
            </div>
            <div class="form-group">
                <label>Department</label>
                <input type="text" name="department" placeholder="e.g. Quality Assurance" class="form-control" />
            </div>
            <div style="display:flex; justify-content: flex-end; gap: 10px; margin-top: 20px;">
                <button type="button" onclick="document.getElementById('userModal').style.display='none'" class="btn btn-secondary">Cancel</button>
                <button type="submit" class="btn btn-primary">Create Account</button>
            </div>
        </form>
    </div>
</div>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
