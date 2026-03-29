package com.hrm.security;

import java.util.ArrayList;
import java.util.List;

/**
 * Gốc dữ liệu phân quyền: danh sách vai trò và danh sách tài khoản người dùng.
 * <p>
 * Được Gson đọc/ghi từ file {@code data/hrm_security.json}; cấu trúc tương ứng một tài liệu JSON duy nhất
 * chứa mảng {@code roles} và {@code users}.
 * </p>
 */
public class SecurityData {
    private List<RoleDefinition> roles = new ArrayList<>();
    private List<UserAccount> users = new ArrayList<>();

    public List<RoleDefinition> getRoles() { return roles; }
    public List<UserAccount> getUsers()    { return users; }

    public void setRoles(List<RoleDefinition> r) { this.roles = r; }
    public void setUsers(List<UserAccount> u)    { this.users = u; }
}
