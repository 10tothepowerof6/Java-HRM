package com.hrm.gui;

import com.hrm.bus.ChucVuBUS;
import com.hrm.dto.ChucVuDTO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;

/**
 * Chức vụ: mã, tên và hệ số lương — tham số nhân cho công thức lương tháng.
 * <p>
 * Hệ số nhân với lương hợp đồng hoặc mức lương cơ sở tùy dữ liệu hợp đồng.
 * </p>
 */
public class ChucVuDialog extends JDialog {

    private static final Color PRIMARY       = new Color(0, 82, 155);
    private static final Color PRIMARY_HOVER = new Color(0, 105, 192);
    private static final Color BG_DIALOG     = new Color(22, 33, 52);
    private static final Color TEXT_WHITE    = new Color(248, 250, 252);
    private static final Color FIELD_BG      = new Color(15, 23, 42);
    private static final Color FIELD_BORDER  = new Color(71, 85, 105);

    private ChucVuBUS chucVuBUS;
    private ChucVuDTO existingCV;
    private boolean saved = false;

    private JTextField txtMaCV;
    private JTextField txtTenCV;
    private JTextField txtHeSoLuong;

    public ChucVuDialog(JFrame parent, String title, ChucVuDTO existing, ChucVuBUS cvBUS) {
        super(parent, title, true);
        this.existingCV = existing;
        this.chucVuBUS = cvBUS;

        initUI();
        if (existing != null) {
            prefillData();
        }
    }

    public boolean isSaved() {
        return saved;
    }

    private void initUI() {
        setSize(400, 250);
        setLocationRelativeTo(getParent());
        setResizable(false);
        getContentPane().setBackground(BG_DIALOG);
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(BG_DIALOG);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        formPanel.add(createFormRow("Mã chức vụ:", txtMaCV = createTextField()));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormRow("Tên chức vụ:", txtTenCV = createTextField()));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormRow("Hệ số lương:", txtHeSoLuong = createTextField()));

        add(formPanel, BorderLayout.CENTER);

        if (existingCV != null) {
            txtMaCV.setEnabled(false); // Cấm sửa mã khi Edit
        }

        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createFormRow(String label, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(TEXT_WHITE);
        lbl.setPreferredSize(new Dimension(100, 30));
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
        txtMaCV.setText(existingCV.getMaCV());
        txtTenCV.setText(existingCV.getTenCV());
        if (existingCV.getHeSoLuong() != null) {
            txtHeSoLuong.setText(existingCV.getHeSoLuong().toString());
        }
    }

    private void performSave() {
        try {
            String maCV = txtMaCV.getText().trim();
            String tenCV = txtTenCV.getText().trim();
            String heSoStr = txtHeSoLuong.getText().trim();

            if (maCV.isEmpty() || tenCV.isEmpty() || heSoStr.isEmpty()) {
                throw new IllegalArgumentException("Vui lòng nhập đầy đủ thông tin!");
            }

            BigDecimal heSoLuong;
            try {
                heSoLuong = new BigDecimal(heSoStr);
                if (heSoLuong.compareTo(BigDecimal.ONE) < 0) {
                    throw new IllegalArgumentException("Hệ số lương phải >= 1.0");
                }
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Hệ số lương phải là số hợp lệ!");
            }

            ChucVuDTO cv = new ChucVuDTO(maCV, tenCV, heSoLuong);

            if (existingCV == null) {
                chucVuBUS.add(cv);
            } else {
                chucVuBUS.update(cv);
            }

            saved = true;
            JOptionPane.showMessageDialog(this,
                    existingCV == null ? "Thêm hệ số lương/chức vụ thành công!" : "Cập nhật thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(), "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi hệ thống: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
