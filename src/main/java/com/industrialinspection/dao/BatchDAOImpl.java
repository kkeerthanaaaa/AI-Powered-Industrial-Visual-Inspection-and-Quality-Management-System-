package com.industrialinspection.dao;

import com.industrialinspection.model.ProductionBatch;
import com.industrialinspection.model.enums.BatchStatus;
import com.industrialinspection.model.enums.Shift;
import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class BatchDAOImpl implements BatchDAO {

    private final DBConnection db = DBConnection.getInstance();
    private static final Map<Integer, ProductionBatch> cache = new ConcurrentHashMap<>();
    private static final AtomicInteger idGen = new AtomicInteger(10);

    static {
        // Seed default batches matching industrial_inspection.sql
        ProductionBatch b1 = new ProductionBatch(1, "BATCH-2026-BRK-01", 1, 500, Date.valueOf("2026-09-28"), Shift.MORNING, 2, 1, BatchStatus.APPROVED);
        b1.setProductName("Brake Disc Ventilated 320mm");
        b1.setMachineName("CNC-002");
        b1.setOperatorName("Kavita Roy");
        cache.put(1, b1);

        ProductionBatch b2 = new ProductionBatch(2, "BATCH-2026-ENG-02", 2, 250, Date.valueOf("2026-09-30"), Shift.EVENING, 2, 2, BatchStatus.IN_INSPECTION);
        b2.setProductName("Engine Mount Hydraulic Assembly");
        b2.setMachineName("CNC-002");
        b2.setOperatorName("Anand Verma");
        cache.put(2, b2);

        ProductionBatch b3 = new ProductionBatch(3, "BATCH-2026-AERO-03", 3, 50, Date.valueOf("2026-10-01"), Shift.MORNING, 1, 1, BatchStatus.IN_INSPECTION);
        b3.setProductName("Aircraft Titanium Wing Bracket");
        b3.setMachineName("CNC-001");
        b3.setOperatorName("Kavita Roy");
        cache.put(3, b3);

        ProductionBatch b4 = new ProductionBatch(4, "BATCH-2026-TRN-04", 4, 180, Date.valueOf("2026-10-02"), Shift.NIGHT, 3, 3, BatchStatus.PENDING);
        b4.setProductName("Transmission Housing 8-Speed");
        b4.setMachineName("PRESS-001");
        b4.setOperatorName("Deepak Kumar");
        cache.put(4, b4);

        ProductionBatch b5 = new ProductionBatch(5, "BATCH-2026-GEAR-05", 5, 80, Date.valueOf("2026-10-03"), Shift.MORNING, 1, 4, BatchStatus.PENDING);
        b5.setProductName("Planetary Gear Assembly Sun Pinion");
        b5.setMachineName("CNC-001");
        b5.setOperatorName("Sunita Joshi");
        cache.put(5, b5);
    }

    @Override
    public void addBatch(ProductionBatch batch) {
        String sql = "INSERT INTO production_batches (batch_number, product_id, batch_size, production_date, shift, machine_id, operator_id, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, batch.getBatchNumber());
            ps.setInt(2, batch.getProductId());
            ps.setInt(3, batch.getBatchSize());
            ps.setDate(4, batch.getProductionDate());
            ps.setString(5, batch.getShift().name());
            ps.setInt(6, batch.getMachineId());
            ps.setInt(7, batch.getOperatorId());
            ps.setString(8, batch.getStatus().name());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    batch.setBatchId(rs.getInt(1));
                }
            }
        } catch (Exception e) {
            if (batch.getBatchId() == 0) {
                batch.setBatchId(idGen.incrementAndGet());
            }
        }
        cache.put(batch.getBatchId(), batch);
    }

    @Override
    public ProductionBatch getBatchById(int id) {
        String sql = "SELECT b.*, p.product_name, m.machine_name, o.name AS operator_name " +
                     "FROM production_batches b " +
                     "LEFT JOIN products p ON b.product_id = p.product_id " +
                     "LEFT JOIN machines m ON b.machine_id = m.machine_id " +
                     "LEFT JOIN operators o ON b.operator_id = o.operator_id " +
                     "WHERE b.batch_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapBatch(rs);
                }
            }
        } catch (Exception ignored) {}
        return cache.get(id);
    }

    @Override
    public ProductionBatch getBatchByNumber(String batchNumber) {
        String sql = "SELECT b.*, p.product_name, m.machine_name, o.name AS operator_name " +
                     "FROM production_batches b " +
                     "LEFT JOIN products p ON b.product_id = p.product_id " +
                     "LEFT JOIN machines m ON b.machine_id = m.machine_id " +
                     "LEFT JOIN operators o ON b.operator_id = o.operator_id " +
                     "WHERE b.batch_number = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, batchNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapBatch(rs);
                }
            }
        } catch (Exception ignored) {}

        for (ProductionBatch b : cache.values()) {
            if (b.getBatchNumber().equalsIgnoreCase(batchNumber)) return b;
        }
        return null;
    }

    @Override
    public List<ProductionBatch> getAllBatches() {
        List<ProductionBatch> list = new ArrayList<>();
        String sql = "SELECT b.*, p.product_name, m.machine_name, o.name AS operator_name " +
                     "FROM production_batches b " +
                     "LEFT JOIN products p ON b.product_id = p.product_id " +
                     "LEFT JOIN machines m ON b.machine_id = m.machine_id " +
                     "LEFT JOIN operators o ON b.operator_id = o.operator_id " +
                     "ORDER BY b.batch_id ASC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapBatch(rs));
            }
            if (!list.isEmpty()) return list;
        } catch (Exception ignored) {}
        return new ArrayList<>(cache.values());
    }

    @Override
    public void updateBatch(ProductionBatch batch) {
        String sql = "UPDATE production_batches SET batch_number = ?, product_id = ?, batch_size = ?, production_date = ?, shift = ?, machine_id = ?, operator_id = ?, status = ? WHERE batch_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, batch.getBatchNumber());
            ps.setInt(2, batch.getProductId());
            ps.setInt(3, batch.getBatchSize());
            ps.setDate(4, batch.getProductionDate());
            ps.setString(5, batch.getShift().name());
            ps.setInt(6, batch.getMachineId());
            ps.setInt(7, batch.getOperatorId());
            ps.setString(8, batch.getStatus().name());
            ps.setInt(9, batch.getBatchId());
            ps.executeUpdate();
        } catch (Exception ignored) {}
        cache.put(batch.getBatchId(), batch);
    }

    @Override
    public void deleteBatch(int id) {
        String sql = "DELETE FROM production_batches WHERE batch_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception ignored) {}
        cache.remove(id);
    }

    @Override
    public List<ProductionBatch> searchBatches(String keyword, String status) {
        return getAllBatches().stream()
                .filter(b -> (keyword == null || keyword.isEmpty() ||
                              b.getBatchNumber().toLowerCase().contains(keyword.toLowerCase()) ||
                              (b.getProductName() != null && b.getProductName().toLowerCase().contains(keyword.toLowerCase()))))
                .filter(b -> (status == null || status.isEmpty() || "ALL".equalsIgnoreCase(status) ||
                              b.getStatus().name().equalsIgnoreCase(status)))
                .collect(Collectors.toList());
    }

    private ProductionBatch mapBatch(ResultSet rs) throws SQLException {
        ProductionBatch b = new ProductionBatch();
        b.setBatchId(rs.getInt("batch_id"));
        b.setBatchNumber(rs.getString("batch_number"));
        b.setProductId(rs.getInt("product_id"));
        b.setBatchSize(rs.getInt("batch_size"));
        b.setProductionDate(rs.getDate("production_date"));
        b.setShift(Shift.fromString(rs.getString("shift")));
        b.setMachineId(rs.getInt("machine_id"));
        b.setOperatorId(rs.getInt("operator_id"));
        b.setStatus(BatchStatus.fromString(rs.getString("status")));
        b.setCreatedAt(rs.getTimestamp("created_at"));
        try {
            b.setProductName(rs.getString("product_name"));
            b.setMachineName(rs.getString("machine_name"));
            b.setOperatorName(rs.getString("operator_name"));
        } catch (SQLException ignored) {}
        return b;
    }
}
