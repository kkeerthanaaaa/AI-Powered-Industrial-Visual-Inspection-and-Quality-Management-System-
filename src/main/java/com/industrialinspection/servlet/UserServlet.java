package com.industrialinspection.servlet;

import com.industrialinspection.dao.UserDAO;
import com.industrialinspection.dao.UserDAOImpl;
import com.industrialinspection.model.User;
import com.industrialinspection.model.enums.UserRole;
import com.industrialinspection.service.AuditService;
import com.industrialinspection.util.PasswordUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet(name = "UserServlet", urlPatterns = {"/users"})
public class UserServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAOImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        User user = (User) session.getAttribute("currentUser");
        if (!user.canManageUsers()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Administrator access required");
            return;
        }

        String action = req.getParameter("action");
        if ("delete".equalsIgnoreCase(action)) {
            int id = Integer.parseInt(req.getParameter("id"));
            if (id != user.getUserId()) {
                userDAO.deleteUser(id);
            }
            resp.sendRedirect(req.getContextPath() + "/users");
            return;
        }

        req.setAttribute("users", userDAO.getAllUsers());
        req.setAttribute("roles", UserRole.values());
        req.getRequestDispatcher("/users.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        User user = (User) session.getAttribute("currentUser");
        if (!user.canManageUsers()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        try {
            String u = req.getParameter("username");
            String name = req.getParameter("fullName");
            String email = req.getParameter("email");
            String rawPass = req.getParameter("password");
            UserRole role = UserRole.fromString(req.getParameter("role"));

            String salt = PasswordUtil.generateSalt();
            String hash = PasswordUtil.hashPassword(rawPass != null ? rawPass : "Pass@123", salt);

            User nu = new User();
            nu.setUsername(u);
            nu.setFullName(name);
            nu.setEmail(email);
            nu.setPasswordHash(hash);
            nu.setSalt(salt);
            nu.setRole(role);
            nu.setStatus("ACTIVE");

            userDAO.addUser(nu);
            AuditService.getInstance().logAction(user.getUserId(), "USER_CREATED", "users", nu.getUserId(),
                    "Admin registered user " + u + " via web");

            resp.sendRedirect(req.getContextPath() + "/users?msg=created");
        } catch (Exception e) {
            req.setAttribute("errorMessage", e.getMessage());
            doGet(req, resp);
        }
    }
}
