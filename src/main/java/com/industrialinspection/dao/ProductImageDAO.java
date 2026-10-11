package com.industrialinspection.dao;

import com.industrialinspection.model.ProductImage;
import java.util.List;

public interface ProductImageDAO {
    void addImage(ProductImage image);
    ProductImage getImageById(int id);
    List<ProductImage> getImagesByBatchId(int batchId);
    List<ProductImage> getImagesByProductId(int productId);
    List<ProductImage> getAllImages();
    void updateInspectionStatus(int imageId, String status);
    void deleteImage(int id);
}
