package com.hrm.bus;

import com.hrm.dao.BangLuongThangDAO;
import com.hrm.dto.BangLuongThangDTO;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.stream.Collectors;
import com.hrm.bus.*;
import com.hrm.dto.*;

/**
 * Lớp nghiệp vụ (BUS) cho nghiệp vụ liên quan bảng BangLuongThang.
 * <p>
 * Ngoài CRUD, cung cấp {@link #generateBangLuong(int, int)} để tổng hợp lương theo tháng cho từng nhân viên đang làm,
 * dựa trên hợp đồng hiệu lực, chức vụ, chấm công, phụ cấp, phân công đề án và chi tiết thưởng trong tháng.
 * </p>
 */
public class BangLuongThangBUS {

    private BangLuongThangDAO dao = new BangLuongThangDAO();
    private ArrayList<BangLuongThangDTO> list = new ArrayList<>();

    public BangLuongThangBUS() {
        loadData();
    }

    public void loadData() {
        list = dao.getAll();
    }

    public ArrayList<BangLuongThangDTO> getList() {
        return list;
    }

    public BangLuongThangDTO getById(String maBangLuong) {
        for (BangLuongThangDTO dto : list) {
            if (dto.getMaBangLuong().equalsIgnoreCase(maBangLuong)) {
                return dto;
            }
        }
        return null;
    }

    public BangLuongThangDTO getByMaNVThangNam(String maNV, int thang, int nam) {
        for (BangLuongThangDTO dto : list) {
            if (dto.getMaNV().equalsIgnoreCase(maNV) && dto.getThang() == thang && dto.getNam() == nam) {
                return dto;
            }
        }
        return null;
    }

    public boolean deleteAll() {
        if (dao.deleteAll()) {
            list.clear();
            return true;
        }
        return false;
    }

    private void validate(BangLuongThangDTO dto) {
        if (dto.getMaBangLuong() == null || dto.getMaBangLuong().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã bảng lương không được để trống!");
        }
        if (dto.getMaNV() == null || dto.getMaNV().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã nhân viên không được để trống!");
        }
        if (dto.getThang() < 1 || dto.getThang() > 12) {
            throw new IllegalArgumentException("Tháng không hợp lệ (1-12)!");
        }
        if (dto.getNam() < 2000) {
            throw new IllegalArgumentException("Năm không hợp lệ!");
        }
    }

    public boolean add(BangLuongThangDTO dto) {
        validate(dto);
        if (getById(dto.getMaBangLuong()) != null) {
            throw new IllegalArgumentException("Mã bảng lương đã tồn tại!");
        }
        if (getByMaNVThangNam(dto.getMaNV(), dto.getThang(), dto.getNam()) != null) {
             throw new IllegalArgumentException("Nhân viên " + dto.getMaNV() + " đã có kết quả tính lương trong tháng " + dto.getThang() + "/" + dto.getNam() + "!");
        }

        if (dao.insert(dto)) {
            list.add(dto);
            return true;
        }
        return false;
    }

    public boolean update(BangLuongThangDTO dto) {
        validate(dto);
        if (dao.update(dto)) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getMaBangLuong().equals(dto.getMaBangLuong())) {
                    list.set(i, dto);
                    break;
                }
            }
            return true;
        }
        return false;
    }

    public boolean delete(String maBangLuong) {
        if (dao.delete(maBangLuong)) {
            list.removeIf(dto -> dto.getMaBangLuong().equals(maBangLuong));
            return true;
        }
        return false;
    }

    /**
     * Tính và ghi bảng lương tháng cho mọi nhân viên có trạng thái "Đang làm".
     * <p>
     * Với từng người: xóa bản ghi cũ cùng (maNV, tháng, năm) nếu có; lương cơ bản từ hợp đồng hiệu lực
     * (hệ số chức vụ × lương hợp đồng), fallback mức lương cơ sở × hệ số; công thực tế đếm dòng chấm công "Đi làm";
     * cộng phụ cấp + phụ cấp đề án; cộng thưởng có ngày trong tháng; trừ BHXH/BHYT/BHTN theo % tham số;
     * sinh mã bảng lương rút gọn và insert. Trả về số bản ghi insert thành công.
     * </p>
     */
    public int generateBangLuong(int thang, int nam) {
        int countSuccess = 0;
        
        NhanVienBUS nvBus = new NhanVienBUS();
        ChucVuBUS cvBus = new ChucVuBUS();
        ThamSoBUS tsBus = new ThamSoBUS();
        PhuCapBUS pcBus = new PhuCapBUS();
        ChiTietThuongBUS thuongBus = new ChiTietThuongBUS();
        BangChamCongBUS ccBus = new BangChamCongBUS();
        PhanCongDeAnBUS pcdaBus = new PhanCongDeAnBUS();
        HopDongBUS hdBus = new HopDongBUS();
        
        // 1. Kéo tham số
        BigDecimal mucLuongCoSo = tsBus.getGiaTriThamSo("MucLuongCoSo");
        BigDecimal tyLeBHXH = tsBus.getGiaTriThamSo("TyLeBHXH");
        BigDecimal tyLeBHYT = tsBus.getGiaTriThamSo("TyLeBHYT");
        BigDecimal tyLeBHTN = tsBus.getGiaTriThamSo("TyLeBHTN");
        
        // Cần đảm bảo tham số != null và != 0 hợp lý
        if(mucLuongCoSo == null || mucLuongCoSo.compareTo(BigDecimal.ZERO) == 0) mucLuongCoSo = new BigDecimal("2340000"); // default luật
        if(tyLeBHXH == null) tyLeBHXH = new BigDecimal("8.00");
        if(tyLeBHYT == null) tyLeBHYT = new BigDecimal("1.50");
        if(tyLeBHTN == null) tyLeBHTN = new BigDecimal("1.00");

        // 2. Lấy danh sách NV đang làm việc
        ArrayList<NhanVienDTO> activeEmployees = (ArrayList<NhanVienDTO>) nvBus.getList().stream()
                .filter(nv -> "Đang làm".equals(nv.getTrangThai()))
                .collect(Collectors.toList());

        for (NhanVienDTO nv : activeEmployees) {
            String maNV = nv.getMaNV();
            
            // Xóa record cũ nếu có (Hoặc hỏi trước ở giao diện, ở đây assume là Overwrite)
            BangLuongThangDTO checkExist = getByMaNVThangNam(maNV, thang, nam);
            if (checkExist != null) {
                delete(checkExist.getMaBangLuong());
            }

            // --- Lương Cơ Bản ---
            String maCV = nv.getMaCV();
            BigDecimal heSoLuong = BigDecimal.ONE;
            ChucVuDTO cv = cvBus.getById(maCV);
            if (cv != null && cv.getHeSoLuong() != null) {
                heSoLuong = cv.getHeSoLuong();
            }

            BigDecimal luongCoBan = BigDecimal.ZERO;
            for (HopDongDTO hd : hdBus.getList()) {
                if (hd.getMaNV().equals(maNV) && "Có hiệu lực".equals(hd.getTrangThai())) {
                    if (hd.getLuongHopDong() != null) {
                        luongCoBan = heSoLuong.multiply(hd.getLuongHopDong()).setScale(0, RoundingMode.HALF_UP);
                        break;
                    }
                }
            }

            // Fallback về Mức lương cơ sở * Hệ số nếu nhân viên chưa có hợp đồng hiệu lực
            if (luongCoBan.compareTo(BigDecimal.ZERO) == 0) {
                luongCoBan = heSoLuong.multiply(mucLuongCoSo).setScale(0, RoundingMode.HALF_UP);
            }

            // --- Số Ngày Công Thực Tế ---
            // Lọc chấm công của tháng/năm này
            long soNgayCongLong = ccBus.getList().stream()
                .filter(cc -> cc.getMaNV().equals(maNV))
                .filter(cc -> cc.getNgayLamViec() != null && (cc.getNgayLamViec().getMonth()+1) == thang && (cc.getNgayLamViec().getYear()+1900) == nam)
                .filter(cc -> "Đi làm".equals(cc.getTrangThai()))
                .count();
            int soNgayCong = (int) soNgayCongLong;

            // --- Phụ Cấp ---
            // Giả định đơn giản: Tổng các phụ cấp đang có hiệu lực trong tháng đó
            // Lọc: NgayApDung <= last day of month, NgayKetThuc >= first day of month (hoặc null)
            BigDecimal tongPhuCap = BigDecimal.ZERO;
            for (PhuCapDTO pc : pcBus.getList()) {
                if (pc.getMaNV().equals(maNV)) {
                    // Logic lọc ngày hiệu lực có thể phức tạp, tạm thời lấy hết các phụ cấp đang có
                    tongPhuCap = tongPhuCap.add(pc.getSoTien());
                }
            }
            
            // --- Cả thưởng từ bảng phân công đề án (Phụ cấp dự án) ---
            for (PhanCongDeAnDTO pcda : pcdaBus.getList()) {
                if (pcda.getMaNV().equals(maNV) && pcda.getPhuCapDeAn() != null) {
                    tongPhuCap = tongPhuCap.add(pcda.getPhuCapDeAn());
                }
            }

            // --- Thưởng ---
            BigDecimal tongThuong = BigDecimal.ZERO;
            for (ChiTietThuongDTO thuong : thuongBus.getList()) {
                if (thuong.getMaNV() != null && thuong.getMaNV().equals(maNV)
                        && thuong.getNgayThuong() != null && thuong.getSoTien() != null) {
                    if ((thuong.getNgayThuong().getMonth()+1) == thang && (thuong.getNgayThuong().getYear()+1900) == nam) {
                        tongThuong = tongThuong.add(thuong.getSoTien());
                    }
                }
            }

            // --- Khấu Trừ Bảo Hiểm --- (Dựa vào Lương Cơ Bản)
            BigDecimal khauTruBHXH = luongCoBan.multiply(tyLeBHXH).divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);
            BigDecimal khauTruBHYT = luongCoBan.multiply(tyLeBHYT).divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);
            BigDecimal khauTruBHTN = luongCoBan.multiply(tyLeBHTN).divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);
            
            // --- Thuế TNCN --- (Tạm tính 0 trong bài toán đơn giản)
            BigDecimal thueTNCN = BigDecimal.ZERO;

            // --- Thực lãnh ---
            BigDecimal thucLanh = luongCoBan.add(tongPhuCap).add(tongThuong)
                                  .subtract(khauTruBHXH).subtract(khauTruBHYT).subtract(khauTruBHTN).subtract(thueTNCN);
            
            if (thucLanh.compareTo(BigDecimal.ZERO) < 0) {
                thucLanh = BigDecimal.ZERO;
            }

            // Tạo mã Bảng Lương ngắn (VD: 0326NV001 - 9 ký tự) để vừa với VARCHAR(10)
            String maBangLuong = String.format("%02d%02d%s", thang, nam % 100, maNV);

            BangLuongThangDTO bl = new BangLuongThangDTO(
                maBangLuong, maNV, thang, nam, soNgayCong, luongCoBan, heSoLuong,
                tongPhuCap, tongThuong, khauTruBHXH, khauTruBHYT, khauTruBHTN, thueTNCN, thucLanh
            );

            if (dao.insert(bl)) {
                list.add(bl);
                countSuccess++;
            }
        }
        return countSuccess;
    }
}
