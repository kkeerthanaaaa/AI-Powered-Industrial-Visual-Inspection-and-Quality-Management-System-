package com.industrialinspection.servlet;

import com.industrialinspection.model.ProductionBatch;
import com.industrialinspection.model.User;
import com.industrialinspection.model.enums.BatchStatus;
import com.industrialinspection.model.enums.Shift;
import com.industrialinspection.service.BatchService;
import com.industrialinspection.service.MachineService;
import com.industrialinspection.service.OperatorService;
import com.industrialinspection.service.ProductService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.Date;
import java.util.List;

@WebServlet(name = "BatchServlet", urlPatterns = {"/batches"})
public class BatchServlet extends HttpServlet {

    private final BatchService batchService = new BatchService();
    private final ProductService productService = new ProductService();
    private final MachineService machineService = new MachineService();
    private final OperatorService operatorService = new OperatorService();

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
            resp.setHeader("Content-Disposition", "attachment; filename=\"batches_export.csv\"");
            java.io.File temp = java.io.File.createTempFile("batches_", ".csv");
            batchService.exportBatchesToCSV(temp);
            java.nio.file.Files.copy(temp.toPath(), resp.getOutputStream());
            resp.getOutputStream().flush();
            temp.delete();
            return;
        }

        if ("delete".equalsIgnoreCase(action)) {
            User user = (User) session.getAttribute("currentUser");
            if (user.canManageBatches()) {
                try {
                    int id = Integer.parseInt(req.getParameter("id"));
                    batchService.deleteBatch(id, user.getUserId());
                } catch (Exception ignored) {}
            }
            resp.sendRedirect(req.getContextPath() + "/batches");
            return;
        }

        String keyword = req.getParameter("keyword");
        String status = req.getParameter("status");
        List<ProductionBatch> batches = batchService.searchBatches(keyword, status);

        req.setAttribute("batches", batches);
        req.setAttribute("products", productService.getAllProducts());
        req.setAttribute("machines", machineService.getAllMachines());
        req.setAttribute("operators", operatorService.getAllOperators());
        req.setAttribute("selectedStatus", status != null ? status : "ALL");
        req.setAttribute("keyword", keyword != null ? keyword : "");

        req.getRequestDispatcher("/batches.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        User user = (User) session.getAttribute("currentUser");
        if (!user.canManageBatches()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized to manage batches");
            return;
        }

        try {
            String batchNum = req.getParameter("batchNumber");
            int productId = Integer.parseInt(req.getParameter("productId"));
            int batchSize = Integer.parseInt(req.getParameter("batchSize"));
            int machineId = Integer.parseInt(req.getParameter("machineId"));
            int operatorId = Integer.parseInt(req.getParameter("operatorId"));
            Shift shift = Shift.fromString(req.getParameter("shift"));
            BatchStatus status = BatchStatus.fromString(req.getParameter("status"));

            ProductionBatch b = new ProductionBatch();
            b.setBatchNumber(batchNum);
            b.setProductId(productId);
            b.setBatchSize(batchSize);
            b.setMachineId(machineId);
            b.setOperatorId(operatorId);
            b.setShift(shift);
            b.setStatus(status);
            b.setProductionDate(new Date(System.currentTimeMillis()));

            batchService.createBatch(b, user.getUserId());
            resp.sendRedirect(req.getContextPath() + "/batches?msg=success");
        } catch (Exception e) {
            req.setAttribute("errorMessage", e.getMessage());
            doGet(req, resp);
        }
    }
}
