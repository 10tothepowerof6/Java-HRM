package com.hrm.gui;

import com.hrm.bus.*;
import com.hrm.dto.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.sql.Time;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Màn hình tổng quan (dashboard): thẻ KPI và các bảng cảnh báo nhanh (sinh nhật, hợp đồng, phép, chấm công).
 * <p>
 * Dữ liệu lấy trực tiếp từ các BUS tương ứng; {@link #refreshData()} nạp lại list và dựng lại UI khi người dùng vào lại menu.
 * </p>
 */
public class TongQuanPanel extends JPanel {

    // ===== BẢNG MÀU =====
    private static final Color PRIMARY       = new Color(0, 82, 155);
    private static final Color PRIMARY_DARK  = new Color(0, 51, 102);
    private static final Color BG_CONTENT    = new Color(30, 41, 59);
    private static final Color BG_CARD       = new Color(22, 33, 52);
    private static final Color TEXT_WHITE    = new Color(248, 250, 252);
    private static final Color TEXT_MUTED    = new Color(148, 163, 184);
    private static final Color TABLE_ROW_ALT = new Color(22, 33, 52);
    private static final Color TABLE_SEL     = new Color(0, 82, 155);
    private static final Color CARD_GREEN    = new Color(16, 185, 129);
    private static final Color CARD_RED      = new Color(239, 68, 68);
    private static final Color CARD_ORANGE   = new Color(245, 158, 11);
    private static final Color CARD_BLUE     = new Color(59, 130, 246);
    private static final Color CARD_PURPLE   = new Color(139, 92, 246);
    private static final Color CARD_CYAN     = new Color(6, 182, 212);
    private static final Color CARD_SLATE    = new Color(100, 116, 139);

    private NhanVienBUS nvBus;
    private PhongBanBUS pbBus;
    private ChucVuBUS cvBus;
    private BangLuongThangBUS blBus;
    private NghiPhepBUS npBus;
    private HopDongBUS hdBus;
    private BangChamCongBUS ccBus;

    private final DecimalFormat dfMoney = new DecimalFormat("#,##0 ₫");
    private final SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");

    public TongQuanPanel() {
        nvBus = new NhanVienBUS();
        pbBus = new PhongBanBUS();
        cvBus = new ChucVuBUS();
        blBus = new BangLuongThangBUS();
        npBus = new NghiPhepBUS();
        hdBus = new HopDongBUS();
        ccBus = new BangChamCongBUS();

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(16, 16));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(BG_CONTENT);

        add(createKPIPanel(), BorderLayout.NORTH);
        add(createListsPanel(), BorderLayout.CENTER);
    }

    private JPanel createKPIPanel() {
        JPanel kpiPanel = new JPanel(new GridLayout(2, 4, 16, 16));
        kpiPanel.setOpaque(false);
        kpiPanel.setPreferredSize(new Dimension(0, 248));

        LocalDate today = LocalDate.now();
        int nam = today.getYear();
        int thang = today.getMonthValue();

        long dangLam = nvBus.getList().stream().filter(nv -> "Đang làm".equals(nv.getTrangThai())).count();
        long daNghi = nvBus.getList().stream().filter(nv -> "Đã nghỉ".equals(nv.getTrangThai())).count();
        int tongPB = pbBus.getList().size();
        int tongCV = cvBus.getList().size();

        BigDecimal tongLuong = BigDecimal.ZERO;
        for (BangLuongThangDTO dto : blBus.getList()) {
            if (dto.getThang() == thang && dto.getNam() == nam) {
                tongLuong = tongLuong.add(dto.getThucLanh());
            }
        }

        long nghiPhepHomNay = countApprovedLeaveOnDate(today);
        long phepChoDuyet = npBus.getList().stream()
                .filter(np -> "Chờ duyệt".equalsIgnoreCase(np.getTrangThai()))
                .count();

        long daChamCongHomNay = ccBus.getList().stream()
                .filter(cc -> today.equals(toLocalDate(cc.getNgayLamViec())))
                .filter(cc -> {
                    NhanVienDTO nv = nvBus.getById(cc.getMaNV());
                    return nv != null && "Đang làm".equals(nv.getTrangThai());
                })
                .map(BangChamCongDTO::getMaNV)
                .distinct()
                .count();

        long hopDongSapHetHan = countContractsExpiringWithinDays(today, 30);

        kpiPanel.add(createCard("Đang Làm Việc", String.valueOf(dangLam), "Người", PRIMARY));
        kpiPanel.add(createCard("Đã Nghỉ Việc", String.valueOf(daNghi), "Người", CARD_SLATE));
        kpiPanel.add(createCard("Phòng Ban", String.valueOf(tongPB), "Phòng", CARD_GREEN));
        kpiPanel.add(createCard("Chức Vụ", String.valueOf(tongCV), "Loại", CARD_BLUE));

        kpiPanel.add(createCard("Quỹ Lương (T" + thang + ")",
                tongLuong.compareTo(BigDecimal.ZERO) == 0 ? "0" : dfMoney.format(tongLuong).replace(" ₫", ""),
                "VNĐ", CARD_ORANGE));
        kpiPanel.add(createCard("Nghỉ Phép Hôm Nay", String.valueOf(nghiPhepHomNay), "Người", CARD_RED));
        kpiPanel.add(createCard("Phép Chờ Duyệt", String.valueOf(phepChoDuyet), "Đơn", CARD_PURPLE));
        kpiPanel.add(createCard("Đã Chấm Công (HN)", String.valueOf(daChamCongHomNay), "Người", CARD_CYAN));

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(kpiPanel, BorderLayout.NORTH);
        JLabel lblHd = new JLabel("Hợp đồng sắp hết hạn (≤30 ngày): " + hopDongSapHetHan);
        lblHd.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblHd.setForeground(TEXT_MUTED);
        lblHd.setBorder(new EmptyBorder(4, 4, 0, 0));
        wrap.add(lblHd, BorderLayout.SOUTH);
        return wrap;
    }

    private long countApprovedLeaveOnDate(LocalDate day) {
        long n = 0;
        for (NghiPhepDTO np : npBus.getList()) {
            if (!"Đã duyệt".equals(np.getTrangThai()) || np.getTuNgay() == null || np.getDenNgay() == null) {
                continue;
            }
            LocalDate start = toLocalDate(np.getTuNgay());
            LocalDate end = toLocalDate(np.getDenNgay());
            if (start == null || end == null) continue;
            if (!day.isBefore(start) && !day.isAfter(end)) n++;
        }
        return n;
    }

    private long countContractsExpiringWithinDays(LocalDate from, int maxDaysAhead) {
        LocalDate limit = from.plusDays(maxDaysAhead);
        long n = 0;
        for (HopDongDTO hd : hdBus.getList()) {
            if (!"Có hiệu lực".equals(hd.getTrangThai()) || hd.getNgayKetThuc() == null) continue;
            LocalDate end = toLocalDate(hd.getNgayKetThuc());
            if (end == null) continue;
            if (!end.isBefore(from) && !end.isAfter(limit)) n++;
        }
        return n;
    }

    private static LocalDate toLocalDate(Date d) {
        if (d == null) return null;
        if (d instanceof java.sql.Date) return ((java.sql.Date) d).toLocalDate();
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static String formatTime(Time t) {
        if (t == null) return "—";
        return t.toString().substring(0, Math.min(8, t.toString().length()));
    }

    private JPanel createCard(String title, String value, String unit, Color accent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
                new EmptyBorder(16, 16, 16, 16)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(TEXT_MUTED);
        card.add(lblTitle, BorderLayout.NORTH);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblValue.setForeground(TEXT_WHITE);
        card.add(lblValue, BorderLayout.CENTER);

        JLabel lblUnit = new JLabel(unit);
        lblUnit.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblUnit.setForeground(TEXT_MUTED);
        lblUnit.setHorizontalAlignment(SwingConstants.RIGHT);
        card.add(lblUnit, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createListsPanel() {
        JPanel listsPanel = new JPanel(new GridLayout(2, 2, 16, 16));
        listsPanel.setOpaque(false);

        listsPanel.add(createSection("SINH NHẬT TRONG THÁNG", createBirthdayTable()));
        listsPanel.add(createSection("HỢP ĐỒNG SẮP HẾT HẠN (<30 NGÀY)", createContractTable()));
        listsPanel.add(createSection("ĐƠN NGHỈ PHÉP CHỜ DUYỆT", createPendingLeaveTable()));
        listsPanel.add(createSection("CHẤM CÔNG HÔM NAY", createTodayAttendanceTable()));

        return listsPanel;
    }

    private JPanel createSection(String title, JTable table) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(TEXT_WHITE);
        lblTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        panel.add(lblTitle, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(40, 50, 70)));
        scrollPane.getViewport().setBackground(BG_CONTENT);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JTable createBirthdayTable() {
        String[] columns = {"Mã NV", "Họ Tên", "Phòng Ban", "Ngày Sinh"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        int currentMonth = LocalDate.now().getMonthValue();
        
        List<NhanVienDTO> nvs = nvBus.getList().stream()
            .filter(nv -> "Đang làm".equals(nv.getTrangThai()) && nv.getNgaySinh() != null)
            .filter(nv -> (nv.getNgaySinh().getMonth() + 1) == currentMonth)
            .collect(Collectors.toList());

        for (NhanVienDTO nv : nvs) {
            String pbName = "Không rõ";
            PhongBanDTO pb = pbBus.getById(nv.getMaPB());
            if (pb != null) pbName = pb.getTenPB();

            model.addRow(new Object[]{
                    nv.getMaNV(),
                    nv.getHo() + " " + nv.getTen(),
                    pbName,
                    sdfDate.format(nv.getNgaySinh())
            });
        }

        return styleTable(model);
    }

    private JTable createContractTable() {
        String[] columns = {"Mã NV", "Họ Tên", "Ngày Hết Hạn", "Còn Lại"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        LocalDate today = LocalDate.now();
        LocalDate in30Days = today.plusDays(30);

        List<HopDongDTO> arr = hdBus.getList().stream()
            .filter(hd -> "Có hiệu lực".equals(hd.getTrangThai()) && hd.getNgayKetThuc() != null)
            .collect(Collectors.toList());

        for (HopDongDTO hd : arr) {
            LocalDate end = toLocalDate(hd.getNgayKetThuc());
            if (end == null) continue;

            if (!end.isBefore(today) && !end.isAfter(in30Days)) {
                long daysBetween = ChronoUnit.DAYS.between(today, end);
                
                NhanVienDTO nv = nvBus.getById(hd.getMaNV());
                String hoTen = nv != null ? nv.getHo() + " " + nv.getTen() : "Không tìm thấy";

                model.addRow(new Object[]{
                        hd.getMaNV(),
                        hoTen,
                        sdfDate.format(hd.getNgayKetThuc()),
                        daysBetween + " ngày"
                });
            }
        }

        return styleTable(model);
    }

    private JTable createPendingLeaveTable() {
        String[] columns = {"Mã đơn", "Mã NV", "Họ Tên", "Từ ngày", "Đến ngày", "Số ngày", "Lý do"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        List<NghiPhepDTO> pending = npBus.getList().stream()
                .filter(np -> "Chờ duyệt".equalsIgnoreCase(np.getTrangThai()))
                .sorted(Comparator.comparing(NghiPhepDTO::getTuNgay, Comparator.nullsLast(Date::compareTo)))
                .collect(Collectors.toList());

        for (NghiPhepDTO np : pending) {
            NhanVienDTO nv = nvBus.getById(np.getMaNV());
            String hoTen = nv != null ? nv.getHo() + " " + nv.getTen() : "—";
            model.addRow(new Object[]{
                    np.getMaNP(),
                    np.getMaNV(),
                    hoTen,
                    np.getTuNgay() != null ? sdfDate.format(np.getTuNgay()) : "—",
                    np.getDenNgay() != null ? sdfDate.format(np.getDenNgay()) : "—",
                    np.getSoNgay(),
                    np.getLyDo() != null ? np.getLyDo() : ""
            });
        }

        return styleTable(model);
    }

    private JTable createTodayAttendanceTable() {
        String[] columns = {"Mã NV", "Họ Tên", "Trạng thái", "Giờ vào", "Giờ ra"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        LocalDate today = LocalDate.now();
        List<BangChamCongDTO> todayRows = ccBus.getList().stream()
                .filter(cc -> today.equals(toLocalDate(cc.getNgayLamViec())))
                .sorted(Comparator.comparing(BangChamCongDTO::getMaNV))
                .collect(Collectors.toList());

        for (BangChamCongDTO cc : todayRows) {
            NhanVienDTO nv = nvBus.getById(cc.getMaNV());
            if (nv != null && !"Đang làm".equals(nv.getTrangThai())) continue;
            String hoTen = nv != null ? nv.getHo() + " " + nv.getTen() : "—";
            String tt = cc.getTrangThai() != null ? cc.getTrangThai() : "—";
            model.addRow(new Object[]{
                    cc.getMaNV(),
                    hoTen,
                    tt,
                    formatTime(cc.getGioVao()),
                    formatTime(cc.getGioRa())
            });
        }

        return styleTable(model);
    }

    private JTable styleTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setForeground(TEXT_WHITE);
        table.setBackground(BG_CONTENT);
        table.setSelectionBackground(TABLE_SEL);
        table.setSelectionForeground(TEXT_WHITE);
        table.setRowHeight(32);
        table.setGridColor(new Color(40, 50, 70));
        table.setShowGrid(true);
        table.setFocusable(false);
        table.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 36));
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
                return lbl;
            }
        });

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(row % 2 == 0 ? BG_CONTENT : TABLE_ROW_ALT);
                c.setForeground(TEXT_WHITE);
                ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return c;
            }
        });

        return table;
    }

    public void refreshData() {
        nvBus.loadData();
        pbBus.loadData();
        cvBus.loadData();
        blBus.loadData();
        npBus.loadData();
        hdBus.loadData();
        ccBus.loadData();
        removeAll();
        initUI();
        revalidate();
        repaint();
    }
}
