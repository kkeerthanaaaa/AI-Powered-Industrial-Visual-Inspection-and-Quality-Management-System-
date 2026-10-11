package com.industrialinspection.servlet;

import com.industrialinspection.model.*;
import com.industrialinspection.model.enums.InspectionDecision;
import com.industrialinspection.service.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "InspectionServlet", urlPatterns = {"/inspections"})
public class InspectionServlet extends HttpServlet {

    private final InspectionService inspectionService = new InspectionService();
    private final ProductService productService = new ProductService();
    private final BatchService batchService = new BatchService();
    private final ImageService imageService = new ImageService();
    private final AIPredictionService aiService = new AIPredictionService();

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
            resp.setHeader("Content-Disposition", "attachment; filename=\"inspections_export.csv\"");
            java.io.File temp = java.io.File.createTempFile("inspections_", ".csv");
            inspectionService.exportInspectionsToCSV(temp);
            java.nio.file.Files.copy(temp.toPath(), resp.getOutputStream());
            resp.getOutputStream().flush();
            temp.delete();
            return;
        }

        String keyword = req.getParameter("keyword");
        String decision = req.getParameter("decision");
        List<InspectionRecord> inspections = inspectionService.searchInspections(keyword, decision);

        req.setAttribute("inspections", inspections);
        req.setAttribute("products", productService.getAllProducts());
        req.setAttribute("batches", batchService.getAllBatches());
        req.setAttribute("images", imageService.getAllImages());
        req.setAttribute("selectedDecision", decision != null ? decision : "ALL");
        req.setAttribute("keyword", keyword != null ? keyword : "");

        req.getRequestDispatcher("/inspections.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        User user = (User) session.getAttribute("currentUser");
        if (!user.canPerformInspection()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized to execute inspections");
            return;
        }

        try {
            int productId = Integer.parseInt(req.getParameter("productId"));
            int batchId = Integer.parseInt(req.getParameter("batchId"));
            int imageId = Integer.parseInt(req.getParameter("imageId"));
            InspectionDecision decision = InspectionDecision.fromString(req.getParameter("finalDecision"));
            String remarks = req.getParameter("remarks");

            // Execute AI Simulation
            ProductImage img = imageService.getImageById(imageId);
            AIPrediction pred = null;
            if (img != null) {
                pred = aiService.runPrediction(img, user.getUserId());
            }

            Integer predId = (pred != null) ? pred.getPredictionId() : null;
            inspectionService.completeInspection(productId, batchId, imageId, user.getUserId(), predId, decision, remarks, "SUBMITTED");

            resp.sendRedirect(req.getContextPath() + "/inspections?msg=success");
        } catch (Exception e) {
            req.setAttribute("errorMessage", e.getMessage());
            doGet(req, resp);
        }
    }
}
