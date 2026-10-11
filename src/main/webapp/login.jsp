<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String error = (String) request.getAttribute("errorMessage");
    String enteredUser = (String) request.getAttribute("enteredUsername");
    if (enteredUser == null) enteredUser = "admin";
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Secure Login | ABC Precision Manufacturing</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css">
    <style>
        .login-wrap {
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 80vh;
        }
        .login-box {
            width: 440px;
        }
    </style>
</head>
<body>
<div class="app-container">
    <header class="top-navbar">
        <div class="brand-wrapper">
            <span class="brand-badge">ABC</span>
            <div>
                <div class="brand-title">ABC PRECISION MANUFACTURING PVT. LTD.</div>
                <div class="brand-subtitle">AI-Powered Visual Inspection &amp; Quality Management</div>
            </div>
        </div>
    </header>

    <div class="login-wrap">
        <div class="card login-box">
            <h2 style="font-size: 20px; font-weight: 700; margin-bottom: 6px;">Operator &amp; Staff Login</h2>
            <p style="color: var(--text-muted); font-size: 12px; margin-bottom: 20px;">
                Enter your credentials to access the industrial inspection workstation.
            </p>

            <% if (error != null) { %>
                <div class="alert alert-danger"><%= error %></div>
            <% } %>

            <form action="<%= request.getContextPath() %>/login" method="POST">
                <div class="form-group">
                    <label class="form-label">Username / Operator ID</label>
                    <input type="text" name="username" id="usernameInput" class="form-control" value="<%= enteredUser %>" required autofocus>
                </div>
                <div class="form-group">
                    <label class="form-label">Password</label>
                    <input type="password" name="password" id="passwordInput" class="form-control" value="Admin@123" required>
                </div>
                <button type="submit" class="btn btn-primary" style="width: 100%; margin-top: 10px; padding: 10px;">
                    Authenticate &amp; Launch Console
                </button>
            </form>

            <div style="margin-top: 24px; padding-top: 16px; border-top: 1px solid var(--border-color);">
                <div style="font-size: 11px; font-weight: 600; color: var(--text-muted); margin-bottom: 8px;">
                    Quick Demo Switcher:
                </div>
                <div style="display: flex; gap: 6px;">
                    <button type="button" class="btn btn-secondary btn-sm" onclick="setCreds('admin', 'Admin@123')">Admin</button>
                    <button type="button" class="btn btn-secondary btn-sm" onclick="setCreds('engineer', 'Pass@123')">Engineer</button>
                    <button type="button" class="btn btn-secondary btn-sm" onclick="setCreds('supervisor', 'Pass@123')">Supervisor</button>
                    <button type="button" class="btn btn-secondary btn-sm" onclick="setCreds('operator', 'Pass@123')">Operator</button>
                </div>
            </div>
        </div>
    </div>

    <footer class="app-footer">
        <div>ABC Precision Manufacturing Pvt. Ltd. | Pune Facility</div>
        <div>Compliant with ISO 9001:2015 &amp; AS9100D Aerospace Standard</div>
    </footer>
</div>

<script>
    function setCreds(u, p) {
        document.getElementById('usernameInput').value = u;
        document.getElementById('passwordInput').value = p;
    }
</script>
</body>
</html>
