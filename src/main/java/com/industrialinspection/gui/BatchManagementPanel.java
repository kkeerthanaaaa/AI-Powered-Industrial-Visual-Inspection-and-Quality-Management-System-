package com.industrialinspection.gui;

import com.industrialinspection.model.Machine;
import com.industrialinspection.model.Operator;
import com.industrialinspection.model.Product;
import com.industrialinspection.model.ProductionBatch;
import com.industrialinspection.model.User;
import com.industrialinspection.model.enums.BatchStatus;
import com.industrialinspection.model.enums.Shift;
import com.industrialinspection.service.BatchService;
import com.industrialinspection.service.MachineService;
import com.industrialinspection.service.OperatorService;
import com.industrialinspection.service.ProductService;
import com.industrialinspection.util.DateTimeUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.Date;
import java.util.List;

/**
 * 5. Production Batch Management Panel.
 */
public class BatchManagementPanel extends JPanel {

    private final BatchService batchService = new BatchService();
    private final ProductService productService = new ProductService();
    private final MachineService machineService = new MachineService();
    private final OperatorService operatorService = new OperatorService();
    private final User currentUser;

    private final JTable table;
    private final DefaultTableModel tableModel;
    private final JTextField searchField;
    private final JComboBox<String> statusFilter;

    public BatchManagementPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.BG_DARKEST);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel topBar = new JPanel(new BorderLayout(10, 10));
        topBar.setBackground(UITheme.BG_DARKEST);

        JLabel title = new JLabel("Production Batch & Lot Management");
        title.setFont(UITheme.FONT_HEADER_LARGE);
        title.setForeground(UITheme.TEXT_MAIN);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setBackground(UITheme.BG_DARKEST);

        searchField = UITheme.createTextField();
        searchField.setPreferredSize(new Dimension(170, 32));

        statusFilter = UITheme.createComboBox(new String[]{"ALL", "PENDING", "IN_INSPECTION", "APPROVED", "REJECTED", "QUARANTINED"});
        statusFilter.setPreferredSize(new Dimension(150, 32));

        JButton filterBtn = UITheme.createSecondaryButton("Filter");
        filterBtn.addActionListener(e -> refreshTable());

        JButton addBtn = UITheme.createPrimaryButton("+ Create Batch");
        addBtn.addActionListener(e -> showAddEditDialog(null));
        if (!currentUser.canManageBatches()) addBtn.setEnabled(false);

        JButton expBtn = UITheme.createSecondaryButton("Export CSV");
        expBtn.addActionListener(e -> exportCSV());

        JButton impBtn = UITheme.createSecondaryButton("Import CSV");
        impBtn.addActionListener(e -> importCSV());
        if (!currentUser.canManageBatches()) impBtn.setEnabled(false);

        controls.add(new JLabel("Search:") {{ setForeground(UITheme.TEXT_MUTED); }});
        controls.add(searchField);
        controls.add(statusFilter);
        controls.add(filterBtn);
        controls.add(addBtn);
        controls.add(expBtn);
        controls.add(impBtn);

        topBar.add(title, BorderLayout.WEST);
        topBar.add(controls, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        String[] cols = {"ID", "Batch Number", "Product", "Lot Size", "Date", "Shift", "Machine", "Operator", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        scrollPane.getViewport().setBackground(UITheme.BG_CARD);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        bottomBar.setBackground(UITheme.BG_DARKEST);

        JButton editBtn = UITheme.createSecondaryButton("Edit Batch");
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int id = (int) tableModel.getValueAt(row, 0);
                try {
                    ProductionBatch b = batchService.getBatchById(id);
                    showAddEditDialog(b);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage());
                }
            } else {
                JOptionPane.showMessageDialog(this, "Select a batch to edit.");
            }
        });

        JButton statusBtn = UITheme.createSecondaryButton("Update Status");
        statusBtn.addActionListener(e -> updateSelectedStatus());

        JButton delBtn = UITheme.createDangerButton("Delete Batch");
        delBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int id = (int) tableModel.getValueAt(row, 0);
                int opt = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete Batch #" + id + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (opt == JOptionPane.YES_OPTION) {
                    try {
                        batchService.deleteBatch(id, currentUser.getUserId());
                        refreshTable();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, ex.getMessage());
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Select a batch to delete.");
            }
        });

        if (!currentUser.canManageBatches()) {
            editBtn.setEnabled(false);
            delBtn.setEnabled(false);
        }

        bottomBar.add(editBtn);
        bottomBar.add(statusBtn);
        bottomBar.add(delBtn);
        add(bottomBar, BorderLayout.SOUTH);

        refreshTable();
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        String kw = searchField.getText().trim();
        String st = (String) statusFilter.getSelectedItem();
        List<ProductionBatch> list = batchService.searchBatches(kw, st);

        for (ProductionBatch b : list) {
            tableModel.addRow(new Object[]{
                b.getBatchId(),
                b.getBatchNumber(),
                b.getProductName() != null ? b.getProductName() : ("Product #" + b.getProductId()),
                b.getBatchSize(),
                DateTimeUtil.formatDate(b.getProductionDate()),
                b.getShift().name(),
                b.getMachineName() != null ? b.getMachineName() : ("Machine #" + b.getMachineId()),
                b.getOperatorName() != null ? b.getOperatorName() : ("Operator #" + b.getOperatorId()),
                b.getStatus().name()
            });
        }
    }

    private void showAddEditDialog(ProductionBatch existing) {
        boolean isEdit = (existing != null);
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), isEdit ? "Edit Batch" : "Create Production Batch", true);
        dlg.setSize(440, 480);
        dlg.setLocationRelativeTo(this);

        JPanel pnl = new JPanel(new GridLayout(8, 2, 8, 10));
        pnl.setBackground(UITheme.BG_CARD);
        pnl.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField numF = UITheme.createTextField();
        JTextField sizeF = UITheme.createTextField();

        List<Product> products = productService.getAllProducts();
        JComboBox<Product> prodCb = new JComboBox<>(products.toArray(new Product[0]));

        List<Machine> machines = machineService.getAllMachines();
        JComboBox<Machine> machCb = new JComboBox<>(machines.toArray(new Machine[0]));

        List<Operator> operators = operatorService.getAllOperators();
        JComboBox<Operator> opCb = new JComboBox<>(operators.toArray(new Operator[0]));

        JComboBox<Shift> shiftCb = new JComboBox<>(Shift.values());
        JComboBox<BatchStatus> statCb = new JComboBox<>(BatchStatus.values());

        if (isEdit) {
            numF.setText(existing.getBatchNumber());
            sizeF.setText(String.valueOf(existing.getBatchSize()));
            shiftCb.setSelectedItem(existing.getShift());
            statCb.setSelectedItem(existing.getStatus());
        } else {
            numF.setText("BATCH-" + System.currentTimeMillis() % 10000);
            sizeF.setText("100");
        }

        pnl.add(createLabel("Batch Number:")); pnl.add(numF);
        pnl.add(createLabel("Select Product:")); pnl.add(prodCb);
        pnl.add(createLabel("Batch Lot Size:")); pnl.add(sizeF);
        pnl.add(createLabel("Production Shift:")); pnl.add(shiftCb);
        pnl.add(createLabel("Assigned Machine:")); pnl.add(machCb);
        pnl.add(createLabel("Assigned Operator:")); pnl.add(opCb);
        pnl.add(createLabel("Initial Status:")); pnl.add(statCb);

        JButton saveBtn = UITheme.createPrimaryButton("Save Batch");
        saveBtn.addActionListener(e -> {
            try {
                ProductionBatch b = isEdit ? existing : new ProductionBatch();
                b.setBatchNumber(numF.getText().trim());
                b.setBatchSize(Integer.parseInt(sizeF.getText().trim()));
                Product selProd = (Product) prodCb.getSelectedItem();
                if (selProd != null) b.setProductId(selProd.getProductId());
                Machine selM = (Machine) machCb.getSelectedItem();
                if (selM != null) b.setMachineId(selM.getMachineId());
                Operator selOp = (Operator) opCb.getSelectedItem();
                if (selOp != null) b.setOperatorId(selOp.getOperatorId());
                b.setShift((Shift) shiftCb.getSelectedItem());
                b.setStatus((BatchStatus) statCb.getSelectedItem());
                if (!isEdit) b.setProductionDate(new Date(System.currentTimeMillis()));

                if (isEdit) {
                    batchService.updateBatch(b, currentUser.getUserId());
                } else {
                    batchService.createBatch(b, currentUser.getUserId());
                }
                dlg.dispose();
                refreshTable();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dlg.dispose());

        pnl.add(cancelBtn);
        pnl.add(saveBtn);

        dlg.setContentPane(pnl);
        dlg.setVisible(true);
    }

    private void updateSelectedStatus() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a batch from the table.");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        BatchStatus newSt = (BatchStatus) JOptionPane.showInputDialog(
                this, "Choose new status:", "Update Batch Status",
                JOptionPane.QUESTION_MESSAGE, null, BatchStatus.values(), BatchStatus.IN_INSPECTION);
        if (newSt != null) {
            batchService.updateBatchStatus(id, newSt, currentUser.getUserId());
            refreshTable();
        }
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_BOLD);
        l.setForeground(UITheme.TEXT_MAIN);
        return l;
    }

    private void exportCSV() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("batches_export.csv"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                batchService.exportBatchesToCSV(fc.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Batches exported successfully to " + fc.getSelectedFile().getName());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void importCSV() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                int count = batchService.importBatchesFromCSV(fc.getSelectedFile(), currentUser.getUserId());
                refreshTable();
                JOptionPane.showMessageDialog(this, "Imported " + count + " batches successfully.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Import failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
