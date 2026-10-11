package com.industrialinspection.servlet;

import com.industrialinspection.model.Product;
import com.industrialinspection.model.User;
import com.industrialinspection.service.ProductService;
import com.industrialinspection.util.DateTimeUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.Date;
import java.util.List;

@WebServlet(name = "ProductServlet", urlPatterns = {"/products"})
public class ProductServlet extends HttpServlet {

    private final ProductService productService = new ProductService();

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
            resp.setHeader("Content-Disposition", "attachment; filename=\"products_export.csv\"");
            java.io.File temp = java.io.File.createTempFile("products_", ".csv");
            productService.exportProductsToCSV(temp);
            java.nio.file.Files.copy(temp.toPath(), resp.getOutputStream());
            resp.getOutputStream().flush();
            temp.delete();
            return;
        }

        if ("delete".equalsIgnoreCase(action)) {
            User user = (User) session.getAttribute("currentUser");
            if (user.canManageProducts()) {
                try {
                    int id = Integer.parseInt(req.getParameter("id"));
                    productService.deleteProduct(id, user.getUserId());
                } catch (Exception ignored) {}
            }
            resp.sendRedirect(req.getContextPath() + "/products");
            return;
        }

        String keyword = req.getParameter("keyword");
        String category = req.getParameter("category");
        List<Product> products = productService.searchProducts(keyword, category);

        req.setAttribute("products", products);
        req.setAttribute("selectedCategory", category != null ? category : "ALL");
        req.setAttribute("keyword", keyword != null ? keyword : "");
        req.getRequestDispatcher("/products.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        User user = (User) session.getAttribute("currentUser");
        if (!user.canManageProducts()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized to manage products");
            return;
        }

        try {
            String name = req.getParameter("productName");
            String category = req.getParameter("category");
            String line = req.getParameter("productionLine");
            String batch = req.getParameter("batchNumber");
            String supplier = req.getParameter("supplier");
            String customer = req.getParameter("customer");
            String desc = req.getParameter("description");
            String dateStr = req.getParameter("manufacturingDate");

            Product p = new Product();
            p.setProductName(name);
            p.setCategory(category);
            p.setProductionLine(line);
            p.setBatchNumber(batch);
            p.setSupplier(supplier);
            p.setCustomer(customer);
            p.setDescription(desc);
            p.setManufacturingDate(dateStr != null && !dateStr.isEmpty() ? DateTimeUtil.parseDate(dateStr) : new Date(System.currentTimeMillis()));

            productService.addProduct(p, user.getUserId());
            resp.sendRedirect(req.getContextPath() + "/products?msg=success");
        } catch (Exception e) {
            req.setAttribute("errorMessage", e.getMessage());
            doGet(req, resp);
        }
    }
}
