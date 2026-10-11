package com.industrialinspection.servlet;

import com.industrialinspection.model.Operator;
import com.industrialinspection.model.User;
import com.industrialinspection.model.enums.OperatorStatus;
import com.industrialinspection.model.enums.Shift;
import com.industrialinspection.service.OperatorService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet(name = "OperatorServlet", urlPatterns = {"/operators"})
public class OperatorServlet extends HttpServlet {

    private final OperatorService operatorService = new OperatorService();

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
            if (user.canManageOperators()) {
                int id = Integer.parseInt(req.getParameter("id"));
                OperatorStatus st = OperatorStatus.fromString(req.getParameter("status"));
                operatorService.updateStatus(id, st, user.getUserId());
            }
            resp.sendRedirect(req.getContextPath() + "/operators");
            return;
        }

        req.setAttribute("operators", operatorService.getAllOperators());
        req.getRequestDispatcher("/operators.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        User user = (User) session.getAttribute("currentUser");
        if (!user.canManageOperators()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        try {
            Operator o = new Operator();
            o.setName(req.getParameter("name"));
            o.setEmployeeCode(req.getParameter("employeeCode"));
            o.setDepartment(req.getParameter("department"));
            o.setContact(req.getParameter("contact"));
            o.setShift(Shift.fromString(req.getParameter("shift")));
            o.setStatus(OperatorStatus.fromString(req.getParameter("status")));

            operatorService.addOperator(o, user.getUserId());
            resp.sendRedirect(req.getContextPath() + "/operators?msg=success");
        } catch (Exception e) {
            req.setAttribute("errorMessage", e.getMessage());
            doGet(req, resp);
        }
    }
}
