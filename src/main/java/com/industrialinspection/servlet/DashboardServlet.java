package com.industrialinspection.servlet;

import com.industrialinspection.model.User;
import com.industrialinspection.service.DashboardService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.Map;

@WebServlet(name = "DashboardServlet", urlPatterns = {"/dashboard"})
public class DashboardServlet extends HttpServlet {

    private final DashboardService dashboardService = new DashboardService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        Map<String, Object> kpi = dashboardService.getOverallSummaryKPIs();
        req.setAttribute("kpi", kpi);
        req.setAttribute("defectDist", dashboardService.getDefectDistribution());
        req.setAttribute("machineDefects", dashboardService.getMachineWiseDefects());
        req.setAttribute("monthlyTrend", dashboardService.getMonthlyQualityTrend());

        req.getRequestDispatcher("/dashboard.jsp").forward(req, resp);
    }
}
