package com.hrm.gui;

import com.hrm.bus.PhongBanBUS;
import com.hrm.bus.NhanVienBUS;
import com.hrm.dto.PhongBanDTO;
import com.hrm.dto.NhanVienDTO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;

/**
 * Form phòng ban: mã (PK) và tên hiển thị; dùng cho thêm mới hoặc đổi tên.
 * <p>
 * Xóa phòng ban có thể bị chặn nếu còn nhân viên tham chiếu (tùy chính sách BUS/DAO).
 * </p>
 */
public class PhongBanDialog extends JDialog {

    private static final Color PRIMARY       = new Color(0, 82, 155);
    private static final Color PRIMARY_HOVER = new Color(0, 105, 192);
    private static final Color BG_DIALOG     = new Color(22, 33, 52);
    private static final Color TEXT_WHITE    = new Color(248, 250, 252);
    private static final Color FIELD_BG      = new Color(15, 23, 42);
    private static final Color FIELD_BORDER  = new Color(71, 85, 105);

    private PhongBanBUS phongBanBUS;
    private NhanVienBUS nhanVienBUS;
    private PhongBanDTO existingPB;
    private boolean saved = false;

    private JTextField txtMaPB;
    private JTextField txtTenPB;
    private JComboBox<String> cboTruongPhong;
    private JTextField txtNgayThanhLap;
    private JTextField txtEmail;

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    public PhongBanDialog(JFrame parent, String title, PhongBanDTO existing, PhongBanBUS pbBUS, NhanVienBUS nvBUS) {
        super(parent, title, true);
        this.existingPB = existing;
        this.phongBanBUS = pbBUS;
        this.nhanVienBUS = nvBUS;
        
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
        setSize(460, 360);
        setLocationRelativeTo(getParent());
        setResizable(false);
        getContentPane().setBackground(BG_DIALOG);
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(BG_DIALOG);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        formPanel.add(createFormRow("Mã phòng ban:", txtMaPB = createTextField()));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormRow("Tên phòng ban:", txtTenPB = createTextField()));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormRow("Trưởng phòng:", cboTruongPhong = createNhanVienCombo()));
        cboTruongPhong.setEnabled(false);
        cboTruongPhong.setToolTipText("Chỉ đọc. Vui lòng gán chức trưởng phòng trong form Nhân viên.");
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormRow("Ngày thành lập:", txtNgayThanhLap = createTextField()));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormRow("Email:", txtEmail = createTextField()));

        add(formPanel, BorderLayout.CENTER);

        if (existingPB != null) {
            txtMaPB.setEnabled(false); // Cấm sửa mã khi Edit
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
        lbl.setPreferredSize(new Dimension(120, 30));
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

    private JComboBox<String> createNhanVienCombo() {
        JComboBox<String> cbo = new JComboBox<>();
        cbo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbo.setBackground(Color.WHITE);
        cbo.setForeground(Color.BLACK);
        cbo.setPreferredSize(new Dimension(0, 30));

        cbo.addItem("--- Chưa có ---");
        ArrayList<NhanVienDTO> listNV = nhanVienBUS.getList();
        for (NhanVienDTO nv : listNV) {
            // Định dạng: "MA_NV - Họ Tên"
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
            public void mouseEntered(MouseEvent e) { btnSave.setBackground(PRIMARY_HOVER); }
            @Override
            public void mouseExited(MouseEvent e) { btnSave.setBackground(PRIMARY); }
        });
        btnSave.addActionListener(e -> performSave());
        panel.add(btnSave);

        return panel;
    }

    private void prefillData() {
        txtMaPB.setText(existingPB.getMaPB());
        txtTenPB.setText(existingPB.getTenPB());
        txtEmail.setText(existingPB.getEmail() != null ? existingPB.getEmail() : "");

        if (existingPB.getNgayThanhLap() != null) {
            txtNgayThanhLap.setText(sdf.format(existingPB.getNgayThanhLap()));
        }

        // Chọn lại Trưởng phòng
        if (existingPB.getMaTruongPB() != null && !existingPB.getMaTruongPB().trim().isEmpty()) {
            NhanVienDTO nv = nhanVienBUS.getById(existingPB.getMaTruongPB());
            if (nv != null) {
                String searchStr = nv.getMaNV() + " - " + nv.getHo() + " " + nv.getTen();
                cboTruongPhong.setSelectedItem(searchStr);
            }
        }
    }

    private void performSave() {
        try {
            String maPB = txtMaPB.getText().trim();
            String tenPB = txtTenPB.getText().trim();
            String ngayThanhLapStr = txtNgayThanhLap.getText().trim();
            String email = txtEmail.getText().trim();
            
            String selectedTP = (String) cboTruongPhong.getSelectedItem();
            String maTruongPB = null;
            if (selectedTP != null && !selectedTP.equals("--- Chưa có ---")) {
                maTruongPB = selectedTP.split(" - ")[0].trim();
            }

            if (maPB.isEmpty() || tenPB.isEmpty()) {
                throw new IllegalArgumentException("Mã phòng ban và Tên phòng ban không được trống!");
            }

            Date ngayThanhLap = null;
            if (!ngayThanhLapStr.isEmpty()) {
                try {
                    ngayThanhLap = sdf.parse(ngayThanhLapStr);
                } catch (ParseException ex) {
                    throw new IllegalArgumentException("Định dạng ngày thành lập không hợp lệ! (dd/MM/yyyy)");
                }
            }

            PhongBanDTO pb = new PhongBanDTO(maPB, tenPB, maTruongPB, ngayThanhLap, email);

            if (existingPB == null) {
                phongBanBUS.add(pb);
            } else {
                phongBanBUS.update(pb);
            }

            saved = true;
            JOptionPane.showMessageDialog(this,
                    existingPB == null ? "Thêm phòng ban thành công!" : "Cập nhật thành công!",
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
