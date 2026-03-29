package com.hrm.gui;

import com.hrm.bus.BangChamCongBUS;
import com.hrm.bus.NhanVienBUS;
import com.hrm.dto.BangChamCongDTO;
import com.hrm.dto.NhanVienDTO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;

/**
 * Một bản ghi chấm công: mã, nhân viên, ngày làm việc, giờ vào/ra và trạng thái ca.
 * <p>
 * BUS kiểm tra giờ ra không trước giờ vào.
 * </p>
 */
public class ChamCongDialog extends JDialog {

    private static final Color PRIMARY       = new Color(0, 82, 155);
    private static final Color PRIMARY_HOVER = new Color(0, 105, 192);
    private static final Color BG_DIALOG     = new Color(22, 33, 52);
    private static final Color TEXT_WHITE    = new Color(248, 250, 252);
    private static final Color FIELD_BG      = new Color(15, 23, 42);
    private static final Color FIELD_BORDER  = new Color(71, 85, 105);

    private BangChamCongBUS chamCongBUS;
    private NhanVienBUS nhanVienBUS;
    private BangChamCongDTO existingDTO;
    private boolean saved = false;

    private JTextField txtMaCC;
    private JComboBox<String> cboNhanVien;
    private JFormattedTextField txtNgayLV;
    private JFormattedTextField txtGioVao;
    private JFormattedTextField txtGioRa;
    private JComboBox<String> cboTrangThai;

    private final SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");

    public ChamCongDialog(JFrame parent, String title, BangChamCongDTO existing, BangChamCongBUS ccBUS, NhanVienBUS nvBUS) {
        super(parent, title, true);
        this.existingDTO = existing;
        this.chamCongBUS = ccBUS;
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
        setSize(460, 420);
        setLocationRelativeTo(getParent());
        setResizable(false);
        getContentPane().setBackground(BG_DIALOG);
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(BG_DIALOG);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        formPanel.add(createFormRow("Mã chấm công:", txtMaCC = createTextField()));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormRow("Nhân viên:", cboNhanVien = createNhanVienCombo()));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormRow("Ngày làm việc:", txtNgayLV = createFormattedDateField()));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormRow("Giờ vào:", txtGioVao = createFormattedTimeField()));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormRow("Giờ ra:", txtGioRa = createFormattedTimeField()));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormRow("Trạng thái:", cboTrangThai = createStatusCombo()));

        add(formPanel, BorderLayout.CENTER);

        if (existingDTO != null) {
            txtMaCC.setEnabled(false); // Cấm sửa khóa chính lúc edit
        }

        add(createButtonPanel(), BorderLayout.SOUTH);
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
            lblHint.setPreferredSize(new Dimension(75, 30));
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

    private JFormattedTextField createFormattedTimeField() {
        try {
            javax.swing.text.MaskFormatter timeMask = new javax.swing.text.MaskFormatter("##:##:##");
            timeMask.setPlaceholderCharacter('_');
            JFormattedTextField tf = new JFormattedTextField(timeMask);
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

    private JComboBox<String> createStatusCombo() {
        JComboBox<String> cbo = new JComboBox<>();
        cbo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbo.setBackground(Color.WHITE);
        cbo.setForeground(Color.BLACK);
        cbo.setPreferredSize(new Dimension(0, 30));
        cbo.addItem("Đi làm");
        cbo.addItem("Ốm");
        cbo.addItem("Phép");
        cbo.addItem("Không lương");
        cbo.addItem("Thai sản");
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
        txtMaCC.setText(existingDTO.getMaChamCong());
        
        if (existingDTO.getMaNV() != null) {
            NhanVienDTO nv = nhanVienBUS.getById(existingDTO.getMaNV());
            if (nv != null) {
                String searchStr = nv.getMaNV() + " - " + nv.getHo() + " " + nv.getTen();
                cboNhanVien.setSelectedItem(searchStr);
            }
        }

        if (existingDTO.getNgayLamViec() != null) {
            txtNgayLV.setText(sdfDate.format(existingDTO.getNgayLamViec()));
        }

        if (existingDTO.getGioVao() != null) {
            txtGioVao.setText(existingDTO.getGioVao().toString()); // returns HH:mm:ss
        }

        if (existingDTO.getGioRa() != null) {
            txtGioRa.setText(existingDTO.getGioRa().toString());
        }

        if (existingDTO.getTrangThai() != null) {
            cboTrangThai.setSelectedItem(existingDTO.getTrangThai());
        }
    }

    private void performSave() {
        try {
            String maCC = txtMaCC.getText().trim();
            String selectedNV = (String) cboNhanVien.getSelectedItem();
            String maNV = selectedNV != null ? selectedNV.split(" - ")[0].trim() : "";
            
            String strNgay = txtNgayLV.getText().replace("_", "").trim();
            if (strNgay.equals("//")) strNgay = "";
            String strGioVao = txtGioVao.getText().replace("_", "").trim();
            if (strGioVao.equals("::")) strGioVao = "";
            String strGioRa = txtGioRa.getText().replace("_", "").trim();
            if (strGioRa.equals("::")) strGioRa = "";
            String trangThai = (String) cboTrangThai.getSelectedItem();

            if (maCC.isEmpty() || maNV.isEmpty() || strNgay.isEmpty()) {
                throw new IllegalArgumentException("Mã CC, Nhân viên và Ngày làm việc không được rỗng!");
            }

            // Xử lý Ngày
            Date ngayLamViec = null;
            try {
                ngayLamViec = sdfDate.parse(strNgay);
            } catch (ParseException ex) {
                throw new IllegalArgumentException("Định dạng Ngày làm việc không đúng! Dùng dd/MM/yyyy");
            }

            // Xử lý Giờ
            Time gioVao = null;
            Time gioRa = null;
            try {
                if (!strGioVao.isEmpty()) gioVao = Time.valueOf(strGioVao);
                if (!strGioRa.isEmpty()) gioRa = Time.valueOf(strGioRa);
            } catch (IllegalArgumentException ex) {
                // Time.valueOf throws IllegalArgumentException if format is bad
                throw new IllegalArgumentException("Định dạng Giờ không đúng! Dùng HH:mm:ss");
            }

            BangChamCongDTO dto = new BangChamCongDTO(maCC, maNV, ngayLamViec, gioVao, gioRa, trangThai);

            if (existingDTO == null) {
                chamCongBUS.add(dto);
            } else {
                chamCongBUS.update(dto);
            }

            saved = true;
            JOptionPane.showMessageDialog(this,
                    existingDTO == null ? "Thêm chấm công thành công!" : "Cập nhật thành công!",
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
