package com.hrm.gui;

import com.hrm.bus.BangLuongThangBUS;
import com.hrm.bus.NhanVienBUS;
import com.hrm.dto.BangLuongThangDTO;
import com.hrm.dto.NhanVienDTO;
import com.hrm.security.PermissionHelper;
import com.hrm.util.ExcelHelper;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Danh sách bảng lương theo tháng/năm, tính lương hàng loạt, xem phiếu lương và xuất Excel.
 * <p>
 * Logic tổng hợp tiền nằm ở {@link com.hrm.bus.BangLuongThangBUS#generateBangLuong(int, int)}.
 * </p>
 */
public class BangLuongThangPanel extends JPanel {

    // ===== BẢNG MÀU =====
    private static final Color PRIMARY       = new Color(0, 82, 155);
    private static final Color PRIMARY_DARK  = new Color(0, 51, 102);
    private static final Color BG_CONTENT    = new Color(30, 41, 59);
    private static final Color BG_TOOLBAR    = new Color(22, 33, 52);
    private static final Color TEXT_WHITE    = new Color(248, 250, 252);
    private static final Color TEXT_MUTED    = new Color(148, 163, 184);
    private static final Color TABLE_ROW_ALT = new Color(22, 33, 52);
    private static final Color TABLE_SEL     = new Color(0, 82, 155);
    private static final Color BTN_SUCCESS   = new Color(16, 185, 129);
    private static final Color BTN_SECONDARY = new Color(55, 65, 81);

    private BangLuongThangBUS bus;
    private NhanVienBUS nvBus;
    private JTable table;
    private DefaultTableModel tableModel;

    private JComboBox<Integer> cboThang;
    private JComboBox<Integer> cboNam;

    private final DecimalFormat dfMoney = new DecimalFormat("#,##0 ₫");

    public BangLuongThangPanel() {
        bus = new BangLuongThangBUS();
        nvBus = new NhanVienBUS();
        initUI();
        loadDataToTable();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(BG_CONTENT);

        add(createToolbar(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(BG_TOOLBAR);
        toolbar.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        // --- Left ---
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftPanel.setOpaque(false);

        JLabel lblThang = new JLabel("Tháng:");
        lblThang.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblThang.setForeground(TEXT_WHITE);
        leftPanel.add(lblThang);

        Integer[] months = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        cboThang = createFilterCombo(months);
        int currentMonth = LocalDate.now().getMonthValue();
        cboThang.setSelectedItem(currentMonth);
        leftPanel.add(cboThang);

        JLabel lblNam = new JLabel("  Năm:");
        lblNam.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblNam.setForeground(TEXT_WHITE);
        leftPanel.add(lblNam);

        Integer[] years = new Integer[11];
        int currentYear = LocalDate.now().getYear();
        for (int i = 0; i <= 10; i++) years[i] = currentYear - 5 + i;
        cboNam = createFilterCombo(years);
        cboNam.setSelectedItem(currentYear);
        leftPanel.add(cboNam);

        JButton btnFilter = createStyledButton("Lọc", BTN_SECONDARY);
        btnFilter.addActionListener(e -> loadDataThangNam());
        leftPanel.add(btnFilter);

        toolbar.add(leftPanel, BorderLayout.WEST);

        // --- Right ---
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightPanel.setOpaque(false);

        JButton btnExport = createStyledButton("Xuất Excel", BTN_SECONDARY);
        btnExport.setPreferredSize(new Dimension(110, 34));
        btnExport.addActionListener(e -> exportExcel());
        rightPanel.add(btnExport);

        JButton btnGenerate = createStyledButton("Tính Lương", BTN_SUCCESS);
        btnGenerate.setPreferredSize(new Dimension(130, 34));
        btnGenerate.addActionListener(e -> openGenerateDialog());
        rightPanel.add(btnGenerate);

        toolbar.add(rightPanel, BorderLayout.EAST);

        PermissionHelper.applyVisible(btnExport, PermissionHelper.canExcel("bangluong"));
        PermissionHelper.applyVisible(btnGenerate,
                PermissionHelper.canAdd("bangluong") || PermissionHelper.canEdit("bangluong"));

        return toolbar;
    }

    private <T> JComboBox<T> createFilterCombo(T[] items) {
        JComboBox<T> cbo = new JComboBox<>(items);
        cbo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbo.setBackground(Color.WHITE);
        cbo.setForeground(Color.BLACK);
        cbo.setPreferredSize(new Dimension(80, 34));
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
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(hoverColor); }
            public void mouseExited(java.awt.event.MouseEvent e) { btn.setBackground(bgColor); }
        });
        return btn;
    }

    private JScrollPane createTablePanel() {
        String[] columns = {"Mã Bảng Lương", "Mã NV", "Họ và tên", "Lương CB", "Phụ Cấp", "Thưởng", "Khấu Trừ & Thuế", "Thực Lãnh"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
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
                lbl.setBackground(PRIMARY_DARK); // Style mới giống NhanVienPanel
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

        // Tiền lưu dạng String (+/- …) → JTable dùng renderer của String.class, không phải Object.class
        DefaultTableCellRenderer bangLuongBodyRenderer = new DefaultTableCellRenderer() {
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

                JLabel lbl = (JLabel) c;
                if (column >= 3 && column <= 7) {
                    lbl.setHorizontalAlignment(SwingConstants.RIGHT);
                } else {
                    lbl.setHorizontalAlignment(SwingConstants.LEFT);
                }

                Font base = table.getFont() != null ? table.getFont() : new Font("Segoe UI", Font.PLAIN, 13);
                if (column == 7) {
                    lbl.setFont(base.deriveFont(Font.BOLD));
                } else {
                    lbl.setFont(base);
                }

                lbl.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return c;
            }
        };
        table.setDefaultRenderer(Object.class, bangLuongBodyRenderer);
        table.setDefaultRenderer(String.class, bangLuongBodyRenderer);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1
                        && PermissionHelper.canView("bangluong")) {
                    openPhieuLuongDialog();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BG_CONTENT);
        return scrollPane;
    }

    private void loadDataThangNam() {
        int t = (int) cboThang.getSelectedItem();
        int n = (int) cboNam.getSelectedItem();
        
        tableModel.setRowCount(0);
        
        for (BangLuongThangDTO dto : bus.getList()) {
            if (dto.getThang() == t && dto.getNam() == n) {
                BigDecimal tongKhauTru = dto.getKhauTruBHXH()
                        .add(dto.getKhauTruBHYT())
                        .add(dto.getKhauTruBHTN())
                        .add(dto.getThueTNCN());

                String hoTen = "Không xác định";
                NhanVienDTO nv = nvBus.getById(dto.getMaNV());
                if (nv != null) {
                    hoTen = nv.getHo() + " " + nv.getTen();
                }

                tableModel.addRow(new Object[]{
                        dto.getMaBangLuong(),
                        dto.getMaNV(),
                        hoTen,
                        "+" + dfMoney.format(dto.getLuongCoBan()),
                        "+" + dfMoney.format(dto.getTongPhuCap()),
                        "+" + dfMoney.format(dto.getTongThuong()),
                        "-" + dfMoney.format(tongKhauTru),
                        dfMoney.format(dto.getThucLanh())
                });
            }
        }
    }

    private void loadDataToTable() {
        loadDataThangNam();
    }

    public void refreshData() {
        bus.loadData();
        loadDataToTable();
    }

    private void openGenerateDialog() {
        HanhDongTinhLuongDialog dialog = new HanhDongTinhLuongDialog(SwingUtilities.getWindowAncestor(this), bus);
        dialog.setVisible(true);

        if (dialog.isGenerated()) {
            refreshData();
            cboThang.setSelectedItem(dialog.getThang());
            cboNam.setSelectedItem(dialog.getNam());
            loadDataThangNam();
        }
    }

    private void exportExcel() {
        int t = (int) cboThang.getSelectedItem();
        int n = (int) cboNam.getSelectedItem();

        List<Object[]> rows = new ArrayList<>();
        for (BangLuongThangDTO dto : bus.getList()) {
            if (dto.getThang() == t && dto.getNam() == n) {
                String hoTen = "";
                NhanVienDTO nv = nvBus.getById(dto.getMaNV());
                if (nv != null) hoTen = nv.getHo() + " " + nv.getTen();

                BigDecimal tongKhauTru = dto.getKhauTruBHXH()
                        .add(dto.getKhauTruBHYT())
                        .add(dto.getKhauTruBHTN())
                        .add(dto.getThueTNCN());

                rows.add(new Object[]{
                    dto.getMaBangLuong(), dto.getMaNV(), hoTen,
                    dto.getSoNgayCong(), dto.getHeSoLuong(),
                    dto.getLuongCoBan(), dto.getTongPhuCap(), dto.getTongThuong(),
                    dto.getKhauTruBHXH(), dto.getKhauTruBHYT(),
                    dto.getKhauTruBHTN(), dto.getThueTNCN(),
                    tongKhauTru, dto.getThucLanh()
                });
            }
        }

        if (rows.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Không có dữ liệu lương tháng " + t + "/" + n + " để xuất!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] headers = {
            "Mã BL", "Mã NV", "Họ tên", "Ngày công", "Hệ số",
            "Lương CB", "Phụ cấp", "Thưởng",
            "BHXH", "BHYT", "BHTN", "Thuế TNCN",
            "Tổng khấu trừ", "Thực lãnh"
        };

        HSSFWorkbook wb = ExcelHelper.createWorkbook("BangLuong_" + t + "_" + n, headers, rows);
        ExcelHelper.saveWithDialog(SwingUtilities.getWindowAncestor(this), wb,
                "BangLuong_Thang" + t + "_" + n);
    }

    private void openPhieuLuongDialog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 bảng lương để xem!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maBL = (String) table.getValueAt(selectedRow, 0);
        BangLuongThangDTO dto = bus.getById(maBL);

        if (dto != null) {
            PhieuLuongDialog dialog = new PhieuLuongDialog(SwingUtilities.getWindowAncestor(this), dto);
            dialog.setVisible(true);
        }
    }
}
