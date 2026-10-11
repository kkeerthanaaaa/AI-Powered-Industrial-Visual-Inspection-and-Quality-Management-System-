package com.industrialinspection.servlet;

import com.industrialinspection.model.Product;
import com.industrialinspection.model.User;
import com.industrialinspection.service.BatchService;
import com.industrialinspection.service.ImageService;
import com.industrialinspection.service.ProductService;
import com.industrialinspection.util.ImageUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.File;
import java.io.IOException;

@WebServlet(name = "ImageUploadServlet", urlPatterns = {"/upload-image"})
public class ImageUploadServlet extends HttpServlet {

    private final ImageService imageService = new ImageService();
    private final ProductService productService = new ProductService();
    private final BatchService batchService = new BatchService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        req.setAttribute("products", productService.getAllProducts());
        req.setAttribute("batches", batchService.getAllBatches());
        req.setAttribute("images", imageService.getAllImages());
        req.getRequestDispatcher("/upload_image.jsp").forward(req, resp);
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
            int productId = Integer.parseInt(req.getParameter("productId"));
            int batchId = Integer.parseInt(req.getParameter("batchId"));
            String camera = req.getParameter("cameraId");

            Product p = productService.getProductById(productId);
            String pName = p != null ? p.getProductName() : "Brake_Rotor";

            // Generate synthetic optical image file for demo if web form submitted
            String filename = "web_capture_" + System.currentTimeMillis() % 10000 + ".png";
            File sample = ImageUtil.createSampleIndustrialImage(filename, pName, true);

            if (sample != null) {
                imageService.uploadAndRegisterImage(sample, productId, batchId, camera, user.getUserId());
            }

            resp.sendRedirect(req.getContextPath() + "/inspections?msg=uploaded");
        } catch (Exception e) {
            req.setAttribute("errorMessage", e.getMessage());
            doGet(req, resp);
        }
    }
}
