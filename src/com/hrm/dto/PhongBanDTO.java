package com.hrm.dto;

import java.util.Date;

/**
 * DTO đại diện cho bảng PhongBan trong CSDL.
 * Chứa thông tin phòng ban trong công ty.
 *
 * 
 */
public class PhongBanDTO {

    private String maPB;
    private String tenPB;
    private String maTruongPB;
    private Date ngayThanhLap;
    private String email;

    /** Constructor mặc định. */
    public PhongBanDTO() {
    }

    /**
     * Constructor đầy đủ tham số.
     *
     * @param maPB         mã phòng ban (PK)
     * @param tenPB        tên phòng ban
     * @param maTruongPB   mã trưởng phòng (FK → NhanVien), nullable
     * @param ngayThanhLap ngày thành lập
     * @param email        email phòng ban
     */
    public PhongBanDTO(String maPB, String tenPB, String maTruongPB,
            Date ngayThanhLap, String email) {
        this.maPB = maPB;
        this.tenPB = tenPB;
        this.maTruongPB = maTruongPB;
        this.ngayThanhLap = ngayThanhLap;
        this.email = email;
    }

    public String getMaPB() {
        return maPB;
    }

    public void setMaPB(String maPB) {
        this.maPB = maPB;
    }

    public String getTenPB() {
        return tenPB;
    }

    public void setTenPB(String tenPB) {
        this.tenPB = tenPB;
    }

    public String getMaTruongPB() {
        return maTruongPB;
    }

    public void setMaTruongPB(String maTruongPB) {
        this.maTruongPB = maTruongPB;
    }

    public Date getNgayThanhLap() {
        return ngayThanhLap;
    }

    public void setNgayThanhLap(Date ngayThanhLap) {
        this.ngayThanhLap = ngayThanhLap;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "PhongBanDTO{" +
                "maPB='" + maPB + '\'' +
                ", tenPB='" + tenPB + '\'' +
                ", maTruongPB='" + maTruongPB + '\'' +
                ", ngayThanhLap=" + ngayThanhLap +
                ", email='" + email + '\'' +
                '}';
    }
}
