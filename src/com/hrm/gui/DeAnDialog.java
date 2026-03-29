package com.hrm.gui;

import com.hrm.bus.DeAnBUS;
import com.hrm.bus.PhongBanBUS;
import com.hrm.dto.DeAnDTO;
import com.hrm.dto.PhongBanDTO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Thông tin đề án: mã, tên, mô tả, thời gian bắt đầu/kết thúc và trạng thái triển khai.
 * <p>
 * Phân công chi tiết thực hiện ở {@link DeAnPanel} hoặc dialog phân công.
 * </p>
 */
public class DeAnDialog extends JDialog {

    private static final Color PRIMARY = new Color(0, 82, 155);
    private static final Color PRIMARY_HOVER = new Color(0, 105, 192);
    private static final Color BG_DIALOG = new Color(22, 33, 52);
    private static final Color TEXT_WHITE = new Color(248, 250, 252);
    private static final Color FIELD_BG = new Color(15, 23, 42);
    private static final Color FIELD_BORDER = new Color(71, 85, 105);

    private final DeAnBUS deAnBUS;
    private final PhongBanBUS phongBanBUS;
    private final DeAnDTO existing;
    private boolean saved = false;

    private JTextField txtMaDA;
    private JTextField txtTenDA;
    private JComboBox<String> cboPhongBan;
    private JTextField txtVonDeAn;
    private JFormattedTextField txtNgayBatDau;
    private JFormattedTextField txtNgayKetThuc;
    private JComboBox<String> cboTrangThai;

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    public DeAnDialog(JFrame parent, String title, DeAnDTO existing, DeAnBUS deAnBUS, PhongBanBUS phongBanBUS) {
        super(parent, title, true);
        this.existing = existing;
        this.deAnBUS = deAnBUS;
        this.phongBanBUS = phongBanBUS;
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
        setSize(520, 420);
        setLocationRelativeTo(getParent());
        setResizable(false);
        getContentPane().setBackground(BG_DIALOG);
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(BG_DIALOG);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        formPanel.add(createFormRow("Mã đề án:", txtMaDA = createTextField()));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormRow("Tên đề án:", txtTenDA = createTextField()));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormRow("Phòng ban quản lý:", cboPhongBan = createPhongBanCombo()));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormRow("Vốn đề án:", txtVonDeAn = createTextField()));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormRow("Ngày bắt đầu:", txtNgayBatDau = createFormattedDateField()));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormRow("Ngày kết thúc:", txtNgayKetThuc = createFormattedDateField()));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormRow("Trạng thái:", cboTrangThai = createTrangThaiCombo()));

        add(formPanel, BorderLayout.CENTER);

        if (existing != null) {
            txtMaDA.setEnabled(false);
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

    private JComboBox<String> createPhongBanCombo() {
        JComboBox<String> cbo = new JComboBox<>();
        cbo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbo.setBackground(Color.WHITE);
        cbo.setForeground(Color.BLACK);
        cbo.setPreferredSize(new Dimension(0, 30));
        for (PhongBanDTO pb : phongBanBUS.getList()) {
            cbo.addItem(pb.getMaPB() + " - " + pb.getTenPB());
        }
        return cbo;
    }

    private JComboBox<String> createTrangThaiCombo() {
        JComboBox<String> cbo = new JComboBox<>(new String[]{"Đang thực hiện", "Hoàn thành", "Hủy"});
        cbo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbo.setBackground(Color.WHITE);
        cbo.setForeground(Color.BLACK);
        cbo.setPreferredSize(new Dimension(0, 30));
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
        txtMaDA.setText(existing.getMaDA());
        txtTenDA.setText(existing.getTenDA());
        txtVonDeAn.setText(existing.getVonDeAn() != null ? existing.getVonDeAn().toPlainString() : "");
        txtNgayBatDau.setText(existing.getNgayBatDau() != null ? sdf.format(existing.getNgayBatDau()) : "");
        txtNgayKetThuc.setText(existing.getNgayKetThuc() != null ? sdf.format(existing.getNgayKetThuc()) : "");
        cboTrangThai.setSelectedItem(existing.getTrangThai());

        String selected = getPhongBanDisplay(existing.getMaPB());
        if (selected != null) {
            cboPhongBan.setSelectedItem(selected);
        }
    }

    private void performSave() {
        try {
            String maDA = txtMaDA.getText().trim();
            String tenDA = txtTenDA.getText().trim();
            String pbDisplay = (String) cboPhongBan.getSelectedItem();
            String maPB = pbDisplay == null ? null : pbDisplay.split(" - ")[0].trim();
            String vonStr = txtVonDeAn.getText().trim();
            String ngayBatDauStr = txtNgayBatDau.getText().replace("_", "").trim();
            if (ngayBatDauStr.equals("//")) ngayBatDauStr = "";
            String ngayKetThucStr = txtNgayKetThuc.getText().replace("_", "").trim();
            if (ngayKetThucStr.equals("//")) ngayKetThucStr = "";
            String trangThai = (String) cboTrangThai.getSelectedItem();

            if (maDA.isEmpty() || tenDA.isEmpty()) {
                throw new IllegalArgumentException("Mã đề án và tên đề án không được để trống!");
            }
            if (maPB == null || maPB.isEmpty()) {
                throw new IllegalArgumentException("Vui lòng chọn phòng ban quản lý!");
            }
            if (ngayBatDauStr.isEmpty()) {
                throw new IllegalArgumentException("Ngày bắt đầu không được để trống!");
            }

            BigDecimal vonDeAn = BigDecimal.ZERO;
            if (!vonStr.isEmpty()) {
                vonDeAn = new BigDecimal(vonStr);
            }

            Date ngayBatDau = sdf.parse(ngayBatDauStr);
            Date ngayKetThuc = null;
            if (!ngayKetThucStr.isEmpty()) {
                ngayKetThuc = sdf.parse(ngayKetThucStr);
            }

            DeAnDTO dto = new DeAnDTO(maDA, tenDA, ngayBatDau, ngayKetThuc, maPB, vonDeAn, trangThai);

            if (existing == null) {
                deAnBUS.add(dto);
            } else {
                deAnBUS.update(dto);
            }

            saved = true;
            JOptionPane.showMessageDialog(this,
                    existing == null ? "Thêm đề án thành công!" : "Cập nhật đề án thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "Định dạng ngày không hợp lệ! Vui lòng nhập dd/MM/yyyy.",
                    "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Vốn đề án phải là số hợp lệ!",
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

    private String getPhongBanDisplay(String maPB) {
        if (maPB == null) {
            return null;
        }
        for (PhongBanDTO pb : phongBanBUS.getList()) {
            if (maPB.equalsIgnoreCase(pb.getMaPB())) {
                return pb.getMaPB() + " - " + pb.getTenPB();
            }
        }
        return null;
    }
}
