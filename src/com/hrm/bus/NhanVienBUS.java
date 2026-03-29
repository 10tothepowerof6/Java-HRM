package com.hrm.bus;

import com.hrm.dao.NhanVienDAO;
import com.hrm.dto.NhanVienDTO;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Lớp nghiệp vụ (BUS) cho nghiệp vụ liên quan bảng NhanVien.
 * <p>
 * Điều phối DAO, kiểm tra hợp lệ dữ liệu trước khi ghi CSDL, giữ danh sách bản ghi trong bộ nhớ
 * để giao diện truy xuất nhanh sau phương thức loadData().
 * </p>
 */
public class NhanVienBUS {

    private NhanVienDAO nhanVienDAO = new NhanVienDAO();
    private ArrayList<NhanVienDTO> listNhanVien = new ArrayList<>();

    public NhanVienBUS() {
        // Tải dữ liệu ban đầu
        loadData();
    }

    /**
     * Nạp lại dữ liệu từ CSDL vào danh sách tạm.
     */
    public void loadData() {
        listNhanVien = nhanVienDAO.getAll();
    }

    /**
     * Lấy danh sách hiện tại (không query lại CSDL).
     */
    public ArrayList<NhanVienDTO> getList() {
        return listNhanVien;
    }

    /**
     * Tìm nhân viên theo mã trong danh sách tạm.
     */
    public NhanVienDTO getById(String maNV) {
        for (NhanVienDTO nv : listNhanVien) {
            if (nv.getMaNV().equalsIgnoreCase(maNV)) {
                return nv;
            }
        }
        return null;
    }

    /**
     * Kiểm tra tính hợp lệ trước khi Insert/Update.
     */
    private boolean validate(NhanVienDTO nv) throws IllegalArgumentException {
        if (nv.getMaNV() == null || nv.getMaNV().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã nhân viên không được để trống!");
        }
        if (nv.getHo() == null || nv.getHo().trim().isEmpty() ||
            nv.getTen() == null || nv.getTen().trim().isEmpty()) {
            throw new IllegalArgumentException("Họ và tên không được để trống!");
        }
        if (nv.getNgaySinh() == null) {
            throw new IllegalArgumentException("Ngày sinh không hợp lệ hoặc bị để trống!");
        }
        if (nv.getNgayBatDau() == null) {
            throw new IllegalArgumentException("Ngày bắt đầu làm việc không hợp lệ hoặc bị để trống!");
        }
        if (nv.getMaPB() == null || nv.getMaPB().trim().isEmpty()) {
            throw new IllegalArgumentException("Chưa chọn phòng ban!");
        }
        if (nv.getMaCV() == null || nv.getMaCV().trim().isEmpty()) {
            throw new IllegalArgumentException("Chưa chọn chức vụ!");
        }
        return true;
    }

    /**
     * Thêm nhân viên mới.
     */
    public boolean add(NhanVienDTO nv) {
        validate(nv);
        if (nhanVienDAO.checkMaNVExists(nv.getMaNV())) {
            throw new IllegalArgumentException("Mã nhân viên đã tồn tại trong hệ thống!");
        }
        
        if (nhanVienDAO.insert(nv)) {
            listNhanVien.add(nv);
            return true;
        }
        return false;
    }

    /**
     * Cập nhật thông tin nhân viên.
     */
    public boolean update(NhanVienDTO nv) {
        validate(nv);
        
        if (nhanVienDAO.update(nv)) {
            // Cập nhật lại list tạm
            for (int i = 0; i < listNhanVien.size(); i++) {
                if (listNhanVien.get(i).getMaNV().equals(nv.getMaNV())) {
                    listNhanVien.set(i, nv);
                    break;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Xóa nhân viên.
     */
    public boolean delete(String maNV) {
        if (nhanVienDAO.delete(maNV)) {
            listNhanVien.removeIf(nv -> nv.getMaNV().equals(maNV));
            return true;
        }
        return false;
    }

    /**
     * Tìm kiếm nhân viên bằng Stream API (Java 8+).
     * @param keyword Từ khóa tìm kiếm (Mã, Tên)
     */
    public ArrayList<NhanVienDTO> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return listNhanVien;
        }
        String lowerKeyword = keyword.toLowerCase().trim();
        return (ArrayList<NhanVienDTO>) listNhanVien.stream()
                .filter(nv -> nv.getMaNV().toLowerCase().contains(lowerKeyword) ||
                              nv.getTen().toLowerCase().contains(lowerKeyword) ||
                              nv.getHo().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }
}
