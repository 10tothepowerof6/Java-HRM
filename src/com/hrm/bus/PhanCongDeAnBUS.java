package com.hrm.bus;

import com.hrm.dao.PhanCongDeAnDAO;
import com.hrm.dto.PhanCongDeAnDTO;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Lớp nghiệp vụ (BUS) cho nghiệp vụ liên quan bảng PhanCongDeAn.
 * <p>
 * Điều phối DAO, kiểm tra hợp lệ dữ liệu trước khi ghi CSDL, giữ danh sách bản ghi trong bộ nhớ
 * để giao diện truy xuất nhanh sau phương thức loadData().
 * </p>
 */
public class PhanCongDeAnBUS {

    private PhanCongDeAnDAO dao = new PhanCongDeAnDAO();
    private ArrayList<PhanCongDeAnDTO> list = new ArrayList<>();

    public PhanCongDeAnBUS() {
        loadData();
    }

    public void loadData() {
        list = dao.getAll();
    }

    public ArrayList<PhanCongDeAnDTO> getList() {
        return list;
    }

    public PhanCongDeAnDTO getById(String maNV, String maDA) {
        for (PhanCongDeAnDTO dto : list) {
            if (dto.getMaNV() != null && dto.getMaNV().equalsIgnoreCase(maNV)
                    && dto.getMaDA() != null && dto.getMaDA().equalsIgnoreCase(maDA)) {
                return dto;
            }
        }
        return null;
    }

    public ArrayList<PhanCongDeAnDTO> getByMaDA(String maDA) {
        if (maDA == null || maDA.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String lower = maDA.trim().toLowerCase();
        return list.stream()
                .filter(dto -> dto.getMaDA() != null && dto.getMaDA().toLowerCase().equals(lower))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<PhanCongDeAnDTO> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return list;
        }
        String lower = keyword.trim().toLowerCase();
        return list.stream()
                .filter(dto -> (dto.getMaNV() != null && dto.getMaNV().toLowerCase().contains(lower))
                        || (dto.getMaDA() != null && dto.getMaDA().toLowerCase().contains(lower)))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void validate(PhanCongDeAnDTO dto) {
        if (dto.getMaNV() == null || dto.getMaNV().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã nhân viên không được để trống!");
        }
        if (dto.getMaDA() == null || dto.getMaDA().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã đề án không được để trống!");
        }
        if (dto.getNgayBatDau() == null) {
            throw new IllegalArgumentException("Ngày bắt đầu không hợp lệ!");
        }
        if (dto.getNgayKetThuc() != null && dto.getNgayKetThuc().before(dto.getNgayBatDau())) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu!");
        }
        if (dto.getPhuCapDeAn() != null && dto.getPhuCapDeAn().signum() < 0) {
            throw new IllegalArgumentException("Phụ cấp đề án không được âm!");
        }
    }

    public boolean add(PhanCongDeAnDTO dto) {
        validate(dto);
        if (getById(dto.getMaNV(), dto.getMaDA()) != null) {
            throw new IllegalArgumentException("Nhân viên này đã được phân công vào đề án này rồi!");
        }
        if (dao.insert(dto)) {
            list.add(dto);
            return true;
        }
        return false;
    }

    public boolean update(PhanCongDeAnDTO dto) {
        validate(dto);
        if (dao.update(dto)) {
            for (int i = 0; i < list.size(); i++) {
                PhanCongDeAnDTO cur = list.get(i);
                if (cur.getMaNV() != null && cur.getMaDA() != null
                        && cur.getMaNV().equalsIgnoreCase(dto.getMaNV())
                        && cur.getMaDA().equalsIgnoreCase(dto.getMaDA())) {
                    list.set(i, dto);
                    break;
                }
            }
            return true;
        }
        return false;
    }

    public boolean delete(String maNV, String maDA) {
        if (dao.delete(maNV, maDA)) {
            list.removeIf(dto -> dto.getMaNV() != null && dto.getMaDA() != null
                    && dto.getMaNV().equals(maNV) && dto.getMaDA().equals(maDA));
            return true;
        }
        return false;
    }
}
