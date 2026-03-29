package com.hrm.gui;

import com.hrm.security.AppSession;
import com.hrm.security.RoleDefinition;
import com.hrm.security.SecurityData;
import com.hrm.security.SecurityRepository;
import com.hrm.security.UserAccount;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.prefs.Preferences;

/**
 * Cửa sổ đăng nhập không viền, nền ảnh và khối kính mờ trung tâm.
 * <p>
 * Xác thực qua {@link com.hrm.security.SecurityRepository}; hỗ trợ ghi nhớ tài khoản (Preferences) và
 * xem danh sách tài khoản/mật khẩu qua liên kết "Quên mật khẩu" (chỉ phù hợp môi trường nội bộ).
 * </p>
 */
public class LoginDialog extends JFrame {

    // ===== BẢNG MÀU =====
    private static final Color PRIMARY        = new Color(0, 82, 155);
    private static final Color PRIMARY_HOVER   = new Color(0, 105, 192);
    private static final Color BG_DARK        = new Color(15, 23, 42, 210);
    private static final Color TEXT_WHITE     = new Color(248, 250, 252);
    private static final Color TEXT_MUTED     = new Color(148, 163, 184);
    private static final Color CLOSE_RED      = new Color(232, 17, 35);
    private static final Color FIELD_BG       = new Color(30, 41, 59, 200);
    private static final Color FIELD_BORDER   = new Color(71, 85, 105);
    private static final Color FIELD_FOCUS    = new Color(56, 189, 248);

    // ===== KÍCH THƯỚC =====
    private static final int WIN_W = 900;
    private static final int WIN_H = 600;
    private static final int BOX_W = 380;
    private static final int BOX_H = 520;

    private static final Preferences LOGIN_PREFS = Preferences.userRoot().node("com/hrm/login");
    private static final String PREF_REMEMBER = "remember";
    private static final String PREF_USERNAME = "username";
    private static final String PREF_PASSWORD = "password";

    // ===== COMPONENTS =====
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JLabel lblError;
    private JButton btnLogin;
    private JButton btnClose;
    private JToggleButton btnShowPass;
    private JCheckBox chkRemember;

    // Kéo thả cửa sổ
    private Point dragOffset;

    // Ảnh nền đã blur
    private BufferedImage blurredBg;

    // Logo gốc (ko scale trước)
    private Image originalLogo;

    public LoginDialog() {
        initBackground();
        initLogo();
        initUI();
    }

    // =========================================================================
    //  LOAD ẢNH NỀN + BLUR
    // =========================================================================
    private void initBackground() {
        try {
            String basePath = System.getProperty("user.dir") + File.separator + "imgs" + File.separator;
            BufferedImage original = ImageIO.read(new File(basePath + "office_login_background.jpeg"));

            // Scale ảnh kiểu "cover" (crop-to-fit) để không bị méo/tràn
            int origW = original.getWidth();
            int origH = original.getHeight();
            double scaleX = (double) WIN_W / origW;
            double scaleY = (double) WIN_H / origH;
            double scale = Math.max(scaleX, scaleY); // Lấy tỷ lệ lớn hơn để phủ kín
            int scaledW = (int) (origW * scale);
            int scaledH = (int) (origH * scale);
            int offsetX = (WIN_W - scaledW) / 2;
            int offsetY = (WIN_H - scaledH) / 2;

            BufferedImage scaled = new BufferedImage(WIN_W, WIN_H, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = scaled.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.drawImage(original, offsetX, offsetY, scaledW, scaledH, null);
            g2.dispose();

            // Không áp dụng blur — chỉ dùng overlay tối để tăng contrast
            blurredBg = scaled;
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback: nền đen
            blurredBg = new BufferedImage(WIN_W, WIN_H, BufferedImage.TYPE_INT_RGB);
        }
    }

    private void initLogo() {
        try {
            String basePath = System.getProperty("user.dir") + File.separator + "imgs" + File.separator;
            originalLogo = ImageIO.read(new File(basePath + "LogoWhite.png"));
            setIconImage(originalLogo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================================================================
    //  KHỞI TẠO GIAO DIỆN
    // =========================================================================
    private void initUI() {
        setTitle("HRM Login");
        setSize(WIN_W, WIN_H);
        setUndecorated(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        // Content pane = panel vẽ nền blur
        JPanel bgPanel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (blurredBg != null) {
                    g2d.drawImage(blurredBg, 0, 0, getWidth(), getHeight(), null);
                }
                // Lớp phủ tối nhẹ để tăng contrast
                g2d.setColor(new Color(0, 0, 0, 60));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        bgPanel.setPreferredSize(new Dimension(WIN_W, WIN_H));
        setContentPane(bgPanel);

        // ===== NÚT ĐÓNG [X] =====
        btnClose = createCloseButton();
        btnClose.setBounds(WIN_W - 46, 0, 46, 32);
        bgPanel.add(btnClose);

        // ===== GLASSMORPHISM CENTER BOX =====
        JPanel glassBox = createGlassBox();
        int boxX = (WIN_W - BOX_W) / 2;
        int boxY = (WIN_H - BOX_H) / 2;
        glassBox.setBounds(boxX, boxY, BOX_W, BOX_H);
        bgPanel.add(glassBox);

        // ===== KÉO THẢ CỬA SỔ =====
        bgPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragOffset = e.getPoint();
            }
        });
        bgPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Point current = e.getLocationOnScreen();
                setLocation(current.x - dragOffset.x, current.y - dragOffset.y);
            }
        });

        // ===== ENTER KEY =====
        getRootPane().setDefaultButton(btnLogin);
    }

    // =========================================================================
    //  TẠO NÚT ĐÓNG [X]
    // =========================================================================
    private JButton createCloseButton() {
        JButton btn = new JButton("X");
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(TEXT_WHITE);
        btn.setBackground(new Color(0, 0, 0, 0));
        btn.setBorder(null);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setOpaque(true);
                btn.setContentAreaFilled(true);
                btn.setBackground(CLOSE_RED);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setOpaque(false);
                btn.setContentAreaFilled(false);
            }
        });

        btn.addActionListener(e -> System.exit(0));
        return btn;
    }

    // =========================================================================
    //  TẠO GLASSMORPHISM BOX
    // =========================================================================
    private JPanel createGlassBox() {
        JPanel box = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Nền bo góc bán trong suốt
                g2.setColor(BG_DARK);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));

                // Viền tinh tế
                g2.setColor(new Color(255, 255, 255, 20));
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(0.75f, 0.75f, getWidth() - 1.5f, getHeight() - 1.5f, 20, 20));

                g2.dispose();
            }
        };
        box.setOpaque(false);

        int y = 30; // Vị trí bắt đầu trong box

        // ===== LOGO =====
        if (originalLogo != null) {
            JPanel pnlLogo = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    // Để OS tự downscale trực tiếp lúc vẽ, kèm full hints cho alpha (viền trong suốt)
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
                    
                    // Vẽ scale khít panel 80x80
                    g2.drawImage(originalLogo, 0, 0, getWidth(), getHeight(), null);
                    g2.dispose();
                }
            };
            pnlLogo.setOpaque(false);
            pnlLogo.setBounds((BOX_W - 80) / 2, y, 80, 80);
            box.add(pnlLogo);
        }
        y += 95;

        // ===== TIÊU ĐỀ =====
        JLabel lblTitle = new JLabel("HỆ THỐNG QUẢN LÝ NHÂN SỰ", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(TEXT_WHITE);
        lblTitle.setBounds(0, y, BOX_W, 24);
        box.add(lblTitle);
        y += 40;

        // ===== USERNAME =====
        JLabel lblUser = new JLabel("Tên đăng nhập");
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblUser.setForeground(TEXT_MUTED);
        lblUser.setBounds(40, y, 300, 18);
        box.add(lblUser);
        y += 22;

        txtUsername = createRoundedTextField();
        txtUsername.setBounds(40, y, BOX_W - 80, 40);
        box.add(txtUsername);
        y += 52;

        // ===== PASSWORD =====
        JLabel lblPass = new JLabel("Mật khẩu");
        lblPass.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblPass.setForeground(TEXT_MUTED);
        lblPass.setBounds(40, y, 300, 18);
        box.add(lblPass);
        y += 22;

        // Panel chứa password field + toggle button
        JPanel passPanel = new JPanel(null);
        passPanel.setOpaque(false);
        passPanel.setBounds(40, y, BOX_W - 80, 40);
        box.add(passPanel);

        txtPassword = createRoundedPasswordField();
        txtPassword.setBounds(0, 0, BOX_W - 80 - 40, 40);
        passPanel.add(txtPassword);

        btnShowPass = new JToggleButton("👁");
        btnShowPass.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        btnShowPass.setBounds(BOX_W - 80 - 40, 0, 40, 40);
        btnShowPass.setForeground(TEXT_MUTED);
        btnShowPass.setBackground(FIELD_BG);
        btnShowPass.setBorder(null);
        btnShowPass.setFocusPainted(false);
        btnShowPass.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnShowPass.addActionListener(e -> {
            if (btnShowPass.isSelected()) {
                txtPassword.setEchoChar((char) 0); // Hiện mật khẩu
            } else {
                txtPassword.setEchoChar('●');
            }
        });
        passPanel.add(btnShowPass);
        y += 48;

        // ===== GHI NHỚ + QUÊN MK =====
        chkRemember = new JCheckBox("Ghi nhớ đăng nhập");
        chkRemember.setOpaque(false);
        chkRemember.setForeground(TEXT_WHITE);
        chkRemember.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkRemember.setBounds(40, y, BOX_W - 80, 22);
        chkRemember.setFocusPainted(false);
        box.add(chkRemember);
        y += 26;

        JButton btnForgot = new JButton("<html><u>Quên mật khẩu?</u></html>");
        btnForgot.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnForgot.setForeground(FIELD_FOCUS);
        btnForgot.setBackground(new Color(0, 0, 0, 0));
        btnForgot.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        btnForgot.setContentAreaFilled(false);
        btnForgot.setOpaque(false);
        btnForgot.setFocusPainted(false);
        btnForgot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnForgot.setHorizontalAlignment(SwingConstants.LEFT);
        btnForgot.setBounds(40, y, 200, 24);
        btnForgot.addActionListener(e -> showForgotPasswordDialog());
        box.add(btnForgot);
        y += 34;

        // ===== NÚT ĐĂNG NHẬP =====
        btnLogin = createLoginButton();
        btnLogin.setBounds(40, y, BOX_W - 80, 42);
        box.add(btnLogin);
        y += 55;

        // ===== LABEL LỖI =====
        lblError = new JLabel("", SwingConstants.CENTER);
        lblError.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblError.setForeground(new Color(239, 68, 68));
        lblError.setBounds(40, y, BOX_W - 80, 20);
        lblError.setVisible(false);
        box.add(lblError);

        applySavedLoginCredentials();
        return box;
    }

    /** Điền tài khoản/mật khẩu đã lưu khi bật ghi nhớ lần trước. */
    private void applySavedLoginCredentials() {
        if (!LOGIN_PREFS.getBoolean(PREF_REMEMBER, false)) return;
        chkRemember.setSelected(true);
        String u = LOGIN_PREFS.get(PREF_USERNAME, "");
        String p = LOGIN_PREFS.get(PREF_PASSWORD, "");
        txtUsername.setText(u);
        txtPassword.setText(p);
    }

    private void persistRememberLoginPreference(String username, String password) {
        if (chkRemember.isSelected()) {
            LOGIN_PREFS.putBoolean(PREF_REMEMBER, true);
            LOGIN_PREFS.put(PREF_USERNAME, username);
            LOGIN_PREFS.put(PREF_PASSWORD, password);
        } else {
            LOGIN_PREFS.putBoolean(PREF_REMEMBER, false);
            LOGIN_PREFS.remove(PREF_USERNAME);
            LOGIN_PREFS.remove(PREF_PASSWORD);
        }
        try {
            LOGIN_PREFS.flush();
        } catch (Exception ignored) {
        }
    }

    /**
     * Hiển thị toàn bộ tài khoản trong hệ thống (tên + mật khẩu lưu plaintext trong JSON).
     * Chỉ phù hợp môi trường nội bộ / demo — không dùng cho hệ thống thật công khai.
     */
    private void showForgotPasswordDialog() {
        SecurityRepository.reload();
        SecurityData data = SecurityRepository.load();
        String[] cols = {"Tên đăng nhập", "Mật khẩu", "Vai trò"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        for (UserAccount u : data.getUsers()) {
            String pass = u.getPassword() != null ? u.getPassword() : "";
            RoleDefinition rd = SecurityRepository.findRole(u.getRoleId());
            String roleLabel = rd != null ? rd.getDisplayName() + " (" + u.getRoleId() + ")" : u.getRoleId();
            model.addRow(new Object[]{u.getUsername(), pass, roleLabel});
        }
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(26);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(480, Math.min(280, 40 + data.getUsers().size() * 26)));

        JOptionPane.showMessageDialog(this, sp, "Danh sách tài khoản (hệ thống)",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // =========================================================================
    //  TẠO CÁC COMPONENT TÙY CHỈNH
    // =========================================================================
    private JTextField createRoundedTextField() {
        JTextField tf = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hasFocus() ? FIELD_FOCUS : FIELD_BORDER);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(0.75f, 0.75f, getWidth() - 1.5f, getHeight() - 1.5f, 10, 10));
                g2.dispose();
            }
        };
        tf.setOpaque(false);
        tf.setBackground(FIELD_BG);
        tf.setForeground(TEXT_WHITE);
        tf.setCaretColor(TEXT_WHITE);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        return tf;
    }

    private JPasswordField createRoundedPasswordField() {
        JPasswordField pf = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hasFocus() ? FIELD_FOCUS : FIELD_BORDER);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(0.75f, 0.75f, getWidth() - 1.5f, getHeight() - 1.5f, 10, 10));
                g2.dispose();
            }
        };
        pf.setOpaque(false);
        pf.setBackground(FIELD_BG);
        pf.setForeground(TEXT_WHITE);
        pf.setCaretColor(TEXT_WHITE);
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pf.setEchoChar('●');
        pf.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        return pf;
    }

    private JButton createLoginButton() {
        JButton btn = new JButton("ĐĂNG NHẬP") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                // Không vẽ viền
            }
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBackground(PRIMARY);
        btn.setForeground(TEXT_WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(PRIMARY_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(PRIMARY);
            }
        });

        btn.addActionListener(e -> performLogin());
        return btn;
    }

    // =========================================================================
    //  XỬ LÝ ĐĂNG NHẬP
    // =========================================================================
    private void performLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showError("Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        UserAccount user = SecurityRepository.findUser(username);
        if (user != null && user.getPassword().equals(password)) {
            RoleDefinition role = SecurityRepository.findRole(user.getRoleId());
            if (role == null) {
                showError("Vai trò \"" + user.getRoleId() + "\" không tồn tại!");
                return;
            }
            AppSession.login(username, role);
            persistRememberLoginPreference(username, password);
            lblError.setVisible(false);
            dispose();
            SwingUtilities.invokeLater(() -> {
                MainFrame mainFrame = new MainFrame();
                mainFrame.setVisible(true);
            });
        } else {
            showError("Sai tên đăng nhập hoặc mật khẩu!");
        }
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);

        // Hiệu ứng rung nhẹ cửa sổ
        Point original = getLocation();
        Timer timer = new Timer(30, null);
        final int[] count = {0};
        timer.addActionListener(e -> {
            if (count[0] < 6) {
                int dx = (count[0] % 2 == 0) ? 5 : -5;
                setLocation(original.x + dx, original.y);
                count[0]++;
            } else {
                setLocation(original);
                timer.stop();
            }
        });
        timer.start();
    }
}
