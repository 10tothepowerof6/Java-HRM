package com.hrm.security;

import javax.swing.JButton;

/**
 * Tiện ích tĩnh kiểm tra quyền theo {@link AppSession} và ẩn/hiện nút trên giao diện.
 * <p>
 * Mỗi {@code moduleId} phải khớp một mục trong {@link SecurityRepository#ALL_MODULES}.
 * Quyền Excel yêu cầu đủ thêm + sửa + xóa trên cùng module để tránh xuất/nhập khi không được phép chỉnh dữ liệu.
 * </p>
 */
public final class PermissionHelper {

    private PermissionHelper() {}

    public static boolean canView(String moduleId)  { return AppSession.getPermission(moduleId).isView(); }
    public static boolean canAdd(String moduleId)    { return AppSession.getPermission(moduleId).isAdd(); }
    public static boolean canEdit(String moduleId)   { return AppSession.getPermission(moduleId).isEdit(); }
    public static boolean canDelete(String moduleId) { return AppSession.getPermission(moduleId).isDelete(); }

    /** Chỉ {@code true} khi vai trò có đủ quyền Thêm, Sửa và Xóa trên module (dùng cho nút Excel). */
    public static boolean canExcel(String moduleId) {
        return AppSession.getPermission(moduleId).hasFullCrud();
    }

    /** Đặt {@code visible} cho nút; {@code null} thì bỏ qua. */
    public static void applyVisible(JButton btn, boolean allowed) {
        if (btn != null) btn.setVisible(allowed);
    }
}
