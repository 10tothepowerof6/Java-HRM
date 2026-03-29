package com.hrm.gui;

import com.hrm.bus.NghiPhepBUS;
import com.hrm.bus.LoaiNghiPhepBUS;
import com.hrm.bus.NhanVienBUS;
import com.hrm.dto.LoaiNghiPhepDTO;
import com.hrm.dto.NghiPhepDTO;
import com.hrm.dto.NhanVienDTO;
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
import java.util.Calendar;

/**
 * Danh sách đơn nghỉ phép, lọc theo trạng thái và từ khóa; thêm/sửa qua dialog.
 * <p>
 * Khi duyệt, {@link com.hrm.bus.NghiPhepBUS} kiểm tra hạn mức ngày phép theo loại trong năm.
 * </p>
 */
public class NghiPhepPanel extends JPanel {

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
    private NghiPhepBUS nghiPhepBUS;
    private LoaiNghiPhepBUS loaiNghiPhepBUS;
    private NhanVienBUS nhanVienBUS;

    // ===== COMPONENTS =====
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> cboThang;
    private JComboBox<String> cboLoaiNP;
    private JComboBox<String> cboTrangThai;
    private JLabel lblStatus;

    private final SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");

    public NghiPhepPanel() {
        nghiPhepBUS = new NghiPhepBUS();
        loaiNghiPhepBUS = new LoaiNghiPhepBUS();
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
        JPanel toolbar = new JPanel(new BorderLayout(0, 4));
        toolbar.setBackground(BG_TOOLBAR);
        toolbar.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        // ── Hàng 1: Bộ lọc ──
        JPanel rowFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        rowFilters.setOpaque(false);

        txtSearch = new JTextField(15);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setForeground(TEXT_WHITE);
        txtSearch.setBackground(FIELD_BG);
        txtSearch.setCaretColor(TEXT_WHITE);
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BORDER),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        txtSearch.setToolTipText("Mã đơn, Mã NV, Tên NV...");
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterData(); }
            public void removeUpdate(DocumentEvent e) { filterData(); }
            public void changedUpdate(DocumentEvent e) { filterData(); }
        });
        rowFilters.add(txtSearch);

        cboThang = createFilterCombo();
        cboThang.addItem("Tất cả các tháng");
        for (int i = 1; i <= 12; i++) {
            cboThang.addItem("Tháng " + i);
        }
        cboThang.addActionListener(e -> filterData());
        rowFilters.add(cboThang);

        cboLoaiNP = createFilterCombo();
        buildLoaiNghiCombo();
        cboLoaiNP.addActionListener(e -> filterData());
        rowFilters.add(cboLoaiNP);

        cboTrangThai = createFilterCombo();
        cboTrangThai.addItem("Tất cả trạng thái");
        cboTrangThai.addItem("Chờ duyệt");
        cboTrangThai.addItem("Đã duyệt");
        cboTrangThai.addItem("Từ chối");
        cboTrangThai.addActionListener(e -> filterData());
        rowFilters.add(cboTrangThai);

        // ── Hàng 2: CRUD trái | Excel phải ──
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
        btnExport.setToolTipText("Xuất đơn nghỉ phép ra file .xls");
        btnExport.addActionListener(e -> exportExcel());
        excelPanel.add(btnExport);

        JButton btnImport = createStyledButton("Nhập Excel", BTN_SECONDARY);
        btnImport.setPreferredSize(excelBtnSize);
        btnImport.setMinimumSize(excelBtnSize);
        btnImport.setToolTipText("Nhập đơn nghỉ phép từ file .xls");
        btnImport.addActionListener(e -> importExcel());
        excelPanel.add(btnImport);

        rowActions.add(crudPanel, BorderLayout.WEST);
        rowActions.add(excelPanel, BorderLayout.EAST);

        toolbar.add(rowFilters, BorderLayout.NORTH);
        toolbar.add(rowActions, BorderLayout.SOUTH);

        PermissionHelper.applyVisible(btnAdd, PermissionHelper.canAdd("nghiphep"));
        PermissionHelper.applyVisible(btnEdit, PermissionHelper.canEdit("nghiphep"));
        PermissionHelper.applyVisible(btnDelete, PermissionHelper.canDelete("nghiphep"));
        PermissionHelper.applyVisible(btnExport, PermissionHelper.canExcel("nghiphep"));
        PermissionHelper.applyVisible(btnImport, PermissionHelper.canExcel("nghiphep"));

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

    private void buildLoaiNghiCombo() {
        cboLoaiNP.removeAllItems();
        cboLoaiNP.addItem("Tất cả loại phép");
        for (LoaiNghiPhepDTO loai : loaiNghiPhepBUS.getList()) {
            cboLoaiNP.addItem(loai.getTenLoai());
        }
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
        String[] columns = {"Mã Đơn", "Nhân viên", "Loại nghỉ", "Từ ngày", "Đến ngày", "Số ngày", "Trạng thái"};
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

                if (column == 6) { // Trạng thái
                    String status = (String) value;
                    if (status != null) {
                        if (status.equals("Đã duyệt")) c.setForeground(new Color(110, 231, 183)); // Xanh lá
                        else if (status.equals("Từ chối")) c.setForeground(new Color(252, 165, 165)); // Đỏ
                        else if (status.equals("Chờ duyệt")) c.setForeground(new Color(253, 230, 138)); // Vàng cam
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
                        && PermissionHelper.canEdit("nghiphep")) {
                    openEditDialog();
                }
            }
        });

        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(60);
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
        nghiPhepBUS.loadData();
        loaiNghiPhepBUS.loadData();
        nhanVienBUS.loadData();
        buildLoaiNghiCombo();
        loadTableData();
    }

    public void loadTableData() {
        filterData();
    }

    private void filterData() {
        if (cboLoaiNP.getItemCount() == 0) return; // Đang load

        String keyword = txtSearch.getText().trim().toLowerCase();
        String selThang = (String) cboThang.getSelectedItem();
        String selLoai = (String) cboLoaiNP.getSelectedItem();
        String selTrangThai = (String) cboTrangThai.getSelectedItem();
        
        ArrayList<NghiPhepDTO> filtered = new ArrayList<>();
        Calendar cal = Calendar.getInstance();

        for (NghiPhepDTO dto : nghiPhepBUS.getList()) {
            NhanVienDTO nv = nhanVienBUS.getById(dto.getMaNV());
            String hoten = nv != null ? (nv.getHo() + " " + nv.getTen()).toLowerCase() : "";
            
            boolean matchKeyword = keyword.isEmpty() ||
                                   dto.getMaNP().toLowerCase().contains(keyword) ||
                                   dto.getMaNV().toLowerCase().contains(keyword) ||
                                   hoten.contains(keyword);
            
            boolean matchStatus = selTrangThai == null || selTrangThai.equals("Tất cả trạng thái") || dto.getTrangThai().equalsIgnoreCase(selTrangThai);
            
            boolean matchMonth = true;
            if (selThang != null && !selThang.equals("Tất cả các tháng")) {
                if (dto.getTuNgay() != null) {
                    cal.setTime(dto.getTuNgay());
                    int month = cal.get(Calendar.MONTH) + 1;
                    String monthStr = "Tháng " + month;
                    matchMonth = monthStr.equals(selThang);
                } else {
                    matchMonth = false;
                }
            }

            boolean matchLoai = true;
            if (selLoai != null && !selLoai.equals("Tất cả loại phép")) {
                LoaiNghiPhepDTO loai = loaiNghiPhepBUS.getById(dto.getMaLoaiNP());
                if (loai != null) {
                    matchLoai = loai.getTenLoai().equals(selLoai);
                } else {
                    matchLoai = false;
                }
            }

            if (matchKeyword && matchStatus && matchMonth && matchLoai) {
                filtered.add(dto);
            }
        }
        populateTable(filtered);
    }

    private void populateTable(ArrayList<NghiPhepDTO> list) {
        tableModel.setRowCount(0);

        for (NghiPhepDTO np : list) {
            String tenNhanVien = np.getMaNV();
            NhanVienDTO nv = nhanVienBUS.getById(np.getMaNV());
            if (nv != null) {
                tenNhanVien = nv.getHo() + " " + nv.getTen() + " (" + nv.getMaNV() + ")";
            }

            String tenLoai = np.getMaLoaiNP();
            LoaiNghiPhepDTO loai = loaiNghiPhepBUS.getById(np.getMaLoaiNP());
            if (loai != null) tenLoai = loai.getTenLoai();

            String tuNgay = np.getTuNgay() != null ? sdfDate.format(np.getTuNgay()) : "";
            String denNgay = np.getDenNgay() != null ? sdfDate.format(np.getDenNgay()) : "";

            tableModel.addRow(new Object[]{
                    np.getMaNP(),
                    tenNhanVien,
                    tenLoai,
                    tuNgay,
                    denNgay,
                    np.getSoNgay(),
                    np.getTrangThai()
            });
        }
        lblStatus.setText("Tổng số đơn nghỉ phép theo bộ lọc: " + list.size());
    }

    // =========================================================================
    //  ACTIONS
    // =========================================================================
    private void openAddDialog() {
        NghiPhepDialog dialog = new NghiPhepDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                "Tạo đơn nghỉ phép",
                null, 
                nghiPhepBUS, nhanVienBUS, loaiNghiPhepBUS
        );
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            nghiPhepBUS.loadData();
            loadTableData();
        }
    }

    private void openEditDialog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn đơn nghỉ phép để xem/sửa!", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maNP = (String) tableModel.getValueAt(selectedRow, 0);
        NghiPhepDTO dto = nghiPhepBUS.getById(maNP);

        if (dto == null) return;

        NghiPhepDialog dialog = new NghiPhepDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                "Phê duyệt đơn nghỉ phép",
                dto, 
                nghiPhepBUS, nhanVienBUS, loaiNghiPhepBUS
        );
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            nghiPhepBUS.loadData();
            loadTableData();
        }
    }

    private void deleteSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn đơn nghỉ phép để xóa!", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maNP = (String) tableModel.getValueAt(selectedRow, 0);
        String thongTin = maNP + " - " + tableModel.getValueAt(selectedRow, 1);

        int choice = JOptionPane.showConfirmDialog(this,
                "Chắc chắn xóa đơn nghỉ phép: " + thongTin + "?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                if (nghiPhepBUS.delete(maNP)) {
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

    // =========================================================================
    //  EXPORT / IMPORT EXCEL
    // =========================================================================
    private static final String[] NP_HEADERS = {
        "Mã đơn", "Mã NV", "Mã loại NP", "Từ ngày (dd/MM/yyyy)", "Đến ngày (dd/MM/yyyy)", "Số ngày", "Lý do", "Trạng thái"
    };

    private void exportExcel() {
        java.util.List<Object[]> rows = new java.util.ArrayList<>();
        for (NghiPhepDTO np : nghiPhepBUS.getList()) {
            rows.add(new Object[]{
                np.getMaNP(), np.getMaNV(), np.getMaLoaiNP(),
                np.getTuNgay(), np.getDenNgay(),
                np.getSoNgay(), np.getLyDo(), np.getTrangThai()
            });
        }
        if (rows.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu nghỉ phép để xuất!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        HSSFWorkbook wb = ExcelHelper.createWorkbook("NghiPhep", NP_HEADERS, rows);
        ExcelHelper.saveWithDialog(SwingUtilities.getWindowAncestor(this), wb, "NghiPhep");
    }

    private void importExcel() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "File Excel cần header:\nMã đơn | Mã NV | Mã loại NP | Từ ngày | Đến ngày | Số ngày | Lý do | Trạng thái\n\n" +
                "Bạn muốn tải file mẫu trước?",
                "Nhập Excel — Nghỉ phép",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            HSSFWorkbook tmpl = ExcelHelper.createTemplate("NghiPhep", NP_HEADERS);
            ExcelHelper.saveWithDialog(SwingUtilities.getWindowAncestor(this), tmpl, "Mau_NghiPhep");
            return;
        }
        if (confirm == JOptionPane.CANCEL_OPTION) return;

        java.util.List<String[]> data = ExcelHelper.openAndRead(this, NP_HEADERS.length);
        if (data == null) return;

        java.util.List<String> errors = new java.util.ArrayList<>();
        int success = 0;

        for (int i = 0; i < data.size(); i++) {
            String[] row = data.get(i);
            int line = i + 2;
            try {
                String maNP = row[0].trim();
                String maNV = row[1].trim();
                String maLoaiNP = row[2].trim();
                java.util.Date tuNgay = ExcelHelper.parseDate(row[3]);
                java.util.Date denNgay = ExcelHelper.parseDate(row[4]);
                int soNgay = ExcelHelper.parseInt(row[5], 0);
                String lyDo = row[6].trim();
                String trangThai = row[7].trim();

                if (maNP.isEmpty()) { errors.add("Dòng " + line + ": Mã đơn trống"); continue; }
                if (maNV.isEmpty()) { errors.add("Dòng " + line + ": Mã NV trống"); continue; }
                if (tuNgay == null) { errors.add("Dòng " + line + ": Từ ngày không hợp lệ"); continue; }
                if (denNgay == null) { errors.add("Dòng " + line + ": Đến ngày không hợp lệ"); continue; }
                if (nhanVienBUS.getById(maNV) == null) { errors.add("Dòng " + line + ": Mã NV '" + maNV + "' không tồn tại"); continue; }

                NghiPhepDTO dto = new NghiPhepDTO(maNP, maNV, maLoaiNP, tuNgay, denNgay, soNgay, lyDo, trangThai);
                nghiPhepBUS.add(dto);
                success++;
            } catch (Exception ex) {
                errors.add("Dòng " + line + ": " + ex.getMessage());
            }
        }

        refreshData();
        ExcelHelper.showImportErrors(this, errors, success);
    }
}
