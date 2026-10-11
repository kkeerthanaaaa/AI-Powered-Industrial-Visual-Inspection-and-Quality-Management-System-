package com.industrialinspection.servlet;

import com.industrialinspection.model.DefectType;
import com.industrialinspection.model.User;
import com.industrialinspection.model.enums.SeverityLevel;
import com.industrialinspection.service.DefectService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet(name = "DefectServlet", urlPatterns = {"/defects"})
public class DefectServlet extends HttpServlet {

    private final DefectService defectService = new DefectService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String action = req.getParameter("action");
        if ("export".equalsIgnoreCase(action)) {
            resp.setContentType("text/csv");
            resp.setHeader("Content-Disposition", "attachment; filename=\"defects_export.csv\"");
            java.io.File temp = java.io.File.createTempFile("defects_", ".csv");
            defectService.exportDefectsToCSV(temp);
            java.nio.file.Files.copy(temp.toPath(), resp.getOutputStream());
            resp.getOutputStream().flush();
            temp.delete();
            return;
        }

        req.setAttribute("defectTypes", defectService.getAllDefectTypes());
        req.setAttribute("defectResults", defectService.getAllDefectResults());
        req.getRequestDispatcher("/defects.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        User user = (User) session.getAttribute("currentUser");
        try {
            String code = req.getParameter("defectCode");
            String name = req.getParameter("defectName");
            String category = req.getParameter("category");
            SeverityLevel sev = SeverityLevel.fromString(req.getParameter("severity"));
            String remedy = req.getParameter("remedy");

            DefectType dt = new DefectType();
            dt.setDefectCode(code);
            dt.setDefectName(name);
            dt.setCategory(category);
            dt.setDefaultSeverity(sev);
            dt.setStandardRemedy(remedy);
            dt.setDescription(name);

            defectService.addDefectType(dt, user.getUserId());
            resp.sendRedirect(req.getContextPath() + "/defects?msg=success");
        } catch (Exception e) {
            req.setAttribute("errorMessage", e.getMessage());
            doGet(req, resp);
        }
    }
}
