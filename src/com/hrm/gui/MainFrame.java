package com.hrm.gui;

import com.hrm.security.AppSession;
import com.hrm.security.PermissionHelper;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Khung chính sau đăng nhập: thanh tiêu đề tùy chỉnh, sidebar module và vùng nội dung {@link CardLayout}.
 * <p>
 * Mục menu ẩn theo {@link com.hrm.security.PermissionHelper#canView(String)}; chuyển sang Tổng quan
 * thì gọi {@link TongQuanPanel#refreshData()}. Footer hiển thị user/role và đăng xuất.
 * </p>
 */
public class MainFrame extends JFrame {

    // ===== BẢNG MÀU =====
    private static final Color PRIMARY_DARK    = new Color(0, 51, 102);
    private static final Color PRIMARY         = new Color(0, 82, 155);
    private static final Color BG_SIDEBAR      = new Color(15, 23, 42);
    private static final Color BG_CONTENT      = new Color(30, 41, 59);
    private static final Color TEXT_WHITE      = new Color(248, 250, 252);
    private static final Color TEXT_MUTED      = new Color(148, 163, 184);
    private static final Color CLOSE_RED       = new Color(232, 17, 35);
    private static final Color MENU_HOVER_BG   = new Color(30, 41, 59);
    private static final Color MENU_ACTIVE_BG  = new Color(12, 35, 64);

    private static final int SIDEBAR_W = 240;
    private static final int TITLEBAR_H = 40;

    // ===== COMPONENTS =====
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JButton activeMenuBtn = null;

    // Kéo thả cửa sổ
    private Point dragOffset;
    private boolean isMaximized = false;
    private Rectangle restoreBounds;
    private JButton btnMaximize;

    public MainFrame() {
        initUI();
    }

    private void initUI() {
        setTitle("Quản Lý Nhân Sự");
        
        // Set Taskbar Icon
        try {
            String basePath = System.getProperty("user.dir") + File.separator + "imgs" + File.separator;
            setIconImage(ImageIO.read(new File(basePath + "LogoWhite.png")));
        } catch (Exception e) {
            // Ignore icon error
        }

        setUndecorated(true);
        setSize(1280, 720);
        setMinimumSize(new Dimension(1024, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_CONTENT);
        setContentPane(mainPanel);

        // Title bar
        mainPanel.add(createTitleBar(), BorderLayout.NORTH);

        // Sidebar
        mainPanel.add(createSidebar(), BorderLayout.WEST);

        // Content Area
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BG_CONTENT);

        contentPanel.add(new TongQuanPanel(), "dashboard");
        contentPanel.add(new NhanVienPanel(), "nhanvien");
        contentPanel.add(new PhongBanPanel(), "phongban");
        contentPanel.add(new ChucVuPanel(), "chucvu");
        contentPanel.add(new HopDongPanel(), "hopdong");
        contentPanel.add(new DeAnPanel(), "dean");
        contentPanel.add(new ChamCongPanel(), "chamcong");
        contentPanel.add(new NghiPhepPanel(), "nghiphep");
        contentPanel.add(new PhuCapPanel(), "phucap");
        contentPanel.add(new ThuongPanel(), "thuong");
        contentPanel.add(new BangLuongThangPanel(), "bangluong");
        contentPanel.add(new ThamSoPanel(), "caidat");
        contentPanel.add(new TaiKhoanQuyenPanel(), "taikhoan");

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        cardLayout.show(contentPanel, "dashboard");
    }

    // =========================================================================
    //  CUSTOM TITLE BAR
    // =========================================================================
    private JPanel createTitleBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(PRIMARY_DARK);
        bar.setPreferredSize(new Dimension(0, TITLEBAR_H));

        // Bên trái: Logo + tiêu đề
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setOpaque(false);

        try {
            String basePath = System.getProperty("user.dir") + File.separator + "imgs" + File.separator;
            BufferedImage originalLogo = ImageIO.read(new File(basePath + "LogoWhite.png"));
            Image logoSmall = progressiveScale(originalLogo, 26, 26);
            JLabel lblLogo = new JLabel(new ImageIcon(logoSmall));
            lblLogo.setBorder(BorderFactory.createEmptyBorder(7, 4, 7, 0));
            leftPanel.add(lblLogo);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JLabel lblTitle = new JLabel("Quản Lý Nhân Sự");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(TEXT_WHITE);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
        leftPanel.add(lblTitle);
        bar.add(leftPanel, BorderLayout.WEST);

        // Bên phải: 3 nút điều khiển
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPanel.setOpaque(false);

        JButton btnMin = createTitleBarButton("_", false);
        btnMin.addActionListener(e -> setState(Frame.ICONIFIED));
        rightPanel.add(btnMin);

        btnMaximize = createTitleBarButton("[]", false);
        btnMaximize.addActionListener(e -> toggleMaximize());
        rightPanel.add(btnMaximize);

        JButton btnClose = createTitleBarButton("X", true);
        btnClose.addActionListener(e -> System.exit(0));
        rightPanel.add(btnClose);

        bar.add(rightPanel, BorderLayout.EAST);

        // Kéo thả + double click
        bar.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { dragOffset = e.getPoint(); }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) toggleMaximize();
            }
        });
        bar.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isMaximized) {
                    toggleMaximize();
                    dragOffset = new Point(restoreBounds.width / 2, TITLEBAR_H / 2);
                }
                Point cur = e.getLocationOnScreen();
                setLocation(cur.x - dragOffset.x, cur.y - dragOffset.y);
            }
        });

        return bar;
    }

    private void toggleMaximize() {
        if (isMaximized) {
            setBounds(restoreBounds);
            isMaximized = false;
        } else {
            restoreBounds = getBounds();
            setBounds(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds());
            isMaximized = true;
        }
    }

    private JButton createTitleBarButton(String text, boolean isClose) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(TEXT_WHITE);
        btn.setBackground(PRIMARY_DARK);
        btn.setPreferredSize(new Dimension(46, TITLEBAR_H));
        btn.setBorder(null);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        Color hoverColor = isClose ? CLOSE_RED : new Color(0, 70, 140);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { btn.setBackground(hoverColor); }

            @Override
            public void mouseExited(MouseEvent e) { btn.setBackground(PRIMARY_DARK); }
        });

        return btn;
    }

    // =========================================================================
    //  SIDEBAR
    // =========================================================================
    private JPanel createSidebar() {
        JPanel sidePanel = new JPanel(new BorderLayout());
        sidePanel.setBackground(BG_SIDEBAR);
        sidePanel.setPreferredSize(new Dimension(SIDEBAR_W, 0));
        sidePanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(30, 41, 59)));

        // Menu
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(BG_SIDEBAR);
        menuPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

        JLabel lblMenu = new JLabel("   MENU CHÍNH");
        lblMenu.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblMenu.setForeground(TEXT_MUTED);
        lblMenu.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblMenu.setMaximumSize(new Dimension(SIDEBAR_W, 25));
        lblMenu.setBorder(BorderFactory.createEmptyBorder(0, 16, 8, 0));
        menuPanel.add(lblMenu);

        // Các nút menu — tiếng Việt CÓ DẤU
        // Sidebar buttons — only shown when user has view permission
        String[][] menuDefs = {
            {"Tổng quan",          "dashboard",  "ic_dashboard.png"},
            {"Nhân viên",          "nhanvien",   "ic_employees.png"},
            {"Phòng ban",          "phongban",   "ic_departments.png"},
            {"Chức vụ",            "chucvu",     "ic_roles.png"},
            {"Hợp đồng",          "hopdong",    "ic_contracts.png"},
            {"Đề án",              "dean",       "ic_project.png"},
            {"Chấm công",          "chamcong",   "ic_timekeeping.png"},
            {"Nghỉ phép",          "nghiphep",   "ic_leave.png"},
            {"Phụ cấp",            "phucap",     "ic_allowance.png"},
            {"Thưởng",             "thuong",     "ic_bonus.png"},
            {"Bảng lương",         "bangluong",  "ic_payroll.png"},
            {"Tài khoản & Quyền", "taikhoan",   "ic_account.png"},
        };

        JButton firstVisible = null;
        for (String[] def : menuDefs) {
            if (!PermissionHelper.canView(def[1])) continue;
            JButton btn = createMenuButton(def[0], def[1], def[2]);
            menuPanel.add(btn);
            if (firstVisible == null) firstVisible = btn;
        }

        menuPanel.add(Box.createVerticalGlue());

        JButton btnCaiDat = null;
        if (PermissionHelper.canView("caidat")) {
            btnCaiDat = createMenuButton("Cài đặt", "caidat", "ic_settings.png");
            menuPanel.add(btnCaiDat);
        }

        sidePanel.add(menuPanel, BorderLayout.CENTER);
        if (firstVisible != null) setActiveMenu(firstVisible);

        // Phần dưới: Thông tin user + Đăng xuất
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(10, 18, 36));
        bottomPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(30, 41, 59)),
                BorderFactory.createEmptyBorder(10, 16, 10, 12)
        ));

        String displayName = AppSession.isLoggedIn() ? AppSession.getUsername() : "admin";
        String displayRole = AppSession.isLoggedIn() ? AppSession.getRoleDisplay() : "Quản trị viên";

        JPanel userInfo = new JPanel();
        userInfo.setLayout(new BoxLayout(userInfo, BoxLayout.Y_AXIS));
        userInfo.setOpaque(false);

        JLabel lblName = new JLabel(displayName);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblName.setForeground(TEXT_WHITE);
        userInfo.add(lblName);

        JLabel lblRole = new JLabel(displayRole);
        lblRole.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblRole.setForeground(TEXT_MUTED);
        userInfo.add(lblRole);

        bottomPanel.add(userInfo, BorderLayout.CENTER);

        JButton btnLogout = new JButton("Đăng xuất");
        btnLogout.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnLogout.setForeground(new Color(239, 68, 68));
        btnLogout.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        btnLogout.setFocusPainted(false);
        btnLogout.setContentAreaFilled(false);
        btnLogout.setOpaque(false);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc muốn đăng xuất?", "Xác nhận",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) {
                AppSession.logout();
                dispose();
                SwingUtilities.invokeLater(() -> {
                    LoginDialog login = new LoginDialog();
                    login.setVisible(true);
                });
            }
        });
        bottomPanel.add(btnLogout, BorderLayout.EAST);

        sidePanel.add(bottomPanel, BorderLayout.SOUTH);
        return sidePanel;
    }

    /**
     * Tạo nút menu sidebar.
     * Dùng setContentAreaFilled(false) + setOpaque(true) để TỰ kiểm soát màu nền,
     * không cho Windows LAF đè màu mặc định.
     */
    private JButton createMenuButton(String text, String cardName, String iconPath) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(TEXT_MUTED);
        btn.setBackground(BG_SIDEBAR);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(SIDEBAR_W, 42));
        btn.setPreferredSize(new Dimension(SIDEBAR_W, 42));
        btn.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 0));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        try {
            String basePath = System.getProperty("user.dir") + File.separator + "imgs" + File.separator;
            BufferedImage img = ImageIO.read(new File(basePath + iconPath));
            Image icon = progressiveScale(img, 18, 18);
            btn.setIcon(new ImageIcon(icon));
            btn.setIconTextGap(12);
        } catch (Exception e) {
            System.err.println("Could not load icon: " + iconPath);
        }

        // QUAN TRỌNG: Tắt content area fill của LAF, tự vẽ nền bằng setOpaque + setBackground
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn != activeMenuBtn) {
                    btn.setBackground(MENU_HOVER_BG);
                    btn.setForeground(TEXT_WHITE);
                    btn.repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (btn != activeMenuBtn) {
                    btn.setBackground(BG_SIDEBAR);
                    btn.setForeground(TEXT_MUTED);
                    btn.repaint();
                }
            }
        });

        btn.addActionListener(e -> {
            setActiveMenu(btn);
            cardLayout.show(contentPanel, cardName);
            
            // Làm mới dữ liệu nếu là màn hình tổng quan
            if ("dashboard".equals(cardName)) {
                for (Component comp : contentPanel.getComponents()) {
                    if (comp.isVisible() && comp instanceof TongQuanPanel) {
                        ((TongQuanPanel) comp).refreshData();
                    }
                }
            }
        });

        return btn;
    }

    private void setActiveMenu(JButton btn) {
        if (activeMenuBtn != null) {
            activeMenuBtn.setBackground(BG_SIDEBAR);
            activeMenuBtn.setForeground(TEXT_MUTED);
            activeMenuBtn.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));
            activeMenuBtn.repaint();
        }
        activeMenuBtn = btn;
        activeMenuBtn.setBackground(MENU_ACTIVE_BG);
        activeMenuBtn.setForeground(TEXT_WHITE);
        activeMenuBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, PRIMARY),
                BorderFactory.createEmptyBorder(0, 9, 0, 0)
        ));
        activeMenuBtn.repaint();
    }

    // =========================================================================
    //  PLACEHOLDER PANEL
    // =========================================================================
    private JPanel createPlaceholderPanel(String title, String subtitle) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_CONTENT);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(TEXT_WHITE);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(lblTitle);

        center.add(Box.createVerticalStrut(10));

        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSub.setForeground(TEXT_MUTED);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(lblSub);

        panel.add(center);
        return panel;
    }

    /**
     * Progressive scale ảnh chất lượng cao.
     */
    private static Image progressiveScale(BufferedImage src, int targetW, int targetH) {
        int type = (src.getTransparency() == Transparency.OPAQUE)
                ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage current = src;
        int w = src.getWidth();
        int h = src.getHeight();

        while (w > targetW * 2 || h > targetH * 2) {
            w = Math.max(w / 2, targetW);
            h = Math.max(h / 2, targetH);
            BufferedImage tmp = new BufferedImage(w, h, type);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            g2.drawImage(current, 0, 0, w, h, null);
            g2.dispose();
            current = tmp;
        }

        BufferedImage result = new BufferedImage(targetW, targetH, type);
        Graphics2D g2 = result.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2.drawImage(current, 0, 0, targetW, targetH, null);
        g2.dispose();
        return result;
    }
}
