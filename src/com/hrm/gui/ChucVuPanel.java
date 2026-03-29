package com.hrm.gui;

import com.hrm.bus.ChucVuBUS;
import com.hrm.dto.ChucVuDTO;
import com.hrm.security.PermissionHelper;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * Danh mục chức vụ và hệ số lương; được nhân viên và tính lương tham chiếu.
 * <p>
 * CRUD chuẩn với kiểm tra quyền module {@code chucvu}.
 * </p>
 */
public class ChucVuPanel extends JPanel {

    // ===== MÀU SẮC =====
    private static final Color PRIMARY       = new Color(0, 82, 155);
    private static final Color PRIMARY_DARK  = new Color(0, 51, 102);
    private static final Color BG_CONTENT    = new Color(30, 41, 59);
    private static final Color BG_TOOLBAR    = new Color(22, 33, 52);
    private static final Color TEXT_WHITE    = new Color(248, 250, 252);
    private static final Color TEXT_MUTED    = new Color(148, 163, 184);
    private static final Color TABLE_ROW_ALT = new Color(22, 33, 52);
    private static final Color TABLE_SEL     = new Color(0, 82, 155);
    private static final Color BTN_DANGER    = new Color(185, 28, 28);
    private static final Color BTN_SECONDARY = new Color(55, 65, 81);
    private static final Color FIELD_BG      = new Color(15, 23, 42);
    private static final Color FIELD_BORDER  = new Color(71, 85, 105);

    // ===== BUS =====
    private ChucVuBUS chucVuBUS;

    // ===== COMPONENTS =====
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JLabel lblStatus;

    public ChucVuPanel() {
        chucVuBUS = new ChucVuBUS();
        initUI();
        loadTableData();

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                refreshData();
            }
        });
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(BG_CONTENT);

        add(createToolbar(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);
    }

    // =========================================================================
    //  TOOLBAR
    // =========================================================================
    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(BG_TOOLBAR);
        toolbar.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        // Bên trái: Search
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftPanel.setOpaque(false);

        txtSearch = new JTextField(20);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setForeground(TEXT_WHITE);
        txtSearch.setBackground(FIELD_BG);
        txtSearch.setCaretColor(TEXT_WHITE);
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BORDER),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        txtSearch.setToolTipText("Tìm theo mã, tên chức vụ...");
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterData(); }
            public void removeUpdate(DocumentEvent e) { filterData(); }
            public void changedUpdate(DocumentEvent e) { filterData(); }
        });
        leftPanel.add(txtSearch);
        toolbar.add(leftPanel, BorderLayout.WEST);

        // Bên phải: Buttons
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightPanel.setOpaque(false);

        JButton btnAdd = createStyledButton("+ Thêm", PRIMARY);
        btnAdd.addActionListener(e -> openAddDialog());
        rightPanel.add(btnAdd);

        JButton btnEdit = createStyledButton("Sửa", BTN_SECONDARY);
        btnEdit.addActionListener(e -> openEditDialog());
        rightPanel.add(btnEdit);

        JButton btnDelete = createStyledButton("Xóa", BTN_DANGER);
        btnDelete.addActionListener(e -> deleteSelected());
        rightPanel.add(btnDelete);

        toolbar.add(rightPanel, BorderLayout.EAST);

        PermissionHelper.applyVisible(btnAdd, PermissionHelper.canAdd("chucvu"));
        PermissionHelper.applyVisible(btnEdit, PermissionHelper.canEdit("chucvu"));
        PermissionHelper.applyVisible(btnDelete, PermissionHelper.canDelete("chucvu"));

        return toolbar;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(TEXT_WHITE);
        btn.setBackground(bgColor);
        btn.setPreferredSize(new Dimension(80, 34));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);

        Color hoverColor = bgColor.brighter();
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { btn.setBackground(hoverColor); }
            @Override
            public void mouseExited(MouseEvent e) { btn.setBackground(bgColor); }
        });
        return btn;
    }

    // =========================================================================
    //  TABLE
    // =========================================================================
    private JScrollPane createTablePanel() {
        String[] columns = {"Mã Chức vụ", "Tên Chức vụ", "Hệ số lương"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setForeground(TEXT_WHITE);
        table.setBackground(BG_CONTENT);
        table.setSelectionBackground(TABLE_SEL);
        table.setSelectionForeground(TEXT_WHITE);
        table.setRowHeight(36);
        table.setGridColor(new Color(40, 50, 70));
        table.setShowGrid(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setIntercellSpacing(new Dimension(0, 1));

        // Custom Header Renderer
        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 38));
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setBackground(PRIMARY_DARK);
                lbl.setForeground(TEXT_WHITE);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(40, 50, 70)),
                        BorderFactory.createEmptyBorder(0, 8, 0, 8)
                ));
                lbl.setHorizontalAlignment(SwingConstants.LEFT);
                return lbl;
            }
        });

        // Cell Renderer cho Alternate Rows
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (isSelected) {
                    c.setBackground(TABLE_SEL);
                    c.setForeground(TEXT_WHITE);
                } else {
                    c.setBackground(row % 2 == 0 ? BG_CONTENT : TABLE_ROW_ALT);
                    c.setForeground(TEXT_WHITE);
                }
                ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return c;
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1
                        && PermissionHelper.canEdit("chucvu")) {
                    openEditDialog();
                }
            }
        });

        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(250);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BG_CONTENT);
        return scrollPane;
    }

    // =========================================================================
    //  STATUS BAR
    // =========================================================================
    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        statusBar.setBackground(BG_TOOLBAR);
        statusBar.setPreferredSize(new Dimension(0, 30));
        statusBar.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));

        lblStatus = new JLabel("");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatus.setForeground(TEXT_MUTED);
        statusBar.add(lblStatus);

        return statusBar;
    }

    // =========================================================================
    //  DATA
    // =========================================================================
    public void refreshData() {
        chucVuBUS.loadData();
        loadTableData();
    }

    public void loadTableData() {
        filterData();
    }

    private void filterData() {
        String keyword = txtSearch.getText().trim();
        ArrayList<ChucVuDTO> list = chucVuBUS.search(keyword);
        populateTable(list);
    }

    private void populateTable(ArrayList<ChucVuDTO> list) {
        tableModel.setRowCount(0);
        for (ChucVuDTO cv : list) {
            tableModel.addRow(new Object[]{
                    cv.getMaCV(),
                    cv.getTenCV(),
                    cv.getHeSoLuong()
            });
        }
        lblStatus.setText("Tổng số chức vụ: " + list.size());
    }

    // =========================================================================
    //  ACTIONS
    // =========================================================================
    private void openAddDialog() {
        ChucVuDialog dialog = new ChucVuDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                "Thêm Chức vụ",
                null, 
                chucVuBUS
        );
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            chucVuBUS.loadData();
            loadTableData();
        }
    }

    private void openEditDialog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn chức vụ cần sửa!", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maCV = (String) tableModel.getValueAt(selectedRow, 0);
        ChucVuDTO cv = chucVuBUS.getById(maCV);
        if (cv == null) return;

        ChucVuDialog dialog = new ChucVuDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                "Chỉnh sửa Chức vụ",
                cv, 
                chucVuBUS
        );
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            chucVuBUS.loadData();
            loadTableData();
        }
    }

    private void deleteSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn chức vụ cần xóa!", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maCV = (String) tableModel.getValueAt(selectedRow, 0);
        String tenCV = (String) tableModel.getValueAt(selectedRow, 1);

        int choice = JOptionPane.showConfirmDialog(this,
                "Chắc chắn xóa chức vụ " + tenCV + " (" + maCV + ")? Các nhân viên thuộc chức vụ này có thể bị ảnh hưởng.",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                if (chucVuBUS.delete(maCV)) {
                    loadTableData();
                    JOptionPane.showMessageDialog(this,
                            "Xóa chức vụ thành công!", "Thành công",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Xóa thất bại!", "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                // Có thể lỗi FK reference do NhanVien đang dùng chức vụ này
                JOptionPane.showMessageDialog(this,
                        "Lỗi khi xóa: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
