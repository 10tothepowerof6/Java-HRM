package com.hrm.gui;

import com.hrm.bus.BangLuongThangBUS;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

/**
 * Hộp thoại chọn tháng/năm rồi gọi tính lương tự động cho toàn bộ nhân viên đang làm việc.
 * <p>
 * Sau khi đóng, {@link #isGenerated()} cho biết đã chạy thành công; {@link #getThang()}/{@link #getNam()} phục vụ làm mới bảng.
 * </p>
 */
public class HanhDongTinhLuongDialog extends JDialog {

    private BangLuongThangBUS bangLuongBUS;
    private boolean generated = false;
    private int selectedThang;
    private int selectedNam;

    private JComboBox<Integer> cboThang;
    private JComboBox<Integer> cboNam;

    public HanhDongTinhLuongDialog(Window parent, BangLuongThangBUS bus) {
        super(parent, "Khởi chạy Tính Lương", ModalityType.APPLICATION_MODAL);
        this.bangLuongBUS = bus;
        initUI();
    }

    public boolean isGenerated() {
        return generated;
    }

    public int getThang() {
        return selectedThang;
    }

    public int getNam() {
        return selectedNam;
    }

    private void initUI() {
        setSize(350, 240);
        setLocationRelativeTo(getParent());
        setResizable(false);
        getContentPane().setBackground(new Color(22, 33, 52));
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("CHẠY TRÌNH TÍNH LƯƠNG TỰ ĐỘNG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(new Color(16, 185, 129));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(lblTitle);
        formPanel.add(Box.createVerticalStrut(20));

        JPanel rowPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        rowPanel.setOpaque(false);

        JLabel lblT = new JLabel("Tháng:");
        lblT.setForeground(Color.WHITE);
        rowPanel.add(lblT);

        Integer[] months = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        cboThang = new JComboBox<>(months);
        int currentMonth = LocalDate.now().getMonthValue();
        cboThang.setSelectedItem(currentMonth);
        rowPanel.add(cboThang);

        JLabel lblN = new JLabel("  Năm:");
        lblN.setForeground(Color.WHITE);
        rowPanel.add(lblN);

        Integer[] years = new Integer[11];
        int currentYear = LocalDate.now().getYear();
        for (int i = 0; i <= 10; i++) years[i] = currentYear - 5 + i;
        cboNam = new JComboBox<>(years);
        cboNam.setSelectedItem(currentYear);
        rowPanel.add(cboNam);

        formPanel.add(rowPanel);
        
        JLabel lblWarning = new JLabel("<html><i>* Lưu ý: Nếu tháng này đã có dữ liệu lương,<br>hệ thống sẽ CẬP NHẬT LẠI bảng lương mới.</i></html>");
        lblWarning.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblWarning.setForeground(new Color(251, 146, 60));
        lblWarning.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(lblWarning);

        add(formPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setOpaque(false);

        JButton btnStart = new JButton("Bắt Đầu Tính Lương");
        btnStart.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnStart.setForeground(Color.WHITE);
        btnStart.setBackground(new Color(16, 185, 129));
        btnStart.setPreferredSize(new Dimension(160, 36));
        btnStart.setFocusPainted(false);
        btnStart.setContentAreaFilled(false);
        btnStart.setOpaque(true);
        btnStart.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnStart.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btnStart.setBackground(new Color(52, 211, 153)); }
            public void mouseExited(java.awt.event.MouseEvent e) { btnStart.setBackground(new Color(16, 185, 129)); }
        });
        btnStart.addActionListener(e -> runGeneration());
        btnPanel.add(btnStart);

        add(btnPanel, BorderLayout.SOUTH);
    }

    private void runGeneration() {
        int t = (int) cboThang.getSelectedItem();
        int n = (int) cboNam.getSelectedItem();

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Bạn có chắc chắn muốn phát sinh bảng lương cho tháng " + t + "/" + n + "?", 
            "Xác nhận", JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Hiển thị trạng thái chờ nếu được (thực tế code blocking thread)
                int count = bangLuongBUS.generateBangLuong(t, n);
                
                generated = true;
                selectedThang = t;
                selectedNam = n;
                
                JOptionPane.showMessageDialog(this, "Đã tính lương thành công cho " + count + " nhân viên!", "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Có lỗi xảy ra: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
