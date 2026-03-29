package com.hrm.gui;

import com.hrm.bus.NhanVienBUS;
import com.hrm.bus.ChiTietNhanVienBUS;
import com.hrm.bus.PhongBanBUS;
import com.hrm.bus.ChucVuBUS;
import com.hrm.dto.NhanVienDTO;
import com.hrm.dto.ChiTietNhanVienDTO;
import com.hrm.dto.PhongBanDTO;
import com.hrm.dto.ChucVuDTO;
import com.hrm.security.PermissionHelper;
import com.hrm.util.ExcelHelper;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

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
 * Danh sách và thao tác CRUD nhân viên; form mở rộng lưu song song {@link com.hrm.dto.ChiTietNhanVienDTO}.
 * <p>
 * Thanh trạng thái hiển thị số đang làm / đã nghỉ; quyền nút theo module {@code nhanvien}.
 * </p>
 */
public class NhanVienPanel extends JPanel {

    // ===== BẢNG MÀU =====
    private static final Color PRIMARY       = new Color(0, 82, 155);
    private static final Color PRIMARY_DARK  = new Color(0, 51, 102);
    private static final Color BG_CONTENT    = new Color(30, 41, 59);
    private static final Color BG_TOOLBAR    = new Color(22, 33, 52);
    private static final Color TEXT_WHITE    = new Color(248, 250, 252);
    private static final Color TEXT_MUTED    = new Color(148, 163, 184);
    private static final Color TABLE_ROW_ALT = new Color(22, 33, 52);
    private static final Color TABLE_SEL     = new Color(0, 82, 155);
    private static final Color STATUS_ACTIVE = new Color(34, 197, 94);
    private static final Color STATUS_INACTIVE = new Color(239, 68, 68);
    private static final Color BTN_DANGER    = new Color(185, 28, 28);
    private static final Color BTN_SECONDARY = new Color(55, 65, 81);
    private static final Color FIELD_BG      = new Color(15, 23, 42);
    private static final Color FIELD_BORDER  = new Color(71, 85, 105);

    // ===== BUS =====
    private NhanVienBUS nhanVienBUS;
    private ChiTietNhanVienBUS chiTietBUS;
    private PhongBanBUS phongBanBUS;
    private ChucVuBUS chucVuBUS;

    // ===== COMPONENTS =====
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> cboPhongBan;
    private JComboBox<String> cboChucVu;
    private JComboBox<String> cboTrangThai;
    private JLabel lblStatus;

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    public NhanVienPanel() {
        initBUS();
        initUI();
        loadTableData();
        
        // Auto refresh khi mở lại panel
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                refreshData();
            }
        });
    }

    private void initBUS() {
        nhanVienBUS = new NhanVienBUS();
        chiTietBUS = new ChiTietNhanVienBUS();
        phongBanBUS = new PhongBanBUS();
        chucVuBUS = new ChucVuBUS();
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
        JPanel toolbar = new JPanel(new BorderLayout(0, 4));
        toolbar.setBackground(BG_TOOLBAR);
        toolbar.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        // ── Hàng 1: Search + Filters ──
        JPanel rowFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        rowFilters.setOpaque(false);

        txtSearch = new JTextField(20);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setForeground(TEXT_WHITE);
        txtSearch.setBackground(FIELD_BG);
        txtSearch.setCaretColor(TEXT_WHITE);
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BORDER),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        txtSearch.setToolTipText("Tìm theo mã, họ tên...");
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterData(); }
            public void removeUpdate(DocumentEvent e) { filterData(); }
            public void changedUpdate(DocumentEvent e) { filterData(); }
        });
        rowFilters.add(txtSearch);

        cboPhongBan = createFilterCombo();
        cboPhongBan.addItem("Tất cả phòng ban");
        for (PhongBanDTO pb : phongBanBUS.getList()) {
            cboPhongBan.addItem(pb.getTenPB());
        }
        cboPhongBan.addActionListener(e -> filterData());
        rowFilters.add(cboPhongBan);

        cboChucVu = createFilterCombo();
        cboChucVu.addItem("Tất cả chức vụ");
        for (ChucVuDTO cv : chucVuBUS.getList()) {
            cboChucVu.addItem(cv.getTenCV());
        }
        cboChucVu.addActionListener(e -> filterData());
        rowFilters.add(cboChucVu);

        cboTrangThai = createFilterCombo();
        cboTrangThai.addItem("Tất cả");
        cboTrangThai.addItem("Đang làm");
        cboTrangThai.addItem("Đã nghỉ");
        cboTrangThai.addActionListener(e -> filterData());
        rowFilters.add(cboTrangThai);

        // ── Hàng 2: Nút hành động (CRUD trái | Excel phải) ──
        JPanel rowActions = new JPanel(new BorderLayout());
        rowActions.setOpaque(false);

        JPanel crudPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        crudPanel.setOpaque(false);

        JButton btnAdd = createStyledButton("+ Thêm", PRIMARY);
        btnAdd.addActionListener(e -> openAddDialog());
        crudPanel.add(btnAdd);

        JButton btnEdit = createStyledButton("Sửa", BTN_SECONDARY);
        btnEdit.addActionListener(e -> openEditDialog());
        crudPanel.add(btnEdit);

        JButton btnDelete = createStyledButton("Xóa", BTN_DANGER);
        btnDelete.addActionListener(e -> deleteSelected());
        crudPanel.add(btnDelete);

        JPanel excelPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        excelPanel.setOpaque(false);

        Dimension excelBtnSize = new Dimension(128, 34);
        JButton btnExport = createStyledButton("Xuất Excel", BTN_SECONDARY);
        btnExport.setPreferredSize(excelBtnSize);
        btnExport.setMinimumSize(excelBtnSize);
        btnExport.setToolTipText("Xuất danh sách nhân viên ra file .xls");
        btnExport.addActionListener(e -> exportExcel());
        excelPanel.add(btnExport);

        JButton btnImport = createStyledButton("Nhập Excel", BTN_SECONDARY);
        btnImport.setPreferredSize(excelBtnSize);
        btnImport.setMinimumSize(excelBtnSize);
        btnImport.setToolTipText("Nhập nhân viên từ file .xls");
        btnImport.addActionListener(e -> importExcel());
        excelPanel.add(btnImport);

        rowActions.add(crudPanel, BorderLayout.WEST);
        rowActions.add(excelPanel, BorderLayout.EAST);

        toolbar.add(rowFilters, BorderLayout.NORTH);
        toolbar.add(rowActions, BorderLayout.SOUTH);

        PermissionHelper.applyVisible(btnAdd, PermissionHelper.canAdd("nhanvien"));
        PermissionHelper.applyVisible(btnEdit, PermissionHelper.canEdit("nhanvien"));
        PermissionHelper.applyVisible(btnDelete, PermissionHelper.canDelete("nhanvien"));
        PermissionHelper.applyVisible(btnExport, PermissionHelper.canExcel("nhanvien"));
        PermissionHelper.applyVisible(btnImport, PermissionHelper.canExcel("nhanvien"));

        return toolbar;
    }

    private JComboBox<String> createFilterCombo() {
        JComboBox<String> cbo = new JComboBox<>();
        cbo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbo.setBackground(Color.WHITE);
        cbo.setForeground(Color.BLACK);
        cbo.setPreferredSize(new Dimension(150, 34));
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
        String[] columns = {"Mã NV", "Họ và tên", "Giới tính", "Ngày sinh", "SĐT", "Phòng ban", "Chức vụ", "Trạng thái"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho sửa trực tiếp
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

        // Header style (Cần Custom Renderer để trị Windows LAF đè nền trắng)
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

        // Alternate row + trạng thái renderer
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

                // Cột trạng thái — đổi màu chữ
                if (column == 7 && value != null) {
                    String status = value.toString();
                    if (status.contains("Đang")) {
                        c.setForeground(STATUS_ACTIVE);
                    } else if (status.contains("nghỉ") || status.contains("Nghỉ")) {
                        c.setForeground(STATUS_INACTIVE);
                    }
                }

                ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return c;
            }
        });

        // Double-click mở dialog sửa
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1
                        && PermissionHelper.canEdit("nhanvien")) {
                    openEditDialog();
                }
            }
        });

        // Đặt chiều rộng cột
        table.getColumnModel().getColumn(0).setPreferredWidth(80);   // Mã NV
        table.getColumnModel().getColumn(1).setPreferredWidth(180);  // Họ tên
        table.getColumnModel().getColumn(2).setPreferredWidth(70);   // Giới tính
        table.getColumnModel().getColumn(3).setPreferredWidth(100);  // Ngày sinh
        table.getColumnModel().getColumn(4).setPreferredWidth(110);  // SĐT
        table.getColumnModel().getColumn(5).setPreferredWidth(140);  // Phòng ban
        table.getColumnModel().getColumn(6).setPreferredWidth(120);  // Chức vụ
        table.getColumnModel().getColumn(7).setPreferredWidth(90);   // Trạng thái

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
        nhanVienBUS.loadData();
        chiTietBUS.loadData();
        phongBanBUS.loadData();
        chucVuBUS.loadData();

        // Refresh Combos (giữ lại selection)
        String selPB = (String) cboPhongBan.getSelectedItem();
        String selCV = (String) cboChucVu.getSelectedItem();

        // Tạm gỡ listener để không bị filterData gọi liên tục khi xoá/thêm
        ActionListener[] pbListeners = cboPhongBan.getActionListeners();
        for (ActionListener al : pbListeners) cboPhongBan.removeActionListener(al);
        
        ActionListener[] cvListeners = cboChucVu.getActionListeners();
        for (ActionListener al : cvListeners) cboChucVu.removeActionListener(al);

        cboPhongBan.removeAllItems();
        cboPhongBan.addItem("Tất cả phòng ban");
        for (PhongBanDTO pb : phongBanBUS.getList()) cboPhongBan.addItem(pb.getTenPB());

        cboChucVu.removeAllItems();
        cboChucVu.addItem("Tất cả chức vụ");
        for (ChucVuDTO cv : chucVuBUS.getList()) cboChucVu.addItem(cv.getTenCV());

        if (selPB != null) cboPhongBan.setSelectedItem(selPB);
        if (selCV != null) cboChucVu.setSelectedItem(selCV);

        // Gắn lại listener
        for (ActionListener al : pbListeners) cboPhongBan.addActionListener(al);
        for (ActionListener al : cvListeners) cboChucVu.addActionListener(al);

        loadTableData();
    }

    public void loadTableData() {
        filterData();
    }

    private void filterData() {
        String keyword = txtSearch.getText().trim();
        ArrayList<NhanVienDTO> list = nhanVienBUS.search(keyword);

        // Lọc theo phòng ban
        String selectedPB = (String) cboPhongBan.getSelectedItem();
        if (selectedPB != null && !selectedPB.startsWith("Tất cả")) {
            String maPB = getMaPBByTen(selectedPB);
            if (maPB != null) {
                list = filterByField(list, "phongban", maPB);
            }
        }

        // Lọc theo chức vụ
        String selectedCV = (String) cboChucVu.getSelectedItem();
        if (selectedCV != null && !selectedCV.startsWith("Tất cả")) {
            String maCV = getMaCVByTen(selectedCV);
            if (maCV != null) {
                list = filterByField(list, "chucvu", maCV);
            }
        }

        // Lọc theo trạng thái
        String selectedTT = (String) cboTrangThai.getSelectedItem();
        if (selectedTT != null && !selectedTT.equals("Tất cả")) {
            list = filterByField(list, "trangthai", selectedTT);
        }

        populateTable(list);
    }

    private ArrayList<NhanVienDTO> filterByField(ArrayList<NhanVienDTO> list, String field, String value) {
        ArrayList<NhanVienDTO> result = new ArrayList<>();
        for (NhanVienDTO nv : list) {
            switch (field) {
                case "phongban":
                    if (value.equalsIgnoreCase(nv.getMaPB())) result.add(nv);
                    break;
                case "chucvu":
                    if (value.equalsIgnoreCase(nv.getMaCV())) result.add(nv);
                    break;
                case "trangthai":
                    if (value.equalsIgnoreCase(nv.getTrangThai())) result.add(nv);
                    break;
            }
        }
        return result;
    }

    private void populateTable(ArrayList<NhanVienDTO> list) {
        tableModel.setRowCount(0);

        int activeCount = 0;
        int inactiveCount = 0;

        for (NhanVienDTO nv : list) {
            // Lookup thông tin chi tiết
            ChiTietNhanVienDTO ct = chiTietBUS.getById(nv.getMaNV());
            String sdt = (ct != null) ? ct.getSdt() : "";

            // Lookup tên phòng ban
            String tenPB = "";
            PhongBanDTO pb = phongBanBUS.getById(nv.getMaPB());
            if (pb != null) tenPB = pb.getTenPB();

            // Lookup tên chức vụ
            String tenCV = "";
            ChucVuDTO cv = chucVuBUS.getById(nv.getMaCV());
            if (cv != null) tenCV = cv.getTenCV();

            // Ngày sinh format
            String ngaySinhStr = "";
            if (nv.getNgaySinh() != null) {
                ngaySinhStr = sdf.format(nv.getNgaySinh());
            }

            // Đếm trạng thái
            if ("Đang làm".equalsIgnoreCase(nv.getTrangThai())) {
                activeCount++;
            } else {
                inactiveCount++;
            }

            tableModel.addRow(new Object[]{
                    nv.getMaNV(),
                    nv.getHo() + " " + nv.getTen(),
                    nv.getGioiTinh(),
                    ngaySinhStr,
                    sdt,
                    tenPB,
                    tenCV,
                    nv.getTrangThai()
            });
        }

        lblStatus.setText("Tổng: " + list.size() + " nhân viên  |  Đang làm: " + activeCount + "  |  Đã nghỉ: " + inactiveCount);
    }

    // =========================================================================
    //  HELPER LOOKUP
    // =========================================================================
    private String getMaPBByTen(String tenPB) {
        for (PhongBanDTO pb : phongBanBUS.getList()) {
            if (pb.getTenPB().equals(tenPB)) return pb.getMaPB();
        }
        return null;
    }

    private String getMaCVByTen(String tenCV) {
        for (ChucVuDTO cv : chucVuBUS.getList()) {
            if (cv.getTenCV().equals(tenCV)) return cv.getMaCV();
        }
        return null;
    }

    // =========================================================================
    //  ACTIONS
    // =========================================================================
    private void openAddDialog() {
        NhanVienDialog dialog = new NhanVienDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                "Thêm nhân viên mới",
                null, // No existing data = ADD mode
                nhanVienBUS, chiTietBUS, phongBanBUS, chucVuBUS
        );
        dialog.setVisible(true);

        // Sau khi đóng dialog → refresh
        if (dialog.isSaved()) {
            nhanVienBUS.loadData();
            chiTietBUS.loadData();
            loadTableData();
        }
    }

    private void openEditDialog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn nhân viên cần sửa!", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maNV = (String) tableModel.getValueAt(selectedRow, 0);
        NhanVienDTO nv = nhanVienBUS.getById(maNV);

        if (nv == null) return;

        NhanVienDialog dialog = new NhanVienDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                "Chỉnh sửa nhân viên",
                nv, // Existing data = EDIT mode
                nhanVienBUS, chiTietBUS, phongBanBUS, chucVuBUS
        );
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            nhanVienBUS.loadData();
            chiTietBUS.loadData();
            loadTableData();
        }
    }

    private void deleteSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn nhân viên cần xóa!", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maNV = (String) tableModel.getValueAt(selectedRow, 0);
        String hoTen = (String) tableModel.getValueAt(selectedRow, 1);

        int choice = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa nhân viên " + hoTen + " (" + maNV + ")?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                // Xóa chi tiết trước (FK constraint)
                chiTietBUS.delete(maNV);
                nhanVienBUS.delete(maNV);
                loadTableData();
                JOptionPane.showMessageDialog(this,
                        "Đã xóa nhân viên " + hoTen + " thành công!",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Lỗi khi xóa: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =========================================================================
    //  EXPORT / IMPORT EXCEL
    // =========================================================================
    private static final String[] NV_HEADERS = {
        "Mã NV", "Họ", "Tên", "Giới tính", "Ngày sinh (dd/MM/yyyy)",
        "Ngày bắt đầu (dd/MM/yyyy)", "Trạng thái", "Mã PB", "Mã CV"
    };

    private void exportExcel() {
        java.util.List<Object[]> rows = new java.util.ArrayList<>();
        for (NhanVienDTO nv : nhanVienBUS.getList()) {
            rows.add(new Object[]{
                nv.getMaNV(), nv.getHo(), nv.getTen(), nv.getGioiTinh(),
                nv.getNgaySinh(), nv.getNgayBatDau(),
                nv.getTrangThai(), nv.getMaPB(), nv.getMaCV()
            });
        }
        if (rows.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu nhân viên để xuất!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        HSSFWorkbook wb = ExcelHelper.createWorkbook("NhanVien", NV_HEADERS, rows);
        ExcelHelper.saveWithDialog(SwingUtilities.getWindowAncestor(this), wb, "NhanVien");
    }

    private void importExcel() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "File Excel cần header:\nMã NV | Họ | Tên | Giới tính | Ngày sinh | Ngày bắt đầu | Trạng thái | Mã PB | Mã CV\n\n" +
                "Dòng có Mã NV trùng sẽ được CẬP NHẬT; Mã NV mới sẽ THÊM.\n\n" +
                "Bạn muốn tải file mẫu trước?",
                "Nhập Excel — Nhân viên",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            HSSFWorkbook tmpl = ExcelHelper.createTemplate("NhanVien", NV_HEADERS);
            ExcelHelper.saveWithDialog(SwingUtilities.getWindowAncestor(this), tmpl, "Mau_NhanVien");
            return;
        }
        if (confirm == JOptionPane.CANCEL_OPTION) return;

        java.util.List<String[]> data = ExcelHelper.openAndRead(this, NV_HEADERS.length);
        if (data == null) return;

        java.util.List<String> errors = new java.util.ArrayList<>();
        int success = 0;

        for (int i = 0; i < data.size(); i++) {
            String[] row = data.get(i);
            int line = i + 2;
            try {
                String maNV = row[0].trim();
                String ho = row[1].trim();
                String ten = row[2].trim();
                String gioiTinh = row[3].trim();
                java.util.Date ngaySinh = ExcelHelper.parseDate(row[4]);
                java.util.Date ngayBD = ExcelHelper.parseDate(row[5]);
                String trangThai = row[6].trim();
                String maPB = row[7].trim();
                String maCV = row[8].trim();

                if (maNV.isEmpty()) { errors.add("Dòng " + line + ": Mã NV trống"); continue; }
                if (ho.isEmpty() || ten.isEmpty()) { errors.add("Dòng " + line + ": Họ/Tên trống"); continue; }
                if (!maPB.isEmpty() && phongBanBUS.getById(maPB) == null) { errors.add("Dòng " + line + ": Mã PB '" + maPB + "' không tồn tại"); continue; }
                if (!maCV.isEmpty() && chucVuBUS.getById(maCV) == null) { errors.add("Dòng " + line + ": Mã CV '" + maCV + "' không tồn tại"); continue; }

                NhanVienDTO dto = new NhanVienDTO(maNV, ho, ten, gioiTinh, ngaySinh, ngayBD, trangThai, maPB, maCV);

                NhanVienDTO existing = nhanVienBUS.getById(maNV);
                if (existing != null) {
                    nhanVienBUS.update(dto);
                } else {
                    nhanVienBUS.add(dto);
                }
                success++;
            } catch (Exception ex) {
                errors.add("Dòng " + line + ": " + ex.getMessage());
            }
        }

        refreshData();
        ExcelHelper.showImportErrors(this, errors, success);
    }
}
