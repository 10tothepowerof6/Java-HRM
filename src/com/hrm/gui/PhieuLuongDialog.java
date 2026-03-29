package com.hrm.gui;

import com.hrm.bus.ChiTietThuongBUS;
import com.hrm.bus.DanhMucThuongBUS;
import com.hrm.bus.DeAnBUS;
import com.hrm.bus.LoaiPhuCapBUS;
import com.hrm.bus.PhanCongDeAnBUS;
import com.hrm.bus.PhuCapBUS;
import com.hrm.dto.BangLuongThangDTO;
import com.hrm.dto.ChiTietThuongDTO;
import com.hrm.dto.DanhMucThuongDTO;
import com.hrm.dto.DeAnDTO;
import com.hrm.dto.LoaiPhuCapDTO;
import com.hrm.dto.PhanCongDeAnDTO;
import com.hrm.dto.PhuCapDTO;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;

/**
 * Phiếu lương: phụ cấp và thưởng hiển thị từng khoản (theo loại / đề án / chi tiết thưởng),
 * sau đó mới có dòng tổng — khớp cách {@link com.hrm.bus.BangLuongThangBUS#generateBangLuong} cộng dồn.
 */
public class PhieuLuongDialog extends JDialog {

    private final BangLuongThangDTO dto;
    private final DecimalFormat df = new DecimalFormat("#,##0 ₫");

    public PhieuLuongDialog(Window parent, BangLuongThangDTO dto) {
        super(parent, "Chi Tiết Phiếu Lương", ModalityType.APPLICATION_MODAL);
        this.dto = dto;
        initUI();
    }

    private void initUI() {
        setSize(500, 680);
        setLocationRelativeTo(getParent());
        setResizable(true);
        getContentPane().setBackground(new Color(255, 255, 255));
        setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(new Color(248, 250, 252));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblCongTy = new JLabel("CÔNG TY CỔ PHẦN ABCXYZ");
        lblCongTy.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblCongTy.setForeground(new Color(15, 23, 42));
        lblCongTy.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitle = new JLabel("PHIẾU LƯƠNG NHÂN VIÊN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(0, 82, 155));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblThang = new JLabel("Tháng " + dto.getThang() + " Năm " + dto.getNam());
        lblThang.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblThang.setForeground(new Color(71, 85, 105));
        lblThang.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(lblCongTy);
        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(lblTitle);
        headerPanel.add(Box.createVerticalStrut(5));
        headerPanel.add(lblThang);

        add(headerPanel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 20, 30));

        contentPanel.add(createRow("Mã Bảng Lương:", dto.getMaBangLuong(), false));
        contentPanel.add(createRow("Mã Nhân Viên:", dto.getMaNV(), false));
        contentPanel.add(createRow("Số ngày công:", String.valueOf(dto.getSoNgayCong()), false));

        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(new JSeparator());
        contentPanel.add(Box.createVerticalStrut(10));

        JLabel lblThuNhap = new JLabel("I. CÁC KHOẢN THU NHẬP");
        lblThuNhap.setFont(new Font("Segoe UI", Font.BOLD, 14));
        contentPanel.add(lblThuNhap);

        contentPanel.add(createRow("Lương cơ bản (HS: " + dto.getHeSoLuong() + "):", df.format(dto.getLuongCoBan()), false));

        contentPanel.add(Box.createVerticalStrut(6));
        JLabel lblPhuCapSub = new JLabel("Phụ cấp (chi tiết từng khoản):");
        lblPhuCapSub.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPhuCapSub.setForeground(new Color(71, 85, 105));
        contentPanel.add(lblPhuCapSub);

        appendPhuCapLines(contentPanel);

        contentPanel.add(Box.createVerticalStrut(6));
        JLabel lblThuongSub = new JLabel("Thưởng (chi tiết từng khoản):");
        lblThuongSub.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblThuongSub.setForeground(new Color(71, 85, 105));
        contentPanel.add(lblThuongSub);

        appendThuongLines(contentPanel);

        BigDecimal tongThuNhap = dto.getLuongCoBan().add(dto.getTongPhuCap()).add(dto.getTongThuong());
        contentPanel.add(Box.createVerticalStrut(8));
        contentPanel.add(createRow("Cộng thu nhập (A):", df.format(tongThuNhap), true));

        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(new JSeparator());
        contentPanel.add(Box.createVerticalStrut(10));

        JLabel lblKhauTru = new JLabel("II. CÁC KHOẢN KHẤU TRỪ");
        lblKhauTru.setFont(new Font("Segoe UI", Font.BOLD, 14));
        contentPanel.add(lblKhauTru);

        contentPanel.add(createRow("Bảo hiểm XH:", df.format(dto.getKhauTruBHXH()), false));
        contentPanel.add(createRow("Bảo hiểm YT:", df.format(dto.getKhauTruBHYT()), false));
        contentPanel.add(createRow("Bảo hiểm TN:", df.format(dto.getKhauTruBHTN()), false));
        contentPanel.add(createRow("Thuế TNCN:", df.format(dto.getThueTNCN()), false));

        BigDecimal tongTru = dto.getKhauTruBHXH().add(dto.getKhauTruBHYT()).add(dto.getKhauTruBHTN()).add(dto.getThueTNCN());
        contentPanel.add(createRow("Cộng khấu trừ (B):", df.format(tongTru), true));

        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(new JSeparator());
        contentPanel.add(Box.createVerticalStrut(10));

        JPanel netPanel = new JPanel(new BorderLayout());
        netPanel.setBackground(new Color(240, 253, 244));
        netPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(74, 222, 128)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel lblNet = new JLabel("THỰC LÃNH (A - B):");
        lblNet.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblNet.setForeground(new Color(22, 101, 52));

        JLabel lblNetVal = new JLabel(df.format(dto.getThucLanh()));
        lblNetVal.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblNetVal.setForeground(new Color(22, 101, 52));

        netPanel.add(lblNet, BorderLayout.WEST);
        netPanel.add(lblNetVal, BorderLayout.EAST);
        contentPanel.add(netPanel);

        add(new JScrollPane(contentPanel), BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(Color.WHITE);
        JButton btnClose = new JButton("Đóng phiếu");
        btnClose.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnClose.addActionListener(e -> dispose());
        footer.add(btnClose);
        add(footer, BorderLayout.SOUTH);
    }

    private void appendPhuCapLines(JPanel contentPanel) {
        PhuCapBUS pcBus = new PhuCapBUS();
        LoaiPhuCapBUS lpcBus = new LoaiPhuCapBUS();
        PhanCongDeAnBUS pcdaBus = new PhanCongDeAnBUS();
        DeAnBUS deAnBus = new DeAnBUS();

        boolean any = false;
        for (PhuCapDTO pc : pcBus.getList()) {
            if (pc.getMaNV() == null || !pc.getMaNV().equalsIgnoreCase(dto.getMaNV())) {
                continue;
            }
            if (pc.getSoTien() == null) {
                continue;
            }
            any = true;
            String tenLoai = resolveTenLoaiPhuCap(lpcBus, pc.getMaLoaiPC());
            contentPanel.add(createRow("  • " + tenLoai + ":", df.format(pc.getSoTien()), false));
        }

        for (PhanCongDeAnDTO pcda : pcdaBus.getList()) {
            if (pcda.getMaNV() == null || !pcda.getMaNV().equalsIgnoreCase(dto.getMaNV())) {
                continue;
            }
            if (pcda.getPhuCapDeAn() == null) {
                continue;
            }
            any = true;
            DeAnDTO da = deAnBus.getById(pcda.getMaDA());
            String tenDa = (da != null && da.getTenDA() != null && !da.getTenDA().isEmpty())
                ? da.getTenDA() + " (" + pcda.getMaDA() + ")"
                : pcda.getMaDA();
            contentPanel.add(createRow("  • Phụ cấp đề án — " + tenDa + ":", df.format(pcda.getPhuCapDeAn()), false));
        }

        if (!any) {
            JLabel hint = new JLabel("  (Không có dòng phụ cấp chi tiết trong kỳ)");
            hint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            hint.setForeground(new Color(148, 163, 184));
            contentPanel.add(hint);
        }

        contentPanel.add(createRow("  Tổng phụ cấp:", df.format(nullSafe(dto.getTongPhuCap())), true));
    }

    private void appendThuongLines(JPanel contentPanel) {
        ChiTietThuongBUS cttBus = new ChiTietThuongBUS();
        DanhMucThuongBUS dmBus = new DanhMucThuongBUS();

        int thang = dto.getThang();
        int nam = dto.getNam();
        boolean any = false;

        for (ChiTietThuongDTO t : cttBus.getList()) {
            if (t.getMaNV() == null || !t.getMaNV().equalsIgnoreCase(dto.getMaNV())) {
                continue;
            }
            if (t.getSoTien() == null || t.getNgayThuong() == null) {
                continue;
            }
            if (!matchesPayrollMonth(t.getNgayThuong(), thang, nam)) {
                continue;
            }
            any = true;
            String tenLoai = resolveTenLoaiThuong(dmBus, t.getMaThuong());
            StringBuilder line = new StringBuilder("  • ").append(tenLoai);
            if (t.getGhiChu() != null && !t.getGhiChu().trim().isEmpty()) {
                line.append(" — ").append(t.getGhiChu().trim());
            }
            contentPanel.add(createRow(line.toString() + ":", df.format(t.getSoTien()), false));
        }

        if (!any) {
            JLabel hint = new JLabel("  (Không có dòng thưởng trong tháng)");
            hint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            hint.setForeground(new Color(148, 163, 184));
            contentPanel.add(hint);
        }

        contentPanel.add(createRow("  Tổng thưởng:", df.format(nullSafe(dto.getTongThuong())), true));
    }

    private static boolean matchesPayrollMonth(Date ngay, int thang, int nam) {
        // Cùng quy ước với BangLuongThangBUS.generateBangLuong (java.util.Date)
        return (ngay.getMonth() + 1) == thang && (ngay.getYear() + 1900) == nam;
    }

    private static String resolveTenLoaiPhuCap(LoaiPhuCapBUS bus, String maLoaiPC) {
        if (maLoaiPC == null || maLoaiPC.trim().isEmpty()) {
            return "Phụ cấp (chưa gán loại)";
        }
        LoaiPhuCapDTO loai = bus.getById(maLoaiPC.trim());
        if (loai != null && loai.getTenLoaiPC() != null && !loai.getTenLoaiPC().isEmpty()) {
            return "Phụ cấp " + loai.getTenLoaiPC();
        }
        return "Phụ cấp (" + maLoaiPC + ")";
    }

    private static String resolveTenLoaiThuong(DanhMucThuongBUS bus, String maThuong) {
        if (maThuong == null || maThuong.trim().isEmpty()) {
            return "Thưởng (chưa gán loại)";
        }
        DanhMucThuongDTO dm = bus.getById(maThuong.trim());
        if (dm != null && dm.getTenLoaiThuong() != null && !dm.getTenLoaiThuong().isEmpty()) {
            return dm.getTenLoaiThuong();
        }
        return "Thưởng (" + maThuong + ")";
    }

    private static BigDecimal nullSafe(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private JPanel createRow(String title, String value, boolean isBold) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JLabel lblT = new JLabel(title);
        lblT.setFont(new Font("Segoe UI", isBold ? Font.BOLD : Font.PLAIN, 13));

        JLabel lblV = new JLabel(value);
        lblV.setFont(new Font("Segoe UI", isBold ? Font.BOLD : Font.PLAIN, 13));

        p.add(lblT, BorderLayout.WEST);
        p.add(lblV, BorderLayout.EAST);
        return p;
    }
}
