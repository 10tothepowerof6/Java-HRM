package com.hrm.gui;

import com.hrm.bus.*;
import com.hrm.dto.*;
import com.hrm.security.PermissionHelper;
import com.hrm.util.ExcelHelper;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Cấu hình tham số hệ thống (mức lương cơ sở, tỷ lệ bảo hiểm…) dùng trong tính lương và báo cáo.
 * <p>
 * Dữ liệu bảng {@code ThamSo}; hỗ trợ xuất/nhập Excel theo quyền module {@code caidat}.
 * </p>
 */
public class ThamSoPanel extends JPanel {

    // ===== BẢNG MÀU =====
    private static final Color PRIMARY       = new Color(0, 82, 155);
    private static final Color PRIMARY_DARK  = new Color(0, 51, 102);
    private static final Color BG_CONTENT    = new Color(30, 41, 59);
    private static final Color BG_TOOLBAR    = new Color(22, 33, 52);
    private static final Color TEXT_WHITE    = new Color(248, 250, 252);
    private static final Color TABLE_ROW_ALT = new Color(22, 33, 52);
    private static final Color TABLE_SEL     = new Color(0, 82, 155);

    private ThamSoBUS thamSoBUS;
    private JTable table;
    private DefaultTableModel tableModel;

    private final DecimalFormat dfMoney = new DecimalFormat("#,##0.##");
    private final SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public ThamSoPanel() {
        thamSoBUS = new ThamSoBUS();
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

        // --- Left (Title instead of search) ---
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftPanel.setOpaque(false);
        
        JLabel lblHeading = new JLabel("CẤU HÌNH HỆ THỐNG");
        lblHeading.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblHeading.setForeground(TEXT_WHITE);
        leftPanel.add(lblHeading);

        toolbar.add(leftPanel, BorderLayout.WEST);

        // --- Right (Buttons) ---
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightPanel.setOpaque(false);

        JButton btnReset = createStyledButton("DB Reset Lương", new Color(220, 38, 38));
        btnReset.setPreferredSize(new Dimension(140, 34));
        btnReset.addActionListener(e -> deleteAllBangLuong());
        rightPanel.add(btnReset);

        JButton btnEdit = createStyledButton("Sửa thuộc tính", PRIMARY);
        btnEdit.setPreferredSize(new Dimension(130, 34));
        btnEdit.addActionListener(e -> updateThamSo());
        rightPanel.add(btnEdit);

        rightPanel.add(Box.createHorizontalStrut(14));

        JButton btnFullExport = createStyledButton("Xuất toàn bộ dữ liệu", new Color(16, 185, 129));
        btnFullExport.setPreferredSize(new Dimension(200, 34));
        btnFullExport.addActionListener(e -> exportFullBackup());
        rightPanel.add(btnFullExport);

        toolbar.add(rightPanel, BorderLayout.EAST);

        PermissionHelper.applyVisible(btnReset, PermissionHelper.canDelete("caidat"));
        PermissionHelper.applyVisible(btnEdit, PermissionHelper.canEdit("caidat"));
        PermissionHelper.applyVisible(btnFullExport, PermissionHelper.canExcel("caidat"));

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
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(hoverColor); }
            public void mouseExited(java.awt.event.MouseEvent e) { btn.setBackground(bgColor); }
        });
        return btn;
    }

    private JScrollPane createTablePanel() {
        String[] columns = {"Mã Tham Số", "Tên Tham Số", "Giá Trị", "Mô Tả", "Lần Cập Nhật Gần Nhất"};
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

                if (column == 0 || column == 2 || column == 4) {
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.LEFT);
                }

                ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return c;
            }
        });

        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(350);
        table.getColumnModel().getColumn(4).setPreferredWidth(150);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BG_CONTENT);
        return scrollPane;
    }

    private void loadDataToTable() {
        tableModel.setRowCount(0);
        thamSoBUS.loadData();
        ArrayList<ThamSoDTO> list = thamSoBUS.getList();

        for (ThamSoDTO ts : list) {
            String giaTriStr = dfMoney.format(ts.getGiaTri());
            if (ts.getTenThamSo().contains("TyLe")) {
                giaTriStr += " %";
            } else if (ts.getTenThamSo().equals("MucLuongCoSo")) {
                giaTriStr += " ₫";
            }

            String ngayCN = ts.getNgayCapNhat() != null ? sdfDate.format(ts.getNgayCapNhat()) : "";

            tableModel.addRow(new Object[]{
                    ts.getMaThamSo(),
                    ts.getTenThamSo(),
                    giaTriStr,
                    ts.getMoTa(),
                    ngayCN
            });
        }
    }

    private void updateThamSo() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 thuộc tính để sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maTS = (String) table.getValueAt(selectedRow, 0);
        ThamSoDTO dto = thamSoBUS.getById(maTS);

        if (dto != null) {
            ThamSoDialog dialog = new ThamSoDialog(SwingUtilities.getWindowAncestor(this), "Cập nhật cấu hình", dto, thamSoBUS);
            dialog.setVisible(true);

            if (dialog.isSaved()) {
                loadDataToTable();
            }
        }
    }

    private void exportFullBackup() {
        HSSFWorkbook wb = new HSSFWorkbook();

        // NhanVien
        NhanVienBUS nvBus = new NhanVienBUS();
        java.util.List<Object[]> nvRows = new java.util.ArrayList<>();
        for (NhanVienDTO nv : nvBus.getList()) {
            nvRows.add(new Object[]{nv.getMaNV(), nv.getHo(), nv.getTen(), nv.getGioiTinh(),
                    nv.getNgaySinh(), nv.getNgayBatDau(), nv.getTrangThai(), nv.getMaPB(), nv.getMaCV()});
        }
        ExcelHelper.addSheet(wb, "NhanVien",
                new String[]{"Mã NV","Họ","Tên","Giới tính","Ngày sinh","Ngày bắt đầu","Trạng thái","Mã PB","Mã CV"}, nvRows);

        // PhongBan
        PhongBanBUS pbBus = new PhongBanBUS();
        java.util.List<Object[]> pbRows = new java.util.ArrayList<>();
        for (PhongBanDTO pb : pbBus.getList()) {
            pbRows.add(new Object[]{pb.getMaPB(), pb.getTenPB(), pb.getMaTruongPB(),
                    pb.getNgayThanhLap(), pb.getEmail()});
        }
        ExcelHelper.addSheet(wb, "PhongBan",
                new String[]{"Mã PB","Tên PB","Mã trưởng PB","Ngày thành lập","Email"}, pbRows);

        // ChucVu
        ChucVuBUS cvBus = new ChucVuBUS();
        java.util.List<Object[]> cvRows = new java.util.ArrayList<>();
        for (ChucVuDTO cv : cvBus.getList()) {
            cvRows.add(new Object[]{cv.getMaCV(), cv.getTenCV(), cv.getHeSoLuong()});
        }
        ExcelHelper.addSheet(wb, "ChucVu",
                new String[]{"Mã CV","Tên CV","Hệ số lương"}, cvRows);

        // ChamCong
        BangChamCongBUS ccBus = new BangChamCongBUS();
        java.util.List<Object[]> ccRows = new java.util.ArrayList<>();
        for (BangChamCongDTO cc : ccBus.getList()) {
            ccRows.add(new Object[]{cc.getMaChamCong(), cc.getMaNV(), cc.getNgayLamViec(),
                    cc.getGioVao() != null ? ExcelHelper.formatTime(cc.getGioVao()) : "",
                    cc.getGioRa() != null ? ExcelHelper.formatTime(cc.getGioRa()) : "",
                    cc.getTrangThai()});
        }
        ExcelHelper.addSheet(wb, "ChamCong",
                new String[]{"Mã CC","Mã NV","Ngày làm việc","Giờ vào","Giờ ra","Trạng thái"}, ccRows);

        // NghiPhep
        NghiPhepBUS npBus = new NghiPhepBUS();
        java.util.List<Object[]> npRows = new java.util.ArrayList<>();
        for (NghiPhepDTO np : npBus.getList()) {
            npRows.add(new Object[]{np.getMaNP(), np.getMaNV(), np.getMaLoaiNP(),
                    np.getTuNgay(), np.getDenNgay(), np.getSoNgay(), np.getLyDo(), np.getTrangThai()});
        }
        ExcelHelper.addSheet(wb, "NghiPhep",
                new String[]{"Mã đơn","Mã NV","Mã loại NP","Từ ngày","Đến ngày","Số ngày","Lý do","Trạng thái"}, npRows);

        // PhuCap
        PhuCapBUS pcBus = new PhuCapBUS();
        java.util.List<Object[]> pcRows = new java.util.ArrayList<>();
        for (PhuCapDTO pc : pcBus.getList()) {
            pcRows.add(new Object[]{pc.getMaPC(), pc.getMaNV(), pc.getMaLoaiPC(),
                    pc.getSoTien(), pc.getNgayApDung(), pc.getNgayKetThuc()});
        }
        ExcelHelper.addSheet(wb, "PhuCap",
                new String[]{"Mã PC","Mã NV","Mã loại PC","Số tiền","Ngày áp dụng","Ngày kết thúc"}, pcRows);

        // ChiTietThuong
        ChiTietThuongBUS cttBus = new ChiTietThuongBUS();
        java.util.List<Object[]> cttRows = new java.util.ArrayList<>();
        for (ChiTietThuongDTO ct : cttBus.getList()) {
            cttRows.add(new Object[]{ct.getMaCTT(), ct.getMaNV(), ct.getMaThuong(),
                    ct.getSoTien(), ct.getNgayThuong(), ct.getGhiChu()});
        }
        ExcelHelper.addSheet(wb, "ChiTietThuong",
                new String[]{"Mã CTT","Mã NV","Mã thưởng","Số tiền","Ngày thưởng","Ghi chú"}, cttRows);

        // PhanCongDeAn
        PhanCongDeAnBUS pcdaBus = new PhanCongDeAnBUS();
        java.util.List<Object[]> pcdaRows = new java.util.ArrayList<>();
        for (PhanCongDeAnDTO pc : pcdaBus.getList()) {
            pcdaRows.add(new Object[]{pc.getMaNV(), pc.getMaDA(),
                    pc.getNgayBatDau(), pc.getNgayKetThuc(), pc.getPhuCapDeAn()});
        }
        ExcelHelper.addSheet(wb, "PhanCongDeAn",
                new String[]{"Mã NV","Mã ĐA","Ngày bắt đầu","Ngày kết thúc","Phụ cấp đề án"}, pcdaRows);

        // BangLuongThang
        BangLuongThangBUS blBus = new BangLuongThangBUS();
        java.util.List<Object[]> blRows = new java.util.ArrayList<>();
        for (BangLuongThangDTO bl : blBus.getList()) {
            blRows.add(new Object[]{bl.getMaBangLuong(), bl.getMaNV(), bl.getThang(), bl.getNam(),
                    bl.getSoNgayCong(), bl.getHeSoLuong(), bl.getLuongCoBan(),
                    bl.getTongPhuCap(), bl.getTongThuong(),
                    bl.getKhauTruBHXH(), bl.getKhauTruBHYT(), bl.getKhauTruBHTN(),
                    bl.getThueTNCN(), bl.getThucLanh()});
        }
        ExcelHelper.addSheet(wb, "BangLuongThang",
                new String[]{"Mã BL","Mã NV","Tháng","Năm","Ngày công","Hệ số","Lương CB",
                        "Phụ cấp","Thưởng","BHXH","BHYT","BHTN","Thuế TNCN","Thực lãnh"}, blRows);

        // ThamSo
        java.util.List<Object[]> tsRows = new java.util.ArrayList<>();
        for (ThamSoDTO ts : thamSoBUS.getList()) {
            tsRows.add(new Object[]{ts.getMaThamSo(), ts.getTenThamSo(), ts.getGiaTri(), ts.getMoTa()});
        }
        ExcelHelper.addSheet(wb, "ThamSo",
                new String[]{"Mã tham số","Tên tham số","Giá trị","Mô tả"}, tsRows);

        ExcelHelper.saveWithDialog(SwingUtilities.getWindowAncestor(this), wb, "FullBackup_HRM");
    }

    private void deleteAllBangLuong() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Bạn có chắc chắn muốn xóa TOÀN BỘ dữ liệu tính lương không?\nViệc này không thể hoàn tác!", 
            "Xác nhận xóa (Dev Only)", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
        if (confirm == JOptionPane.YES_OPTION) {
            BangLuongThangBUS blBus = new BangLuongThangBUS();
            if (blBus.deleteAll()) {
                JOptionPane.showMessageDialog(this, "Đã xóa toàn bộ dữ liệu bảng lương!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa bảng lương!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
