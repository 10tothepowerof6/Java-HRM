package com.hrm;

import com.hrm.gui.LoginDialog;
import javax.swing.*;

/**
 * Điểm vào chương trình ứng dụng quản lý nhân sự (HRM) dạng Swing.
 * <p>
 * Cấu hình LAF hệ thống, font cho hộp thoại, rồi mở {@link com.hrm.gui.LoginDialog} trên EDT.
 * </p>
 */
public class Main {
    public static void main(String[] args) {
        // Look and Feel gần với Windows để nút và bảng ổn định trên desktop
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        // Thiết lập font mặc định hỗ trợ Unicode cho toàn bộ ứng dụng
        UIManager.put("OptionPane.messageFont", new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        UIManager.put("OptionPane.buttonFont", new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));

        // Khởi chạy LoginDialog trên EDT
        SwingUtilities.invokeLater(() -> {
            LoginDialog loginDialog = new LoginDialog();
            loginDialog.setVisible(true);
        });
    }
}
