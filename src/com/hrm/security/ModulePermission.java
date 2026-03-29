package com.hrm.security;

/**
 * Bộ bốn quyền thao tác trên một module chức năng của ứng dụng: xem, thêm, sửa, xóa.
 * <p>
 * Dùng trong {@link RoleDefinition}; các nhà máy tĩnh {@link #fullAccess()}, {@link #viewOnly()}, {@link #none()}
 * tạo nhanh tổ hợp phổ biến. {@link #hasFullCrud()} phục vụ điều kiện hiện nút xuất/nhập Excel trên toolbar.
 * </p>
 */
public class ModulePermission {
    private boolean view;
    private boolean add;
    private boolean edit;
    private boolean delete;

    public ModulePermission() {}

    public ModulePermission(boolean view, boolean add, boolean edit, boolean delete) {
        this.view = view;
        this.add = add;
        this.edit = edit;
        this.delete = delete;
    }

    public static ModulePermission fullAccess() {
        return new ModulePermission(true, true, true, true);
    }

    public static ModulePermission viewOnly() {
        return new ModulePermission(true, false, false, false);
    }

    public static ModulePermission none() {
        return new ModulePermission(false, false, false, false);
    }

    public boolean isView()   { return view; }
    public boolean isAdd()    { return add; }
    public boolean isEdit()   { return edit; }
    public boolean isDelete() { return delete; }

    public void setView(boolean v)   { this.view = v; }
    public void setAdd(boolean v)    { this.add = v; }
    public void setEdit(boolean v)   { this.edit = v; }
    public void setDelete(boolean v) { this.delete = v; }

    public boolean hasFullCrud() {
        return add && edit && delete;
    }
}
