package com.industrialinspection.servlet;

import com.industrialinspection.model.QualityReport;
import com.industrialinspection.model.User;
import com.industrialinspection.model.enums.ReportType;
import com.industrialinspection.service.QualityReportService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;

@WebServlet(name = "ReportServlet", urlPatterns = {"/reports", "/reports/download"})
public class ReportServlet extends HttpServlet {

    private final QualityReportService reportService = new QualityReportService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String path = req.getServletPath();
        if ("/reports/download".equals(path)) {
            downloadReport(req, resp);
            return;
        }

        req.setAttribute("reportTypes", ReportType.values());
        req.setAttribute("reports", reportService.getAllGeneratedReports());
        req.getRequestDispatcher("/reports.jsp").forward(req, resp);
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
            ReportType type = ReportType.fromString(req.getParameter("reportType"));
            String format = req.getParameter("fileFormat");

            QualityReport rep = reportService.generateReport(type, format, user.getUserId(), null);
            resp.sendRedirect(req.getContextPath() + "/reports?generated=" + rep.getReportId());
        } catch (Exception e) {
            req.setAttribute("errorMessage", e.getMessage());
            doGet(req, resp);
        }
    }

    private void downloadReport(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int id = Integer.parseInt(req.getParameter("id"));
        QualityReport r = reportService.getReportById(id);
        if (r == null || r.getFilePath() == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        File file = new File(r.getFilePath());
        if (!file.exists()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Report file not found on disk");
            return;
        }

        resp.setContentType("application/octet-stream");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
        resp.setContentLengthLong(file.length());

        try (InputStream in = new FileInputStream(file);
             OutputStream out = resp.getOutputStream()) {
            byte[] buf = new byte[8192];
            int read;
            while ((read = in.read(buf)) != -1) {
                out.write(buf, 0, read);
            }
        }
    }
}
