package com.hrm.gui;

import com.hrm.security.*;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

/**
 * Quản trị tài khoản đăng nhập và vai trò — đọc/ghi {@code hrm_security.json}, ma trận quyền theo module.
 * <p>
 * Không dùng SQL; đồng bộ với {@link com.hrm.security.SecurityRepository}. Toolbar và double-click sửa tuân thủ
 * {@link com.hrm.security.PermissionHelper} trên module {@code taikhoan}.
 * </p>
 */
public class TaiKhoanQuyenPanel extends JPanel {

    private static final Color PRIMARY       = new Color(0, 82, 155);
    private static final Color PRIMARY_DARK  = new Color(0, 51, 102);
    private static final Color BG_CONTENT    = new Color(30, 41, 59);
    private static final Color BG_TOOLBAR    = new Color(22, 33, 52);
    private static final Color TEXT_WHITE    = new Color(248, 250, 252);
    private static final Color TEXT_LINK     = new Color(56, 189, 248);
    private static final Color TEXT_MUTED    = new Color(148, 163, 184);
    private static final Color TABLE_ROW_ALT = new Color(22, 33, 52);
    private static final Color TABLE_SEL     = new Color(0, 82, 155);
    private static final Color GRID_COLOR    = new Color(40, 50, 70);
    private static final Color BTN_SECONDARY = new Color(55, 65, 81);
    private static final Color BTN_DANGER    = new Color(220, 38, 38);
    private static final Color BTN_SUCCESS   = new Color(16, 185, 129);

    private JTable userTable, roleTable;
    private DefaultTableModel userModel, roleModel;

    /** Mật khẩu thật theo username (không hiển thị trong model). */
    private final Map<String, String> passwordByUser = new LinkedHashMap<>();
    /** true = hiện mật khẩu rõ; false = ẩn (•••) — mặc định ẩn. */
    private final Map<String, Boolean> passwordVisible = new HashMap<>();

    public TaiKhoanQuyenPanel() {
        setLayout(new BorderLayout());
        setBackground(BG_CONTENT);
        add(createToolbar(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);
        loadData();
    }

    // ── Toolbar ──
    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(0, 4));
        toolbar.setBackground(BG_TOOLBAR);
        toolbar.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        JLabel title = new JLabel("Quản lý Tài khoản & Quyền");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_WHITE);

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        leftPanel.setOpaque(false);
        leftPanel.add(title);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        rightPanel.setOpaque(false);

        JButton btnAddUser = createStyledButton("+ Tài khoản", PRIMARY);
        btnAddUser.addActionListener(e -> openAddUserDialog());
        rightPanel.add(btnAddUser);

        JButton btnEditUser = createStyledButton("Sửa TK", BTN_SECONDARY);
        btnEditUser.addActionListener(e -> editSelectedUser());
        rightPanel.add(btnEditUser);

        JButton btnDeleteUser = createStyledButton("Xóa TK", BTN_DANGER);
        btnDeleteUser.addActionListener(e -> deleteSelectedUser());
        rightPanel.add(btnDeleteUser);

        rightPanel.add(Box.createHorizontalStrut(16));

        JButton btnAddRole = createStyledButton("+ Vai trò", BTN_SUCCESS);
        btnAddRole.addActionListener(e -> openAddRoleDialog());
        rightPanel.add(btnAddRole);

        JButton btnEditRole = createStyledButton("Sửa VT", BTN_SECONDARY);
        btnEditRole.addActionListener(e -> editSelectedRole());
        rightPanel.add(btnEditRole);

        JButton btnDeleteRole = createStyledButton("Xóa VT", BTN_DANGER);
        btnDeleteRole.addActionListener(e -> deleteSelectedRole());
        rightPanel.add(btnDeleteRole);

        JPanel rows = new JPanel(new BorderLayout());
        rows.setOpaque(false);
        rows.add(leftPanel, BorderLayout.WEST);
        rows.add(rightPanel, BorderLayout.EAST);

        toolbar.add(rows, BorderLayout.CENTER);

        PermissionHelper.applyVisible(btnAddUser, PermissionHelper.canAdd("taikhoan"));
        PermissionHelper.applyVisible(btnEditUser, PermissionHelper.canEdit("taikhoan"));
        PermissionHelper.applyVisible(btnDeleteUser, PermissionHelper.canDelete("taikhoan"));
        PermissionHelper.applyVisible(btnAddRole, PermissionHelper.canAdd("taikhoan"));
        PermissionHelper.applyVisible(btnEditRole, PermissionHelper.canEdit("taikhoan"));
        PermissionHelper.applyVisible(btnDeleteRole, PermissionHelper.canDelete("taikhoan"));

        return toolbar;
    }

    // ── Content: Split pane with Users (top) and Roles (bottom) ──
    private JComponent createContent() {
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setBackground(BG_CONTENT);
        split.setDividerLocation(260);
        split.setResizeWeight(0.5);
        split.setBorder(BorderFactory.createEmptyBorder());

        split.setTopComponent(createUserTablePanel());
        split.setBottomComponent(createRoleTablePanel());
        split.setDividerSize(8);
        return split;
    }

    /** Giống ThuongPanel: tiêu đề JLabel + scroll không TitledBorder (tránh vùng trắng). */
    private JPanel createUserTablePanel() {
        String[] cols = {"Tên đăng nhập", "Mật khẩu", "Vai trò", "Hiện / Ẩn"};
        userModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        userTable = buildTable(userModel);
        userTable.getColumnModel().getColumn(1).setCellRenderer(new PasswordColumnRenderer());
        userTable.getColumnModel().getColumn(3).setMinWidth(72);
        userTable.getColumnModel().getColumn(3).setMaxWidth(88);
        userTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        userTable.getColumnModel().getColumn(3).setCellRenderer(new TogglePasswordColumnRenderer());
        userTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = userTable.rowAtPoint(e.getPoint());
                int col = userTable.columnAtPoint(e.getPoint());
                if (row < 0) return;

                if (col == 3) {
                    if (e.getClickCount() != 1) return;
                    String username = (String) userModel.getValueAt(row, 0);
                    if (username == null) return;
                    String pwd = passwordByUser.getOrDefault(username, "");
                    if (pwd.isEmpty()) return;
                    boolean now = passwordVisible.getOrDefault(username, false);
                    passwordVisible.put(username, !now);
                    userModel.fireTableRowsUpdated(row, row);
                    return;
                }

                if (e.getClickCount() == 2 && PermissionHelper.canEdit("taikhoan")) {
                    String username = (String) userModel.getValueAt(row, 0);
                    if (username != null) openEditUserDialog(username);
                }
            }
        });
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(BG_CONTENT);
        wrap.setBorder(BorderFactory.createEmptyBorder(8, 10, 4, 10));
        JLabel lbl = new JLabel("Danh sách Tài khoản");
        lbl.setForeground(TEXT_WHITE);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        wrap.add(lbl, BorderLayout.NORTH);
        wrap.add(createStyledScrollPane(userTable), BorderLayout.CENTER);
        return wrap;
    }

    private JPanel createRoleTablePanel() {
        String[] cols = {"Mã vai trò", "Tên hiển thị"};
        roleModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        roleTable = buildTable(roleModel);
        roleTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() != 2 || !PermissionHelper.canEdit("taikhoan")) return;
                int row = roleTable.rowAtPoint(e.getPoint());
                if (row < 0) return;
                roleTable.setRowSelectionInterval(row, row);
                String roleId = (String) roleModel.getValueAt(row, 0);
                openEditRoleById(roleId);
            }
        });
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(BG_CONTENT);
        wrap.setBorder(BorderFactory.createEmptyBorder(4, 10, 8, 10));
        JLabel lbl = new JLabel("Danh sách Vai trò");
        lbl.setForeground(TEXT_WHITE);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        wrap.add(lbl, BorderLayout.NORTH);
        wrap.add(createStyledScrollPane(roleTable), BorderLayout.CENTER);
        return wrap;
    }

    private JScrollPane createStyledScrollPane(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BG_CONTENT);
        scrollPane.getViewport().setOpaque(true);
        scrollPane.setBackground(BG_CONTENT);
        return scrollPane;
    }

    // ── Data ──
    public void loadData() {
        SecurityRepository.reload();
        SecurityData data = SecurityRepository.load();

        passwordByUser.clear();
        userModel.setRowCount(0);
        for (UserAccount u : data.getUsers()) {
            String un = u.getUsername();
            passwordByUser.put(un, u.getPassword() != null ? u.getPassword() : "");
            if (!passwordVisible.containsKey(un)) passwordVisible.put(un, false);
            RoleDefinition rd = SecurityRepository.findRole(u.getRoleId());
            String roleName = rd != null ? rd.getDisplayName() : u.getRoleId();
            userModel.addRow(new Object[]{un, "", roleName, ""});
        }
        passwordVisible.keySet().removeIf(k -> !passwordByUser.containsKey(k));

        roleModel.setRowCount(0);
        for (RoleDefinition r : data.getRoles()) {
            roleModel.addRow(new Object[]{r.getId(), r.getDisplayName()});
        }
    }

    // ── Add User Dialog ──
    private void openAddUserDialog() {
        SecurityData data = SecurityRepository.load();
        String[] roleNames = data.getRoles().stream()
                .map(r -> r.getId() + " — " + r.getDisplayName())
                .toArray(String[]::new);
        if (roleNames.length == 0) {
            JOptionPane.showMessageDialog(this, "Chưa có vai trò nào! Tạo vai trò trước.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JTextField txtUser = new JTextField(20);
        JPasswordField txtPass = new JPasswordField(20);
        JComboBox<String> cboRole = new JComboBox<>(roleNames);

        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        panel.add(new JLabel("Tên đăng nhập:"));
        panel.add(txtUser);
        panel.add(new JLabel("Mật khẩu:"));
        panel.add(txtPass);
        panel.add(new JLabel("Vai trò:"));
        panel.add(cboRole);

        int result = JOptionPane.showConfirmDialog(this, panel, "Thêm tài khoản", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        String username = txtUser.getText().trim();
        String password = new String(txtPass.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên đăng nhập và mật khẩu không được trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (SecurityRepository.findUser(username) != null) {
            JOptionPane.showMessageDialog(this, "Tên đăng nhập đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String selectedRole = data.getRoles().get(cboRole.getSelectedIndex()).getId();

        data.getUsers().add(new UserAccount(username, password, selectedRole));
        SecurityRepository.save(data);
        loadData();
    }

    private void editSelectedUser() {
        int row = userTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Chọn tài khoản cần sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String username = (String) userModel.getValueAt(row, 0);
        if (username != null) openEditUserDialog(username);
    }

    /**
     * Sửa tên đăng nhập, mật khẩu (để trống = giữ nguyên), vai trò.
     * Tài khoản {@code admin} không đổi được tên đăng nhập.
     */
    private void openEditUserDialog(String originalUsername) {
        SecurityData data = SecurityRepository.load();
        UserAccount ua = null;
        for (UserAccount u : data.getUsers()) {
            if (u.getUsername().equals(originalUsername)) {
                ua = u;
                break;
            }
        }
        if (ua == null) return;

        List<RoleDefinition> roles = data.getRoles();
        if (roles.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chưa có vai trò nào!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] roleNames = roles.stream()
                .map(r -> r.getId() + " — " + r.getDisplayName())
                .toArray(String[]::new);

        JTextField txtUser = new JTextField(originalUsername, 20);
        boolean isAdmin = "admin".equalsIgnoreCase(originalUsername);
        if (isAdmin) {
            txtUser.setEditable(false);
            txtUser.setToolTipText("Không đổi được tên tài khoản admin mặc định.");
        }

        JPasswordField txtPass = new JPasswordField(20);
        txtPass.setToolTipText("Để trống nếu giữ nguyên mật khẩu hiện tại.");

        JComboBox<String> cboRole = new JComboBox<>(roleNames);
        int roleIdx = 0;
        for (int i = 0; i < roles.size(); i++) {
            if (roles.get(i).getId().equals(ua.getRoleId())) {
                roleIdx = i;
                break;
            }
        }
        cboRole.setSelectedIndex(roleIdx);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Tên đăng nhập:"), gbc);
        gbc.gridx = 1;
        panel.add(txtUser, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Mật khẩu mới:"), gbc);
        gbc.gridx = 1;
        panel.add(txtPass, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        JLabel lblHint = new JLabel("(Để trống nếu không đổi mật khẩu)");
        lblHint.setFont(lblHint.getFont().deriveFont(Font.ITALIC, 11f));
        lblHint.setForeground(new Color(148, 163, 184));
        panel.add(lblHint, gbc);
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        panel.add(new JLabel("Vai trò:"), gbc);
        gbc.gridx = 1;
        panel.add(cboRole, gbc);

        int result = JOptionPane.showConfirmDialog(this, panel, "Sửa tài khoản",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String newUsername = txtUser.getText().trim();
        if (newUsername.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên đăng nhập không được trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!isAdmin && !newUsername.equals(originalUsername)
                && SecurityRepository.findUser(newUsername) != null) {
            JOptionPane.showMessageDialog(this, "Tên đăng nhập đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String newPass = new String(txtPass.getPassword());
        String selectedRoleId = roles.get(cboRole.getSelectedIndex()).getId();

        if (!newUsername.equals(originalUsername)) {
            Boolean vis = passwordVisible.remove(originalUsername);
            String pwdMap = passwordByUser.remove(originalUsername);
            ua.setUsername(newUsername);
            if (pwdMap != null) passwordByUser.put(newUsername, pwdMap);
            if (vis != null) passwordVisible.put(newUsername, vis);
        }

        if (!newPass.isEmpty()) {
            ua.setPassword(newPass);
        }
        ua.setRoleId(selectedRoleId);

        SecurityRepository.save(data);
        loadData();
    }

    // ── Delete User ──
    private void deleteSelectedUser() {
        int row = userTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Chọn tài khoản cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String username = (String) userModel.getValueAt(row, 0);
        if ("admin".equals(username)) {
            JOptionPane.showMessageDialog(this, "Không thể xóa tài khoản admin mặc định!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        SecurityData data = SecurityRepository.load();
        if (data.getUsers().size() <= 1) {
            JOptionPane.showMessageDialog(this, "Không thể xóa tài khoản cuối cùng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Xóa tài khoản \"" + username + "\"?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        data.getUsers().removeIf(u -> u.getUsername().equals(username));
        SecurityRepository.save(data);
        loadData();
    }

    // ── Add Role Dialog ──
    private void openAddRoleDialog() {
        showRoleDialog(null);
    }

    private void editSelectedRole() {
        int row = roleTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Chọn vai trò cần sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String roleId = (String) roleModel.getValueAt(row, 0);
        openEditRoleById(roleId);
    }

    /** Mở dialog sửa vai trò theo mã (dùng cho nút Sửa VT và double-click dòng). */
    private void openEditRoleById(String roleId) {
        if (roleId == null) return;
        RoleDefinition rd = SecurityRepository.findRole(roleId);
        if (rd != null) showRoleDialog(rd);
    }

    /** Show create/edit role dialog. Pass null to create new. */
    private void showRoleDialog(RoleDefinition existing) {
        boolean isNew = (existing == null);
        String dialogTitle = isNew ? "Tạo vai trò mới" : "Sửa vai trò: " + existing.getDisplayName();

        JTextField txtId = new JTextField(isNew ? "" : existing.getId(), 20);
        JTextField txtName = new JTextField(isNew ? "" : existing.getDisplayName(), 20);
        if (!isNew) txtId.setEditable(false);

        // Permission matrix
        String[] moduleIds = SecurityRepository.ALL_MODULES;
        Map<String, String> labels = SecurityRepository.MODULE_LABELS;

        DefaultTableModel permModel = new DefaultTableModel(new String[]{"Module", "Xem", "Thêm", "Sửa", "Xóa"}, 0) {
            @Override public Class<?> getColumnClass(int col) { return col == 0 ? String.class : Boolean.class; }
            @Override public boolean isCellEditable(int r, int c) { return c > 0; }
        };

        for (String mid : moduleIds) {
            ModulePermission mp = isNew ? ModulePermission.none() : existing.getPermission(mid);
            permModel.addRow(new Object[]{labels.getOrDefault(mid, mid), mp.isView(), mp.isAdd(), mp.isEdit(), mp.isDelete()});
        }

        JTable permTable = new JTable(permModel);
        permTable.setRowHeight(28);
        permTable.setBackground(BG_CONTENT);
        permTable.setForeground(TEXT_WHITE);
        permTable.setGridColor(GRID_COLOR);
        permTable.setSelectionBackground(TABLE_SEL);
        permTable.setSelectionForeground(TEXT_WHITE);
        applyDarkTableChrome(permTable);
        permTable.getColumnModel().getColumn(0).setPreferredWidth(180);
        for (int i = 1; i <= 4; i++) permTable.getColumnModel().getColumn(i).setPreferredWidth(60);
        JScrollPane permScroll = new JScrollPane(permTable);
        permScroll.getViewport().setBackground(BG_CONTENT);
        permScroll.setBorder(BorderFactory.createLineBorder(GRID_COLOR));
        permScroll.setPreferredSize(new Dimension(500, 380));

        JButton btnAll = new JButton("Chọn tất cả");
        btnAll.addActionListener(e -> {
            for (int r = 0; r < permModel.getRowCount(); r++)
                for (int c = 1; c <= 4; c++) permModel.setValueAt(true, r, c);
        });
        JButton btnNone = new JButton("Bỏ chọn tất cả");
        btnNone.addActionListener(e -> {
            for (int r = 0; r < permModel.getRowCount(); r++)
                for (int c = 1; c <= 4; c++) permModel.setValueAt(false, r, c);
        });

        JPanel helperPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        helperPanel.add(btnAll);
        helperPanel.add(btnNone);

        JPanel topFields = new JPanel(new GridLayout(2, 2, 8, 6));
        topFields.add(new JLabel("Mã vai trò (ID):"));
        topFields.add(txtId);
        topFields.add(new JLabel("Tên hiển thị:"));
        topFields.add(txtName);

        JPanel dialogPanel = new JPanel(new BorderLayout(0, 8));
        dialogPanel.add(topFields, BorderLayout.NORTH);
        dialogPanel.add(permScroll, BorderLayout.CENTER);
        dialogPanel.add(helperPanel, BorderLayout.SOUTH);

        int result = JOptionPane.showConfirmDialog(this, dialogPanel, dialogTitle,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String id = txtId.getText().trim();
        String name = txtName.getText().trim();
        if (id.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Mã vai trò và tên hiển thị không được trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SecurityData data = SecurityRepository.load();

        RoleDefinition role;
        if (isNew) {
            if (SecurityRepository.findRole(id) != null) {
                JOptionPane.showMessageDialog(this, "Mã vai trò đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            role = new RoleDefinition(id, name);
            data.getRoles().add(role);
        } else {
            role = SecurityRepository.findRole(id);
            if (role == null) return;
            role.setDisplayName(name);
        }

        Map<String, ModulePermission> perms = new LinkedHashMap<>();
        for (int r = 0; r < permModel.getRowCount(); r++) {
            boolean v = (Boolean) permModel.getValueAt(r, 1);
            boolean a = (Boolean) permModel.getValueAt(r, 2);
            boolean ed = (Boolean) permModel.getValueAt(r, 3);
            boolean d = (Boolean) permModel.getValueAt(r, 4);
            perms.put(moduleIds[r], new ModulePermission(v, a, ed, d));
        }
        role.setPermissions(perms);

        SecurityRepository.save(data);
        loadData();
    }

    // ── Delete Role ──
    private void deleteSelectedRole() {
        int row = roleTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Chọn vai trò cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String roleId = (String) roleModel.getValueAt(row, 0);
        if ("ADMIN".equals(roleId)) {
            JOptionPane.showMessageDialog(this, "Không thể xóa vai trò ADMIN mặc định!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        SecurityData data = SecurityRepository.load();
        boolean inUse = data.getUsers().stream().anyMatch(u -> u.getRoleId().equals(roleId));
        if (inUse) {
            JOptionPane.showMessageDialog(this, "Vai trò đang được sử dụng bởi tài khoản! Không thể xóa.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Xóa vai trò \"" + roleId + "\"?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        data.getRoles().removeIf(r -> r.getId().equals(roleId));
        SecurityRepository.save(data);
        loadData();
    }

    // ── Helpers ──
    private JTable buildTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(34);
        table.setBackground(BG_CONTENT);
        table.setForeground(TEXT_WHITE);
        table.setSelectionBackground(TABLE_SEL);
        table.setSelectionForeground(TEXT_WHITE);
        table.setGridColor(GRID_COLOR);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);

        DefaultTableCellRenderer bodyRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                if (!sel) setBackground(r % 2 == 0 ? BG_CONTENT : TABLE_ROW_ALT);
                setForeground(TEXT_WHITE);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        };
        table.setDefaultRenderer(Object.class, bodyRenderer);

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 38));
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                lbl.setBackground(PRIMARY_DARK);
                lbl.setForeground(TEXT_WHITE);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                lbl.setOpaque(true);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 1, GRID_COLOR),
                        BorderFactory.createEmptyBorder(0, 8, 0, 8)));
                lbl.setHorizontalAlignment(SwingConstants.LEFT);
                return lbl;
            }
        });

        return table;
    }

    /** Header + nền dòng cho bảng phụ (dialog quyền). */
    private void applyDarkTableChrome(JTable table) {
        DefaultTableCellRenderer body = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                if (!sel) comp.setBackground(r % 2 == 0 ? BG_CONTENT : TABLE_ROW_ALT);
                comp.setForeground(TEXT_WHITE);
                return comp;
            }
        };
        table.setDefaultRenderer(Object.class, body);
        table.setDefaultRenderer(Boolean.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                String s = Boolean.TRUE.equals(val) ? "\u2713" : "";
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, s, sel, foc, r, c);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                if (!sel) lbl.setBackground(r % 2 == 0 ? BG_CONTENT : TABLE_ROW_ALT);
                lbl.setForeground(Boolean.TRUE.equals(val) ? TEXT_LINK : TEXT_MUTED);
                return lbl;
            }
        });

        JTableHeader h = table.getTableHeader();
        h.setPreferredSize(new Dimension(0, 34));
        h.setReorderingAllowed(false);
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                lbl.setBackground(PRIMARY_DARK);
                lbl.setForeground(TEXT_WHITE);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lbl.setOpaque(true);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 1, GRID_COLOR),
                        BorderFactory.createEmptyBorder(0, 6, 0, 6)));
                return lbl;
            }
        });
    }

    /** Cột mật khẩu: ẩn bằng • hoặc hiện rõ theo passwordVisible. */
    private class PasswordColumnRenderer extends DefaultTableCellRenderer {
        PasswordColumnRenderer() {
            setHorizontalAlignment(SwingConstants.LEFT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row, int col) {
            String username = (String) userModel.getValueAt(row, 0);
            String pwd = passwordByUser.getOrDefault(username, "");
            boolean show = passwordVisible.getOrDefault(username, false);
            String display;
            if (pwd.isEmpty()) display = "";
            else if (show) display = pwd;
            else {
                StringBuilder sb = new StringBuilder(pwd.length());
                for (int i = 0; i < pwd.length(); i++) sb.append('\u2022');
                display = sb.toString();
            }
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, display, sel, foc, row, col);
            if (!sel) lbl.setBackground(row % 2 == 0 ? BG_CONTENT : TABLE_ROW_ALT);
            lbl.setForeground(TEXT_WHITE);
            lbl.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            return lbl;
        }
    }

    /** Cột bấm Hiện / Ẩn (giống ý tưởng nút trên màn login). */
    private class TogglePasswordColumnRenderer extends DefaultTableCellRenderer {
        TogglePasswordColumnRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row, int col) {
            String username = (String) userModel.getValueAt(row, 0);
            boolean show = passwordVisible.getOrDefault(username, false);
            String pwd = passwordByUser.getOrDefault(username, "");
            String text = pwd.isEmpty() ? "—" : (show ? "Ẩn" : "Hiện");
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, text, sel, foc, row, col);
            if (!sel) lbl.setBackground(row % 2 == 0 ? BG_CONTENT : TABLE_ROW_ALT);
            lbl.setForeground(pwd.isEmpty() ? TEXT_MUTED : TEXT_LINK);
            lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 12f));
            return lbl;
        }
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(TEXT_WHITE);
        btn.setBackground(bgColor);
        btn.setPreferredSize(new Dimension(120, 34));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        Color hover = bgColor.brighter();
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(bgColor); }
        });
        return btn;
    }
}
