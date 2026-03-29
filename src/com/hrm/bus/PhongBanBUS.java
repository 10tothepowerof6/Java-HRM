package com.hrm.bus;

import com.hrm.dao.PhongBanDAO;
import com.hrm.dto.PhongBanDTO;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Lớp nghiệp vụ (BUS) cho nghiệp vụ liên quan bảng PhongBan.
 * <p>
 * Điều phối DAO, kiểm tra hợp lệ dữ liệu trước khi ghi CSDL, giữ danh sách bản ghi trong bộ nhớ
 * để giao diện truy xuất nhanh sau phương thức loadData().
 * </p>
 */
public class PhongBanBUS {

    private PhongBanDAO phongBanDAO = new PhongBanDAO();
    private ArrayList<PhongBanDTO> listPhongBan = new ArrayList<>();

    public PhongBanBUS() {
        loadData();
    }

    public void loadData() {
        listPhongBan = phongBanDAO.getAll();
    }

    public ArrayList<PhongBanDTO> getList() {
        return listPhongBan;
    }

    public PhongBanDTO getById(String maPB) {
        for (PhongBanDTO pb : listPhongBan) {
            if (pb.getMaPB().equalsIgnoreCase(maPB)) {
                return pb;
            }
        }
        return null;
    }

    private void validate(PhongBanDTO pb) {
        if (pb.getMaPB() == null || pb.getMaPB().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã phòng ban không được để trống!");
        }
        if (pb.getTenPB() == null || pb.getTenPB().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên phòng ban không được để trống!");
        }
    }

    public boolean add(PhongBanDTO pb) {
        validate(pb);
        if (getById(pb.getMaPB()) != null) {
             throw new IllegalArgumentException("Mã phòng ban đã tồn tại!");
        }

        if (phongBanDAO.insert(pb)) {
            listPhongBan.add(pb);
            return true;
        }
        return false;
    }

    public boolean update(PhongBanDTO pb) {
        validate(pb);
        if (phongBanDAO.update(pb)) {
            for (int i = 0; i < listPhongBan.size(); i++) {
                if (listPhongBan.get(i).getMaPB().equals(pb.getMaPB())) {
                    listPhongBan.set(i, pb);
                    break;
                }
            }
            return true;
        }
        return false;
    }

    public boolean delete(String maPB) {
        if (phongBanDAO.delete(maPB)) {
            listPhongBan.removeIf(pb -> pb.getMaPB().equals(maPB));
            return true;
        }
        return false;
    }

    public ArrayList<PhongBanDTO> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return listPhongBan;
        }
        String lowerKeyword = keyword.toLowerCase().trim();
        return (ArrayList<PhongBanDTO>) listPhongBan.stream()
                .filter(pb -> pb.getMaPB().toLowerCase().contains(lowerKeyword) ||
                              pb.getTenPB().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }
}
