package com.hrm.gui;

import com.hrm.bus.NhanVienBUS;
import com.hrm.bus.PhanCongDeAnBUS;
import com.hrm.dto.NhanVienDTO;
import com.hrm.dto.PhanCongDeAnDTO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Gán nhân viên vào đề án: vai trò trong dự án, phụ cấp đề án (cộng vào tổng phụ cấp khi tính lương).
 * <p>
 * Khóa ngoại tới {@link com.hrm.dto.DeAnDTO} và {@link com.hrm.dto.NhanVienDTO}.
 * </p>
 */
public class PhanCongDeAnDialog extends JDialog {

    private static final Color PRIMARY = new Color(0, 82, 155);
    private static final Color PRIMARY_HOVER = new Color(0, 105, 192);
    private static final Color BG_DIALOG = new Color(22, 33, 52);
    private static final Color TEXT_WHITE = new Color(248, 250, 252);
    private static final Color FIELD_BG = new Color(15, 23, 42);
    private static final Color FIELD_BORDER = new Color(71, 85, 105);

    private final PhanCongDeAnBUS phanCongBUS;
    private final NhanVienBUS nhanVienBUS;
    private final PhanCongDeAnDTO existing;
    private final String maDA;
    private boolean saved = false;

    private JComboBox<String> cboNhanVien;
    private JFormattedTextField txtNgayBatDau;
    private JFormattedTextField txtNgayKetThuc;
    private JTextField txtPhuCap;

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    public PhanCongDeAnDialog(JFrame parent, String title, String maDA, PhanCongDeAnDTO existing,
                              PhanCongDeAnBUS phanCongBUS, NhanVienBUS nhanVienBUS) {
        super(parent, title, true);
        this.maDA = maDA;
        this.existing = existing;
        this.phanCongBUS = phanCongBUS;
        this.nhanVienBUS = nhanVienBUS;
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
        setSize(500, 340);
        setLocationRelativeTo(getParent());
        setResizable(false);
        getContentPane().setBackground(BG_DIALOG);
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(BG_DIALOG);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel lblMaDA = new JLabel("Mã đề án: " + maDA);
        lblMaDA.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblMaDA.setForeground(TEXT_WHITE);
        lblMaDA.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(lblMaDA);
        formPanel.add(Box.createVerticalStrut(12));

        formPanel.add(createFormRow("Nhân viên:", cboNhanVien = createNhanVienCombo()));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormRow("Ngày bắt đầu:", txtNgayBatDau = createFormattedDateField()));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormRow("Ngày kết thúc:", txtNgayKetThuc = createFormattedDateField()));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormRow("Phụ cấp đề án:", txtPhuCap = createTextField()));

        add(formPanel, BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createFormRow(String label, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(TEXT_WHITE);
        lbl.setPreferredSize(new Dimension(190, 30));
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

    private JComboBox<String> createNhanVienCombo() {
        JComboBox<String> cbo = new JComboBox<>();
        cbo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbo.setBackground(Color.WHITE);
        cbo.setForeground(Color.BLACK);
        cbo.setPreferredSize(new Dimension(0, 30));
        for (NhanVienDTO nv : nhanVienBUS.getList()) {
            cbo.addItem(nv.getMaNV() + " - " + nv.getHo() + " " + nv.getTen());
        }
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
            public void mouseEntered(MouseEvent e) {
                btnSave.setBackground(PRIMARY_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnSave.setBackground(PRIMARY);
            }
        });
        btnSave.addActionListener(e -> performSave());
        panel.add(btnSave);

        return panel;
    }

    private void prefillData() {
        txtNgayBatDau.setText(existing.getNgayBatDau() != null ? sdf.format(existing.getNgayBatDau()) : "");
        txtNgayKetThuc.setText(existing.getNgayKetThuc() != null ? sdf.format(existing.getNgayKetThuc()) : "");
        txtPhuCap.setText(existing.getPhuCapDeAn() != null ? existing.getPhuCapDeAn().toPlainString() : "");

        String selected = getNhanVienDisplay(existing.getMaNV());
        if (selected != null) {
            cboNhanVien.setSelectedItem(selected);
        }
        cboNhanVien.setEnabled(false);
    }

    private void performSave() {
        try {
            String nvDisplay = (String) cboNhanVien.getSelectedItem();
            if (nvDisplay == null || nvDisplay.trim().isEmpty()) {
                throw new IllegalArgumentException("Vui lòng chọn nhân viên!");
            }
            String maNV = nvDisplay.split(" - ")[0].trim();
            String ngayBatDauStr = txtNgayBatDau.getText().replace("_", "").trim();
            if (ngayBatDauStr.equals("//")) ngayBatDauStr = "";
            if (ngayBatDauStr.isEmpty()) {
                throw new IllegalArgumentException("Ngày bắt đầu không được để trống!");
            }
            Date ngayBatDau = sdf.parse(ngayBatDauStr);

            Date ngayKetThuc = null;
            String ngayKetThucStr = txtNgayKetThuc.getText().replace("_", "").trim();
            if (ngayKetThucStr.equals("//")) ngayKetThucStr = "";
            if (!ngayKetThucStr.isEmpty()) {
                ngayKetThuc = sdf.parse(ngayKetThucStr);
            }

            BigDecimal phuCap = BigDecimal.ZERO;
            String phuCapStr = txtPhuCap.getText().trim();
            if (!phuCapStr.isEmpty()) {
                phuCap = new BigDecimal(phuCapStr);
            }

            PhanCongDeAnDTO dto = new PhanCongDeAnDTO(maNV, maDA, ngayBatDau, ngayKetThuc, phuCap);

            if (existing == null) {
                phanCongBUS.add(dto);
            } else {
                phanCongBUS.update(dto);
            }

            saved = true;
            JOptionPane.showMessageDialog(this,
                    existing == null ? "Thêm phân công thành công!" : "Cập nhật phân công thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "Định dạng ngày không hợp lệ! Vui lòng nhập dd/MM/yyyy.",
                    "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Phụ cấp đề án phải là số hợp lệ!",
                    "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(), "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi hệ thống: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private String getNhanVienDisplay(String maNV) {
        for (NhanVienDTO nv : nhanVienBUS.getList()) {
            if (nv.getMaNV() != null && nv.getMaNV().equalsIgnoreCase(maNV)) {
                return nv.getMaNV() + " - " + nv.getHo() + " " + nv.getTen();
            }
        }
        return null;
    }
}
