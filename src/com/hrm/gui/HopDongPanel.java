package com.hrm.gui;

import com.hrm.bus.HopDongBUS;
import com.hrm.bus.NhanVienBUS;
import com.hrm.dto.HopDongDTO;
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
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Hợp đồng lao động theo nhân viên: lương, ngày hiệu lực và kết thúc.
 * <p>
 * Hợp đồng trạng thái "Có hiệu lực" quyết định lương cơ bản trong bảng lương tháng.
 * </p>
 */
public class HopDongPanel extends JPanel {

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
    private HopDongBUS hopDongBUS;
    private NhanVienBUS nhanVienBUS;

    // ===== COMPONENTS =====
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> cboLoaiHD;
    private JComboBox<String> cboTrangThai;
    private JLabel lblStatus;

    private final SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public HopDongPanel() {
        hopDongBUS = new HopDongBUS();
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

        // Bên trái: Bộ lọc
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftPanel.setOpaque(false);

        // Ô tìm kiếm
        txtSearch = new JTextField(15);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setForeground(TEXT_WHITE);
        txtSearch.setBackground(FIELD_BG);
        txtSearch.setCaretColor(TEXT_WHITE);
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BORDER),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        txtSearch.setToolTipText("Mã HĐ, Mã NV...");
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterData(); }
            public void removeUpdate(DocumentEvent e) { filterData(); }
            public void changedUpdate(DocumentEvent e) { filterData(); }
        });
        leftPanel.add(txtSearch);

        // Combo Loại HĐ
        cboLoaiHD = createFilterCombo();
        cboLoaiHD.addItem("Tất cả loại HĐ");
        cboLoaiHD.addItem("Thử việc");
        cboLoaiHD.addItem("1 năm");
        cboLoaiHD.addItem("3 năm");
        cboLoaiHD.addItem("Vô thời hạn");
        cboLoaiHD.addActionListener(e -> filterData());
        leftPanel.add(cboLoaiHD);

        // Combo Trạng thái
        cboTrangThai = createFilterCombo();
        cboTrangThai.addItem("Tất cả trạng thái");
        cboTrangThai.addItem("Có hiệu lực");
        cboTrangThai.addItem("Đã hết hạn");
        cboTrangThai.addItem("Chấm dứt");
        cboTrangThai.addActionListener(e -> filterData());
        leftPanel.add(cboTrangThai);

        toolbar.add(leftPanel, BorderLayout.WEST);

        // Bên phải: Nút action
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

        PermissionHelper.applyVisible(btnAdd, PermissionHelper.canAdd("hopdong"));
        PermissionHelper.applyVisible(btnEdit, PermissionHelper.canEdit("hopdong"));
        PermissionHelper.applyVisible(btnDelete, PermissionHelper.canDelete("hopdong"));

        return toolbar;
    }

    private JComboBox<String> createFilterCombo() {
        JComboBox<String> cbo = new JComboBox<>();
        cbo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbo.setPreferredSize(new Dimension(140, 32));
        cbo.setBackground(Color.WHITE);
        cbo.setForeground(Color.BLACK);
        cbo.setFocusable(false);
        return cbo;
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
        String[] columns = {"Mã HĐ", "Nhân viên", "Loại Hợp đồng", "Ngày bắt đầu", "Ngày kết thúc", "Lương cơ bản", "Trạng thái"};
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
                if (column == 5) lbl.setHorizontalAlignment(SwingConstants.RIGHT);
                return lbl;
            }
        });

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

                ((JLabel) c).setHorizontalAlignment(SwingConstants.LEFT);
                if (column == 5) { // Lương cơ bản
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.RIGHT);
                    c.setForeground(new Color(96, 165, 250)); // Xanh nhạt
                }
                
                if (column == 6) { // Trạng thái
                    String status = (String) value;
                    if (status != null) {
                        if (status.equals("Có hiệu lực")) c.setForeground(new Color(110, 231, 183)); // Xanh lá
                        else if (status.equals("Chấm dứt")) c.setForeground(new Color(252, 165, 165)); // Đỏ
                        else if (status.equals("Đã hết hạn")) c.setForeground(new Color(253, 230, 138)); // Vàng cam
                        else c.setForeground(new Color(209, 213, 219)); // Xám
                    }
                }

                ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return c;
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1
                        && PermissionHelper.canEdit("hopdong")) {
                    openEditDialog();
                }
            }
        });

        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(120);
        table.getColumnModel().getColumn(6).setPreferredWidth(100);

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
        hopDongBUS.loadData();
        nhanVienBUS.loadData();
        loadTableData();
    }

    public void loadTableData() {
        filterData();
    }

    private void filterData() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        String selLoai = (String) cboLoaiHD.getSelectedItem();
        String selTrangThai = (String) cboTrangThai.getSelectedItem();
        
        ArrayList<HopDongDTO> filtered = new ArrayList<>();

        for (HopDongDTO dto : hopDongBUS.getList()) {
            NhanVienDTO nv = nhanVienBUS.getById(dto.getMaNV());
            String hoten = nv != null ? (nv.getHo() + " " + nv.getTen()).toLowerCase() : "";
            
            boolean matchKeyword = keyword.isEmpty() ||
                                   dto.getMaHD().toLowerCase().contains(keyword) ||
                                   dto.getMaNV().toLowerCase().contains(keyword) ||
                                   hoten.contains(keyword);
            
            boolean matchStatus = selTrangThai == null || selTrangThai.equals("Tất cả trạng thái") || dto.getTrangThai().equalsIgnoreCase(selTrangThai);
            boolean matchLoai = selLoai == null || selLoai.equals("Tất cả loại HĐ") || dto.getLoaiHD().equalsIgnoreCase(selLoai);

            if (matchKeyword && matchStatus && matchLoai) {
                filtered.add(dto);
            }
        }
        populateTable(filtered);
    }

    private void populateTable(ArrayList<HopDongDTO> list) {
        tableModel.setRowCount(0);

        for (HopDongDTO dto : list) {
            String tenNhanVien = dto.getMaNV();
            NhanVienDTO nv = nhanVienBUS.getById(dto.getMaNV());
            if (nv != null) {
                tenNhanVien = nv.getMaNV() + " - " + nv.getHo() + " " + nv.getTen();
            }

            String ngayBatDau = dto.getNgayBatDau() != null ? sdfDate.format(dto.getNgayBatDau()) : "";
            String ngayKetThuc = dto.getNgayKetThuc() != null ? sdfDate.format(dto.getNgayKetThuc()) : "Vô thời hạn";
            String luongText = dto.getLuongHopDong() != null ? currencyFormat.format(dto.getLuongHopDong()) : "0 ₫";

            tableModel.addRow(new Object[]{
                    dto.getMaHD(),
                    tenNhanVien,
                    dto.getLoaiHD(),
                    ngayBatDau,
                    ngayKetThuc,
                    luongText,
                    dto.getTrangThai()
            });
        }
        lblStatus.setText("Tổng số hợp đồng theo bộ lọc: " + list.size());
    }

    // =========================================================================
    //  ACTIONS
    // =========================================================================
    private void openAddDialog() {
        HopDongDialog dialog = new HopDongDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                "Thêm mới Hợp đồng",
                null, 
                hopDongBUS, nhanVienBUS
        );
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            hopDongBUS.loadData();
            loadTableData();
        }
    }

    private void openEditDialog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn hợp đồng để xem/sửa!", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maHD = (String) tableModel.getValueAt(selectedRow, 0);
        HopDongDTO dto = hopDongBUS.getById(maHD);

        if (dto == null) return;

        HopDongDialog dialog = new HopDongDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                "Cập nhật Hợp đồng",
                dto, 
                hopDongBUS, nhanVienBUS
        );
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            hopDongBUS.loadData();
            loadTableData();
        }
    }

    private void deleteSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn hợp đồng để xóa!", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maHD = (String) tableModel.getValueAt(selectedRow, 0);
        String thongTin = maHD + " - " + tableModel.getValueAt(selectedRow, 1);

        int choice = JOptionPane.showConfirmDialog(this,
                "Chắc chắn xóa hợp đồng: " + thongTin + "?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                if (hopDongBUS.delete(maHD)) {
                    loadTableData();
                    JOptionPane.showMessageDialog(this,
                            "Xóa thành công!", "Thành công",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Xóa thất bại!", "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Lỗi khi xóa: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
