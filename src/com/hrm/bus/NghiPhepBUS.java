package com.hrm.bus;

import com.hrm.dao.NghiPhepDAO;
import com.hrm.dto.NghiPhepDTO;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Lớp nghiệp vụ (BUS) cho nghiệp vụ liên quan bảng NghiPhep.
 * <p>
 * Tự tính {@code soNgay} từ khoảng ngày khi thêm/sửa. Khi chuyển trạng thái sang <b>Đã duyệt</b>, có thể kiểm tra
 * tổng ngày đã duyệt trong năm dương lịch theo từng loại phép so với {@link com.hrm.dto.LoaiNghiPhepDTO#getSoNgayToiDa()}.
 * </p>
 */
public class NghiPhepBUS {

    private NghiPhepDAO dao = new NghiPhepDAO();
    private ArrayList<NghiPhepDTO> list = new ArrayList<>();

    public NghiPhepBUS() {
        loadData();
    }

    public void loadData() {
        list = dao.getAll();
    }

    public ArrayList<NghiPhepDTO> getList() {
        return list;
    }

    public NghiPhepDTO getById(String maNP) {
        for (NghiPhepDTO dto : list) {
            if (dto.getMaNP().equalsIgnoreCase(maNP)) {
                return dto;
            }
        }
        return null;
    }

    private void validate(NghiPhepDTO dto) {
        if (dto.getMaNP() == null || dto.getMaNP().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã nghỉ phép không được để trống!");
        }
        if (dto.getMaNV() == null || dto.getMaNV().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã nhân viên không được để trống!");
        }
        if (dto.getTuNgay() == null || dto.getDenNgay() == null) {
            throw new IllegalArgumentException("Ngày bắt đầu và kết thúc không hợp lệ!");
        }
        if (dto.getDenNgay().before(dto.getTuNgay())) {
            throw new IllegalArgumentException("Ngày kết thúc không được trước ngày bắt đầu!");
        }
    }

    public boolean add(NghiPhepDTO dto) {
        validate(dto);
        if (getById(dto.getMaNP()) != null) {
            throw new IllegalArgumentException("Mã nghỉ phép đã tồn tại!");
        }
        
        // Tự động tính số ngày = (DenNgay - TuNgay) theo milliseconds / (24*60*60*1000) + 1
        long diffMillies = Math.abs(dto.getDenNgay().getTime() - dto.getTuNgay().getTime());
        long diffDays = (diffMillies / (24 * 60 * 60 * 1000)) + 1;
        dto.setSoNgay((int) diffDays);

        if (dao.insert(dto)) {
            list.add(dto);
            return true;
        }
        return false;
    }

    /**
     * Cập nhật đơn nghỉ; nếu trạng thái là Đã duyệt thì áp dụng kiểm tra hạn mức năm (mô tả dưới đây).
     */
    public boolean update(NghiPhepDTO dto) {
        validate(dto);
        
        long diffMillies = Math.abs(dto.getDenNgay().getTime() - dto.getTuNgay().getTime());
        long diffDays = (diffMillies / (24 * 60 * 60 * 1000)) + 1;
        dto.setSoNgay((int) diffDays);
        
        /*
         * Khi duyệt: với mỗi cặp (nhân viên + loại phép), cộng dồn số ngày của mọi đơn khác
         * đã duyệt có TuNgay thuộc cùng năm dương lịch với "hôm nay". Không tính lại chính đơn hiện tại (so khớp MaNP).
         * Nếu tổng cũ + số ngày đơn này vượt SoNgayToiDa trên LoaiNghiPhep (>0) thì ném IllegalArgumentException.
         */
        if ("Đã duyệt".equalsIgnoreCase(dto.getTrangThai())) {
            LoaiNghiPhepBUS loaiNghiPhepBUS = new LoaiNghiPhepBUS();
            com.hrm.dto.LoaiNghiPhepDTO loaiNP = loaiNghiPhepBUS.getById(dto.getMaLoaiNP());
            if (loaiNP != null && loaiNP.getSoNgayToiDa() > 0) {
                int currentYear = java.time.LocalDate.now().getYear();
                int tongSoNgayDaNghi = 0;
                for (NghiPhepDTO np : list) {
                    if (np.getMaNV().equals(dto.getMaNV()) 
                            && np.getMaLoaiNP().equals(dto.getMaLoaiNP())
                            && "Đã duyệt".equalsIgnoreCase(np.getTrangThai())
                            && !np.getMaNP().equals(dto.getMaNP())) {
                        
                        java.util.Calendar cal = java.util.Calendar.getInstance();
                        cal.setTime(np.getTuNgay());
                        if (cal.get(java.util.Calendar.YEAR) == currentYear) {
                            tongSoNgayDaNghi += np.getSoNgay();
                        }
                    }
                }
                
                if (tongSoNgayDaNghi + dto.getSoNgay() > loaiNP.getSoNgayToiDa()) {
                    throw new IllegalArgumentException("Không thể duyệt! Tổng số ngày nghỉ phép loại này trong năm đã vượt quá mức tối đa (" + loaiNP.getSoNgayToiDa() + " ngày).");
                }
            }
        }
        if (dao.update(dto)) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getMaNP().equals(dto.getMaNP())) {
                    list.set(i, dto);
                    break;
                }
            }
            return true;
        }
        return false;
    }

    public boolean delete(String maNP) {
        if (dao.delete(maNP)) {
            list.removeIf(dto -> dto.getMaNP().equals(maNP));
            return true;
        }
        return false;
    }

    public ArrayList<NghiPhepDTO> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return list;
        }
        String lowerKeyword = keyword.toLowerCase().trim();
        return (ArrayList<NghiPhepDTO>) list.stream()
                .filter(dto -> dto.getMaNP().toLowerCase().contains(lowerKeyword) ||
                               dto.getMaNV().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }
}
