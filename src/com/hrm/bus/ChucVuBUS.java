package com.hrm.bus;

import com.hrm.dao.ChucVuDAO;
import com.hrm.dto.ChucVuDTO;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Lớp nghiệp vụ (BUS) cho nghiệp vụ liên quan bảng ChucVu.
 * <p>
 * Điều phối DAO, kiểm tra hợp lệ dữ liệu trước khi ghi CSDL, giữ danh sách bản ghi trong bộ nhớ
 * để giao diện truy xuất nhanh sau phương thức loadData().
 * </p>
 */
public class ChucVuBUS {

    private ChucVuDAO chucVuDAO = new ChucVuDAO();
    private ArrayList<ChucVuDTO> listChucVu = new ArrayList<>();

    public ChucVuBUS() {
        loadData();
    }

    public void loadData() {
        listChucVu = chucVuDAO.getAll();
    }

    public ArrayList<ChucVuDTO> getList() {
        return listChucVu;
    }

    public ChucVuDTO getById(String maCV) {
        for (ChucVuDTO cv : listChucVu) {
            if (cv.getMaCV().equalsIgnoreCase(maCV)) {
                return cv;
            }
        }
        return null;
    }

    private void validate(ChucVuDTO cv) {
        if (cv.getMaCV() == null || cv.getMaCV().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã chức vụ không được để trống!");
        }
        if (cv.getTenCV() == null || cv.getTenCV().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên chức vụ không được để trống!");
        }
    }

    public boolean add(ChucVuDTO cv) {
        validate(cv);
        if (getById(cv.getMaCV()) != null) {
            throw new IllegalArgumentException("Mã chức vụ đã tồn tại!");
        }

        if (chucVuDAO.insert(cv)) {
            listChucVu.add(cv);
            return true;
        }
        return false;
    }

    public boolean update(ChucVuDTO cv) {
        validate(cv);
        if (chucVuDAO.update(cv)) {
            for (int i = 0; i < listChucVu.size(); i++) {
                if (listChucVu.get(i).getMaCV().equals(cv.getMaCV())) {
                    listChucVu.set(i, cv);
                    break;
                }
            }
            return true;
        }
        return false;
    }

    public boolean delete(String maCV) {
        if (chucVuDAO.delete(maCV)) {
            listChucVu.removeIf(cv -> cv.getMaCV().equals(maCV));
            return true;
        }
        return false;
    }

    public ArrayList<ChucVuDTO> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return listChucVu;
        }
        String lowerKeyword = keyword.toLowerCase().trim();
        return (ArrayList<ChucVuDTO>) listChucVu.stream()
                .filter(cv -> cv.getMaCV().toLowerCase().contains(lowerKeyword) ||
                              cv.getTenCV().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }
}
