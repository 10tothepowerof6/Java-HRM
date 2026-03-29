package com.hrm.bus;

import com.hrm.dao.DanhMucThuongDAO;
import com.hrm.dto.DanhMucThuongDTO;
import java.util.ArrayList;

/**
 * Lớp nghiệp vụ (BUS) cho nghiệp vụ liên quan bảng DanhMucThuong.
 * <p>
 * Điều phối DAO, kiểm tra hợp lệ dữ liệu trước khi ghi CSDL, giữ danh sách bản ghi trong bộ nhớ
 * để giao diện truy xuất nhanh sau phương thức loadData().
 * </p>
 */
public class DanhMucThuongBUS {

    private DanhMucThuongDAO dao = new DanhMucThuongDAO();
    private ArrayList<DanhMucThuongDTO> list = new ArrayList<>();
    private static final DanhMucThuongDTO[] DEFAULT_CATEGORIES = new DanhMucThuongDTO[]{
        new DanhMucThuongDTO("TH01", "Thưởng hiệu suất", "Thưởng theo KPI/hiệu suất công việc"),
        new DanhMucThuongDTO("TH02", "Thưởng chuyên cần", "Thưởng đi làm đầy đủ, đúng giờ"),
        new DanhMucThuongDTO("TH03", "Thưởng dự án", "Thưởng hoàn thành dự án"),
        new DanhMucThuongDTO("TH04", "Thưởng sáng kiến", "Thưởng cải tiến quy trình/ý tưởng mới")
    };

    public DanhMucThuongBUS() {
        loadData();
    }

    public void loadData() {
        list = dao.getAll();
        ensureDefaultData();
    }

    /**
     * Tự động bổ sung danh mục thưởng mặc định còn thiếu để đảm bảo FK cho ChiTietThuong.
     */
    private void ensureDefaultData() {
        boolean hasInserted = false;
        for (DanhMucThuongDTO item : DEFAULT_CATEGORIES) {
            if (getById(item.getMaThuong()) == null) {
                if (dao.insert(item)) {
                    hasInserted = true;
                }
            }
        }
        if (hasInserted) {
            list = dao.getAll();
        }
    }

    public ArrayList<DanhMucThuongDTO> getList() {
        return list;
    }

    public DanhMucThuongDTO getById(String maThuong) {
        for (DanhMucThuongDTO dto : list) {
            if (dto.getMaThuong() != null && dto.getMaThuong().equalsIgnoreCase(maThuong)) {
                return dto;
            }
        }
        return null;
    }

    private void validate(DanhMucThuongDTO dto) {
        if (dto.getMaThuong() == null || dto.getMaThuong().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã thưởng không được để trống!");
        }
        if (dto.getTenLoaiThuong() == null || dto.getTenLoaiThuong().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên loại thưởng không được để trống!");
        }
    }

    public boolean add(DanhMucThuongDTO dto) {
        validate(dto);
        if (getById(dto.getMaThuong()) != null) {
            throw new IllegalArgumentException("Mã loại thưởng đã tồn tại!");
        }
        if (dao.insert(dto)) {
            list.add(dto);
            return true;
        }
        return false;
    }

    public boolean update(DanhMucThuongDTO dto) {
        validate(dto);
        if (dao.update(dto)) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getMaThuong() != null && list.get(i).getMaThuong().equalsIgnoreCase(dto.getMaThuong())) {
                    list.set(i, dto);
                    break;
                }
            }
            return true;
        }
        return false;
    }

    public boolean delete(String maThuong) {
        if (dao.delete(maThuong)) {
            list.removeIf(dto -> dto.getMaThuong() != null && dto.getMaThuong().equals(maThuong));
            return true;
        }
        return false;
    }
}
