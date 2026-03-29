package com.hrm.security;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Định nghĩa một vai trò (role): mã định danh, tên hiển thị và ma trận quyền theo từng module.
 * <p>
 * Mỗi khóa trong {@code permissions} là {@code moduleId} (ví dụ {@code nhanvien}, {@code bangluong}).
 * {@link #getPermission(String)} luôn trả về đối tượng hợp lệ — nếu chưa cấu hình module thì coi như {@link ModulePermission#none()}.
 * </p>
 */
public class RoleDefinition {
    private String id;
    private String displayName;
    private Map<String, ModulePermission> permissions = new LinkedHashMap<>();

    public RoleDefinition() {}

    public RoleDefinition(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId()          { return id; }
    public String getDisplayName() { return displayName; }
    public Map<String, ModulePermission> getPermissions() { return permissions; }

    public void setId(String id)                  { this.id = id; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setPermissions(Map<String, ModulePermission> p) { this.permissions = p; }

    public ModulePermission getPermission(String moduleId) {
        ModulePermission mp = permissions.get(moduleId);
        return mp != null ? mp : ModulePermission.none();
    }
}
