package com.hrm.security;

/**
 * Phiên đăng nhập hiện tại: tên người dùng và vai trò đã phân giải từ {@code hrm_security.json}.
 * <p>
 * Được gán sau khi đăng nhập thành công, xóa khi đăng xuất. Toàn bộ kiểm tra menu và nút CRUD
 * đều đọc quyền qua {@link #getPermission(String)}.
 * </p>
 */
public final class AppSession {

    private static String username;
    private static RoleDefinition role;

    private AppSession() {}

    public static void login(String user, RoleDefinition r) {
        username = user;
        role = r;
    }

    public static void logout() {
        username = null;
        role = null;
    }

    public static boolean isLoggedIn()      { return username != null && role != null; }
    public static String  getUsername()      { return username; }
    public static RoleDefinition getRole()   { return role; }
    public static String  getRoleDisplay()   { return role != null ? role.getDisplayName() : ""; }

    public static ModulePermission getPermission(String moduleId) {
        if (role == null) return ModulePermission.none();
        return role.getPermission(moduleId);
    }
}
