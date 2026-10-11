package com.industrialinspection.servlet;

import com.industrialinspection.dao.SystemSettingsDAO;
import com.industrialinspection.dao.SystemSettingsDAOImpl;
import com.industrialinspection.model.User;
import com.industrialinspection.service.AuditService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet(name = "SettingsServlet", urlPatterns = {"/settings"})
public class SettingsServlet extends HttpServlet {

    private final SystemSettingsDAO settingsDAO = new SystemSettingsDAOImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        req.setAttribute("settings", settingsDAO.getAllSettings());
        req.getRequestDispatcher("/settings.jsp").forward(req, resp);
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

        String key = req.getParameter("settingKey");
        String val = req.getParameter("settingValue");
        String grp = req.getParameter("settingGroup");
        String desc = req.getParameter("description");

        settingsDAO.setSetting(key, val, grp, desc);
        AuditService.getInstance().logAction(user.getUserId(), "SETTING_UPDATED", "system_settings", 0, "Updated " + key);

        resp.sendRedirect(req.getContextPath() + "/settings?msg=updated");
    }
}
