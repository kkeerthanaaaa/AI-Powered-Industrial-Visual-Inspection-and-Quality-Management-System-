package com.industrialinspection.servlet;

import com.industrialinspection.model.User;
import com.industrialinspection.service.DatabaseBackupService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.File;
import java.io.IOException;

@WebServlet(name = "BackupServlet", urlPatterns = {"/backup"})
public class BackupServlet extends HttpServlet {

    private final DatabaseBackupService backupService = new DatabaseBackupService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        File backupDir = new File("backups");
        if (!backupDir.exists()) backupDir.mkdirs();
        File[] files = backupDir.listFiles((dir, name) -> name.endsWith(".sql"));

        req.setAttribute("backupFiles", files != null ? files : new File[0]);
        req.getRequestDispatcher("/backup.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        User user = (User) session.getAttribute("currentUser");
        if (!user.canBackupDatabase()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Administrator access required for backups");
            return;
        }

        try {
            File backup = backupService.backupDatabase(new File("backups"), user.getUserId());
            resp.sendRedirect(req.getContextPath() + "/backup?msg=success&file=" + backup.getName());
        } catch (Exception e) {
            req.setAttribute("errorMessage", e.getMessage());
            doGet(req, resp);
        }
    }
}
