package com.hrm.gui;

import com.hrm.bus.NhanVienBUS;
import com.hrm.bus.PhuCapBUS;
import com.hrm.dto.NhanVienDTO;
import com.hrm.dto.PhuCapDTO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Gán số tiền phụ cấp cho nhân viên theo loại phụ cấp và (nếu có) khoảng ngày hiệu lực.
 * <p>
 * Dữ liệu đọc/ghi qua {@link com.hrm.bus.PhuCapBUS}.
 * </p>
 */
public class PhuCapDialog extends JDialog {

    private static final Color PRIMARY = new Color(0, 82, 155);
    private static final Color PRIMARY_HOVER = new Color(0, 105, 192);
    private static final Color BG_DIALOG = new Color(22, 33, 52);
    private static final Color TEXT_WHITE = new Color(248, 250, 252);
    private static final Color FIELD_BG = new Color(15, 23, 42);
    private static final Color FIELD_BORDER = new Color(71, 85, 105);

    // Loại phụ cấp được hardcode để không phụ thuộc dữ liệu bảng LoaiPhuCap trong CSDL.
    // Nếu bạn muốn đổi mã/tên, sửa tại đây.
    private static final String[][] LOAI_PHU_CAPS = new String[][]{
            {"PC01", "Đi lại"},
            {"PC02", "Nhà ở"},
            {"PC03", "Xăng xe"},
            {"PC04", "Điện thoại"},
            {"PC05", "Trách nhiệm"},
            {"PC06", "Chuyên cần"},
            {"PC07", "Ca đêm"}
    };

    private final PhuCapBUS phuCapBUS;
    private final NhanVienBUS nhanVienBUS;

    private final PhuCapDTO existing;
    private boolean saved = false;

    private JTextField txtMaPC;
    private JComboBox<String> cboNhanVien;
    private JComboBox<String> cboLoaiPC;
    private JTextField txtSoTien;
    private JFormattedTextField txtNgayApDung;
    private JFormattedTextField txtNgayKetThuc;

    private final SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");

    public PhuCapDialog(JFrame parent, String title, PhuCapDTO existing,
                         PhuCapBUS phuCapBUS, NhanVienBUS nhanVienBUS) {
        super(parent, title, true);
        this.existing = existing;
        this.phuCapBUS = phuCapBUS;
        this.nhanVienBUS = nhanVienBUS;
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
        setSize(540, 420);
        setLocationRelativeTo(getParent());
        setResizable(false);
        getContentPane().setBackground(BG_DIALOG);
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(BG_DIALOG);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        formPanel.add(createFormRow("Mã phụ cấp:", txtMaPC = createTextField()));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormRow("Nhân viên:", cboNhanVien = createNhanVienCombo()));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormRow("Loại phụ cấp:", cboLoaiPC = createLoaiPCCombo()));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormRow("Số tiền:", txtSoTien = createTextField()));
        formPanel.add(Box.createVerticalStrut(10));

        formPanel.add(createFormRow("Ngày áp dụng:", txtNgayApDung = createFormattedDateField()));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormRow("Ngày kết thúc:", txtNgayKetThuc = createFormattedDateField(), "(Trống = vô thời hạn)"));
        formPanel.add(Box.createVerticalStrut(10));

        add(formPanel, BorderLayout.CENTER);

        if (existing != null) {
            txtMaPC.setEnabled(false); // Cấm sửa mã phụ cấp
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
        lbl.setPreferredSize(new Dimension(190, 30));
        row.add(lbl, BorderLayout.WEST);

        JPanel fieldPanel = new JPanel(new BorderLayout(5, 0));
        fieldPanel.setOpaque(false);
        fieldPanel.add(field, BorderLayout.CENTER);

        if (hint != null && !hint.trim().isEmpty()) {
            JLabel lblHint = new JLabel(hint);
            lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            lblHint.setForeground(new Color(156, 163, 175));
            lblHint.setPreferredSize(new Dimension(170, 30));
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

        for (NhanVienDTO nv : nhanVienBUS.getList()) {
            String item = nv.getMaNV() + " - " + nv.getHo() + " " + nv.getTen();
            cbo.addItem(item);
        }
        return cbo;
    }

    private JComboBox<String> createLoaiPCCombo() {
        JComboBox<String> cbo = new JComboBox<>();
        cbo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbo.setBackground(Color.WHITE);
        cbo.setForeground(Color.BLACK);
        cbo.setPreferredSize(new Dimension(0, 30));

        for (String[] type : LOAI_PHU_CAPS) {
            cbo.addItem(type[0] + " - " + type[1]);
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
        txtMaPC.setText(existing.getMaPC());
        txtSoTien.setText(existing.getSoTien() != null ? existing.getSoTien().toPlainString() : "");

        if (existing.getNgayApDung() != null) {
            txtNgayApDung.setText(sdfDate.format(existing.getNgayApDung()));
        }
        if (existing.getNgayKetThuc() != null) {
            txtNgayKetThuc.setText(sdfDate.format(existing.getNgayKetThuc()));
        }

        String nvSelected = getNhanVienDisplay(existing.getMaNV());
        if (nvSelected != null) {
            cboNhanVien.setSelectedItem(nvSelected);
        }

        String loaiSelected = getLoaiPCDisplay(existing.getMaLoaiPC());
        if (loaiSelected != null && !loaiSelected.trim().isEmpty()) {
            ensureComboHasItem(cboLoaiPC, loaiSelected);
            cboLoaiPC.setSelectedItem(loaiSelected);
        }
    }

    private void ensureComboHasItem(JComboBox<String> combo, String item) {
        if (item == null) return;
        for (int i = 0; i < combo.getItemCount(); i++) {
            Object existingItem = combo.getItemAt(i);
            if (existingItem != null && existingItem.toString().equals(item)) return;
        }
        combo.addItem(item);
    }

    private void performSave() {
        try {
            String maPC = txtMaPC.getText().trim();
            String nvDisplay = (String) cboNhanVien.getSelectedItem();
            String maNV = nvDisplay == null ? null : nvDisplay.split(" - ")[0].trim();
            String lpcDisplay = (String) cboLoaiPC.getSelectedItem();
            String maLoaiPC = lpcDisplay == null ? null : lpcDisplay.split(" - ")[0].trim();

            String soTienStr = txtSoTien.getText().trim();

            String ngayApDungStr = txtNgayApDung.getText().replace("_", "").trim();
            if (ngayApDungStr.equals("//")) ngayApDungStr = "";

            String ngayKetThucStr = txtNgayKetThuc.getText().replace("_", "").trim();
            if (ngayKetThucStr.equals("//")) ngayKetThucStr = "";

            if (maPC.isEmpty()) {
                throw new IllegalArgumentException("Mã phụ cấp không được để trống!");
            }
            if (maNV == null || maNV.isEmpty()) {
                throw new IllegalArgumentException("Vui lòng chọn nhân viên!");
            }
            if (maLoaiPC == null || maLoaiPC.isEmpty()) {
                throw new IllegalArgumentException("Vui lòng chọn loại phụ cấp!");
            }
            if (soTienStr.isEmpty()) {
                throw new IllegalArgumentException("Số tiền phụ cấp không được để trống!");
            }
            if (ngayApDungStr.isEmpty()) {
                throw new IllegalArgumentException("Ngày áp dụng không được để trống!");
            }

            BigDecimal soTien = new BigDecimal(soTienStr);
            Date ngayApDung = sdfDate.parse(ngayApDungStr);
            Date ngayKetThuc = null;
            if (!ngayKetThucStr.isEmpty()) {
                ngayKetThuc = sdfDate.parse(ngayKetThucStr);
            }

            PhuCapDTO dto = new PhuCapDTO(maPC, maNV, maLoaiPC, soTien, ngayApDung, ngayKetThuc);

            if (existing == null) {
                phuCapBUS.add(dto);
            } else {
                phuCapBUS.update(dto);
            }

            saved = true;
            JOptionPane.showMessageDialog(this,
                    existing == null ? "Thêm phụ cấp thành công!" : "Cập nhật phụ cấp thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "Định dạng ngày không hợp lệ! Vui lòng nhập dd/MM/yyyy.",
                    "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Số tiền phụ cấp phải là số hợp lệ!",
                    "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(), "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi hệ thống: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
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

    private String getLoaiPCDisplay(String maLoaiPC) {
        if (maLoaiPC == null) return "";
        for (String[] type : LOAI_PHU_CAPS) {
            if (maLoaiPC.equalsIgnoreCase(type[0])) {
                return type[0] + " - " + type[1];
            }
        }
        // Nếu dữ liệu cũ trong DB có mã không nằm trong danh sách hardcode,
        // vẫn hiển thị để người dùng thấy và giữ nguyên mã khi lưu.
        return maLoaiPC + " - Loại khác";
    }
}

