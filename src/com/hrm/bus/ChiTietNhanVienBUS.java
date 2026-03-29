package com.hrm.bus;

import com.hrm.dao.ChiTietNhanVienDAO;
import com.hrm.dto.ChiTietNhanVienDTO;
import java.util.ArrayList;

/**
 * Lớp nghiệp vụ (BUS) cho nghiệp vụ liên quan bảng ChiTietNhanVien.
 * <p>
 * Điều phối DAO, kiểm tra hợp lệ dữ liệu trước khi ghi CSDL, giữ danh sách bản ghi trong bộ nhớ
 * để giao diện truy xuất nhanh sau phương thức loadData().
 * </p>
 */
public class ChiTietNhanVienBUS {

    private ChiTietNhanVienDAO chiTietNhanVienDAO = new ChiTietNhanVienDAO();
    private ArrayList<ChiTietNhanVienDTO> listChiTiet = new ArrayList<>();

    public ChiTietNhanVienBUS() {
        loadData();
    }

    public void loadData() {
        listChiTiet = chiTietNhanVienDAO.getAll();
    }

    public ArrayList<ChiTietNhanVienDTO> getList() {
        return listChiTiet;
    }

    public ChiTietNhanVienDTO getById(String maNV) {
        for (ChiTietNhanVienDTO ct : listChiTiet) {
            if (ct.getMaNV().equalsIgnoreCase(maNV)) {
                return ct;
            }
        }
        return null;
    }

    private void validate(ChiTietNhanVienDTO ct) {
        if (ct.getMaNV() == null || ct.getMaNV().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã nhân viên không được để trống!");
        }
        if (ct.getCccd() == null || ct.getCccd().trim().length() < 9) {
            throw new IllegalArgumentException("CCCD không hợp lệ!");
        }
    }

    public boolean add(ChiTietNhanVienDTO ct) {
        validate(ct);
        if (chiTietNhanVienDAO.insert(ct)) {
            listChiTiet.add(ct);
            return true;
        }
        return false;
    }

    public boolean update(ChiTietNhanVienDTO ct) {
        validate(ct);
        if (chiTietNhanVienDAO.update(ct)) {
            for (int i = 0; i < listChiTiet.size(); i++) {
                if (listChiTiet.get(i).getMaNV().equals(ct.getMaNV())) {
                    listChiTiet.set(i, ct);
                    break;
                }
            }
            return true;
        }
        return false;
    }

    public boolean delete(String maNV) {
        if (chiTietNhanVienDAO.delete(maNV)) {
            listChiTiet.removeIf(ct -> ct.getMaNV().equals(maNV));
            return true;
        }
        return false;
    }
}
