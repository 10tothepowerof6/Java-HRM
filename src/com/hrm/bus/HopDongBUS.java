package com.hrm.bus;

import com.hrm.dao.HopDongDAO;
import com.hrm.dto.HopDongDTO;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Lớp nghiệp vụ (BUS) cho nghiệp vụ liên quan bảng HopDong.
 * <p>
 * Điều phối DAO, kiểm tra hợp lệ dữ liệu trước khi ghi CSDL, giữ danh sách bản ghi trong bộ nhớ
 * để giao diện truy xuất nhanh sau phương thức loadData().
 * </p>
 */
public class HopDongBUS {

    private HopDongDAO dao = new HopDongDAO();
    private ArrayList<HopDongDTO> list = new ArrayList<>();

    public HopDongBUS() {
        loadData();
    }

    public void loadData() {
        list = dao.getAll();
    }

    public ArrayList<HopDongDTO> getList() {
        return list;
    }

    public HopDongDTO getById(String maHD) {
        for (HopDongDTO dto : list) {
            if (dto.getMaHD().equalsIgnoreCase(maHD)) {
                return dto;
            }
        }
        return null;
    }

    private void validate(HopDongDTO dto) {
        if (dto.getMaHD() == null || dto.getMaHD().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã hợp đồng không được để trống!");
        }
        if (dto.getMaNV() == null || dto.getMaNV().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã nhân viên không được để trống!");
        }
        if (dto.getNgayBatDau() == null) {
            throw new IllegalArgumentException("Ngày bắt đầu không hợp lệ!");
        }
        if (dto.getNgayKetThuc() != null && dto.getNgayKetThuc().before(dto.getNgayBatDau())) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu!");
        }
        if (dto.getLuongHopDong() != null && dto.getLuongHopDong().signum() < 0) {
            throw new IllegalArgumentException("Lương hợp đồng không được âm!");
        }
    }

    public boolean add(HopDongDTO dto) {
        validate(dto);
        if (getById(dto.getMaHD()) != null) {
            throw new IllegalArgumentException("Mã hợp đồng đã tồn tại!");
        }

        if (dao.insert(dto)) {
            list.add(dto);
            return true;
        }
        return false;
    }

    public boolean update(HopDongDTO dto) {
        validate(dto);
        if (dao.update(dto)) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getMaHD().equals(dto.getMaHD())) {
                    list.set(i, dto);
                    break;
                }
            }
            return true;
        }
        return false;
    }

    public boolean delete(String maHD) {
        if (dao.delete(maHD)) {
            list.removeIf(dto -> dto.getMaHD().equals(maHD));
            return true;
        }
        return false;
    }

    public ArrayList<HopDongDTO> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return list;
        }
        String lowerKeyword = keyword.toLowerCase().trim();
        return (ArrayList<HopDongDTO>) list.stream()
                .filter(dto -> dto.getMaHD().toLowerCase().contains(lowerKeyword) ||
                               dto.getMaNV().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }
}
