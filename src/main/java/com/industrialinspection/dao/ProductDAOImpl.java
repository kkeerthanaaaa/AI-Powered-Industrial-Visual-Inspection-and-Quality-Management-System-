package com.industrialinspection.dao;

import com.industrialinspection.model.Product;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ProductDAOImpl implements ProductDAO {

    private final DBConnection db = DBConnection.getInstance();
    private static final Map<Integer, Product> cache = new ConcurrentHashMap<>();
    private static final AtomicInteger idGen = new AtomicInteger(10);

    static {
        // Pre-seed sample products
        Product p1 = new Product(1, "Brake Disc Ventilated 320mm", "Automotive Braking", "Auto-Line-2",
                Date.valueOf("2026-09-28"), "BATCH-2026-BRK-01", "Tata Steel Ltd", "Mahindra Automotive");
        p1.setDescription("High-carbon cast iron ventilated rotor for premium utility vehicles");
        cache.put(1, p1);

        Product p2 = new Product(2, "Engine Mount Hydraulic Assembly", "Automotive Powertrain", "Auto-Line-2",
                Date.valueOf("2026-09-30"), "BATCH-2026-ENG-02", "Kalyani Forgings", "Tata Motors Ltd");
        p2.setDescription("Hydraulic fluid filled damping bracket for high-torque diesel platforms");
        cache.put(2, p2);

        Product p3 = new Product(3, "Aircraft Titanium Wing Bracket", "Aerospace Structural", "Aero-Line-1",
                Date.valueOf("2026-10-01"), "BATCH-2026-AERO-03", "Mishra Dhatu Nigam", "HAL Aerospace");
        p3.setDescription("Ti-6Al-4V grade critical wing spar joint bracket with tight tolerance");
        cache.put(3, p3);

        Product p4 = new Product(4, "Transmission Housing 8-Speed", "Automotive Transmission", "Chassis-Line-1",
                Date.valueOf("2026-10-02"), "BATCH-2026-TRN-04", "Hindalco Industries", "Maruti Suzuki India");
        p4.setDescription("Die-cast aluminum casing for 8-speed automatic torque-converter gearbox");
        cache.put(4, p4);

        Product p5 = new Product(5, "Planetary Gear Assembly Sun Pinion", "Aerospace Transmission", "Aero-Line-1",
                Date.valueOf("2026-10-03"), "BATCH-2026-GEAR-05", "Steel Authority of India", "Bharat Dynamics Ltd");
        p5.setDescription("Carburized case-hardened alloy gear for satellite solar deployment drives");
        cache.put(5, p5);
    }

    @Override
    public void addProduct(Product product) {
        String sql = "INSERT INTO products (product_name, category, production_line, manufacturing_date, batch_number, supplier, customer, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, product.getProductName());
            ps.setString(2, product.getCategory());
            ps.setString(3, product.getProductionLine());
            ps.setDate(4, product.getManufacturingDate());
            ps.setString(5, product.getBatchNumber());
            ps.setString(6, product.getSupplier());
            ps.setString(7, product.getCustomer());
            ps.setString(8, product.getDescription());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    product.setProductId(rs.getInt(1));
                }
            }
        } catch (Exception e) {
            if (product.getProductId() == 0) {
                product.setProductId(idGen.incrementAndGet());
            }
        }
        cache.put(product.getProductId(), product);
    }

    @Override
    public Product getProductById(int id) {
        String sql = "SELECT * FROM products WHERE product_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapProduct(rs);
                }
            }
        } catch (Exception ignored) {}
        return cache.get(id);
    }

    @Override
    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY product_id ASC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapProduct(rs));
            }
            if (!list.isEmpty()) return list;
        } catch (Exception ignored) {}
        return new ArrayList<>(cache.values());
    }

    @Override
    public void updateProduct(Product product) {
        String sql = "UPDATE products SET product_name = ?, category = ?, production_line = ?, manufacturing_date = ?, batch_number = ?, supplier = ?, customer = ?, description = ? WHERE product_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.getProductName());
            ps.setString(2, product.getCategory());
            ps.setString(3, product.getProductionLine());
            ps.setDate(4, product.getManufacturingDate());
            ps.setString(5, product.getBatchNumber());
            ps.setString(6, product.getSupplier());
            ps.setString(7, product.getCustomer());
            ps.setString(8, product.getDescription());
            ps.setInt(9, product.getProductId());
            ps.executeUpdate();
        } catch (Exception ignored) {}
        cache.put(product.getProductId(), product);
    }

    @Override
    public void deleteProduct(int id) {
        String sql = "DELETE FROM products WHERE product_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception ignored) {}
        cache.remove(id);
    }

    @Override
    public List<Product> searchProducts(String keyword, String category) {
        return getAllProducts().stream()
                .filter(p -> (keyword == null || keyword.isEmpty() ||
                              p.getProductName().toLowerCase().contains(keyword.toLowerCase()) ||
                              p.getBatchNumber().toLowerCase().contains(keyword.toLowerCase())))
                .filter(p -> (category == null || category.isEmpty() || "ALL".equalsIgnoreCase(category) ||
                              p.getCategory().equalsIgnoreCase(category)))
                .collect(Collectors.toList());
    }

    private Product mapProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductId(rs.getInt("product_id"));
        p.setProductName(rs.getString("product_name"));
        p.setCategory(rs.getString("category"));
        p.setProductionLine(rs.getString("production_line"));
        p.setManufacturingDate(rs.getDate("manufacturing_date"));
        p.setBatchNumber(rs.getString("batch_number"));
        p.setSupplier(rs.getString("supplier"));
        p.setCustomer(rs.getString("customer"));
        p.setDescription(rs.getString("description"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        p.setUpdatedAt(rs.getTimestamp("updated_at"));
        return p;
    }
}
