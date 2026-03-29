package com.hrm.dto;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Đối tượng chuyển tải dữ liệu (DTO) ánh xạ bảng PhuCap trong cơ sở dữ liệu.
 * <p>
 * Dùng truyền dữ liệu giữa DAO, BUS và Swing; không chứa logic truy cập CSDL.
 * </p>
 */
public class PhuCapDTO {

    private String maPC; // PK
    private String maNV; // FK
    private String maLoaiPC; // FK
    private BigDecimal soTien; // Số tiền phụ cấp
    private Date ngayApDung; // Ngày bắt đầu áp dụng
    private Date ngayKetThuc; // Ngày kết thúc (có thể null = vô thời hạn)

    public PhuCapDTO() {
    }

    public PhuCapDTO(String maPC, String maNV, String maLoaiPC,
            BigDecimal soTien, Date ngayApDung, Date ngayKetThuc) {
        this.maPC = maPC;
        this.maNV = maNV;
        this.maLoaiPC = maLoaiPC;
        this.soTien = soTien;
        this.ngayApDung = ngayApDung;
        this.ngayKetThuc = ngayKetThuc;
    }

    public String getMaPC() {
        return maPC;
    }

    public void setMaPC(String maPC) {
        this.maPC = maPC;
    }

    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public String getMaLoaiPC() {
        return maLoaiPC;
    }

    public void setMaLoaiPC(String maLoaiPC) {
        this.maLoaiPC = maLoaiPC;
    }

    public BigDecimal getSoTien() {
        return soTien;
    }

    public void setSoTien(BigDecimal soTien) {
        this.soTien = soTien;
    }

    public Date getNgayApDung() {
        return ngayApDung;
    }

    public void setNgayApDung(Date ngayApDung) {
        this.ngayApDung = ngayApDung;
    }

    public Date getNgayKetThuc() {
        return ngayKetThuc;
    }

    public void setNgayKetThuc(Date ngayKetThuc) {
        this.ngayKetThuc = ngayKetThuc;
    }

    @Override
    public String toString() {
        return "PhuCapDTO{"
                + "maPC='" + maPC + '\''
                + ", maNV='" + maNV + '\''
                + ", maLoaiPC='" + maLoaiPC + '\''
                + ", soTien=" + soTien
                + ", ngayApDung=" + ngayApDung
                + ", ngayKetThuc=" + ngayKetThuc
                + '}';
    }
}
