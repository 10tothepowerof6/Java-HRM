package com.hrm.gui;

import com.hrm.bus.DeAnBUS;
import com.hrm.bus.NhanVienBUS;
import com.hrm.bus.PhanCongDeAnBUS;
import com.hrm.bus.PhongBanBUS;
import com.hrm.dto.DeAnDTO;
import com.hrm.dto.NhanVienDTO;
import com.hrm.dto.PhanCongDeAnDTO;
import com.hrm.dto.PhongBanDTO;
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
 * Đề án (master) và phân công nhân viên lên từng đề án (detail), đồng bộ hai lưới.
 * <p>
 * Phụ cấp đề án tham gia vào luồng tính lương tổng hợp; quyền thao tác theo module {@code dean}.
 * </p>
 */
public class DeAnPanel extends JPanel {

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

    private final DeAnBUS deAnBUS;
    private final PhanCongDeAnBUS phanCongBUS;
    private final PhongBanBUS phongBanBUS;
    private final NhanVienBUS nhanVienBUS;

    private JTable tblDeAn;
    private JTable tblPhanCong;
    private DefaultTableModel modelDeAn;
    private DefaultTableModel modelPhanCong;
    private JTextField txtSearch;
    private JLabel lblStatus;

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public DeAnPanel() {
        deAnBUS = new DeAnBUS();
        phanCongBUS = new PhanCongDeAnBUS();
        phongBanBUS = new PhongBanBUS();
        nhanVienBUS = new NhanVienBUS();

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
        add(createMainContent(), BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(0, 4));
        toolbar.setBackground(BG_TOOLBAR);
        toolbar.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        // ── Hàng 1: Search ──
        JPanel rowSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        rowSearch.setOpaque(false);

        txtSearch = new JTextField(22);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setForeground(TEXT_WHITE);
        txtSearch.setBackground(FIELD_BG);
        txtSearch.setCaretColor(TEXT_WHITE);
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BORDER),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        txtSearch.setToolTipText("Tìm theo mã/tên đề án...");
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { loadDeAnTable(); }
            public void removeUpdate(DocumentEvent e) { loadDeAnTable(); }
            public void changedUpdate(DocumentEvent e) { loadDeAnTable(); }
        });
        rowSearch.add(txtSearch);

        // ── Hàng 2: CRUD trái | Excel phải ──
        JPanel rowActions = new JPanel(new BorderLayout());
        rowActions.setOpaque(false);

        JPanel crudPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        crudPanel.setOpaque(false);

        JButton btnAdd = createStyledButton("+ Đề án", PRIMARY);
        btnAdd.addActionListener(e -> openAddDeAnDialog());
        crudPanel.add(btnAdd);
        JButton btnEdit = createStyledButton("Sửa ĐA", BTN_SECONDARY);
        btnEdit.addActionListener(e -> openEditDeAnDialog());
        crudPanel.add(btnEdit);
        JButton btnDelete = createStyledButton("Xóa ĐA", BTN_DANGER);
        btnDelete.addActionListener(e -> deleteSelectedDeAn());
        crudPanel.add(btnDelete);

        crudPanel.add(Box.createHorizontalStrut(10));

        JButton btnAddAssign = createStyledButton("+ Phân công", PRIMARY);
        btnAddAssign.addActionListener(e -> openAddAssignDialog());
        crudPanel.add(btnAddAssign);
        JButton btnEditAssign = createStyledButton("Sửa PC", BTN_SECONDARY);
        btnEditAssign.addActionListener(e -> openEditAssignDialog());
        crudPanel.add(btnEditAssign);
        JButton btnDeleteAssign = createStyledButton("Xóa PC", BTN_DANGER);
        btnDeleteAssign.addActionListener(e -> deleteSelectedAssign());
        crudPanel.add(btnDeleteAssign);

        JPanel excelPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        excelPanel.setOpaque(false);

        JButton btnExport = createStyledButton("Xuất Excel", BTN_SECONDARY);
        btnExport.setToolTipText("Xuất đề án & phân công ra file .xls");
        btnExport.addActionListener(e -> exportExcel());
        excelPanel.add(btnExport);

        JButton btnImport = createStyledButton("Nhập Excel", BTN_SECONDARY);
        btnImport.setToolTipText("Nhập từ file .xls");
        btnImport.addActionListener(e -> importExcel());
        excelPanel.add(btnImport);

        rowActions.add(crudPanel, BorderLayout.WEST);
        rowActions.add(excelPanel, BorderLayout.EAST);

        toolbar.add(rowSearch, BorderLayout.NORTH);
        toolbar.add(rowActions, BorderLayout.SOUTH);

        PermissionHelper.applyVisible(btnAdd, PermissionHelper.canAdd("dean"));
        PermissionHelper.applyVisible(btnEdit, PermissionHelper.canEdit("dean"));
        PermissionHelper.applyVisible(btnDelete, PermissionHelper.canDelete("dean"));
        PermissionHelper.applyVisible(btnAddAssign, PermissionHelper.canAdd("dean"));
        PermissionHelper.applyVisible(btnEditAssign, PermissionHelper.canEdit("dean"));
        PermissionHelper.applyVisible(btnDeleteAssign, PermissionHelper.canDelete("dean"));
        PermissionHelper.applyVisible(btnExport, PermissionHelper.canExcel("dean"));
        PermissionHelper.applyVisible(btnImport, PermissionHelper.canExcel("dean"));

        return toolbar;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(TEXT_WHITE);
        btn.setBackground(bgColor);
        btn.setPreferredSize(new Dimension(100, 34));
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

    private JSplitPane createMainContent() {
        modelDeAn = new DefaultTableModel(
                new String[]{"Mã ĐA", "Tên đề án", "Phòng ban", "Ngày bắt đầu", "Ngày kết thúc", "Vốn", "Trạng thái"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblDeAn = createStyledTable(modelDeAn);
        tblDeAn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (tblDeAn.getSelectedRow() != -1) {
                    loadPhanCongTable();
                }
                if (e.getClickCount() == 2 && tblDeAn.getSelectedRow() != -1
                        && PermissionHelper.canEdit("dean")) {
                    openEditDeAnDialog();
                }
            }
        });

        modelPhanCong = new DefaultTableModel(
                new String[]{"Mã NV", "Họ tên", "Ngày bắt đầu", "Ngày kết thúc", "Phụ cấp đề án"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblPhanCong = createStyledTable(modelPhanCong);
        tblPhanCong.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tblPhanCong.getSelectedRow() != -1
                        && PermissionHelper.canEdit("dean")) {
                    openEditAssignDialog();
                }
            }
        });

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(BG_CONTENT);
        JLabel lblTop = createSectionLabel("DANH SÁCH ĐỀ ÁN");
        top.add(lblTop, BorderLayout.NORTH);
        top.add(createStyledScrollPane(tblDeAn), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(BG_CONTENT);
        JLabel lblBottom = createSectionLabel("PHÂN CÔNG CỦA ĐỀ ÁN ĐANG CHỌN");
        bottom.add(lblBottom, BorderLayout.NORTH);
        bottom.add(createStyledScrollPane(tblPhanCong), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, bottom);
        split.setResizeWeight(0.58);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setDividerSize(6);
        split.setBackground(BG_CONTENT);
        return split;
    }

    private JLabel createSectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_MUTED);
        lbl.setBorder(BorderFactory.createEmptyBorder(8, 4, 8, 4));
        return lbl;
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setForeground(TEXT_WHITE);
        table.setBackground(BG_CONTENT);
        table.setSelectionBackground(TABLE_SEL);
        table.setSelectionForeground(TEXT_WHITE);
        table.setRowHeight(34);
        table.setGridColor(new Color(40, 50, 70));
        table.setShowGrid(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 36));
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
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
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (isSelected) {
                    c.setBackground(TABLE_SEL);
                    c.setForeground(TEXT_WHITE);
                } else {
                    c.setBackground(row % 2 == 0 ? BG_CONTENT : TABLE_ROW_ALT);
                    c.setForeground(TEXT_WHITE);
                }

                ((JLabel) c).setHorizontalAlignment(SwingConstants.LEFT);
                if (column == 5) {
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.RIGHT);
                    c.setForeground(new Color(96, 165, 250));
                }
                if (column == 6) {
                    String status = String.valueOf(value);
                    if ("Đang thực hiện".equals(status)) c.setForeground(new Color(253, 230, 138));
                    else if ("Hủy".equals(status)) c.setForeground(new Color(252, 165, 165));
                    else if ("Hoàn thành".equals(status)) c.setForeground(new Color(110, 231, 183));
                    else c.setForeground(new Color(209, 213, 219));
                }
                ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return c;
            }
        });
        return table;
    }

    private JScrollPane createStyledScrollPane(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BG_CONTENT);
        scrollPane.getViewport().setOpaque(true);
        scrollPane.setBackground(BG_CONTENT);
        scrollPane.setOpaque(true);
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

    public void refreshData() {
        deAnBUS.loadData();
        phanCongBUS.loadData();
        phongBanBUS.loadData();
        nhanVienBUS.loadData();
        loadDeAnTable();
        loadPhanCongTable();
    }

    private void loadDeAnTable() {
        String keyword = txtSearch == null ? "" : txtSearch.getText().trim();
        ArrayList<DeAnDTO> list = deAnBUS.search(keyword);
        modelDeAn.setRowCount(0);

        int dangThucHien = 0;
        for (DeAnDTO da : list) {
            String tenPB = getTenPhongBan(da.getMaPB());
            String start = da.getNgayBatDau() != null ? sdf.format(da.getNgayBatDau()) : "";
            String end = da.getNgayKetThuc() != null ? sdf.format(da.getNgayKetThuc()) : "";
            String von = da.getVonDeAn() != null ? currencyFormat.format(da.getVonDeAn()) : currencyFormat.format(0);
            if ("Đang thực hiện".equalsIgnoreCase(da.getTrangThai())) {
                dangThucHien++;
            }
            modelDeAn.addRow(new Object[]{
                    da.getMaDA(),
                    da.getTenDA(),
                    tenPB,
                    start,
                    end,
                    von,
                    da.getTrangThai()
            });
        }
        lblStatus.setText("Đề án: " + list.size() + "  |  Đang thực hiện: " + dangThucHien);
    }

    private void loadPhanCongTable() {
        modelPhanCong.setRowCount(0);
        String maDA = getSelectedMaDA();
        if (maDA == null) {
            return;
        }
        ArrayList<PhanCongDeAnDTO> list = phanCongBUS.getByMaDA(maDA);
        for (PhanCongDeAnDTO pc : list) {
            String tenNV = getHoTenNhanVien(pc.getMaNV());
            String start = pc.getNgayBatDau() != null ? sdf.format(pc.getNgayBatDau()) : "";
            String end = pc.getNgayKetThuc() != null ? sdf.format(pc.getNgayKetThuc()) : "";
            String phuCap = pc.getPhuCapDeAn() != null ? currencyFormat.format(pc.getPhuCapDeAn()) : currencyFormat.format(0);
            modelPhanCong.addRow(new Object[]{pc.getMaNV(), tenNV, start, end, phuCap});
        }
    }

    private void openAddDeAnDialog() {
        DeAnDialog dialog = new DeAnDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                "Thêm đề án",
                null,
                deAnBUS,
                phongBanBUS
        );
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshData();
        }
    }

    private void openEditDeAnDialog() {
        String maDA = getSelectedMaDA();
        if (maDA == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đề án cần sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        DeAnDTO dto = deAnBUS.getById(maDA);
        if (dto == null) {
            return;
        }

        DeAnDialog dialog = new DeAnDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                "Sửa đề án",
                dto,
                deAnBUS,
                phongBanBUS
        );
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshData();
            selectDeAn(maDA);
        }
    }

    private void deleteSelectedDeAn() {
        String maDA = getSelectedMaDA();
        if (maDA == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đề án cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!deAnBUS.canDelete(maDA)) {
            JOptionPane.showMessageDialog(this,
                    "Đề án đang có phân công nhân viên, không thể xóa!",
                    "Không thể xóa", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa đề án [" + maDA + "]?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        if (deAnBUS.delete(maDA)) {
            JOptionPane.showMessageDialog(this, "Xóa đề án thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            refreshData();
        } else {
            JOptionPane.showMessageDialog(this, "Xóa đề án thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openAddAssignDialog() {
        String maDA = getSelectedMaDA();
        if (maDA == null) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn đề án trước khi thêm phân công!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        PhanCongDeAnDialog dialog = new PhanCongDeAnDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                "Thêm phân công",
                maDA,
                null,
                phanCongBUS,
                nhanVienBUS
        );
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshData();
            selectDeAn(maDA);
        }
    }

    private void openEditAssignDialog() {
        String maDA = getSelectedMaDA();
        int row = tblPhanCong.getSelectedRow();
        if (maDA == null || row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phân công cần sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String maNV = String.valueOf(modelPhanCong.getValueAt(row, 0));
        PhanCongDeAnDTO dto = phanCongBUS.getById(maNV, maDA);
        if (dto == null) {
            return;
        }

        PhanCongDeAnDialog dialog = new PhanCongDeAnDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                "Sửa phân công",
                maDA,
                dto,
                phanCongBUS,
                nhanVienBUS
        );
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshData();
            selectDeAn(maDA);
        }
    }

    private void deleteSelectedAssign() {
        String maDA = getSelectedMaDA();
        int row = tblPhanCong.getSelectedRow();
        if (maDA == null || row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phân công cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String maNV = String.valueOf(modelPhanCong.getValueAt(row, 0));
        int choice = JOptionPane.showConfirmDialog(this,
                "Xóa phân công nhân viên [" + maNV + "] khỏi đề án [" + maDA + "]?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        if (phanCongBUS.delete(maNV, maDA)) {
            JOptionPane.showMessageDialog(this, "Xóa phân công thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            refreshData();
            selectDeAn(maDA);
        } else {
            JOptionPane.showMessageDialog(this, "Xóa phân công thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getSelectedMaDA() {
        int row = tblDeAn.getSelectedRow();
        if (row == -1) {
            return null;
        }
        return String.valueOf(modelDeAn.getValueAt(row, 0));
    }

    private String getTenPhongBan(String maPB) {
        PhongBanDTO pb = phongBanBUS.getById(maPB);
        return pb != null ? pb.getTenPB() : maPB;
    }

    private String getHoTenNhanVien(String maNV) {
        NhanVienDTO nv = nhanVienBUS.getById(maNV);
        return nv != null ? nv.getHo() + " " + nv.getTen() : maNV;
    }

    private void selectDeAn(String maDA) {
        for (int i = 0; i < modelDeAn.getRowCount(); i++) {
            if (maDA.equals(String.valueOf(modelDeAn.getValueAt(i, 0)))) {
                tblDeAn.setRowSelectionInterval(i, i);
                break;
            }
        }
        loadPhanCongTable();
    }

    // =========================================================================
    //  EXPORT / IMPORT EXCEL (Phân công đề án)
    // =========================================================================
    private static final String[] PCDA_HEADERS = {
        "Mã NV", "Mã ĐA", "Ngày bắt đầu (dd/MM/yyyy)", "Ngày kết thúc (dd/MM/yyyy)", "Phụ cấp đề án"
    };

    private void exportExcel() {
        java.util.List<Object[]> rows = new java.util.ArrayList<>();
        for (PhanCongDeAnDTO pc : phanCongBUS.getList()) {
            rows.add(new Object[]{
                pc.getMaNV(), pc.getMaDA(),
                pc.getNgayBatDau(), pc.getNgayKetThuc(),
                pc.getPhuCapDeAn()
            });
        }
        if (rows.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu phân công đề án để xuất!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        HSSFWorkbook wb = ExcelHelper.createWorkbook("PhanCongDeAn", PCDA_HEADERS, rows);
        ExcelHelper.saveWithDialog(SwingUtilities.getWindowAncestor(this), wb, "PhanCongDeAn");
    }

    private void importExcel() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "File Excel cần header:\nMã NV | Mã ĐA | Ngày bắt đầu | Ngày kết thúc | Phụ cấp đề án\n\n" +
                "Bạn muốn tải file mẫu trước?",
                "Nhập Excel — Phân công đề án",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            HSSFWorkbook tmpl = ExcelHelper.createTemplate("PhanCongDeAn", PCDA_HEADERS);
            ExcelHelper.saveWithDialog(SwingUtilities.getWindowAncestor(this), tmpl, "Mau_PhanCongDeAn");
            return;
        }
        if (confirm == JOptionPane.CANCEL_OPTION) return;

        java.util.List<String[]> data = ExcelHelper.openAndRead(this, PCDA_HEADERS.length);
        if (data == null) return;

        java.util.List<String> errors = new java.util.ArrayList<>();
        int success = 0;

        for (int i = 0; i < data.size(); i++) {
            String[] row = data.get(i);
            int line = i + 2;
            try {
                String maNV = row[0].trim();
                String maDA = row[1].trim();
                java.util.Date ngayBD = ExcelHelper.parseDate(row[2]);
                java.util.Date ngayKT = ExcelHelper.parseDate(row[3]);
                java.math.BigDecimal phuCap = ExcelHelper.parseMoney(row[4]);

                if (maNV.isEmpty()) { errors.add("Dòng " + line + ": Mã NV trống"); continue; }
                if (maDA.isEmpty()) { errors.add("Dòng " + line + ": Mã ĐA trống"); continue; }
                if (nhanVienBUS.getById(maNV) == null) { errors.add("Dòng " + line + ": Mã NV '" + maNV + "' không tồn tại"); continue; }
                if (deAnBUS.getById(maDA) == null) { errors.add("Dòng " + line + ": Mã ĐA '" + maDA + "' không tồn tại"); continue; }

                PhanCongDeAnDTO dto = new PhanCongDeAnDTO(maNV, maDA, ngayBD, ngayKT, phuCap);
                phanCongBUS.add(dto);
                success++;
            } catch (Exception ex) {
                errors.add("Dòng " + line + ": " + ex.getMessage());
            }
        }

        refreshData();
        ExcelHelper.showImportErrors(this, errors, success);
    }
}
