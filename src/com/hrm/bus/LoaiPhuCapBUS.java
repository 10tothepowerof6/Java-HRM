package com.hrm.bus;

import com.hrm.dao.LoaiPhuCapDAO;
import com.hrm.dto.LoaiPhuCapDTO;
import java.util.ArrayList;

/**
 * Lớp nghiệp vụ (BUS) cho nghiệp vụ liên quan bảng LoaiPhuCap.
 * <p>
 * Điều phối DAO, kiểm tra hợp lệ dữ liệu trước khi ghi CSDL, giữ danh sách bản ghi trong bộ nhớ
 * để giao diện truy xuất nhanh sau phương thức loadData().
 * </p>
 */
public class LoaiPhuCapBUS {

    private LoaiPhuCapDAO dao = new LoaiPhuCapDAO();
    private ArrayList<LoaiPhuCapDTO> list = new ArrayList<>();
    private static final LoaiPhuCapDTO[] DEFAULT_TYPES = new LoaiPhuCapDTO[]{
        new LoaiPhuCapDTO("PC01", "Đi lại", "Phụ cấp hỗ trợ đi lại"),
        new LoaiPhuCapDTO("PC02", "Nhà ở", "Phụ cấp hỗ trợ nhà ở"),
        new LoaiPhuCapDTO("PC03", "Xăng xe", "Phụ cấp hỗ trợ xăng xe"),
        new LoaiPhuCapDTO("PC04", "Điện thoại", "Phụ cấp điện thoại"),
        new LoaiPhuCapDTO("PC05", "Trách nhiệm", "Phụ cấp trách nhiệm"),
        new LoaiPhuCapDTO("PC06", "Chuyên cần", "Phụ cấp chuyên cần"),
        new LoaiPhuCapDTO("PC07", "Ca đêm", "Phụ cấp làm ca đêm")
    };

    public LoaiPhuCapBUS() {
        loadData();
    }

    public void loadData() {
        list = dao.getAll();
        ensureDefaultData();
    }

    /**
     * Tự động bổ sung các loại phụ cấp mặc định còn thiếu để tránh lỗi FK khi thêm phụ cấp.
     */
    private void ensureDefaultData() {
        boolean hasInserted = false;
        for (LoaiPhuCapDTO item : DEFAULT_TYPES) {
            if (getById(item.getMaLoaiPC()) == null) {
                if (dao.insert(item)) {
                    hasInserted = true;
                }
            }
        }
        if (hasInserted) {
            list = dao.getAll();
        }
    }

    public ArrayList<LoaiPhuCapDTO> getList() {
        return list;
    }

    public LoaiPhuCapDTO getById(String maLoaiPC) {
        for (LoaiPhuCapDTO dto : list) {
            if (dto.getMaLoaiPC() != null && dto.getMaLoaiPC().equalsIgnoreCase(maLoaiPC)) {
                return dto;
            }
        }
        return null;
    }

    private void validate(LoaiPhuCapDTO dto) {
        if (dto.getMaLoaiPC() == null || dto.getMaLoaiPC().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã loại phụ cấp không được để trống!");
        }
        if (dto.getTenLoaiPC() == null || dto.getTenLoaiPC().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên loại phụ cấp không được để trống!");
        }
    }

    public boolean add(LoaiPhuCapDTO dto) {
        validate(dto);
        if (getById(dto.getMaLoaiPC()) != null) {
            throw new IllegalArgumentException("Mã loại phụ cấp đã tồn tại!");
        }
        if (dao.insert(dto)) {
            list.add(dto);
            return true;
        }
        return false;
    }

    public boolean update(LoaiPhuCapDTO dto) {
        validate(dto);
        if (dao.update(dto)) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getMaLoaiPC() != null && list.get(i).getMaLoaiPC().equalsIgnoreCase(dto.getMaLoaiPC())) {
                    list.set(i, dto);
                    break;
                }
            }
            return true;
        }
        return false;
    }

    public boolean delete(String maLoaiPC) {
        if (dao.delete(maLoaiPC)) {
            list.removeIf(dto -> dto.getMaLoaiPC() != null && dto.getMaLoaiPC().equals(maLoaiPC));
            return true;
        }
        return false;
    }
}
