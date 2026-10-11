package com.industrialinspection.servlet;

import com.industrialinspection.model.Machine;
import com.industrialinspection.model.User;
import com.industrialinspection.model.enums.MachineStatus;
import com.industrialinspection.service.MachineService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.Date;

@WebServlet(name = "MachineServlet", urlPatterns = {"/machines"})
public class MachineServlet extends HttpServlet {

    private final MachineService machineService = new MachineService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String action = req.getParameter("action");
        if ("status".equalsIgnoreCase(action)) {
            User user = (User) session.getAttribute("currentUser");
            if (user.canManageMachines()) {
                int id = Integer.parseInt(req.getParameter("id"));
                MachineStatus st = MachineStatus.fromString(req.getParameter("status"));
                machineService.updateStatus(id, st, user.getUserId());
            }
            resp.sendRedirect(req.getContextPath() + "/machines");
            return;
        }

        req.setAttribute("machines", machineService.getAllMachines());
        req.getRequestDispatcher("/machines.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        User user = (User) session.getAttribute("currentUser");
        if (!user.canManageMachines()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        try {
            Machine m = new Machine();
            m.setMachineCode(req.getParameter("machineCode"));
            m.setMachineName(req.getParameter("machineName"));
            m.setMachineType(req.getParameter("machineType"));
            m.setProductionLine(req.getParameter("productionLine"));
            m.setStatus(MachineStatus.fromString(req.getParameter("status")));
            m.setInstallationDate(new Date(System.currentTimeMillis()));

            machineService.addMachine(m, user.getUserId());
            resp.sendRedirect(req.getContextPath() + "/machines?msg=success");
        } catch (Exception e) {
            req.setAttribute("errorMessage", e.getMessage());
            doGet(req, resp);
        }
    }
}
