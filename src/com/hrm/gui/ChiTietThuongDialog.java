package com.hrm.gui;

import com.hrm.bus.ChiTietThuongBUS;
import com.hrm.bus.DanhMucThuongBUS;
import com.hrm.bus.NhanVienBUS;
import com.hrm.dto.ChiTietThuongDTO;
import com.hrm.dto.DanhMucThuongDTO;
import com.hrm.dto.NhanVienDTO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.MaskFormatter;

/**
 * Thêm hoặc sửa một dòng chi tiết thưởng: nhân viên, danh mục thưởng, số tiền và ngày ghi nhận.
 * <p>
 * {@link #isSaved()} cho biết người dùng đã xác nhận lưu hợp lệ.
 * </p>
 */
public class ChiTietThuongDialog extends JDialog {

    private static final Color PRIMARY = new Color(0, 82, 155);
    private static final Color PRIMARY_HOVER = new Color(0, 105, 192);
    private static final Color BG_DIALOG = new Color(22, 33, 52);
    private static final Color TEXT_WHITE = new Color(248, 250, 252);
    private static final Color FIELD_BG = new Color(15, 23, 42);
    private static final Color FIELD_BORDER = new Color(71, 85, 105);

    private final ChiTietThuongBUS chiTietThuongBUS;
    private final DanhMucThuongBUS danhMucThuongBUS;
    private final NhanVienBUS nhanVienBUS;
    private final ChiTietThuongDTO existing;
    private final String preselectedMaThuong;
    private boolean saved = false;

    private JTextField txtMaCTT;
    private JComboBox<String> cboNhanVien;
    private JComboBox<String> cboDanhMucThuong;
    private JTextField txtSoTien;
    private JFormattedTextField txtNgayThuong;
    private JTextField txtGhiChu;

    private final SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");

    public ChiTietThuongDialog(JFrame parent, String title, ChiTietThuongDTO existing,
            ChiTietThuongBUS chiTietThuongBUS, DanhMucThuongBUS danhMucThuongBUS,
            NhanVienBUS nhanVienBUS, String preselectedMaThuong) {
        super(parent, title, true);
        this.existing = existing;
        this.chiTietThuongBUS = chiTietThuongBUS;
        this.danhMucThuongBUS = danhMucThuongBUS;
        this.nhanVienBUS = nhanVienBUS;
        this.preselectedMaThuong = preselectedMaThuong;
        sdfDate.setLenient(false);

        initUI();
        if (existing != null) {
            prefillData();
        } else if (preselectedMaThuong != null && !preselectedMaThuong.trim().isEmpty()) {
            String selected = getDanhMucDisplay(preselectedMaThuong);
            if (selected != null) {
                cboDanhMucThuong.setSelectedItem(selected);
            }
        }
    }

    public boolean isSaved() {
        return saved;
    }

    private void initUI() {
        setSize(560, 420);
        setLocationRelativeTo(getParent());
        setResizable(false);
        getContentPane().setBackground(BG_DIALOG);
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(BG_DIALOG);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        formPanel.add(createFormRow("Mã chi tiết thưởng:", txtMaCTT = createTextField()));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormRow("Nhân viên:", cboNhanVien = createNhanVienCombo()));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormRow("Loại thưởng:", cboDanhMucThuong = createDanhMucCombo()));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormRow("Số tiền:", txtSoTien = createTextField()));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormRow("Ngày thưởng:", txtNgayThuong = createFormattedDateField()));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormRow("Ghi chú:", txtGhiChu = createTextField()));
        add(formPanel, BorderLayout.CENTER);

        if (existing != null) {
            txtMaCTT.setEnabled(false);
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
        lbl.setPreferredSize(new Dimension(150, 30));
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
            MaskFormatter dateMask = new MaskFormatter("##/##/####");
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

    private JComboBox<String> createDanhMucCombo() {
        JComboBox<String> cbo = new JComboBox<>();
        cbo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbo.setBackground(Color.WHITE);
        cbo.setForeground(Color.BLACK);
        cbo.setPreferredSize(new Dimension(0, 30));
        for (DanhMucThuongDTO dm : danhMucThuongBUS.getList()) {
            cbo.addItem(dm.getMaThuong() + " - " + dm.getTenLoaiThuong());
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
        txtMaCTT.setText(existing.getMaCTT());
        txtSoTien.setText(existing.getSoTien() != null ? existing.getSoTien().toPlainString() : "");
        txtGhiChu.setText(existing.getGhiChu() == null ? "" : existing.getGhiChu());
        txtNgayThuong.setText(existing.getNgayThuong() != null ? sdfDate.format(existing.getNgayThuong()) : "");

        String nv = getNhanVienDisplay(existing.getMaNV());
        if (nv != null) {
            cboNhanVien.setSelectedItem(nv);
        }

        String dm = getDanhMucDisplay(existing.getMaThuong());
        if (dm != null) {
            cboDanhMucThuong.setSelectedItem(dm);
        }
    }

    private void performSave() {
        try {
            String maCTT = txtMaCTT.getText().trim();
            String nvDisplay = (String) cboNhanVien.getSelectedItem();
            String maNV = nvDisplay == null ? null : nvDisplay.split(" - ")[0].trim();
            String dmDisplay = (String) cboDanhMucThuong.getSelectedItem();
            String maThuong = dmDisplay == null ? null : dmDisplay.split(" - ")[0].trim();
            String soTienStr = txtSoTien.getText().trim();
            String ngayThuongStr = txtNgayThuong.getText().replace("_", "").trim();
            if ("//".equals(ngayThuongStr)) ngayThuongStr = "";
            String ghiChu = txtGhiChu.getText().trim();

            if (maCTT.isEmpty()) throw new IllegalArgumentException("Mã chi tiết thưởng không được để trống!");
            if (maNV == null || maNV.isEmpty()) throw new IllegalArgumentException("Vui lòng chọn nhân viên!");
            if (maThuong == null || maThuong.isEmpty()) throw new IllegalArgumentException("Vui lòng chọn loại thưởng!");
            if (soTienStr.isEmpty()) throw new IllegalArgumentException("Số tiền không được để trống!");
            if (ngayThuongStr.isEmpty()) throw new IllegalArgumentException("Ngày thưởng không được để trống!");

            BigDecimal soTien = new BigDecimal(soTienStr);
            Date ngayThuong = sdfDate.parse(ngayThuongStr);

            ChiTietThuongDTO dto = new ChiTietThuongDTO(maCTT, maNV, maThuong, soTien, ngayThuong, ghiChu);
            if (existing == null) {
                chiTietThuongBUS.add(dto);
            } else {
                chiTietThuongBUS.update(dto);
            }

            saved = true;
            JOptionPane.showMessageDialog(this,
                    existing == null ? "Thêm chi tiết thưởng thành công!" : "Cập nhật chi tiết thưởng thành công!",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(this, "Định dạng ngày không hợp lệ! Vui lòng nhập dd/MM/yyyy.",
                    "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Số tiền thưởng phải là số hợp lệ!",
                    "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi hệ thống: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private String getNhanVienDisplay(String maNV) {
        if (maNV == null) return null;
        for (NhanVienDTO nv : nhanVienBUS.getList()) {
            if (maNV.equalsIgnoreCase(nv.getMaNV())) {
                return nv.getMaNV() + " - " + nv.getHo() + " " + nv.getTen();
            }
        }
        return null;
    }

    private String getDanhMucDisplay(String maThuong) {
        if (maThuong == null) return null;
        for (DanhMucThuongDTO dm : danhMucThuongBUS.getList()) {
            if (maThuong.equalsIgnoreCase(dm.getMaThuong())) {
                return dm.getMaThuong() + " - " + dm.getTenLoaiThuong();
            }
        }
        return null;
    }
}

