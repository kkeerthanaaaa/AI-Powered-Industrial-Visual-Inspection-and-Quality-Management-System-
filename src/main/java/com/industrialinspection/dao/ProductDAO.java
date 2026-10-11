package com.industrialinspection.dao;

import com.industrialinspection.model.Product;
import java.util.List;

public interface ProductDAO {
    void addProduct(Product product);
    Product getProductById(int id);
    List<Product> getAllProducts();
    void updateProduct(Product product);
    void deleteProduct(int id);
    List<Product> searchProducts(String keyword, String category);
}
