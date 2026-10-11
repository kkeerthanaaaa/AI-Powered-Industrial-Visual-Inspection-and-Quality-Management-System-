package com.industrialinspection.service;

import com.industrialinspection.dao.ProductImageDAO;
import com.industrialinspection.dao.ProductImageDAOImpl;
import com.industrialinspection.exception.InvalidImageException;
import com.industrialinspection.exception.ValidationException;
import com.industrialinspection.model.ProductImage;
import com.industrialinspection.util.ImageUtil;
import com.industrialinspection.util.ValidationUtil;

import java.io.File;
import java.util.List;

public class ImageService {

    private final ProductImageDAO imageDAO;

    public ImageService() {
        this(new ProductImageDAOImpl());
    }

    public ImageService(ProductImageDAO imageDAO) {
        this.imageDAO = imageDAO;
    }

    public ProductImage uploadAndRegisterImage(File sourceFile, int productId, int batchId, 
                                               String cameraId, Integer userId) 
            throws ValidationException, InvalidImageException {
        ValidationUtil.validateImageFile(sourceFile);

        // Read physical image dimensions via ImageIO
        int[] dims = ImageUtil.getImageDimensions(sourceFile);
        int width = dims[0];
        int height = dims[1];

        // Safely persist to storage repository
        File savedFile = ImageUtil.saveInspectionImage(sourceFile, productId, batchId);

        ProductImage img = new ProductImage();
        img.setProductId(productId);
        img.setBatchId(batchId);
        img.setFileName(savedFile.getName());
        img.setFilePath(savedFile.getPath().replace("\\", "/"));
        img.setWidth(width);
        img.setHeight(height);
        img.setFileSizeBytes(savedFile.length());
        img.setCameraId(cameraId != null ? cameraId : "CAM-OPTICAL-01");
        img.setInspectionStatus("UNINSPECTED");

        imageDAO.addImage(img);
        AuditService.getInstance().logAction(userId, "IMAGE_UPLOADED", "product_images", img.getImageId(),
                "Uploaded optical capture: " + img.getFileName() + " (" + width + "x" + height + ")");

        return img;
    }

    public ProductImage getImageById(int id) {
        return imageDAO.getImageById(id);
    }

    public List<ProductImage> getAllImages() {
        return imageDAO.getAllImages();
    }

    public List<ProductImage> getImagesByBatchId(int batchId) {
        return imageDAO.getImagesByBatchId(batchId);
    }

    public void updateInspectionStatus(int imageId, String status) {
        imageDAO.updateInspectionStatus(imageId, status);
    }
}
