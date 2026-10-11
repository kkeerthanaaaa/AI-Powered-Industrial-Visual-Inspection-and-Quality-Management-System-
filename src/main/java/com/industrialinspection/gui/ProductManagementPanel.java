package com.industrialinspection.gui;

import com.industrialinspection.model.Product;
import com.industrialinspection.model.User;
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
 * 4. Product Registration & Management Panel.
 */
public class ProductManagementPanel extends JPanel {

    private final ProductService productService = new ProductService();
    private final User currentUser;
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final JTextField searchField;
    private final JComboBox<String> categoryFilter;

    public ProductManagementPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.BG_DARKEST);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        // Top Toolbar
        JPanel topBar = new JPanel(new BorderLayout(10, 10));
        topBar.setBackground(UITheme.BG_DARKEST);

        JLabel title = new JLabel("Product Catalog & Components Management");
        title.setFont(UITheme.FONT_HEADER_LARGE);
        title.setForeground(UITheme.TEXT_MAIN);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setBackground(UITheme.BG_DARKEST);

        searchField = UITheme.createTextField();
        searchField.setPreferredSize(new Dimension(180, 32));
        searchField.putClientProperty("JTextField.placeholderText", "Search name or batch...");

        categoryFilter = UITheme.createComboBox(new String[]{"ALL", "Automotive Braking", "Automotive Powertrain", "Aerospace Structural", "Automotive Transmission", "Aerospace Transmission"});
        categoryFilter.setPreferredSize(new Dimension(170, 32));

        JButton searchBtn = UITheme.createSecondaryButton("Filter");
        searchBtn.addActionListener(e -> refreshTable());

        JButton addBtn = UITheme.createPrimaryButton("+ Add Component");
        addBtn.addActionListener(e -> showAddEditDialog(null));
        if (!currentUser.canManageProducts()) addBtn.setEnabled(false);

        JButton expBtn = UITheme.createSecondaryButton("Export CSV");
        expBtn.addActionListener(e -> exportCSV());

        JButton impBtn = UITheme.createSecondaryButton("Import CSV");
        impBtn.addActionListener(e -> importCSV());
        if (!currentUser.canManageProducts()) impBtn.setEnabled(false);

        controls.add(new JLabel("Search:") {{ setForeground(UITheme.TEXT_MUTED); }});
        controls.add(searchField);
        controls.add(categoryFilter);
        controls.add(searchBtn);
        controls.add(addBtn);
        controls.add(expBtn);
        controls.add(impBtn);

        topBar.add(title, BorderLayout.WEST);
        topBar.add(controls, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Component Name", "Category", "Line", "Mfg Date", "Batch #", "Supplier", "Customer"};
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

        // Bottom Actions Bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        bottomBar.setBackground(UITheme.BG_DARKEST);

        JButton editBtn = UITheme.createSecondaryButton("Edit Selected");
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int id = (int) tableModel.getValueAt(row, 0);
                try {
                    Product p = productService.getProductById(id);
                    showAddEditDialog(p);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage());
                }
            } else {
                JOptionPane.showMessageDialog(this, "Select a product to edit.");
            }
        });

        JButton delBtn = UITheme.createDangerButton("Delete Selected");
        delBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int id = (int) tableModel.getValueAt(row, 0);
                int opt = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete Product #" + id + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (opt == JOptionPane.YES_OPTION) {
                    try {
                        productService.deleteProduct(id, currentUser.getUserId());
                        refreshTable();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, ex.getMessage());
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Select a product to delete.");
            }
        });

        if (!currentUser.canManageProducts()) {
            editBtn.setEnabled(false);
            delBtn.setEnabled(false);
        }

        bottomBar.add(editBtn);
        bottomBar.add(delBtn);
        add(bottomBar, BorderLayout.SOUTH);

        refreshTable();
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        String kw = searchField.getText().trim();
        String cat = (String) categoryFilter.getSelectedItem();
        List<Product> list = productService.searchProducts(kw, cat);

        for (Product p : list) {
            tableModel.addRow(new Object[]{
                p.getProductId(),
                p.getProductName(),
                p.getCategory(),
                p.getProductionLine(),
                DateTimeUtil.formatDate(p.getManufacturingDate()),
                p.getBatchNumber(),
                p.getSupplier(),
                p.getCustomer()
            });
        }
    }

    private void showAddEditDialog(Product existing) {
        boolean isEdit = (existing != null);
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), isEdit ? "Edit Product" : "Register Product", true);
        dlg.setSize(440, 520);
        dlg.setLocationRelativeTo(this);

        JPanel pnl = new JPanel(new GridLayout(9, 2, 8, 10));
        pnl.setBackground(UITheme.BG_CARD);
        pnl.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField nameF = UITheme.createTextField();
        JTextField catF = UITheme.createTextField();
        JTextField lineF = UITheme.createTextField();
        JTextField batchF = UITheme.createTextField();
        JTextField suppF = UITheme.createTextField();
        JTextField custF = UITheme.createTextField();
        JTextField descF = UITheme.createTextField();

        if (isEdit) {
            nameF.setText(existing.getProductName());
            catF.setText(existing.getCategory());
            lineF.setText(existing.getProductionLine());
            batchF.setText(existing.getBatchNumber());
            suppF.setText(existing.getSupplier());
            custF.setText(existing.getCustomer());
            descF.setText(existing.getDescription() != null ? existing.getDescription() : "");
        } else {
            catF.setText("Automotive Powertrain");
            lineF.setText("Auto-Line-2");
            batchF.setText("BATCH-" + System.currentTimeMillis() % 10000);
            suppF.setText("Local Precision Foundry");
            custF.setText("Tier 1 OEM Client");
        }

        pnl.add(createLabel("Product Name:")); pnl.add(nameF);
        pnl.add(createLabel("Category:")); pnl.add(catF);
        pnl.add(createLabel("Production Line:")); pnl.add(lineF);
        pnl.add(createLabel("Batch Number:")); pnl.add(batchF);
        pnl.add(createLabel("Supplier:")); pnl.add(suppF);
        pnl.add(createLabel("Customer:")); pnl.add(custF);
        pnl.add(createLabel("Description:")); pnl.add(descF);

        JButton saveBtn = UITheme.createPrimaryButton("Save Product");
        saveBtn.addActionListener(e -> {
            try {
                Product p = isEdit ? existing : new Product();
                p.setProductName(nameF.getText().trim());
                p.setCategory(catF.getText().trim());
                p.setProductionLine(lineF.getText().trim());
                p.setBatchNumber(batchF.getText().trim());
                p.setSupplier(suppF.getText().trim());
                p.setCustomer(custF.getText().trim());
                p.setDescription(descF.getText().trim());
                if (!isEdit) p.setManufacturingDate(new Date(System.currentTimeMillis()));

                if (isEdit) {
                    productService.updateProduct(p, currentUser.getUserId());
                } else {
                    productService.addProduct(p, currentUser.getUserId());
                }
                dlg.dispose();
                refreshTable();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dlg.dispose());

        pnl.add(cancelBtn);
        pnl.add(saveBtn);

        dlg.setContentPane(pnl);
        dlg.setVisible(true);
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_BOLD);
        l.setForeground(UITheme.TEXT_MAIN);
        return l;
    }

    private void exportCSV() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("products_export.csv"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                productService.exportProductsToCSV(fc.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Products exported successfully to " + fc.getSelectedFile().getName());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void importCSV() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                int count = productService.importProductsFromCSV(fc.getSelectedFile(), currentUser.getUserId());
                refreshTable();
                JOptionPane.showMessageDialog(this, "Imported " + count + " products successfully.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Import failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
