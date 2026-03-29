package com.hrm.dto;

import java.math.BigDecimal;
import java.util.Date;

/**
 * DTO đại diện cho bảng PhanCongDeAn trong CSDL.
 * Đây là bảng trung gian (junction table) cho quan hệ N-N giữa NhanVien và DeAn.
 */
public class PhanCongDeAnDTO {

    private String maNV; // PK (composite)
    private String maDA; // PK (composite)
    private Date ngayBatDau;
    private Date ngayKetThuc; // có thể null
    private BigDecimal phuCapDeAn;

    public PhanCongDeAnDTO() {
    }

    public PhanCongDeAnDTO(String maNV, String maDA, Date ngayBatDau,
            Date ngayKetThuc, BigDecimal phuCapDeAn) {
        this.maNV = maNV;
        this.maDA = maDA;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.phuCapDeAn = phuCapDeAn;
    }

    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public String getMaDA() {
        return maDA;
    }

    public void setMaDA(String maDA) {
        this.maDA = maDA;
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

    public BigDecimal getPhuCapDeAn() {
        return phuCapDeAn;
    }

    public void setPhuCapDeAn(BigDecimal phuCapDeAn) {
        this.phuCapDeAn = phuCapDeAn;
    }

    @Override
    public String toString() {
        return "PhanCongDeAnDTO{"
                + "maNV='" + maNV + '\''
                + ", maDA='" + maDA + '\''
                + ", ngayBatDau=" + ngayBatDau
                + ", ngayKetThuc=" + ngayKetThuc
                + ", phuCapDeAn=" + phuCapDeAn
                + '}';
    }
}
