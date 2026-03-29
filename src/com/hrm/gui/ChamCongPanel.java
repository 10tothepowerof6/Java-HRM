package com.hrm.gui;

import com.hrm.bus.BangChamCongBUS;
import com.hrm.bus.NhanVienBUS;
import com.hrm.dto.BangChamCongDTO;
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
import java.util.List;

/**
 * Bảng chấm công theo ngày: nhân viên, giờ vào/ra và trạng thái (Đi làm, Phép…).
 * <p>
 * Số ngày công "Đi làm" trong tháng được đếm khi tính lương hàng loạt.
 * </p>
 */
public class ChamCongPanel extends JPanel {

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
    private BangChamCongBUS chamCongBUS;
    private NhanVienBUS nhanVienBUS;

    // ===== COMPONENTS =====
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> cboThang;
    private JComboBox<String> cboTrangThai;
    private JLabel lblStatus;

    private final SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");
    private final SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");

    public ChamCongPanel() {
        chamCongBUS = new BangChamCongBUS();
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
        txtSearch.setToolTipText("Mã CC, Mã NV, Tên NV...");
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterData(); }
            public void removeUpdate(DocumentEvent e) { filterData(); }
            public void changedUpdate(DocumentEvent e) { filterData(); }
        });
        leftPanel.add(txtSearch);

        // Combo Tháng
        cboThang = createFilterCombo();
        cboThang.addItem("Tất cả các tháng");
        for (int i = 1; i <= 12; i++) {
            cboThang.addItem("Tháng " + i);
        }
        cboThang.addActionListener(e -> filterData());
        leftPanel.add(cboThang);

        // Combo Trạng thái
        cboTrangThai = createFilterCombo();
        cboTrangThai.addItem("Tất cả trạng thái");
        cboTrangThai.addItem("Đi làm");
        cboTrangThai.addItem("Ốm");
        cboTrangThai.addItem("Phép");
        cboTrangThai.addItem("Không lương");
        cboTrangThai.addItem("Thai sản");
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

        rightPanel.add(Box.createHorizontalStrut(10));

        JButton btnExport = createStyledButton("Xuất Excel", BTN_SECONDARY);
        btnExport.setPreferredSize(new Dimension(100, 34));
        btnExport.addActionListener(e -> exportExcel());
        rightPanel.add(btnExport);

        JButton btnImport = createStyledButton("Nhập Excel", BTN_SECONDARY);
        btnImport.setPreferredSize(new Dimension(100, 34));
        btnImport.addActionListener(e -> importExcel());
        rightPanel.add(btnImport);

        toolbar.add(rightPanel, BorderLayout.EAST);

        PermissionHelper.applyVisible(btnAdd, PermissionHelper.canAdd("chamcong"));
        PermissionHelper.applyVisible(btnEdit, PermissionHelper.canEdit("chamcong"));
        PermissionHelper.applyVisible(btnDelete, PermissionHelper.canDelete("chamcong"));
        PermissionHelper.applyVisible(btnExport, PermissionHelper.canExcel("chamcong"));
        PermissionHelper.applyVisible(btnImport, PermissionHelper.canExcel("chamcong"));

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
        String[] columns = {"Mã CC", "Nhân viên", "Ngày làm việc", "Giờ vào", "Giờ ra", "Trạng thái"};
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

                if (column == 5) { // Trạng thái
                    String status = (String) value;
                    if (status != null) {
                        if (status.equals("Đi làm")) c.setForeground(new Color(110, 231, 183)); // Xanh lá
                        else if (status.equals("Ốm")) c.setForeground(new Color(252, 165, 165)); // Đỏ nhạt
                        else if (status.equals("Phép")) c.setForeground(new Color(253, 230, 138)); // Vàng
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
                        && PermissionHelper.canEdit("chamcong")) {
                    openEditDialog();
                }
            }
        });

        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(220);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(120);

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
        chamCongBUS.loadData();
        nhanVienBUS.loadData();
        loadTableData();
    }

    public void loadTableData() {
        filterData();
    }

    private void filterData() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        String selStatus = (String) cboTrangThai.getSelectedItem();
        String selMonth = (String) cboThang.getSelectedItem();
        
        ArrayList<BangChamCongDTO> filtered = new ArrayList<>();
        Calendar cal = Calendar.getInstance();

        for (BangChamCongDTO dto : chamCongBUS.getList()) {
            NhanVienDTO nv = nhanVienBUS.getById(dto.getMaNV());
            String hoten = nv != null ? (nv.getHo() + " " + nv.getTen()).toLowerCase() : "";
            
            boolean matchKeyword = keyword.isEmpty() ||
                                   dto.getMaChamCong().toLowerCase().contains(keyword) ||
                                   dto.getMaNV().toLowerCase().contains(keyword) ||
                                   hoten.contains(keyword);
            
            boolean matchStatus = selStatus == null || selStatus.equals("Tất cả trạng thái") || dto.getTrangThai().equalsIgnoreCase(selStatus);
            
            boolean matchMonth = true;
            if (selMonth != null && !selMonth.equals("Tất cả các tháng")) {
                if (dto.getNgayLamViec() != null) {
                    cal.setTime(dto.getNgayLamViec());
                    int month = cal.get(Calendar.MONTH) + 1;
                    String monthStr = "Tháng " + month;
                    matchMonth = monthStr.equals(selMonth);
                } else {
                    matchMonth = false;
                }
            }

            if (matchKeyword && matchStatus && matchMonth) {
                filtered.add(dto);
            }
        }
        populateTable(filtered);
    }

    private void populateTable(ArrayList<BangChamCongDTO> list) {
        tableModel.setRowCount(0);

        for (BangChamCongDTO cc : list) {
            String tenNhanVien = cc.getMaNV();
            NhanVienDTO nv = nhanVienBUS.getById(cc.getMaNV());
            if (nv != null) {
                tenNhanVien = nv.getHo() + " " + nv.getTen() + " (" + nv.getMaNV() + ")";
            }

            String ngayLV = cc.getNgayLamViec() != null ? sdfDate.format(cc.getNgayLamViec()) : "";
            String gVao = cc.getGioVao() != null ? sdfTime.format(cc.getGioVao()) : "--:--:--";
            String gRa = cc.getGioRa() != null ? sdfTime.format(cc.getGioRa()) : "--:--:--";

            tableModel.addRow(new Object[]{
                    cc.getMaChamCong(),
                    tenNhanVien,
                    ngayLV,
                    gVao,
                    gRa,
                    cc.getTrangThai()
            });
        }
        lblStatus.setText("Tổng số lượt chấm công theo bộ lọc: " + list.size());
    }

    // =========================================================================
    //  ACTIONS
    // =========================================================================
    private void openAddDialog() {
        ChamCongDialog dialog = new ChamCongDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                "Thêm lượt chấm công",
                null, 
                chamCongBUS, nhanVienBUS
        );
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            chamCongBUS.loadData();
            loadTableData();
        }
    }

    private void openEditDialog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn dòng chấm công để sửa!", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maCC = (String) tableModel.getValueAt(selectedRow, 0);
        BangChamCongDTO dto = chamCongBUS.getById(maCC);

        if (dto == null) return;

        ChamCongDialog dialog = new ChamCongDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                "Sửa thông tin chấm công",
                dto, 
                chamCongBUS, nhanVienBUS
        );
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            chamCongBUS.loadData();
            loadTableData();
        }
    }

    private void deleteSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn dòng chấm công để xóa!", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maCC = (String) tableModel.getValueAt(selectedRow, 0);
        String thongTin = maCC + " - " + tableModel.getValueAt(selectedRow, 1);

        int choice = JOptionPane.showConfirmDialog(this,
                "Chắc chắn xóa lượt chấm công: " + thongTin + "?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                if (chamCongBUS.delete(maCC)) {
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
    private static final String[] CC_HEADERS = {
        "Mã CC", "Mã NV", "Ngày làm việc (dd/MM/yyyy)", "Giờ vào (HH:mm)", "Giờ ra (HH:mm)", "Trạng thái"
    };

    private void exportExcel() {
        List<Object[]> rows = new ArrayList<>();
        for (BangChamCongDTO cc : chamCongBUS.getList()) {
            rows.add(new Object[]{
                cc.getMaChamCong(),
                cc.getMaNV(),
                cc.getNgayLamViec(),
                cc.getGioVao() != null ? ExcelHelper.formatTime(cc.getGioVao()) : "",
                cc.getGioRa() != null ? ExcelHelper.formatTime(cc.getGioRa()) : "",
                cc.getTrangThai()
            });
        }

        if (rows.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu chấm công để xuất!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        HSSFWorkbook wb = ExcelHelper.createWorkbook("ChamCong", CC_HEADERS, rows);
        ExcelHelper.saveWithDialog(SwingUtilities.getWindowAncestor(this), wb, "ChamCong");
    }

    private void importExcel() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "File Excel cần có header:\nMã CC | Mã NV | Ngày làm việc (dd/MM/yyyy) | Giờ vào (HH:mm) | Giờ ra (HH:mm) | Trạng thái\n\n" +
                "Bạn muốn tải file mẫu trước?",
                "Nhập Excel — Chấm công",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            HSSFWorkbook tmpl = ExcelHelper.createTemplate("ChamCong", CC_HEADERS);
            ExcelHelper.saveWithDialog(SwingUtilities.getWindowAncestor(this), tmpl, "Mau_ChamCong");
            return;
        }
        if (confirm == JOptionPane.CANCEL_OPTION) return;

        List<String[]> data = ExcelHelper.openAndRead(this, CC_HEADERS.length);
        if (data == null) return;

        List<String> errors = new ArrayList<>();
        int success = 0;

        for (int i = 0; i < data.size(); i++) {
            String[] row = data.get(i);
            int line = i + 2;
            try {
                String maCC = row[0].trim();
                String maNV = row[1].trim();
                java.util.Date ngay = ExcelHelper.parseDate(row[2]);
                java.sql.Time gioVao = ExcelHelper.parseTime(row[3]);
                java.sql.Time gioRa = ExcelHelper.parseTime(row[4]);
                String trangThai = row[5].trim();

                if (maCC.isEmpty()) { errors.add("Dòng " + line + ": Mã CC trống"); continue; }
                if (maNV.isEmpty()) { errors.add("Dòng " + line + ": Mã NV trống"); continue; }
                if (ngay == null) { errors.add("Dòng " + line + ": Ngày làm việc không hợp lệ"); continue; }
                if (nhanVienBUS.getById(maNV) == null) { errors.add("Dòng " + line + ": Mã NV '" + maNV + "' không tồn tại"); continue; }

                BangChamCongDTO dto = new BangChamCongDTO(maCC, maNV, ngay, gioVao, gioRa, trangThai);
                chamCongBUS.add(dto);
                success++;
            } catch (Exception ex) {
                errors.add("Dòng " + line + ": " + ex.getMessage());
            }
        }

        chamCongBUS.loadData();
        loadTableData();
        ExcelHelper.showImportErrors(this, errors, success);
    }
}
