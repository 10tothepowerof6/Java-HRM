package com.hrm.bus;

import com.hrm.dao.ThamSoDAO;
import com.hrm.dto.ThamSoDTO;
import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Lớp nghiệp vụ (BUS) cho nghiệp vụ liên quan bảng ThamSo.
 * <p>
 * Điều phối DAO, kiểm tra hợp lệ dữ liệu trước khi ghi CSDL, giữ danh sách bản ghi trong bộ nhớ
 * để giao diện truy xuất nhanh sau phương thức loadData().
 * </p>
 */
public class ThamSoBUS {

    private ThamSoDAO dao = new ThamSoDAO();
    private ArrayList<ThamSoDTO> list = new ArrayList<>();

    public ThamSoBUS() {
        loadData();
    }

    public void loadData() {
        list = dao.getAll();
    }

    public ArrayList<ThamSoDTO> getList() {
        return list;
    }

    public ThamSoDTO getById(String maThamSo) {
        for (ThamSoDTO dto : list) {
            if (dto.getMaThamSo().equalsIgnoreCase(maThamSo)) {
                return dto;
            }
        }
        return null;
    }

    /**
     * API quan trọng để các BUS khác (như Tính Lương) lấy tỷ lệ.
     * Ví dụ: getGiaTriTamSo("TyLeBHXH")
     */
    public BigDecimal getGiaTriThamSo(String tenThamSo) {
        for (ThamSoDTO dto : list) {
            if (dto.getTenThamSo().equalsIgnoreCase(tenThamSo)) {
                return dto.getGiaTri();
            }
        }
        // Gọi DAO nếu không có trong cache
        ThamSoDTO dbDTO = dao.getByTen(tenThamSo);
        if (dbDTO != null) {
            list.add(dbDTO);
            return dbDTO.getGiaTri();
        }
        return BigDecimal.ZERO; 
    }

    private void validate(ThamSoDTO dto) {
        if (dto.getMaThamSo() == null || dto.getMaThamSo().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã tham số không được để trống!");
        }
        if (dto.getTenThamSo() == null || dto.getTenThamSo().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên tham số không được để trống!");
        }
        if (dto.getGiaTri() == null) {
            throw new IllegalArgumentException("Giá trị tham số không được để trống!");
        }
    }

    public boolean add(ThamSoDTO dto) {
        validate(dto);
        if (getById(dto.getMaThamSo()) != null) {
            throw new IllegalArgumentException("Mã tham số đã tồn tại!");
        }

        if (dao.insert(dto)) {
            list.add(dto);
            return true;
        }
        return false;
    }

    public boolean update(ThamSoDTO dto) {
        validate(dto);
        dto.setNgayCapNhat(new java.util.Date()); // Tự động set ngày giờ cập nhật
        if (dao.update(dto)) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getMaThamSo().equals(dto.getMaThamSo())) {
                    list.set(i, dto);
                    break;
                }
            }
            return true;
        }
        return false;
    }

    public boolean delete(String maThamSo) {
        if (dao.delete(maThamSo)) {
            list.removeIf(dto -> dto.getMaThamSo().equals(maThamSo));
            return true;
        }
        return false;
    }
}
