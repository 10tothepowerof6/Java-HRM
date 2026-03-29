package com.hrm.security;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Truy cập tập trung vào file phân quyền {@code data/hrm_security.json} (UTF-8, Gson).
 * <p>
 * Dùng bộ nhớ đệm tĩnh {@code cache} để tránh đọc đĩa lặp lại; {@link #reload()} xóa đệm trước khi nạp lại.
 * Lần đầu chạy khi chưa có file: tạo vai trò ADMIN toàn quyền và tài khoản {@code admin/admin}.
 * </p>
 */
public final class SecurityRepository {

    private static final String DIR = "data";
    private static final String FILE_NAME = "hrm_security.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** Bản đồ đã parse; null cho đến lần gọi {@link #load()} đầu tiên. */
    private static SecurityData cache;

    private SecurityRepository() {}

    /**
     * Nạp dữ liệu bảo mật: trả về cache nếu đã có; nếu không thì đọc JSON hoặc tạo mặc định và ghi file.
     */
    public static synchronized SecurityData load() {
        if (cache != null) return cache;

        File file = getFile();
        if (file.exists()) {
            try (Reader r = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                cache = GSON.fromJson(r, SecurityData.class);
                if (cache != null) return cache;
            } catch (Exception e) {
                System.err.println("[Security] Error reading " + file + ": " + e.getMessage());
            }
        }

        cache = createDefault();
        save(cache);
        return cache;
    }

    /** Ghi đè file JSON và cập nhật cache trỏ tới cùng tham chiếu {@code data}. */
    public static synchronized void save(SecurityData data) {
        cache = data;
        File file = getFile();
        file.getParentFile().mkdirs();
        try (Writer w = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            GSON.toJson(data, w);
        } catch (IOException e) {
            System.err.println("[Security] Error writing " + file + ": " + e.getMessage());
        }
    }

    /** Buộc đọc lại từ đĩa ở lần {@link #load()} kế tiếp (sau khi sửa tay file hoặc đồng bộ từ UI khác). */
    public static synchronized void reload() {
        cache = null;
        load();
    }

    public static UserAccount findUser(String username) {
        for (UserAccount u : load().getUsers()) {
            if (u.getUsername().equals(username)) return u;
        }
        return null;
    }

    public static RoleDefinition findRole(String roleId) {
        for (RoleDefinition r : load().getRoles()) {
            if (r.getId().equals(roleId)) return r;
        }
        return null;
    }

    // ------------------------------------------------------------------
    /** Danh sách định danh module cố định — dùng cho ma trận quyền và ẩn menu. */
    public static final String[] ALL_MODULES = {
        "dashboard", "nhanvien", "phongban", "chucvu", "hopdong",
        "dean", "chamcong", "nghiphep", "phucap", "thuong",
        "bangluong", "caidat", "taikhoan"
    };

    /** Nhãn tiếng Việt hiển thị cho từng {@code moduleId} (sidebar, ma trận quyền). */
    public static final Map<String, String> MODULE_LABELS = new LinkedHashMap<>();
    static {
        MODULE_LABELS.put("dashboard", "Tổng quan");
        MODULE_LABELS.put("nhanvien",  "Nhân viên");
        MODULE_LABELS.put("phongban",  "Phòng ban");
        MODULE_LABELS.put("chucvu",    "Chức vụ");
        MODULE_LABELS.put("hopdong",   "Hợp đồng");
        MODULE_LABELS.put("dean",      "Đề án");
        MODULE_LABELS.put("chamcong",  "Chấm công");
        MODULE_LABELS.put("nghiphep",  "Nghỉ phép");
        MODULE_LABELS.put("phucap",    "Phụ cấp");
        MODULE_LABELS.put("thuong",    "Thưởng");
        MODULE_LABELS.put("bangluong", "Bảng lương");
        MODULE_LABELS.put("caidat",    "Cài đặt");
        MODULE_LABELS.put("taikhoan",  "Tài khoản & Quyền");
    }

    /** Seed ban đầu: một role ADMIN và user admin gắn role đó. */
    private static SecurityData createDefault() {
        SecurityData data = new SecurityData();

        RoleDefinition admin = new RoleDefinition("ADMIN", "Quản trị");
        Map<String, ModulePermission> perms = new LinkedHashMap<>();
        for (String m : ALL_MODULES) {
            perms.put(m, ModulePermission.fullAccess());
        }
        admin.setPermissions(perms);
        data.getRoles().add(admin);

        data.getUsers().add(new UserAccount("admin", "admin", "ADMIN"));

        return data;
    }

    private static File getFile() {
        return new File(System.getProperty("user.dir") + File.separator + DIR + File.separator + FILE_NAME);
    }
}
