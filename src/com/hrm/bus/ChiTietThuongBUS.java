package com.hrm.bus;

import com.hrm.dao.ChiTietThuongDAO;
import com.hrm.dto.ChiTietThuongDTO;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Lớp nghiệp vụ (BUS) cho nghiệp vụ liên quan bảng ChiTietThuong.
 * <p>
 * Điều phối DAO, kiểm tra hợp lệ dữ liệu trước khi ghi CSDL, giữ danh sách bản ghi trong bộ nhớ
 * để giao diện truy xuất nhanh sau phương thức loadData().
 * </p>
 */
public class ChiTietThuongBUS {

    private ChiTietThuongDAO dao = new ChiTietThuongDAO();
    private ArrayList<ChiTietThuongDTO> list = new ArrayList<>();

    public ChiTietThuongBUS() {
        loadData();
    }

    public void loadData() {
        list = dao.getAll();
    }

    public ArrayList<ChiTietThuongDTO> getList() {
        return list;
    }

    public ChiTietThuongDTO getById(String maCTT) {
        for (ChiTietThuongDTO dto : list) {
            if (dto.getMaCTT() != null && dto.getMaCTT().equalsIgnoreCase(maCTT)) {
                return dto;
            }
        }
        return null;
    }

    private void validate(ChiTietThuongDTO dto) {
        if (dto.getMaCTT() == null || dto.getMaCTT().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã chi tiết thưởng không được để trống!");
        }
        if (dto.getMaNV() == null || dto.getMaNV().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã nhân viên không được để trống!");
        }
        if (dto.getSoTien() == null || dto.getSoTien().signum() < 0) {
            throw new IllegalArgumentException("Số tiền thưởng không hợp lệ!");
        }
        if (dto.getNgayThuong() == null) {
            throw new IllegalArgumentException("Ngày thưởng không được để trống!");
        }
    }

    public boolean add(ChiTietThuongDTO dto) {
        validate(dto);
        // Đảm bảo danh mục thưởng mặc định tồn tại, tránh lỗi FK khi insert chi tiết thưởng.
        new DanhMucThuongBUS();
        if (getById(dto.getMaCTT()) != null) {
            throw new IllegalArgumentException("Mã chi tiết thưởng đã tồn tại!");
        }

        if (dao.insert(dto)) {
            list.add(dto);
            return true;
        }
        return false;
    }

    public boolean update(ChiTietThuongDTO dto) {
        validate(dto);
        // Đảm bảo danh mục thưởng mặc định tồn tại, tránh lỗi FK khi update chi tiết thưởng.
        new DanhMucThuongBUS();
        if (dao.update(dto)) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getMaCTT() != null && list.get(i).getMaCTT().equalsIgnoreCase(dto.getMaCTT())) {
                    list.set(i, dto);
                    break;
                }
            }
            return true;
        }
        return false;
    }

    public boolean delete(String maCTT) {
        if (dao.delete(maCTT)) {
            list.removeIf(dto -> dto.getMaCTT() != null && dto.getMaCTT().equals(maCTT));
            return true;
        }
        return false;
    }

    public ArrayList<ChiTietThuongDTO> getByMaThuong(String maThuong) {
        if (maThuong == null || maThuong.trim().isEmpty()) {
            return new ArrayList<>(list);
        }
        String key = maThuong.trim().toLowerCase();
        return (ArrayList<ChiTietThuongDTO>) list.stream()
                .filter(dto -> dto.getMaThuong() != null && dto.getMaThuong().toLowerCase().equals(key))
                .collect(Collectors.toList());
    }

    public ArrayList<ChiTietThuongDTO> search(String keyword, String maThuong) {
        String key = keyword == null ? "" : keyword.trim().toLowerCase();
        return (ArrayList<ChiTietThuongDTO>) list.stream()
                .filter(dto -> maThuong == null || maThuong.trim().isEmpty()
                        || (dto.getMaThuong() != null && dto.getMaThuong().equalsIgnoreCase(maThuong)))
                .filter(dto -> key.isEmpty()
                        || (dto.getMaCTT() != null && dto.getMaCTT().toLowerCase().contains(key))
                        || (dto.getMaNV() != null && dto.getMaNV().toLowerCase().contains(key))
                        || (dto.getGhiChu() != null && dto.getGhiChu().toLowerCase().contains(key)))
                .collect(Collectors.toList());
    }
}
