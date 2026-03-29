package com.hrm.dto;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Đối tượng chuyển tải dữ liệu (DTO) ánh xạ bảng DeAn trong cơ sở dữ liệu.
 * <p>
 * Dùng truyền dữ liệu giữa DAO, BUS và Swing; không chứa logic truy cập CSDL.
 * </p>
 */
public class DeAnDTO {

    private String maDA; // PK
    private String tenDA;
    private Date ngayBatDau;
    private Date ngayKetThuc; // có thể null
    private String maPB; // FK
    private BigDecimal vonDeAn;
    private String trangThai;

    public DeAnDTO() {
    }

    public DeAnDTO(String maDA, String tenDA, Date ngayBatDau,
            Date ngayKetThuc, String maPB, BigDecimal vonDeAn,
            String trangThai) {
        this.maDA = maDA;
        this.tenDA = tenDA;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.maPB = maPB;
        this.vonDeAn = vonDeAn;
        this.trangThai = trangThai;
    }

    public String getMaDA() {
        return maDA;
    }

    public void setMaDA(String maDA) {
        this.maDA = maDA;
    }

    public String getTenDA() {
        return tenDA;
    }

    public void setTenDA(String tenDA) {
        this.tenDA = tenDA;
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

    public String getMaPB() {
        return maPB;
    }

    public void setMaPB(String maPB) {
        this.maPB = maPB;
    }

    public BigDecimal getVonDeAn() {
        return vonDeAn;
    }

    public void setVonDeAn(BigDecimal vonDeAn) {
        this.vonDeAn = vonDeAn;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    @Override
    public String toString() {
        return "DeAnDTO{"
                + "maDA='" + maDA + '\''
                + ", tenDA='" + tenDA + '\''
                + ", ngayBatDau=" + ngayBatDau
                + ", ngayKetThuc=" + ngayKetThuc
                + ", maPB='" + maPB + '\''
                + ", vonDeAn=" + vonDeAn
                + ", trangThai='" + trangThai + '\''
                + '}';
    }
}
