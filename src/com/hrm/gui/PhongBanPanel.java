package com.hrm.gui;

import com.hrm.bus.PhongBanBUS;
import com.hrm.bus.NhanVienBUS;
import com.hrm.dto.PhongBanDTO;
import com.hrm.dto.NhanVienDTO;
import com.hrm.security.PermissionHelper;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Danh mục phòng ban; nhân viên tham chiếu {@code maPB} để hiển thị và báo cáo.
 * <p>
 * Giao diện đồng bộ theme tối với các panel CRUD khác.
 * </p>
 */
public class PhongBanPanel extends JPanel {

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
    private PhongBanBUS phongBanBUS;
    private NhanVienBUS nhanVienBUS;

    // ===== COMPONENTS =====
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JLabel lblStatus;

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    public PhongBanPanel() {
        phongBanBUS = new PhongBanBUS();
        nhanVienBUS = new NhanVienBUS();
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
        txtSearch.setToolTipText("Tìm theo mã, tên phòng ban...");
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

        PermissionHelper.applyVisible(btnAdd, PermissionHelper.canAdd("phongban"));
        PermissionHelper.applyVisible(btnEdit, PermissionHelper.canEdit("phongban"));
        PermissionHelper.applyVisible(btnDelete, PermissionHelper.canDelete("phongban"));

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
        String[] columns = {"Mã PB", "Tên Phòng ban", "Trưởng phòng", "Ngày thành lập", "Email"};
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

        // Alternate Rows
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
                        && PermissionHelper.canEdit("phongban")) {
                    openEditDialog();
                }
            }
        });

        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(180);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(200);

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
        phongBanBUS.loadData();
        nhanVienBUS.loadData();
        loadTableData();
    }

    public void loadTableData() {
        filterData();
    }

    private void filterData() {
        String keyword = txtSearch.getText().trim();
        ArrayList<PhongBanDTO> list = phongBanBUS.search(keyword);
        populateTable(list);
    }

    private void populateTable(ArrayList<PhongBanDTO> list) {
        tableModel.setRowCount(0);

        for (PhongBanDTO pb : list) {
            String tenTruongPhong = "Chưa có";
            if (pb.getMaTruongPB() != null && !pb.getMaTruongPB().trim().isEmpty()) {
                NhanVienDTO nv = nhanVienBUS.getById(pb.getMaTruongPB());
                if (nv != null) {
                    tenTruongPhong = nv.getHo() + " " + nv.getTen() + " (" + nv.getMaNV() + ")";
                }
            }

            String ngayTL = "";
            if (pb.getNgayThanhLap() != null) {
                ngayTL = sdf.format(pb.getNgayThanhLap());
            }

            tableModel.addRow(new Object[]{
                    pb.getMaPB(),
                    pb.getTenPB(),
                    tenTruongPhong,
                    ngayTL,
                    pb.getEmail()
            });
        }
        lblStatus.setText("Tổng số phòng ban: " + list.size());
    }

    // =========================================================================
    //  ACTIONS
    // =========================================================================
    private void openAddDialog() {
        PhongBanDialog dialog = new PhongBanDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                "Thêm Phòng ban",
                null, 
                phongBanBUS, nhanVienBUS
        );
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            phongBanBUS.loadData();
            loadTableData();
        }
    }

    private void openEditDialog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn phòng ban cần sửa!", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maPB = (String) tableModel.getValueAt(selectedRow, 0);
        PhongBanDTO pb = phongBanBUS.getById(maPB);

        if (pb == null) return;

        PhongBanDialog dialog = new PhongBanDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                "Chỉnh sửa Phòng ban",
                pb, 
                phongBanBUS, nhanVienBUS
        );
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            phongBanBUS.loadData();
            loadTableData();
        }
    }

    private void deleteSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn phòng ban cần xóa!", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maPB = (String) tableModel.getValueAt(selectedRow, 0);
        String tenPB = (String) tableModel.getValueAt(selectedRow, 1);

        int choice = JOptionPane.showConfirmDialog(this,
                "Chắc chắn xóa phòng ban " + tenPB + " (" + maPB + ")? Các nhân viên thuộc phòng ban này có thể bị ảnh hưởng.",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                if (phongBanBUS.delete(maPB)) {
                    loadTableData();
                    JOptionPane.showMessageDialog(this,
                            "Xóa phòng ban thành công!", "Thành công",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Xóa thất bại!", "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                // Lỗi khóa ngoại
                JOptionPane.showMessageDialog(this,
                        "Lỗi khi xóa: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
