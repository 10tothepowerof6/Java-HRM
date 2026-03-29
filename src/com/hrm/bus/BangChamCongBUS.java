package com.hrm.bus;

import com.hrm.dao.BangChamCongDAO;
import com.hrm.dto.BangChamCongDTO;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Lớp nghiệp vụ (BUS) cho nghiệp vụ liên quan bảng BangChamCong.
 * <p>
 * Điều phối DAO, kiểm tra hợp lệ dữ liệu trước khi ghi CSDL, giữ danh sách bản ghi trong bộ nhớ
 * để giao diện truy xuất nhanh sau phương thức loadData().
 * </p>
 */
public class BangChamCongBUS {

    private BangChamCongDAO dao = new BangChamCongDAO();
    private ArrayList<BangChamCongDTO> list = new ArrayList<>();

    public BangChamCongBUS() {
        loadData();
    }

    public void loadData() {
        list = dao.getAll();
    }

    public ArrayList<BangChamCongDTO> getList() {
        return list;
    }

    public BangChamCongDTO getById(String maChamCong) {
        for (BangChamCongDTO dto : list) {
            if (dto.getMaChamCong().equalsIgnoreCase(maChamCong)) {
                return dto;
            }
        }
        return null;
    }

    private void validate(BangChamCongDTO dto) {
        if (dto.getMaChamCong() == null || dto.getMaChamCong().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã chấm công không được để trống!");
        }
        if (dto.getMaNV() == null || dto.getMaNV().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã nhân viên không được để trống!");
        }
        if (dto.getNgayLamViec() == null) {
            throw new IllegalArgumentException("Ngày làm việc không hợp lệ!");
        }
        if (dto.getGioVao() != null && dto.getGioRa() != null) {
            if(dto.getGioRa().before(dto.getGioVao())) {
                throw new IllegalArgumentException("Giờ ra không được trước Giờ vào!");
            }
        }
    }

    public boolean add(BangChamCongDTO dto) {
        validate(dto);
        if (getById(dto.getMaChamCong()) != null) {
            throw new IllegalArgumentException("Mã chấm công đã tồn tại!");
        }

        if (dao.insert(dto)) {
            list.add(dto);
            return true;
        }
        return false;
    }

    public boolean update(BangChamCongDTO dto) {
        validate(dto);
        if (dao.update(dto)) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getMaChamCong().equals(dto.getMaChamCong())) {
                    list.set(i, dto);
                    break;
                }
            }
            return true;
        }
        return false;
    }

    public boolean delete(String maChamCong) {
        if (dao.delete(maChamCong)) {
            list.removeIf(dto -> dto.getMaChamCong().equals(maChamCong));
            return true;
        }
        return false;
    }

    public ArrayList<BangChamCongDTO> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return list;
        }
        String lowerKeyword = keyword.toLowerCase().trim();
        return (ArrayList<BangChamCongDTO>) list.stream()
                .filter(dto -> dto.getMaChamCong().toLowerCase().contains(lowerKeyword) ||
                               dto.getMaNV().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }
}
