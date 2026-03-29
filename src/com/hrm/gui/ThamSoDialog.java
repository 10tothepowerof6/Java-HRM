package com.hrm.gui;

import com.hrm.bus.ThamSoBUS;
import com.hrm.dto.ThamSoDTO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;

/**
 * Form thêm/sửa một bản ghi tham số ({@link com.hrm.dto.ThamSoDTO}): mã, tên, giá trị (số), mô tả.
 * <p>
 * Chế độ sửa khóa mã tham số; {@link #isSaved()} phản hồi người gọi sau khi lưu thành công.
 * </p>
 */
public class ThamSoDialog extends JDialog {

    private static final Color PRIMARY       = new Color(0, 82, 155);
    private static final Color PRIMARY_HOVER = new Color(0, 105, 192);
    private static final Color BG_DIALOG     = new Color(22, 33, 52);
    private static final Color TEXT_WHITE    = new Color(248, 250, 252);
    private static final Color FIELD_BG      = new Color(15, 23, 42);
    private static final Color FIELD_BORDER  = new Color(71, 85, 105);

    private ThamSoBUS thamSoBUS;
    private ThamSoDTO existingDTO;
    private boolean saved = false;

    private JTextField txtMaThamSo;
    private JTextField txtTenThamSo;
    private JTextField txtGiaTri;
    private JTextArea txtMoTa;

    public ThamSoDialog(Window parent, String title, ThamSoDTO existing, ThamSoBUS tsBUS) {
        super(parent, title, ModalityType.APPLICATION_MODAL);
        this.existingDTO = existing;
        this.thamSoBUS = tsBUS;
        
        initUI();
        if (existing != null) {
            prefillData();
        }
    }

    public boolean isSaved() {
        return saved;
    }

    private void initUI() {
        setSize(420, 360);
        setLocationRelativeTo(getParent());
        setResizable(false);
        getContentPane().setBackground(BG_DIALOG);
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(BG_DIALOG);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        formPanel.add(createFormRow("Mã cấu hình:", txtMaThamSo = createTextField()));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormRow("Tên thuộc tính:", txtTenThamSo = createTextField()));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormRow("Giá trị mới:", txtGiaTri = createTextField()));
        formPanel.add(Box.createVerticalStrut(10));

        txtMoTa = new JTextArea(3, 20);
        txtMoTa.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtMoTa.setForeground(TEXT_WHITE);
        txtMoTa.setBackground(FIELD_BG);
        txtMoTa.setCaretColor(TEXT_WHITE);
        txtMoTa.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BORDER),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        txtMoTa.setLineWrap(true);
        txtMoTa.setWrapStyleWord(true);
        txtMoTa.setEnabled(false); // Không cho sửa mô tả hệ thống gốc
        JScrollPane scrollMoTa = new JScrollPane(txtMoTa);
        formPanel.add(createFormRow("Mô tả:", scrollMoTa));

        add(formPanel, BorderLayout.CENTER);

        // Lock primary constraints
        txtMaThamSo.setEnabled(false);
        txtTenThamSo.setEnabled(false);

        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createFormRow(String label, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, field instanceof JScrollPane ? 60 : 30));

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
        txtMaThamSo.setText(existingDTO.getMaThamSo());
        txtTenThamSo.setText(existingDTO.getTenThamSo());
        // Hiển thị giá trị nguyên gốc
        txtGiaTri.setText(existingDTO.getGiaTri().toPlainString());
        txtMoTa.setText(existingDTO.getMoTa());
    }

    private void performSave() {
        try {
            String strGiaTri = txtGiaTri.getText().trim();
            if (strGiaTri.isEmpty()) {
                throw new IllegalArgumentException("Giá trị không được rỗng!");
            }
            BigDecimal giaTri = new BigDecimal(strGiaTri);

            existingDTO.setGiaTri(giaTri);

            if (thamSoBUS.update(existingDTO)) {
                saved = true;
                JOptionPane.showMessageDialog(this, "Đã cập nhật cấu hình hệ thống!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                throw new Exception("Quá trình cập nhật Database thất bại!");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Giá trị phải là một chữ số (VD: 8.5 hoặc 2340000)", "Lỗi Validation", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
