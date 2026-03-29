package com.hrm.bus;

import com.hrm.dao.DeAnDAO;
import com.hrm.dto.DeAnDTO;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Lớp nghiệp vụ (BUS) cho nghiệp vụ liên quan bảng DeAn.
 * <p>
 * Điều phối DAO, kiểm tra hợp lệ dữ liệu trước khi ghi CSDL, giữ danh sách bản ghi trong bộ nhớ
 * để giao diện truy xuất nhanh sau phương thức loadData().
 * </p>
 */
public class DeAnBUS {

    private DeAnDAO deAnDAO = new DeAnDAO();
    private ArrayList<DeAnDTO> listDeAn = new ArrayList<>();

    public DeAnBUS() {
        loadData();
    }

    public void loadData() {
        listDeAn = deAnDAO.getAll();
    }

    public ArrayList<DeAnDTO> getList() {
        return listDeAn;
    }

    public DeAnDTO getById(String maDA) {
        for (DeAnDTO dto : listDeAn) {
            if (dto.getMaDA() != null && dto.getMaDA().equalsIgnoreCase(maDA)) {
                return dto;
            }
        }
        return null;
    }

    private void validate(DeAnDTO da) {
        if (da.getMaDA() == null || da.getMaDA().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã đề án không được để trống!");
        }
        if (da.getTenDA() == null || da.getTenDA().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên đề án không được để trống!");
        }
        if (da.getNgayBatDau() == null) {
            throw new IllegalArgumentException("Ngày bắt đầu không hợp lệ!");
        }
        if (da.getNgayKetThuc() != null && da.getNgayKetThuc().before(da.getNgayBatDau())) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu!");
        }
    }

    public boolean add(DeAnDTO da) {
        validate(da);
        if (getById(da.getMaDA()) != null) {
            throw new IllegalArgumentException("Mã đề án đã tồn tại!");
        }

        if (deAnDAO.insert(da)) {
            listDeAn.add(da);
            return true;
        }
        return false;
    }

    public boolean update(DeAnDTO da) {
        validate(da);
        if (deAnDAO.update(da)) {
            for (int i = 0; i < listDeAn.size(); i++) {
                if (listDeAn.get(i).getMaDA() != null
                        && listDeAn.get(i).getMaDA().equalsIgnoreCase(da.getMaDA())) {
                    listDeAn.set(i, da);
                    break;
                }
            }
            return true;
        }
        return false;
    }

    public boolean delete(String maDA) {
        if (deAnDAO.delete(maDA)) {
            listDeAn.removeIf(da -> da.getMaDA() != null && da.getMaDA().equals(maDA));
            return true;
        }
        return false;
    }

    public boolean canDelete(String maDA) {
        if (maDA == null || maDA.trim().isEmpty()) {
            return false;
        }
        PhanCongDeAnBUS phanCongBUS = new PhanCongDeAnBUS();
        return phanCongBUS.getByMaDA(maDA).isEmpty();
    }

    public ArrayList<DeAnDTO> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return listDeAn;
        }

        String lower = keyword.toLowerCase().trim();
        return listDeAn.stream()
                .filter(da -> (da.getMaDA() != null && da.getMaDA().toLowerCase().contains(lower))
                        || (da.getTenDA() != null && da.getTenDA().toLowerCase().contains(lower)))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
