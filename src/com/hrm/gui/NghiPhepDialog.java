package com.hrm.gui;

import com.hrm.bus.NghiPhepBUS;
import com.hrm.bus.NhanVienBUS;
import com.hrm.bus.LoaiNghiPhepBUS;
import com.hrm.dto.NghiPhepDTO;
import com.hrm.dto.NhanVienDTO;
import com.hrm.dto.LoaiNghiPhepDTO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;

/**
 * Form đơn nghỉ: nhân viên, loại phép, khoảng ngày (tự tính số ngày ở BUS), lý do và trạng thái.
 * <p>
 * Trạng thái "Đã duyệt" kích hoạt kiểm tra hạn mức năm trên {@link com.hrm.bus.NghiPhepBUS#update(com.hrm.dto.NghiPhepDTO)}.
 * </p>
 */
public class NghiPhepDialog extends JDialog {

    private static final Color PRIMARY       = new Color(0, 82, 155);
    private static final Color PRIMARY_HOVER = new Color(0, 105, 192);
    private static final Color BG_DIALOG     = new Color(22, 33, 52);
    private static final Color TEXT_WHITE    = new Color(248, 250, 252);
    private static final Color FIELD_BG      = new Color(15, 23, 42);
    private static final Color FIELD_BORDER  = new Color(71, 85, 105);

    private NghiPhepBUS nghiPhepBUS;
    private NhanVienBUS nhanVienBUS;
    private LoaiNghiPhepBUS loaiNghiPhepBUS;
    
    private NghiPhepDTO existingDTO;
    private boolean saved = false;

    private JTextField txtMaNP;
    private JComboBox<String> cboNhanVien;
    private JComboBox<String> cboLoaiNP;
    private JFormattedTextField txtTuNgay;
    private JFormattedTextField txtDenNgay;
    private JTextField txtLyDo;
    private JComboBox<String> cboTrangThai;

    private final SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");

    public NghiPhepDialog(JFrame parent, String title, NghiPhepDTO existing, NghiPhepBUS npBUS, NhanVienBUS nvBUS, LoaiNghiPhepBUS lnpBUS) {
        super(parent, title, true);
        this.existingDTO = existing;
        this.nghiPhepBUS = npBUS;
        this.nhanVienBUS = nvBUS;
        this.loaiNghiPhepBUS = lnpBUS;
        
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
        setSize(480, 480);
        setLocationRelativeTo(getParent());
        setResizable(false);
        getContentPane().setBackground(BG_DIALOG);
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(BG_DIALOG);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        formPanel.add(createFormRow("Mã đơn:", txtMaNP = createTextField()));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormRow("Nhân viên:", cboNhanVien = createNhanVienCombo()));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormRow("Loại phép:", cboLoaiNP = createLoaiNghiCombo()));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormRow("Từ ngày:", txtTuNgay = createFormattedDateField()));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormRow("Đến ngày:", txtDenNgay = createFormattedDateField()));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormRow("Lý do:", txtLyDo = createTextField()));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormRow("Trạng thái:", cboTrangThai = createStatusCombo()));

        add(formPanel, BorderLayout.CENTER);

        if (existingDTO != null) {
            txtMaNP.setEnabled(false); // Cấm sửa mã đơn
        } else {
            // Mặc định tạo mới là Chờ duyệt, disable không cho sửa thành duyệt luôn
            cboTrangThai.setSelectedItem("Chờ duyệt");
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

    private JComboBox<String> createLoaiNghiCombo() {
        JComboBox<String> cbo = new JComboBox<>();
        cbo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbo.setBackground(Color.WHITE);
        cbo.setForeground(Color.BLACK);
        cbo.setPreferredSize(new Dimension(0, 30));

        ArrayList<LoaiNghiPhepDTO> listLoai = loaiNghiPhepBUS.getList();
        for (LoaiNghiPhepDTO loai : listLoai) {
            cbo.addItem(loai.getMaLoaiNP() + " - " + loai.getTenLoai() + " (Tối đa " + loai.getSoNgayToiDa() + " ngày/năm)");
        }
        return cbo;
    }

    private JComboBox<String> createStatusCombo() {
        JComboBox<String> cbo = new JComboBox<>();
        cbo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbo.setBackground(Color.WHITE);
        cbo.setForeground(Color.BLACK);
        cbo.setPreferredSize(new Dimension(0, 30));
        cbo.addItem("Chờ duyệt");
        cbo.addItem("Đã duyệt");
        cbo.addItem("Từ chối");
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
        txtMaNP.setText(existingDTO.getMaNP());
        
        if (existingDTO.getMaNV() != null) {
            NhanVienDTO nv = nhanVienBUS.getById(existingDTO.getMaNV());
            if (nv != null) {
                String searchStr = nv.getMaNV() + " - " + nv.getHo() + " " + nv.getTen();
                cboNhanVien.setSelectedItem(searchStr);
            }
        }

        if (existingDTO.getMaLoaiNP() != null) {
            LoaiNghiPhepDTO loai = loaiNghiPhepBUS.getById(existingDTO.getMaLoaiNP());
            if (loai != null) {
                String searchStr = loai.getMaLoaiNP() + " - " + loai.getTenLoai() + " (Tối đa " + loai.getSoNgayToiDa() + " ngày/năm)";
                cboLoaiNP.setSelectedItem(searchStr);
            }
        }

        if (existingDTO.getTuNgay() != null) {
            txtTuNgay.setText(sdfDate.format(existingDTO.getTuNgay()));
        }

        if (existingDTO.getDenNgay() != null) {
            txtDenNgay.setText(sdfDate.format(existingDTO.getDenNgay()));
        }

        if (existingDTO.getLyDo() != null) {
            txtLyDo.setText(existingDTO.getLyDo());
        }

        if (existingDTO.getTrangThai() != null) {
            cboTrangThai.setSelectedItem(existingDTO.getTrangThai());
        }
    }

    private void performSave() {
        try {
            String maNP = txtMaNP.getText().trim();
            
            String selectedNV = (String) cboNhanVien.getSelectedItem();
            String maNV = selectedNV != null ? selectedNV.split(" - ")[0].trim() : "";
            
            String selectedLoai = (String) cboLoaiNP.getSelectedItem();
            String maLoaiNP = selectedLoai != null ? selectedLoai.split(" - ")[0].trim() : "";
            
            String strTuNgay = txtTuNgay.getText().replace("_", "").trim();
            if (strTuNgay.equals("//")) strTuNgay = "";
            String strDenNgay = txtDenNgay.getText().replace("_", "").trim();
            if (strDenNgay.equals("//")) strDenNgay = "";
            String lyDo = txtLyDo.getText().trim();
            String trangThai = (String) cboTrangThai.getSelectedItem();

            if (maNP.isEmpty() || maNV.isEmpty() || maLoaiNP.isEmpty() || strTuNgay.isEmpty() || strDenNgay.isEmpty()) {
                throw new IllegalArgumentException("Vui lòng điền đầy đủ các thông tin bắt buộc!");
            }

            // Xử lý Ngày
            Date tuNgay = null;
            Date denNgay = null;
            try {
                tuNgay = sdfDate.parse(strTuNgay);
                denNgay = sdfDate.parse(strDenNgay);
            } catch (ParseException ex) {
                throw new IllegalArgumentException("Định dạng Ngày không đúng! Dùng dd/MM/yyyy");
            }

            NghiPhepDTO dto = new NghiPhepDTO(maNP, maNV, maLoaiNP, tuNgay, denNgay, 0, lyDo, trangThai);

            // Ghi chú: Số ngày (soNgay) = 0 ở trên sẽ được BUS tự tính lại!

            if (existingDTO == null) {
                nghiPhepBUS.add(dto);
            } else {
                nghiPhepBUS.update(dto);
            }

            saved = true;
            JOptionPane.showMessageDialog(this,
                    existingDTO == null ? "Tạo đơn nghỉ phép thành công!" : "Cập nhật thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (IllegalArgumentException ex) {
            // Hứng lỗi validation của BUS, đặc biệt là lỗi "quá số ngày tiêu chuẩn" khi duyệt phép
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(), "Thông báo", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi hệ thống: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
