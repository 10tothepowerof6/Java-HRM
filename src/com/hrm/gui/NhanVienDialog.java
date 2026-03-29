package com.hrm.gui;

import com.hrm.bus.NhanVienBUS;
import com.hrm.bus.ChiTietNhanVienBUS;
import com.hrm.bus.PhongBanBUS;
import com.hrm.bus.ChucVuBUS;
import com.hrm.dto.NhanVienDTO;
import com.hrm.dto.ChiTietNhanVienDTO;
import com.hrm.dto.PhongBanDTO;
import com.hrm.dto.ChucVuDTO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Dialog thêm/sửa nhân viên.
 * <ul>
 *   <li>Thêm: tham số {@code nhanVienDTO == null}</li>
 *   <li>Sửa: truyền DTO hiện có; có thể chỉnh chi tiết nhân viên trong cùng hộp thoại</li>
 * </ul>
 */
public class NhanVienDialog extends JDialog {

    // ===== BẢNG MÀU =====
    private static final Color PRIMARY       = new Color(0, 82, 155);
    private static final Color PRIMARY_HOVER = new Color(0, 105, 192);
    private static final Color BG_DIALOG     = new Color(22, 33, 52);
    private static final Color BG_SECTION    = new Color(30, 41, 59);
    private static final Color TEXT_WHITE    = new Color(248, 250, 252);
    private static final Color TEXT_MUTED    = new Color(148, 163, 184);
    private static final Color FIELD_BG      = new Color(15, 23, 42);
    private static final Color FIELD_BORDER  = new Color(71, 85, 105);

    // ===== BUS =====
    private NhanVienBUS nhanVienBUS;
    private ChiTietNhanVienBUS chiTietBUS;
    private PhongBanBUS phongBanBUS;
    private ChucVuBUS chucVuBUS;

    // ===== DATA =====
    private NhanVienDTO existingNV; // null = ADD, not null = EDIT
    private boolean saved = false;

    // ===== FORM FIELDS — Section 1: Thông tin cơ bản =====
    private JTextField txtMaNV;
    private JTextField txtHo;
    private JTextField txtTen;
    private JComboBox<String> cboGioiTinh;
    private JFormattedTextField txtNgaySinh;
    private JFormattedTextField txtNgayBatDau;
    private JComboBox<String> cboPhongBan;
    private JComboBox<String> cboChucVu;
    private JComboBox<String> cboTrangThai;
    private JCheckBox chkTruongPhong;

    // ===== FORM FIELDS — Section 2: Thông tin liên hệ =====
    private JTextField txtCCCD;
    private JTextField txtDiaChi;
    private JTextField txtSDT;
    private JTextField txtEmail;

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    public NhanVienDialog(JFrame parent, String title, NhanVienDTO existing,
                          NhanVienBUS nvBUS, ChiTietNhanVienBUS ctBUS,
                          PhongBanBUS pbBUS, ChucVuBUS cvBUS) {
        super(parent, title, true); // Modal
        this.existingNV = existing;
        this.nhanVienBUS = nvBUS;
        this.chiTietBUS = ctBUS;
        this.phongBanBUS = pbBUS;
        this.chucVuBUS = cvBUS;

        sdf.setLenient(false);
        initUI();
        if (existing != null) {
            prefillData();
        }
    }

    public boolean isSaved() {
        return saved;
    }

    private void initUI() {
        setSize(540, 620);
        setLocationRelativeTo(getParent());
        setResizable(false);
        getContentPane().setBackground(BG_DIALOG);
        setLayout(new BorderLayout());

        // Main content panel with scroll
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(BG_DIALOG);
        formPanel.setBorder(BorderFactory.createEmptyBorder(16, 20, 10, 20));

        // Section 1
        formPanel.add(createSectionLabel("THÔNG TIN CƠ BẢN"));
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(createFormRow("Mã nhân viên:", txtMaNV = createTextField()));
        formPanel.add(createFormRow("Họ:", txtHo = createTextField()));
        formPanel.add(createFormRow("Tên:", txtTen = createTextField()));
        formPanel.add(createFormRow("Giới tính:", cboGioiTinh = createCombo(new String[]{"Nam", "Nữ"})));
        formPanel.add(createFormRow("Ngày sinh:", txtNgaySinh = createFormattedDateField()));
        formPanel.add(createFormRow("Ngày bắt đầu:", txtNgayBatDau = createFormattedDateField()));
        formPanel.add(createFormRow("Phòng ban:", cboPhongBan = createPhongBanCombo()));
        formPanel.add(createFormRow("Chức vụ:", cboChucVu = createChucVuCombo()));
        formPanel.add(createFormRow("Trạng thái:", cboTrangThai = createCombo(new String[]{"Đang làm", "Đã nghỉ"})));

        // Checkbox gán trưởng phòng
        chkTruongPhong = new JCheckBox("Gán làm trưởng phòng của phòng ban đã chọn");
        chkTruongPhong.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkTruongPhong.setForeground(TEXT_WHITE);
        chkTruongPhong.setOpaque(false);
        chkTruongPhong.setAlignmentX(Component.LEFT_ALIGNMENT);
        chkTruongPhong.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        formPanel.add(Box.createVerticalStrut(4));
        formPanel.add(chkTruongPhong);

        formPanel.add(Box.createVerticalStrut(12));

        // Section 2
        formPanel.add(createSectionLabel("THÔNG TIN LIÊN HỆ"));
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(createFormRow("CCCD:", txtCCCD = createTextField()));
        formPanel.add(createFormRow("Địa chỉ:", txtDiaChi = createTextField()));
        formPanel.add(createFormRow("Số điện thoại:", txtSDT = createTextField()));
        formPanel.add(createFormRow("Email:", txtEmail = createTextField()));

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BG_DIALOG);
        add(scrollPane, BorderLayout.CENTER);

        // Nếu mode EDIT → disable mã NV
        if (existingNV != null) {
            txtMaNV.setEnabled(false);
        }

        // Buttons
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    // =========================================================================
    //  FORM HELPERS
    // =========================================================================
    private JLabel createSectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(TEXT_MUTED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        return lbl;
    }

    private JPanel createFormRow(String label, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        row.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(TEXT_WHITE);
        lbl.setPreferredSize(new Dimension(180, 30));
        row.add(lbl, BorderLayout.WEST);

        row.add(field, BorderLayout.CENTER);
        return row;
    }

    private JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setForeground(TEXT_WHITE);
        tf.setBackground(FIELD_BG);
        tf.setCaretColor(TEXT_WHITE);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BORDER),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        tf.setPreferredSize(new Dimension(0, 30));
        return tf;
    }

    private JFormattedTextField createFormattedDateField() {
        try {
            javax.swing.text.MaskFormatter dateMask = new javax.swing.text.MaskFormatter("##/##/####");
            dateMask.setPlaceholderCharacter('_');
            JFormattedTextField tf = new JFormattedTextField(dateMask);
            tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            tf.setForeground(TEXT_WHITE);
            tf.setBackground(FIELD_BG);
            tf.setCaretColor(TEXT_WHITE);
            tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(FIELD_BORDER),
                    BorderFactory.createEmptyBorder(4, 8, 4, 8)
            ));
            tf.setPreferredSize(new Dimension(0, 30));
            return tf;
        } catch (Exception e) {
            return new JFormattedTextField();
        }
    }

    private JComboBox<String> createCombo(String[] items) {
        JComboBox<String> cbo = new JComboBox<>(items);
        cbo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbo.setBackground(Color.WHITE);
        cbo.setForeground(Color.BLACK);
        cbo.setPreferredSize(new Dimension(0, 30));
        return cbo;
    }

    private JComboBox<String> createPhongBanCombo() {
        JComboBox<String> cbo = new JComboBox<>();
        cbo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbo.setBackground(Color.WHITE);
        cbo.setForeground(Color.BLACK);
        cbo.setPreferredSize(new Dimension(0, 30));
        for (PhongBanDTO pb : phongBanBUS.getList()) {
            cbo.addItem(pb.getTenPB());
        }
        return cbo;
    }

    private JComboBox<String> createChucVuCombo() {
        JComboBox<String> cbo = new JComboBox<>();
        cbo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbo.setBackground(Color.WHITE);
        cbo.setForeground(Color.BLACK);
        cbo.setPreferredSize(new Dimension(0, 30));
        for (ChucVuDTO cv : chucVuBUS.getList()) {
            cbo.addItem(cv.getTenCV());
        }
        return cbo;
    }

    // =========================================================================
    //  BUTTON PANEL
    // =========================================================================
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panel.setBackground(BG_DIALOG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(40, 50, 70)),
                BorderFactory.createEmptyBorder(5, 20, 5, 20)
        ));

        JButton btnCancel = new JButton("Hủy");
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnCancel.setForeground(TEXT_WHITE);
        btnCancel.setBackground(new Color(55, 65, 81));
        btnCancel.setPreferredSize(new Dimension(90, 34));
        btnCancel.setFocusPainted(false);
        btnCancel.setContentAreaFilled(false);
        btnCancel.setOpaque(true);
        btnCancel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> dispose());
        panel.add(btnCancel);

        JButton btnSave = new JButton("Lưu");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSave.setForeground(TEXT_WHITE);
        btnSave.setBackground(PRIMARY);
        btnSave.setPreferredSize(new Dimension(90, 34));
        btnSave.setFocusPainted(false);
        btnSave.setContentAreaFilled(false);
        btnSave.setOpaque(true);
        btnSave.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnSave.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { btnSave.setBackground(PRIMARY_HOVER); }
            @Override
            public void mouseExited(MouseEvent e) { btnSave.setBackground(PRIMARY); }
        });

        btnSave.addActionListener(e -> performSave());
        panel.add(btnSave);

        return panel;
    }

    // =========================================================================
    //  PREFILL (EDIT MODE)
    // =========================================================================
    private void prefillData() {
        txtMaNV.setText(existingNV.getMaNV());
        txtHo.setText(existingNV.getHo());
        txtTen.setText(existingNV.getTen());
        cboGioiTinh.setSelectedItem(existingNV.getGioiTinh());

        if (existingNV.getNgaySinh() != null)
            txtNgaySinh.setText(sdf.format(existingNV.getNgaySinh()));
        if (existingNV.getNgayBatDau() != null)
            txtNgayBatDau.setText(sdf.format(existingNV.getNgayBatDau()));

        // Chọn phòng ban theo maPB
        PhongBanDTO pb = phongBanBUS.getById(existingNV.getMaPB());
        if (pb != null) {
            cboPhongBan.setSelectedItem(pb.getTenPB());
            // Tự tick checkbox nếu nhân viên này đang là trưởng phòng
            if (existingNV.getMaNV().equals(pb.getMaTruongPB())) {
                chkTruongPhong.setSelected(true);
            }
        }

        // Chọn chức vụ theo maCV
        ChucVuDTO cv = chucVuBUS.getById(existingNV.getMaCV());
        if (cv != null) cboChucVu.setSelectedItem(cv.getTenCV());

        cboTrangThai.setSelectedItem(existingNV.getTrangThai());

        // Chi tiết nhân viên
        ChiTietNhanVienDTO ct = chiTietBUS.getById(existingNV.getMaNV());
        if (ct != null) {
            txtCCCD.setText(ct.getCccd());
            txtDiaChi.setText(ct.getDiaChi());
            txtSDT.setText(ct.getSdt());
            txtEmail.setText(ct.getEmail());
        }
    }

    // =========================================================================
    //  SAVE
    // =========================================================================
    private void performSave() {
        try {
            // === Thu thập dữ liệu NhanVien ===
            String maNV = txtMaNV.getText().trim();
            String ho = txtHo.getText().trim();
            String ten = txtTen.getText().trim();
            String gioiTinh = (String) cboGioiTinh.getSelectedItem();
            String ngaySinhStr = txtNgaySinh.getText().replace("_", "").trim();
            if (ngaySinhStr.equals("//")) ngaySinhStr = "";
            String ngayBatDauStr = txtNgayBatDau.getText().replace("_", "").trim();
            if (ngayBatDauStr.equals("//")) ngayBatDauStr = "";
            String trangThai = (String) cboTrangThai.getSelectedItem();

            // Parse ngày
            Date ngaySinh = null;
            Date ngayBatDau = null;
            try {
                if (!ngaySinhStr.isEmpty()) ngaySinh = sdf.parse(ngaySinhStr);
                if (!ngayBatDauStr.isEmpty()) ngayBatDau = sdf.parse(ngayBatDauStr);
            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(this,
                        "Định dạng ngày không hợp lệ! Vui lòng nhập dd/MM/yyyy",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Lookup mã phòng ban
            String selectedPBName = (String) cboPhongBan.getSelectedItem();
            String maPB = getMaPBByTen(selectedPBName);

            // Lookup mã chức vụ
            String selectedCVName = (String) cboChucVu.getSelectedItem();
            String maCV = getMaCVByTen(selectedCVName);

            NhanVienDTO nv = new NhanVienDTO(maNV, ho, ten, gioiTinh,
                    ngaySinh, ngayBatDau, trangThai, maPB, maCV);

            // === Thu thập dữ liệu ChiTiet ===
            String cccd = txtCCCD.getText().trim();
            String diaChi = txtDiaChi.getText().trim();
            String sdt = txtSDT.getText().trim();
            String email = txtEmail.getText().trim();

            ChiTietNhanVienDTO ct = new ChiTietNhanVienDTO(maNV, cccd, diaChi, sdt, email);

            // === KIỂM TRA OVERWRITE TRƯỞNG PHÒNG TRƯỚC KHI LƯU ===
            if (chkTruongPhong.isSelected() && maPB != null) {
                PhongBanDTO pbToUpdate = phongBanBUS.getById(maPB);
                if (pbToUpdate != null) {
                    String currentTruongPho = pbToUpdate.getMaTruongPB();
                    if (currentTruongPho != null && !currentTruongPho.trim().isEmpty() && !currentTruongPho.equals(maNV)) {
                        NhanVienDTO currentTP = nhanVienBUS.getById(currentTruongPho);
                        String tenCurrent = currentTP != null ? currentTP.getHo() + " " + currentTP.getTen() : "Không xác định";
                        
                        int choice = JOptionPane.showConfirmDialog(this,
                                "Phòng ban [" + pbToUpdate.getTenPB() + "] hiện đã có trưởng phòng là:\n" + 
                                currentTruongPho + " - " + tenCurrent + "\n\n" +
                                "Bạn có chắc chắn muốn thay thế bằng nhân viên này (" + ho + " " + ten + ") không?",
                                "Xác nhận ghi đè Trưởng phòng",
                                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                        
                        if (choice != JOptionPane.YES_OPTION) {
                            return; // Hủy toàn bộ quá trình lưu
                        }
                    }
                }
            }

            // === Gọi BUS ===
            if (existingNV == null) {
                // ADD mode
                nhanVienBUS.add(nv);
                chiTietBUS.add(ct);
            } else {
                // EDIT mode
                nhanVienBUS.update(nv);
                // Chi tiết có thể chưa tồn tại (nếu trước đó chưa thêm)
                ChiTietNhanVienDTO existingCT = chiTietBUS.getById(maNV);
                if (existingCT != null) {
                    chiTietBUS.update(ct);
                } else {
                    chiTietBUS.add(ct);
                }
            }

            // === Auto-sync Trưởng phòng (dùng mã ID, KHÔNG so sánh tiếng Việt) ===
            if (chkTruongPhong.isSelected() && maPB != null) {
                PhongBanDTO pbToUpdate = phongBanBUS.getById(maPB);
                if (pbToUpdate != null) {
                    pbToUpdate.setMaTruongPB(maNV);
                    phongBanBUS.update(pbToUpdate);
                }
            } else if (!chkTruongPhong.isSelected() && maPB != null) {
                // Nếu bỏ tick → xóa trưởng phòng khỏi phòng ban (chỉ khi người này đang là TP)
                PhongBanDTO pbCheck = phongBanBUS.getById(maPB);
                if (pbCheck != null && maNV.equals(pbCheck.getMaTruongPB())) {
                    pbCheck.setMaTruongPB(null);
                    phongBanBUS.update(pbCheck);
                }
            }

            saved = true;
            JOptionPane.showMessageDialog(this,
                    existingNV == null ? "Thêm nhân viên thành công!" : "Cập nhật thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(), "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // =========================================================================
    //  HELPERS
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
}
