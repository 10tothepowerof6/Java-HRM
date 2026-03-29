package com.hrm.security;

/**
 * Mô hình một tài khoản đăng nhập ứng dụng, đồng bộ với phần tử {@code users} trong file {@code data/hrm_security.json}.
 * <p>
 * Mật khẩu hiện lưu dạng văn bản thuần (phù hợp môi trường nội bộ/demo); vai trò tham chiếu {@link RoleDefinition} theo {@code roleId}.
 * </p>
 */
public class UserAccount {
    private String username;
    private String password;
    private String roleId;

    public UserAccount() {}

    public UserAccount(String username, String password, String roleId) {
        this.username = username;
        this.password = password;
        this.roleId = roleId;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRoleId()   { return roleId; }

    public void setUsername(String u) { this.username = u; }
    public void setPassword(String p) { this.password = p; }
    public void setRoleId(String r)   { this.roleId = r; }
}
