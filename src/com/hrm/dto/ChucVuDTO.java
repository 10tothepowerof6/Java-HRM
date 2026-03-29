package com.hrm.dto;

import java.math.BigDecimal;

/**
 * DTO đại diện cho bảng ChucVu trong CSDL.
 * Bảng danh mục chứa chức vụ và hệ số lương tương ứng.
 *
 * 
 */
public class ChucVuDTO {

    private String maCV;
    private String tenCV;
    private BigDecimal heSoLuong;

    /** Constructor mặc định. */
    public ChucVuDTO() {
    }

    /**
     * Constructor đầy đủ tham số.
     *
     * @param maCV      mã chức vụ (PK)
     * @param tenCV     tên chức vụ
     * @param heSoLuong hệ số lương (VD: 1.0, 1.5, 2.0)
     */
    public ChucVuDTO(String maCV, String tenCV, BigDecimal heSoLuong) {
        this.maCV = maCV;
        this.tenCV = tenCV;
        this.heSoLuong = heSoLuong;
    }

    public String getMaCV() {
        return maCV;
    }

    public void setMaCV(String maCV) {
        this.maCV = maCV;
    }

    public String getTenCV() {
        return tenCV;
    }

    public void setTenCV(String tenCV) {
        this.tenCV = tenCV;
    }

    public BigDecimal getHeSoLuong() {
        return heSoLuong;
    }

    public void setHeSoLuong(BigDecimal heSoLuong) {
        this.heSoLuong = heSoLuong;
    }

    @Override
    public String toString() {
        return "ChucVuDTO{" +
                "maCV='" + maCV + '\'' +
                ", tenCV='" + tenCV + '\'' +
                ", heSoLuong=" + heSoLuong +
                '}';
    }
}
