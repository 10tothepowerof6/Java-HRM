package com.hrm.dto;

import java.sql.Time;
import java.util.Date;

/**
 * DTO đại diện cho bảng BangChamCong trong CSDL.
 * Ghi nhận thông tin chấm công hàng ngày của nhân viên.
 *
 * 
 */
public class BangChamCongDTO {

    private String maChamCong;
    private String maNV;
    private Date ngayLamViec;
    private Time gioVao;
    private Time gioRa;
    private String trangThai;

    /** Constructor mặc định. */
    public BangChamCongDTO() {
    }

    /**
     * Constructor đầy đủ tham số.
     *
     * @param maChamCong  mã chấm công (PK)
     * @param maNV        mã nhân viên (FK)
     * @param ngayLamViec ngày làm việc
     * @param gioVao      giờ vào (nullable)
     * @param gioRa       giờ ra (nullable)
     * @param trangThai   trạng thái (Đi làm / Ốm / Phép / Không lương / Thai sản)
     */
    public BangChamCongDTO(String maChamCong, String maNV, Date ngayLamViec,
            Time gioVao, Time gioRa, String trangThai) {
        this.maChamCong = maChamCong;
        this.maNV = maNV;
        this.ngayLamViec = ngayLamViec;
        this.gioVao = gioVao;
        this.gioRa = gioRa;
        this.trangThai = trangThai;
    }

    public String getMaChamCong() {
        return maChamCong;
    }

    public void setMaChamCong(String maChamCong) {
        this.maChamCong = maChamCong;
    }

    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public Date getNgayLamViec() {
        return ngayLamViec;
    }

    public void setNgayLamViec(Date ngayLamViec) {
        this.ngayLamViec = ngayLamViec;
    }

    public Time getGioVao() {
        return gioVao;
    }

    public void setGioVao(Time gioVao) {
        this.gioVao = gioVao;
    }

    public Time getGioRa() {
        return gioRa;
    }

    public void setGioRa(Time gioRa) {
        this.gioRa = gioRa;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    @Override
    public String toString() {
        return "BangChamCongDTO{" +
                "maChamCong='" + maChamCong + '\'' +
                ", maNV='" + maNV + '\'' +
                ", ngayLamViec=" + ngayLamViec +
                ", gioVao=" + gioVao +
                ", gioRa=" + gioRa +
                ", trangThai='" + trangThai + '\'' +
                '}';
    }
}
