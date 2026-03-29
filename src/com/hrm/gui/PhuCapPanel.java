package com.hrm.gui;

import com.hrm.bus.LoaiPhuCapBUS;
import com.hrm.bus.NhanVienBUS;
import com.hrm.bus.PhuCapBUS;
import com.hrm.dto.NhanVienDTO;
import com.hrm.dto.PhuCapDTO;
import com.hrm.security.PermissionHelper;
import com.hrm.util.ExcelHelper;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Quản lý các khoản phụ cấp gắn nhân viên và loại phụ cấp; hỗ trợ lọc và Excel.
 * <p>
 * Dữ liệu tham gia cộng vào tổng phụ cấp khi {@link com.hrm.bus.BangLuongThangBUS#generateBangLuong(int, int)}.
 * </p>
 */
public class PhuCapPanel extends JPanel {

    // ===== MÀU SẮC =====
    private static final Color PRIMARY = new Color(0, 82, 155);
    private static final Color PRIMARY_DARK = new Color(0, 51, 102);
    private static final Color BG_CONTENT = new Color(30, 41, 59);
    private static final Color BG_TOOLBAR = new Color(22, 33, 52);
    private static final Color TEXT_WHITE = new Color(248, 250, 252);
    private static final Color TEXT_MUTED = new Color(148, 163, 184);
    private static final Color TABLE_ROW_ALT = new Color(22, 33, 52);
    private static final Color TABLE_SEL = new Color(0, 82, 155);
    private static final Color BTN_DANGER = new Color(185, 28, 28);
    private static final Color BTN_SECONDARY = new Color(55, 65, 81);
    private static final Color FIELD_BG = new Color(15, 23, 42);
    private static final Color FIELD_BORDER = new Color(71, 85, 105);

    // Loại phụ cấp hiển thị trong UI (hardcode để không phụ thuộc dữ liệu bảng LoaiPhuCap trong CSDL).
    private static final String[][] LOAI_PHU_CAPS = new String[][]{
            {"PC01", "Đi lại"},
            {"PC02", "Nhà ở"},
            {"PC03", "Xăng xe"},
            {"PC04", "Điện thoại"},
            {"PC05", "Trách nhiệm"},
            {"PC06", "Chuyên cần"},
            {"PC07", "Ca đêm"}
    };

    // ===== BUS =====
    private final PhuCapBUS phuCapBUS;
    private final NhanVienBUS nhanVienBUS;

    // ===== COMPONENTS =====
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JLabel lblStatus;

    private final SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public PhuCapPanel() {
        this.phuCapBUS = new PhuCapBUS();
        this.nhanVienBUS = new NhanVienBUS();

        initUI();
        refreshData();

        addComponentListener(new ComponentAdapter() {
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

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(BG_TOOLBAR);
        toolbar.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);

        txtSearch = new JTextField(22);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setForeground(TEXT_WHITE);
        txtSearch.setBackground(FIELD_BG);
        txtSearch.setCaretColor(TEXT_WHITE);
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BORDER),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        txtSearch.setToolTipText("Tìm theo mã PC / NV / loại phụ cấp...");
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                loadTableData();
            }

            public void removeUpdate(DocumentEvent e) {
                loadTableData();
            }

            public void changedUpdate(DocumentEvent e) {
                loadTableData();
            }
        });
        left.add(txtSearch);
        toolbar.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setOpaque(false);

        JButton btnAdd = createStyledButton("+ Thêm", PRIMARY);
        btnAdd.addActionListener(e -> openAddDialog());
        JButton btnEdit = createStyledButton("Sửa", BTN_SECONDARY);
        btnEdit.addActionListener(e -> openEditDialog());
        JButton btnDelete = createStyledButton("Xóa", BTN_DANGER);
        btnDelete.addActionListener(e -> deleteSelected());

        right.add(btnAdd);
        right.add(btnEdit);
        right.add(btnDelete);
        right.add(Box.createHorizontalStrut(10));

        JButton btnExport = createStyledButton("Xuất Excel", BTN_SECONDARY);
        btnExport.setPreferredSize(new Dimension(100, 34));
        btnExport.addActionListener(e -> exportExcel());
        right.add(btnExport);

        JButton btnImport = createStyledButton("Nhập Excel", BTN_SECONDARY);
        btnImport.setPreferredSize(new Dimension(100, 34));
        btnImport.addActionListener(e -> importExcel());
        right.add(btnImport);

        toolbar.add(right, BorderLayout.EAST);

        PermissionHelper.applyVisible(btnAdd, PermissionHelper.canAdd("phucap"));
        PermissionHelper.applyVisible(btnEdit, PermissionHelper.canEdit("phucap"));
        PermissionHelper.applyVisible(btnDelete, PermissionHelper.canDelete("phucap"));
        PermissionHelper.applyVisible(btnExport, PermissionHelper.canExcel("phucap"));
        PermissionHelper.applyVisible(btnImport, PermissionHelper.canExcel("phucap"));

        return toolbar;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(TEXT_WHITE);
        btn.setBackground(bgColor);
        btn.setPreferredSize(new Dimension(90, 34));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);

        Color hoverColor = bgColor.brighter();
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bgColor);
            }
        });
        return btn;
    }

    private JScrollPane createTablePanel() {
        String[] columns = {"Mã PC", "Nhân viên", "Loại phụ cấp", "Số tiền", "Ngày áp dụng", "Ngày kết thúc"};

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

                ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                if (column == 3) {
                    c.setForeground(new Color(96, 165, 250));
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.RIGHT);
                } else {
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.LEFT);
                }
                return c;
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1
                        && PermissionHelper.canEdit("phucap")) {
                    openEditDialog();
                }
            }
        });

        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(120);
        table.getColumnModel().getColumn(5).setPreferredWidth(130);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BG_CONTENT);
        return scrollPane;
    }

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

    private void refreshData() {
        phuCapBUS.loadData();
        nhanVienBUS.loadData();
        loadTableData();
    }

    private void loadTableData() {
        String keyword = txtSearch == null ? "" : txtSearch.getText().trim().toLowerCase();

        ArrayList<PhuCapDTO> list = phuCapBUS.getList();
        ArrayList<PhuCapDTO> filtered = new ArrayList<>();

        for (PhuCapDTO dto : list) {
            boolean matchKeyword;
            if (keyword.isEmpty()) {
                matchKeyword = true;
            } else {
                matchKeyword = matchPhuCap(dto, keyword);
            }
            if (matchKeyword) {
                filtered.add(dto);
            }
        }

        tableModel.setRowCount(0);
        for (PhuCapDTO dto : filtered) {
            String tenNhanVien = getNhanVienDisplay(dto.getMaNV());
            String tenLoaiPC = getLoaiPCDisplay(dto.getMaLoaiPC());
            String soTienText = dto.getSoTien() != null ? currencyFormat.format(dto.getSoTien()) : "0 ₫";
            String ngayApDung = dto.getNgayApDung() != null ? sdfDate.format(dto.getNgayApDung()) : "";
            String ngayKetThuc = dto.getNgayKetThuc() != null ? sdfDate.format(dto.getNgayKetThuc()) : "Vô thời hạn";

            tableModel.addRow(new Object[]{
                    dto.getMaPC(),
                    tenNhanVien,
                    tenLoaiPC,
                    soTienText,
                    ngayApDung,
                    ngayKetThuc
            });
        }

        lblStatus.setText("Tổng số phụ cấp theo bộ lọc: " + filtered.size());
    }

    private boolean matchPhuCap(PhuCapDTO dto, String keyword) {
        String maPC = dto.getMaPC() == null ? "" : dto.getMaPC().toLowerCase();
        String maNV = dto.getMaNV() == null ? "" : dto.getMaNV().toLowerCase();
        String maLoaiPC = dto.getMaLoaiPC() == null ? "" : dto.getMaLoaiPC().toLowerCase();

        NhanVienDTO nv = nhanVienBUS.getById(dto.getMaNV());
        String hoTenNV = nv != null
                ? (nv.getHo() + " " + nv.getTen()).toLowerCase()
                : "";

        String tenLoaiPC = getTenLoaiPC(dto.getMaLoaiPC()).toLowerCase();

        return maPC.contains(keyword)
                || maNV.contains(keyword)
                || hoTenNV.contains(keyword)
                || maLoaiPC.contains(keyword)
                || tenLoaiPC.contains(keyword);
    }

    private String getNhanVienDisplay(String maNV) {
        if (maNV == null) return "";
        NhanVienDTO nv = nhanVienBUS.getById(maNV);
        if (nv != null) {
            return nv.getMaNV() + " - " + nv.getHo() + " " + nv.getTen();
        }
        return maNV;
    }

    private String getLoaiPCDisplay(String maLoaiPC) {
        if (maLoaiPC == null) return "";
        String ten = getTenLoaiPC(maLoaiPC);
        if (ten != null && !ten.trim().isEmpty()) {
            return maLoaiPC + " - " + ten;
        }
        return maLoaiPC; // fallback nếu mã không nằm trong hardcode list
    }

    private String getTenLoaiPC(String maLoaiPC) {
        if (maLoaiPC == null) return "";
        for (String[] type : LOAI_PHU_CAPS) {
            if (maLoaiPC.equalsIgnoreCase(type[0])) {
                return type[1];
            }
        }
        return "";
    }

    private void openAddDialog() {
        PhuCapDialog dialog = new PhuCapDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                "Thêm mới Phụ cấp",
                null,
                phuCapBUS,
                nhanVienBUS
        );
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            refreshData();
        }
    }

    private void openEditDialog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn phụ cấp để xem/sửa!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maPC = (String) tableModel.getValueAt(selectedRow, 0);
        PhuCapDTO dto = phuCapBUS.getById(maPC);
        if (dto == null) return;

        PhuCapDialog dialog = new PhuCapDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                "Chỉnh sửa Phụ cấp",
                dto,
                phuCapBUS,
                nhanVienBUS
        );
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            refreshData();
        }
    }

    private void deleteSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn phụ cấp để xóa!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maPC = (String) tableModel.getValueAt(selectedRow, 0);
        PhuCapDTO dto = phuCapBUS.getById(maPC);
        if (dto == null) return;

        // Delete guard: chặn xóa nếu đã/đang ảnh hưởng tới bảng lương tháng
        if (!phuCapBUS.canDelete(dto)) {
            String reason = phuCapBUS.getDeleteBlockReason(dto);
            JOptionPane.showMessageDialog(this,
                    reason == null ? "Không thể xóa phụ cấp này." : reason,
                    "Chặn xóa", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa phụ cấp mã " + maPC + "?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            phuCapBUS.delete(maPC);
            refreshData();
        }
    }

    // =========================================================================
    //  EXPORT / IMPORT EXCEL
    // =========================================================================
    private static final String[] PC_HEADERS = {
        "Mã PC", "Mã NV", "Mã loại PC", "Số tiền", "Ngày áp dụng (dd/MM/yyyy)", "Ngày kết thúc (dd/MM/yyyy)"
    };

    private void exportExcel() {
        java.util.List<Object[]> rows = new java.util.ArrayList<>();
        for (PhuCapDTO pc : phuCapBUS.getList()) {
            rows.add(new Object[]{
                pc.getMaPC(), pc.getMaNV(), pc.getMaLoaiPC(),
                pc.getSoTien(), pc.getNgayApDung(), pc.getNgayKetThuc()
            });
        }
        if (rows.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu phụ cấp để xuất!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        HSSFWorkbook wb = ExcelHelper.createWorkbook("PhuCap", PC_HEADERS, rows);
        ExcelHelper.saveWithDialog(SwingUtilities.getWindowAncestor(this), wb, "PhuCap");
    }

    private void importExcel() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "File Excel cần header:\nMã PC | Mã NV | Mã loại PC | Số tiền | Ngày áp dụng | Ngày kết thúc\n\n" +
                "Bạn muốn tải file mẫu trước?",
                "Nhập Excel — Phụ cấp",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            HSSFWorkbook tmpl = ExcelHelper.createTemplate("PhuCap", PC_HEADERS);
            ExcelHelper.saveWithDialog(SwingUtilities.getWindowAncestor(this), tmpl, "Mau_PhuCap");
            return;
        }
        if (confirm == JOptionPane.CANCEL_OPTION) return;

        java.util.List<String[]> data = ExcelHelper.openAndRead(this, PC_HEADERS.length);
        if (data == null) return;

        LoaiPhuCapBUS lpcBus = new LoaiPhuCapBUS();
        java.util.List<String> errors = new java.util.ArrayList<>();
        int success = 0;

        for (int i = 0; i < data.size(); i++) {
            String[] row = data.get(i);
            int line = i + 2;
            try {
                String maPC = row[0].trim();
                String maNV = row[1].trim();
                String maLoaiPC = row[2].trim();
                java.math.BigDecimal soTien = ExcelHelper.parseMoney(row[3]);
                java.util.Date ngayAD = ExcelHelper.parseDate(row[4]);
                java.util.Date ngayKT = ExcelHelper.parseDate(row[5]);

                if (maPC.isEmpty()) { errors.add("Dòng " + line + ": Mã PC trống"); continue; }
                if (maNV.isEmpty()) { errors.add("Dòng " + line + ": Mã NV trống"); continue; }
                if (soTien == null) { errors.add("Dòng " + line + ": Số tiền không hợp lệ"); continue; }
                if (nhanVienBUS.getById(maNV) == null) { errors.add("Dòng " + line + ": Mã NV '" + maNV + "' không tồn tại"); continue; }
                if (!maLoaiPC.isEmpty() && lpcBus.getById(maLoaiPC) == null) { errors.add("Dòng " + line + ": Mã loại PC '" + maLoaiPC + "' không tồn tại"); continue; }

                PhuCapDTO dto = new PhuCapDTO(maPC, maNV, maLoaiPC, soTien, ngayAD, ngayKT);
                phuCapBUS.add(dto);
                success++;
            } catch (Exception ex) {
                errors.add("Dòng " + line + ": " + ex.getMessage());
            }
        }

        refreshData();
        ExcelHelper.showImportErrors(this, errors, success);
    }
}

