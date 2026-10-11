package com.industrialinspection.servlet;

import com.industrialinspection.model.CorrectiveAction;
import com.industrialinspection.model.User;
import com.industrialinspection.model.enums.ActionStatus;
import com.industrialinspection.service.CorrectiveActionService;
import com.industrialinspection.service.InspectionService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet(name = "CorrectiveActionServlet", urlPatterns = {"/capa"})
public class CorrectiveActionServlet extends HttpServlet {

    private final CorrectiveActionService capaService = new CorrectiveActionService();
    private final InspectionService inspectionService = new InspectionService();

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
            if (user.canCreateCorrectiveAction()) {
                int id = Integer.parseInt(req.getParameter("id"));
                ActionStatus st = ActionStatus.fromString(req.getParameter("status"));
                capaService.updateStatus(id, st, "Updated via web portal", user.getUserId());
            }
            resp.sendRedirect(req.getContextPath() + "/capa");
            return;
        }

        req.setAttribute("actions", capaService.getAllActions());
        req.setAttribute("inspections", inspectionService.getAllInspections());
        req.getRequestDispatcher("/corrective_actions.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        User user = (User) session.getAttribute("currentUser");
        if (!user.canCreateCorrectiveAction()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        try {
            int inspId = Integer.parseInt(req.getParameter("inspectionId"));
            String root = req.getParameter("rootCause");
            String act = req.getParameter("correctiveAction");
            String respPerson = req.getParameter("responsiblePerson");

            CorrectiveAction ca = new CorrectiveAction();
            ca.setInspectionId(inspId);
            ca.setRootCause(root);
            ca.setCorrectiveAction(act);
            ca.setResponsiblePerson(respPerson);
            ca.setStatus(ActionStatus.OPEN);

            capaService.createCorrectiveAction(ca, user.getUserId());
            resp.sendRedirect(req.getContextPath() + "/capa?msg=success");
        } catch (Exception e) {
            req.setAttribute("errorMessage", e.getMessage());
            doGet(req, resp);
        }
    }
}
