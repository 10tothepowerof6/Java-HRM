package com.hrm.bus;

import com.hrm.dao.LoaiNghiPhepDAO;
import com.hrm.dto.LoaiNghiPhepDTO;
import java.util.ArrayList;

/**
 * Lớp nghiệp vụ (BUS) cho nghiệp vụ liên quan bảng LoaiNghiPhep.
 * <p>
 * Điều phối DAO, kiểm tra hợp lệ dữ liệu trước khi ghi CSDL, giữ danh sách bản ghi trong bộ nhớ
 * để giao diện truy xuất nhanh sau phương thức loadData().
 * </p>
 */
public class LoaiNghiPhepBUS {

    private LoaiNghiPhepDAO dao = new LoaiNghiPhepDAO();
    private ArrayList<LoaiNghiPhepDTO> list = new ArrayList<>();

    public LoaiNghiPhepBUS() {
        loadData();
    }

    public void loadData() {
        list = dao.getAll();
        // Tự động tạo dữ liệu mẫu nếu bảng LoaiNghiPhep đang trống
        if (list.isEmpty()) {
            add(new LoaiNghiPhepDTO("LNP01", "Nghỉ phép năm (Có lương)", 12, "Nghỉ theo tiêu chuẩn hàng năm"));
            add(new LoaiNghiPhepDTO("LNP02", "Nghỉ ốm đau", 30, "Nghỉ hưởng chế độ BHXH"));
            add(new LoaiNghiPhepDTO("LNP03", "Nghỉ thai sản", 180, "Nghỉ theo luật BHXH"));
            add(new LoaiNghiPhepDTO("LNP04", "Nghỉ không lương", 14, "Nghỉ không tính lương"));
            list = dao.getAll(); // Tải lại sau khi insert
        }
    }

    public ArrayList<LoaiNghiPhepDTO> getList() {
        return list;
    }

    public LoaiNghiPhepDTO getById(String maLoaiNP) {
        for (LoaiNghiPhepDTO dto : list) {
            if (dto.getMaLoaiNP().equalsIgnoreCase(maLoaiNP)) {
                return dto;
            }
        }
        return null;
    }

    private void validate(LoaiNghiPhepDTO dto) {
        if (dto.getMaLoaiNP() == null || dto.getMaLoaiNP().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã loại nghỉ phép không được để trống!");
        }
        if (dto.getTenLoai() == null || dto.getTenLoai().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên loại nghỉ phép không được để trống!");
        }
        if (dto.getSoNgayToiDa() < 0) {
            throw new IllegalArgumentException("Số ngày tối đa không được âm!");
        }
    }

    public boolean add(LoaiNghiPhepDTO dto) {
        validate(dto);
        if (getById(dto.getMaLoaiNP()) != null) {
            throw new IllegalArgumentException("Mã loại nghỉ phép đã tồn tại!");
        }

        if (dao.insert(dto)) {
            list.add(dto);
            return true;
        }
        return false;
    }

    public boolean update(LoaiNghiPhepDTO dto) {
        validate(dto);
        if (dao.update(dto)) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getMaLoaiNP().equals(dto.getMaLoaiNP())) {
                    list.set(i, dto);
                    break;
                }
            }
            return true;
        }
        return false;
    }

    public boolean delete(String maLoaiNP) {
        if (dao.delete(maLoaiNP)) {
            list.removeIf(dto -> dto.getMaLoaiNP().equals(maLoaiNP));
            return true;
        }
        return false;
    }
}
