package com.hrm.dto;

import java.math.BigDecimal;
import java.util.Date;

/**
 * DTO đại diện cho bảng HopDong trong CSDL.
 * Chứa thông tin hợp đồng lao động của nhân viên.
 *
 * 
 */
public class HopDongDTO {

    private String maHD;
    private String maNV;
    private String loaiHD;
    private Date ngayBatDau;
    private Date ngayKetThuc;
    private BigDecimal luongHopDong;
    private String trangThai;
    private String ghiChu;

    /** Constructor mặc định. */
    public HopDongDTO() {
    }

    /**
     * Constructor đầy đủ tham số.
     *
     * @param maHD         mã hợp đồng (PK)
     * @param maNV         mã nhân viên (FK)
     * @param loaiHD       loại hợp đồng (Thử việc / 1 năm / 3 năm / Vô thời hạn)
     * @param ngayBatDau   ngày bắt đầu hiệu lực
     * @param ngayKetThuc  ngày kết thúc (NULL nếu vô thời hạn)
     * @param luongHopDong mức lương ghi trên hợp đồng
     * @param trangThai    trạng thái (Hiệu lực / Hết hạn / Chấm dứt)
     * @param ghiChu       ghi chú
     */
    public HopDongDTO(String maHD, String maNV, String loaiHD,
            Date ngayBatDau, Date ngayKetThuc,
            BigDecimal luongHopDong, String trangThai,
            String ghiChu) {
        this.maHD = maHD;
        this.maNV = maNV;
        this.loaiHD = loaiHD;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.luongHopDong = luongHopDong;
        this.trangThai = trangThai;
        this.ghiChu = ghiChu;
    }

    public String getMaHD() {
        return maHD;
    }

    public void setMaHD(String maHD) {
        this.maHD = maHD;
    }

    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public String getLoaiHD() {
        return loaiHD;
    }

    public void setLoaiHD(String loaiHD) {
        this.loaiHD = loaiHD;
    }

    public Date getNgayBatDau() {
        return ngayBatDau;
    }

    public void setNgayBatDau(Date ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public Date getNgayKetThuc() {
        return ngayKetThuc;
    }

    public void setNgayKetThuc(Date ngayKetThuc) {
        this.ngayKetThuc = ngayKetThuc;
    }

    public BigDecimal getLuongHopDong() {
        return luongHopDong;
    }

    public void setLuongHopDong(BigDecimal luongHopDong) {
        this.luongHopDong = luongHopDong;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    @Override
    public String toString() {
        return "HopDongDTO{" +
                "maHD='" + maHD + '\'' +
                ", maNV='" + maNV + '\'' +
                ", loaiHD='" + loaiHD + '\'' +
                ", ngayBatDau=" + ngayBatDau +
                ", ngayKetThuc=" + ngayKetThuc +
                ", luongHopDong=" + luongHopDong +
                ", trangThai='" + trangThai + '\'' +
                ", ghiChu='" + ghiChu + '\'' +
                '}';
    }
}
