package com.industrialinspection.dao;

import com.industrialinspection.model.ProductImage;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ProductImageDAOImpl implements ProductImageDAO {

    private final DBConnection db = DBConnection.getInstance();
    private static final Map<Integer, ProductImage> cache = new ConcurrentHashMap<>();
    private static final AtomicInteger idGen = new AtomicInteger(10);

    static {
        ProductImage img1 = new ProductImage(1, 1, 1, "sample_brake_disc_inspected.png", "uploads/inspections/sample_brake_disc_inspected.png", 1920, 1080, 2458900L);
        img1.setProductName("Brake Disc Ventilated 320mm");
        img1.setBatchNumber("BATCH-2026-BRK-01");
        img1.setInspectionStatus("INSPECTED");
        cache.put(1, img1);

        ProductImage img2 = new ProductImage(2, 3, 3, "sample_aero_bracket_crack.png", "uploads/inspections/sample_aero_bracket_crack.png", 2048, 1536, 3125400L);
        img2.setProductName("Aircraft Titanium Wing Bracket");
        img2.setBatchNumber("BATCH-2026-AERO-03");
        img2.setInspectionStatus("INSPECTED");
        cache.put(2, img2);

        ProductImage img3 = new ProductImage(3, 2, 2, "sample_engine_mount_good.png", "uploads/inspections/sample_engine_mount_good.png", 1920, 1080, 2198000L);
        img3.setProductName("Engine Mount Hydraulic Assembly");
        img3.setBatchNumber("BATCH-2026-ENG-02");
        img3.setInspectionStatus("INSPECTED");
        cache.put(3, img3);
    }

    @Override
    public void addImage(ProductImage image) {
        String sql = "INSERT INTO product_images (product_id, batch_id, file_name, file_path, camera_id, width, height, file_size_bytes, inspection_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, image.getProductId());
            ps.setInt(2, image.getBatchId());
            ps.setString(3, image.getFileName());
            ps.setString(4, image.getFilePath());
            ps.setString(5, image.getCameraId());
            ps.setInt(6, image.getWidth());
            ps.setInt(7, image.getHeight());
            ps.setLong(8, image.getFileSizeBytes());
            ps.setString(9, image.getInspectionStatus());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    image.setImageId(rs.getInt(1));
                }
            }
        } catch (Exception e) {
            if (image.getImageId() == 0) {
                image.setImageId(idGen.incrementAndGet());
            }
        }
        cache.put(image.getImageId(), image);
    }

    @Override
    public ProductImage getImageById(int id) {
        String sql = "SELECT img.*, p.product_name, b.batch_number FROM product_images img " +
                     "LEFT JOIN products p ON img.product_id = p.product_id " +
                     "LEFT JOIN production_batches b ON img.batch_id = b.batch_id " +
                     "WHERE img.image_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapImage(rs);
                }
            }
        } catch (Exception ignored) {}
        return cache.get(id);
    }

    @Override
    public List<ProductImage> getImagesByBatchId(int batchId) {
        return getAllImages().stream().filter(img -> img.getBatchId() == batchId).collect(Collectors.toList());
    }

    @Override
    public List<ProductImage> getImagesByProductId(int productId) {
        return getAllImages().stream().filter(img -> img.getProductId() == productId).collect(Collectors.toList());
    }

    @Override
    public List<ProductImage> getAllImages() {
        List<ProductImage> list = new ArrayList<>();
        String sql = "SELECT img.*, p.product_name, b.batch_number FROM product_images img " +
                     "LEFT JOIN products p ON img.product_id = p.product_id " +
                     "LEFT JOIN production_batches b ON img.batch_id = b.batch_id " +
                     "ORDER BY img.image_id DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapImage(rs));
            }
            if (!list.isEmpty()) return list;
        } catch (Exception ignored) {}
        return new ArrayList<>(cache.values());
    }

    @Override
    public void updateInspectionStatus(int imageId, String status) {
        String sql = "UPDATE product_images SET inspection_status = ? WHERE image_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, imageId);
            ps.executeUpdate();
        } catch (Exception ignored) {}

        ProductImage img = cache.get(imageId);
        if (img != null) {
            img.setInspectionStatus(status);
        }
    }

    @Override
    public void deleteImage(int id) {
        String sql = "DELETE FROM product_images WHERE image_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception ignored) {}
        cache.remove(id);
    }

    private ProductImage mapImage(ResultSet rs) throws SQLException {
        ProductImage img = new ProductImage();
        img.setImageId(rs.getInt("image_id"));
        img.setProductId(rs.getInt("product_id"));
        img.setBatchId(rs.getInt("batch_id"));
        img.setFileName(rs.getString("file_name"));
        img.setFilePath(rs.getString("file_path"));
        img.setCaptureDate(rs.getTimestamp("capture_date"));
        img.setCameraId(rs.getString("camera_id"));
        img.setWidth(rs.getInt("width"));
        img.setHeight(rs.getInt("height"));
        img.setFileSizeBytes(rs.getLong("file_size_bytes"));
        img.setInspectionStatus(rs.getString("inspection_status"));
        img.setCreatedAt(rs.getTimestamp("created_at"));
        try {
            img.setProductName(rs.getString("product_name"));
            img.setBatchNumber(rs.getString("batch_number"));
        } catch (SQLException ignored) {}
        return img;
    }
}
