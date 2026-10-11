package com.industrialinspection.service;

import com.industrialinspection.dao.ProductDAO;
import com.industrialinspection.dao.ProductDAOImpl;
import com.industrialinspection.exception.ProductNotFoundException;
import com.industrialinspection.exception.ValidationException;
import com.industrialinspection.model.Product;
import com.industrialinspection.util.CSVUtil;
import com.industrialinspection.util.DateTimeUtil;
import com.industrialinspection.util.ValidationUtil;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class ProductService {

    private final ProductDAO productDAO;

    public ProductService() {
        this(new ProductDAOImpl());
    }

    public ProductService(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    public void addProduct(Product product, Integer userId) throws ValidationException {
        ValidationUtil.validateNotEmpty(product.getProductName(), "Product Name");
        ValidationUtil.validateNotEmpty(product.getCategory(), "Category");
        ValidationUtil.validateNotEmpty(product.getProductionLine(), "Production Line");
        ValidationUtil.validateNotEmpty(product.getBatchNumber(), "Batch Number");
        ValidationUtil.validateNotEmpty(product.getSupplier(), "Supplier");
        ValidationUtil.validateNotEmpty(product.getCustomer(), "Customer");

        if (product.getManufacturingDate() == null) {
            product.setManufacturingDate(new Date(System.currentTimeMillis()));
        }

        productDAO.addProduct(product);
        AuditService.getInstance().logAction(userId, "PRODUCT_CREATED", "products", product.getProductId(),
                "Registered new component: " + product.getProductName());
    }

    public void updateProduct(Product product, Integer userId) throws ValidationException, ProductNotFoundException {
        ValidationUtil.validateNotEmpty(product.getProductName(), "Product Name");
        if (productDAO.getProductById(product.getProductId()) == null) {
            throw new ProductNotFoundException("Product with ID " + product.getProductId() + " not found.");
        }

        productDAO.updateProduct(product);
        AuditService.getInstance().logAction(userId, "PRODUCT_UPDATED", "products", product.getProductId(),
                "Updated component specifications: " + product.getProductName());
    }

    public void deleteProduct(int productId, Integer userId) throws ProductNotFoundException {
        Product p = productDAO.getProductById(productId);
        if (p == null) {
            throw new ProductNotFoundException("Product with ID " + productId + " not found.");
        }
        productDAO.deleteProduct(productId);
        AuditService.getInstance().logAction(userId, "PRODUCT_DELETED", "products", productId,
                "Deleted component: " + p.getProductName());
    }

    public Product getProductById(int productId) throws ProductNotFoundException {
        Product p = productDAO.getProductById(productId);
        if (p == null) {
            throw new ProductNotFoundException("Product with ID " + productId + " not found.");
        }
        return p;
    }

    public List<Product> getAllProducts() {
        return productDAO.getAllProducts();
    }

    public List<Product> searchProducts(String keyword, String category) {
        return productDAO.searchProducts(keyword, category);
    }

    public void exportProductsToCSV(File destination) throws IOException {
        List<Product> products = getAllProducts();
        List<String[]> csvData = new ArrayList<>();
        csvData.add(new String[]{"Product ID", "Product Name", "Category", "Production Line", "Manufacturing Date", "Batch Number", "Supplier", "Customer", "Description"});

        for (Product p : products) {
            csvData.add(new String[]{
                String.valueOf(p.getProductId()),
                p.getProductName(),
                p.getCategory(),
                p.getProductionLine(),
                DateTimeUtil.formatDate(p.getManufacturingDate()),
                p.getBatchNumber(),
                p.getSupplier(),
                p.getCustomer(),
                p.getDescription() != null ? p.getDescription() : ""
            });
        }
        CSVUtil.writeCSV(destination, csvData);
    }

    public int importProductsFromCSV(File sourceFile, Integer userId) throws IOException, ValidationException {
        List<String[]> rows = CSVUtil.readCSV(sourceFile);
        if (rows.isEmpty()) return 0;

        int importedCount = 0;
        int startIndex = 0;
        // Check if row 0 is header
        if (rows.get(0)[0].toLowerCase().contains("product") || rows.get(0)[0].toLowerCase().contains("id") || rows.get(0)[0].toLowerCase().contains("name")) {
            startIndex = 1;
        }

        for (int i = startIndex; i < rows.size(); i++) {
            String[] row = rows.get(i);
            if (row.length >= 7) {
                Product p = new Product();
                p.setProductName(row[1]);
                p.setCategory(row[2]);
                p.setProductionLine(row[3]);
                p.setManufacturingDate(DateTimeUtil.parseDate(row[4]));
                p.setBatchNumber(row[5]);
                p.setSupplier(row[6]);
                p.setCustomer(row.length > 7 ? row[7] : "General Customer");
                if (row.length > 8) p.setDescription(row[8]);

                addProduct(p, userId);
                importedCount++;
            }
        }
        AuditService.getInstance().logAction(userId, "CSV_PRODUCT_IMPORT", "products", 0, "Imported " + importedCount + " products from CSV");
        return importedCount;
    }
}
