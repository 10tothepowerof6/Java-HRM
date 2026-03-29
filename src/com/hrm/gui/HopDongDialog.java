package com.hrm.gui;

import com.hrm.bus.HopDongBUS;
import com.hrm.bus.NhanVienBUS;
import com.hrm.dto.HopDongDTO;
import com.hrm.dto.NhanVienDTO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;

/**
 * Hợp đồng gắn nhân viên: loại hợp đồng, lương, ngày ký và kết thúc, trạng thái hiệu lực.
 * <p>
 * Lương hợp đồng × hệ số chức vụ tạo lương cơ bản trong bảng lương.
 * </p>
 */
public class HopDongDialog extends JDialog {

    private static final Color PRIMARY       = new Color(0, 82, 155);
    private static final Color PRIMARY_HOVER = new Color(0, 105, 192);
    private static final Color BG_DIALOG     = new Color(22, 33, 52);
    private static final Color TEXT_WHITE    = new Color(248, 250, 252);
    private static final Color FIELD_BG      = new Color(15, 23, 42);
    private static final Color FIELD_BORDER  = new Color(71, 85, 105);

    private HopDongBUS hopDongBUS;
    private NhanVienBUS nhanVienBUS;
    
    private HopDongDTO existingDTO;
    private boolean saved = false;

    private JTextField txtMaHD;
    private JComboBox<String> cboNhanVien;
    private JComboBox<String> cboLoaiHD;
    private JFormattedTextField txtNgayBatDau;
    private JFormattedTextField txtNgayKetThuc;
    private JTextField txtLuongHopDong;
    private JComboBox<String> cboTrangThai;
    private JTextField txtGhiChu;

    private final SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");

    public HopDongDialog(JFrame parent, String title, HopDongDTO existing, HopDongBUS hdBUS, NhanVienBUS nvBUS) {
        super(parent, title, true);
        this.existingDTO = existing;
        this.hopDongBUS = hdBUS;
        this.nhanVienBUS = nvBUS;
        
        sdfDate.setLenient(false);

        initUI();
        if (existing != null) {
            prefillData();
        }
    }

    public boolean isSaved() {
        return saved;
    }

    private void initUI() {
        setSize(500, 520);
        setLocationRelativeTo(getParent());
        setResizable(false);
        getContentPane().setBackground(BG_DIALOG);
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(BG_DIALOG);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        formPanel.add(createFormRow("Mã Hợp đồng:", txtMaHD = createTextField()));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormRow("Nhân viên:", cboNhanVien = createNhanVienCombo()));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormRow("Loại HĐ:", cboLoaiHD = createLoaiHDCombo()));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormRow("Ngày bắt đầu:", txtNgayBatDau = createFormattedDateField()));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormRow("Ngày kết thúc:", txtNgayKetThuc = createFormattedDateField(), "(Trống nếu Vô thời hạn)"));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormRow("Mức lương:", txtLuongHopDong = createTextField(), "(Chỉ nhập số lẻ, VD: 15000000)"));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormRow("Trạng thái:", cboTrangThai = createStatusCombo()));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormRow("Ghi chú:", txtGhiChu = createTextField()));

        add(formPanel, BorderLayout.CENTER);

        if (existingDTO != null) {
            txtMaHD.setEnabled(false); // Cấm sửa mã HĐ
        }

        add(createButtonPanel(), BorderLayout.SOUTH);
        
        // Listener toggle Ngay Ket Thuc based on LoaiHD
        cboLoaiHD.addActionListener(e -> {
            String loai = (String) cboLoaiHD.getSelectedItem();
            if ("Vô thời hạn".equals(loai)) {
                txtNgayKetThuc.setText("");
                txtNgayKetThuc.setEnabled(false);
                txtNgayKetThuc.setBackground(new Color(40, 50, 70));
            } else {
                txtNgayKetThuc.setEnabled(true);
                txtNgayKetThuc.setBackground(FIELD_BG);
            }
        });
    }

    private JPanel createFormRow(String label, JComponent field) {
        return createFormRow(label, field, "");
    }
    
    private JPanel createFormRow(String label, JComponent field, String hint) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(TEXT_WHITE);
        lbl.setPreferredSize(new Dimension(100, 30));
        row.add(lbl, BorderLayout.WEST);

        JPanel fieldPanel = new JPanel(new BorderLayout(5, 0));
        fieldPanel.setOpaque(false);
        fieldPanel.add(field, BorderLayout.CENTER);
        
        if (!hint.isEmpty()) {
            JLabel lblHint = new JLabel(hint);
            lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            lblHint.setForeground(new Color(156, 163, 175));
            lblHint.setPreferredSize(new Dimension(85, 30)); // Đủ rộng để hiển thị chữ
            fieldPanel.add(lblHint, BorderLayout.EAST);
        }

        row.add(fieldPanel, BorderLayout.CENTER);
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

    private JComboBox<String> createNhanVienCombo() {
        JComboBox<String> cbo = new JComboBox<>();
        cbo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbo.setBackground(Color.WHITE);
        cbo.setForeground(Color.BLACK);
        cbo.setPreferredSize(new Dimension(0, 30));

        ArrayList<NhanVienDTO> listNV = nhanVienBUS.getList();
        for (NhanVienDTO nv : listNV) {
            cbo.addItem(nv.getMaNV() + " - " + nv.getHo() + " " + nv.getTen());
        }
        return cbo;
    }

    private JComboBox<String> createLoaiHDCombo() {
        JComboBox<String> cbo = new JComboBox<>();
        cbo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbo.setBackground(Color.WHITE);
        cbo.setForeground(Color.BLACK);
        cbo.setPreferredSize(new Dimension(0, 30));
        cbo.addItem("Thử việc");
        cbo.addItem("1 năm");
        cbo.addItem("3 năm");
        cbo.addItem("Vô thời hạn");
        return cbo;
    }

    private JComboBox<String> createStatusCombo() {
        JComboBox<String> cbo = new JComboBox<>();
        cbo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbo.setBackground(Color.WHITE);
        cbo.setForeground(Color.BLACK);
        cbo.setPreferredSize(new Dimension(0, 30));
        cbo.addItem("Có hiệu lực");
        cbo.addItem("Đã hết hạn");
        cbo.addItem("Chấm dứt");
        return cbo;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panel.setBackground(BG_DIALOG);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(40, 50, 70)));

        JButton btnCancel = new JButton("Hủy");
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnCancel.setForeground(TEXT_WHITE);
        btnCancel.setBackground(new Color(55, 65, 81));
        btnCancel.setPreferredSize(new Dimension(80, 32));
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
        btnSave.setPreferredSize(new Dimension(80, 32));
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

    private void prefillData() {
        txtMaHD.setText(existingDTO.getMaHD());
        
        if (existingDTO.getMaNV() != null) {
            NhanVienDTO nv = nhanVienBUS.getById(existingDTO.getMaNV());
            if (nv != null) {
                String searchStr = nv.getMaNV() + " - " + nv.getHo() + " " + nv.getTen();
                cboNhanVien.setSelectedItem(searchStr);
            }
        }

        if (existingDTO.getLoaiHD() != null) {
            cboLoaiHD.setSelectedItem(existingDTO.getLoaiHD());
        }

        if (existingDTO.getNgayBatDau() != null) {
            txtNgayBatDau.setText(sdfDate.format(existingDTO.getNgayBatDau()));
        }

        if (existingDTO.getNgayKetThuc() != null) {
            txtNgayKetThuc.setText(sdfDate.format(existingDTO.getNgayKetThuc()));
        }

        if (existingDTO.getLuongHopDong() != null) {
            // Chuyển kiểu BigDecimal ra string số thường (vd: 15000000)
            txtLuongHopDong.setText(existingDTO.getLuongHopDong().toPlainString());
        }

        if (existingDTO.getTrangThai() != null) {
            cboTrangThai.setSelectedItem(existingDTO.getTrangThai());
        }
        
        if (existingDTO.getGhiChu() != null) {
            txtGhiChu.setText(existingDTO.getGhiChu());
        }
    }

    private void performSave() {
        try {
            String maHD = txtMaHD.getText().trim();
            
            String selectedNV = (String) cboNhanVien.getSelectedItem();
            String maNV = selectedNV != null ? selectedNV.split(" - ")[0].trim() : "";
            
            String loaiHD = (String) cboLoaiHD.getSelectedItem();
            String strNgayBatDau = txtNgayBatDau.getText().replace("_", "").trim();
            if (strNgayBatDau.equals("//")) strNgayBatDau = "";
            String strNgayKetThuc = txtNgayKetThuc.getText().replace("_", "").trim();
            if (strNgayKetThuc.equals("//")) strNgayKetThuc = "";
            String strLuong = txtLuongHopDong.getText().trim();
            String trangThai = (String) cboTrangThai.getSelectedItem();
            String ghiChu = txtGhiChu.getText().trim();

            if (maHD.isEmpty() || maNV.isEmpty() || loaiHD.isEmpty() || strNgayBatDau.isEmpty() || strLuong.isEmpty()) {
                throw new IllegalArgumentException("Vui lòng điền đầy đủ các thông tin bắt buộc!");
            }

            // Xử lý Ngày
            Date ngayBatDau = null;
            Date ngayKetThuc = null;
            try {
                ngayBatDau = sdfDate.parse(strNgayBatDau);
                if (!strNgayKetThuc.isEmpty() && !"Vô thời hạn".equals(loaiHD)) {
                    ngayKetThuc = sdfDate.parse(strNgayKetThuc);
                    if (ngayKetThuc.before(ngayBatDau)) {
                        throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu!");
                    }
                }
            } catch (ParseException ex) {
                throw new IllegalArgumentException("Định dạng Ngày không đúng! Dùng dd/MM/yyyy");
            }
            
            // Xử lý Mức lương BigDecimal
            BigDecimal luong = BigDecimal.ZERO;
            try {
                luong = new BigDecimal(strLuong);
                if (luong.signum() < 0) {
                    throw new IllegalArgumentException("Mức lương không được là số âm!");
                }
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Mức lương phải là một số hợp lệ (ví dụ: 15000000)");
            }

            HopDongDTO dto = new HopDongDTO(maHD, maNV, loaiHD, ngayBatDau, ngayKetThuc, luong, trangThai, ghiChu);

            if (existingDTO == null) {
                hopDongBUS.add(dto);
            } else {
                hopDongBUS.update(dto);
            }

            saved = true;
            JOptionPane.showMessageDialog(this,
                    existingDTO == null ? "Tạo hợp đồng thành công!" : "Cập nhật thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(), "Thông báo", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi hệ thống: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
