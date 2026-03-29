package com.hrm.bus;

import com.hrm.dto.BangLuongThangDTO;
import com.hrm.dao.PhuCapDAO;
import com.hrm.dto.PhuCapDTO;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Lớp nghiệp vụ (BUS) cho nghiệp vụ liên quan bảng PhuCap.
 * <p>
 * Điều phối DAO, kiểm tra hợp lệ dữ liệu trước khi ghi CSDL, giữ danh sách bản ghi trong bộ nhớ
 * để giao diện truy xuất nhanh sau phương thức loadData().
 * </p>
 */
public class PhuCapBUS {

    private PhuCapDAO dao = new PhuCapDAO();
    private ArrayList<PhuCapDTO> list = new ArrayList<>();

    public PhuCapBUS() {
        loadData();
    }

    public void loadData() {
        list = dao.getAll();
    }

    public ArrayList<PhuCapDTO> getList() {
        return list;
    }

    public PhuCapDTO getById(String maPC) {
        for (PhuCapDTO dto : list) {
            if (dto.getMaPC() != null && dto.getMaPC().equalsIgnoreCase(maPC)) {
                return dto;
            }
        }
        return null;
    }

    private void validate(PhuCapDTO dto) {
        if (dto.getMaPC() == null || dto.getMaPC().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã phụ cấp không được để trống!");
        }
        if (dto.getMaNV() == null || dto.getMaNV().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã nhân viên không được để trống!");
        }
        if (dto.getSoTien() == null || dto.getSoTien().signum() < 0) {
            throw new IllegalArgumentException("Số tiền phụ cấp không hợp lệ!");
        }
        if (dto.getNgayApDung() == null) {
            throw new IllegalArgumentException("Ngày áp dụng không được để trống!");
        }
        if (dto.getNgayKetThuc() != null && dto.getNgayKetThuc().before(dto.getNgayApDung())) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày áp dụng!");
        }
    }

    public boolean add(PhuCapDTO dto) {
        validate(dto);
        // Đảm bảo các mã loại phụ cấp mặc định đã có trong DB để tránh lỗi FK.
        new LoaiPhuCapBUS();
        if (getById(dto.getMaPC()) != null) {
            throw new IllegalArgumentException("Mã phụ cấp đã tồn tại!");
        }

        if (dao.insert(dto)) {
            list.add(dto);
            return true;
        }
        return false;
    }

    public boolean update(PhuCapDTO dto) {
        validate(dto);
        // Đảm bảo các mã loại phụ cấp mặc định đã có trong DB để tránh lỗi FK.
        new LoaiPhuCapBUS();
        if (dao.update(dto)) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getMaPC() != null && list.get(i).getMaPC().equalsIgnoreCase(dto.getMaPC())) {
                    list.set(i, dto);
                    break;
                }
            }
            return true;
        }
        return false;
    }

    public boolean delete(String maPC) {
        if (dao.delete(maPC)) {
            list.removeIf(dto -> dto.getMaPC() != null && dto.getMaPC().equals(maPC));
            return true;
        }
        return false;
    }

    /**
     * Guard xóa: chặn nếu phụ cấp (theo khoảng ngày) đang "giao" với tháng/năm
     * của các bản bảng lương tháng đã tạo.
     */
    public boolean canDelete(PhuCapDTO dto) {
        String reason = getDeleteBlockReason(dto);
        return reason == null || reason.trim().isEmpty();
    }

    /**
     * @return rỗng/null nếu được phép xóa, ngược lại trả về lý do chặn xóa.
     */
    public String getDeleteBlockReason(PhuCapDTO dto) {
        if (dto == null) return "";

        // Dùng danh sách hiện tại của bảng lương tháng.
        BangLuongThangBUS bangLuongThangBUS = new BangLuongThangBUS();
        ArrayList<BangLuongThangDTO> listBangLuong = bangLuongThangBUS.getList();

        Date ngayApDung = dto.getNgayApDung();
        if (ngayApDung == null) {
            return ""; // dữ liệu lỗi thì để cho thao tác CRUD tự validate/throw
        }

        Date ngayKetThuc = dto.getNgayKetThuc();
        Date farFuture = getFarFutureDate();

        Date allowanceStart = truncateTime(ngayApDung);
        Date allowanceEnd = truncateTime(ngayKetThuc != null ? ngayKetThuc : farFuture);

        ArrayList<String> affectedPeriods = new ArrayList<>();

        for (BangLuongThangDTO bl : listBangLuong) {
            if (bl.getMaNV() == null || dto.getMaNV() == null) continue;
            if (!bl.getMaNV().equalsIgnoreCase(dto.getMaNV())) continue;

            Date monthStart = getMonthStart(bl.getThang(), bl.getNam());
            Date monthEnd = getMonthEnd(bl.getThang(), bl.getNam());

            // Giao khoảng (inclusive):
            // allowanceStart <= monthEnd && allowanceEnd >= monthStart
            if (allowanceStart.compareTo(monthEnd) <= 0 && allowanceEnd.compareTo(monthStart) >= 0) {
                affectedPeriods.add(bl.getThang() + "/" + bl.getNam());
                if (affectedPeriods.size() >= 3) break; // chặn message dài
            }
        }

        if (affectedPeriods.isEmpty()) return "";

        return "Không thể xóa phụ cấp vì phụ cấp này đang ảnh hưởng tới bảng lương tháng: "
                + String.join(", ", affectedPeriods)
                + (affectedPeriods.size() >= 3 ? " (và các tháng khác nếu có)." : ".");
    }

    private static Date getFarFutureDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 9999);
        cal.set(Calendar.MONTH, Calendar.DECEMBER);
        cal.set(Calendar.DAY_OF_MONTH, 31);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private static Date truncateTime(Date d) {
        if (d == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private static Date getMonthStart(int thang, int nam) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, nam);
        cal.set(Calendar.MONTH, thang - 1);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private static Date getMonthEnd(int thang, int nam) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, nam);
        cal.set(Calendar.MONTH, thang - 1);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }
}
