package com.hrm.dto;

import java.util.Date;

/**
 * Đối tượng chuyển tải dữ liệu (DTO) ánh xạ bảng {@code NhanVien} — hồ sơ nhân sự tối thiểu phục vụ nghiệp vụ chung.
 * <p>
 * Thông tin nhạy cảm chi tiết (CCCD, địa chỉ…) nằm ở {@link ChiTietNhanVienDTO}. Trạng thái làm việc dùng lọc
 * tính lương, báo cáo tổng quan, v.v.
 * </p>
 */
public class NhanVienDTO {

    private String maNV;
    private String ho;
    private String ten;
    private String gioiTinh;
    private Date ngaySinh;
    private Date ngayBatDau;
    private String trangThai;
    private String maPB;
    private String maCV;

    /** Constructor mặc định. */
    public NhanVienDTO() {
    }

    /**
     * Constructor đầy đủ tham số.
     *
     * @param maNV       mã nhân viên (PK)
     * @param ho         họ nhân viên
     * @param ten        tên nhân viên
     * @param gioiTinh   giới tính (Nam/Nữ)
     * @param ngaySinh   ngày sinh
     * @param ngayBatDau ngày bắt đầu làm việc
     * @param trangThai  trạng thái (Đang làm / Đã nghỉ)
     * @param maPB       mã phòng ban (FK)
     * @param maCV       mã chức vụ (FK)
     */
    public NhanVienDTO(String maNV, String ho, String ten, String gioiTinh,
            Date ngaySinh, Date ngayBatDau, String trangThai,
            String maPB, String maCV) {
        this.maNV = maNV;
        this.ho = ho;
        this.ten = ten;
        this.gioiTinh = gioiTinh;
        this.ngaySinh = ngaySinh;
        this.ngayBatDau = ngayBatDau;
        this.trangThai = trangThai;
        this.maPB = maPB;
        this.maCV = maCV;
    }

    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public String getHo() {
        return ho;
    }

    public void setHo(String ho) {
        this.ho = ho;
    }

    public String getTen() {
        return ten;
    }

    public void setTen(String ten) {
        this.ten = ten;
    }

    public String getGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(String gioiTinh) {
        this.gioiTinh = gioiTinh;
    }

    public Date getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(Date ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public Date getNgayBatDau() {
        return ngayBatDau;
    }

    public void setNgayBatDau(Date ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getMaPB() {
        return maPB;
    }

    public void setMaPB(String maPB) {
        this.maPB = maPB;
    }

    public String getMaCV() {
        return maCV;
    }

    public void setMaCV(String maCV) {
        this.maCV = maCV;
    }

    @Override
    public String toString() {
        return "NhanVienDTO{" +
                "maNV='" + maNV + '\'' +
                ", ho='" + ho + '\'' +
                ", ten='" + ten + '\'' +
                ", gioiTinh='" + gioiTinh + '\'' +
                ", ngaySinh=" + ngaySinh +
                ", ngayBatDau=" + ngayBatDau +
                ", trangThai='" + trangThai + '\'' +
                ", maPB='" + maPB + '\'' +
                ", maCV='" + maCV + '\'' +
                '}';
    }
}
