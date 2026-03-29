package com.hrm.dto;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Đối tượng chuyển tải dữ liệu (DTO) ánh xạ bảng ChiTietThuong trong cơ sở dữ liệu.
 * <p>
 * Dùng truyền dữ liệu giữa DAO, BUS và Swing; không chứa logic truy cập CSDL.
 * </p>
 */
public class ChiTietThuongDTO {

    private String maCTT; // PK
    private String maNV; // FK
    private String maThuong; // FK -> DanhMucThuong
    private BigDecimal soTien; // Số tiền thưởng
    private Date ngayThuong; // Ngày thưởng
    private String ghiChu; // Ghi chú

    public ChiTietThuongDTO() {
    }

    public ChiTietThuongDTO(String maCTT, String maNV, String maThuong,
            BigDecimal soTien, Date ngayThuong, String ghiChu) {
        this.maCTT = maCTT;
        this.maNV = maNV;
        this.maThuong = maThuong;
        this.soTien = soTien;
        this.ngayThuong = ngayThuong;
        this.ghiChu = ghiChu;
    }

    public String getMaCTT() {
        return maCTT;
    }

    public void setMaCTT(String maCTT) {
        this.maCTT = maCTT;
    }

    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public String getMaThuong() {
        return maThuong;
    }

    public void setMaThuong(String maThuong) {
        this.maThuong = maThuong;
    }

    public BigDecimal getSoTien() {
        return soTien;
    }

    public void setSoTien(BigDecimal soTien) {
        this.soTien = soTien;
    }

    public Date getNgayThuong() {
        return ngayThuong;
    }

    public void setNgayThuong(Date ngayThuong) {
        this.ngayThuong = ngayThuong;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    @Override
    public String toString() {
        return "ChiTietThuongDTO{"
                + "maCTT='" + maCTT + '\''
                + ", maNV='" + maNV + '\''
                + ", maThuong='" + maThuong + '\''
                + ", soTien=" + soTien
                + ", ngayThuong=" + ngayThuong
                + ", ghiChu='" + ghiChu + '\''
                + '}';
    }
}
