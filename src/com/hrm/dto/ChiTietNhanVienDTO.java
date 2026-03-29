package com.hrm.dto;

/**
 * Đối tượng chuyển tải dữ liệu (DTO) ánh xạ bảng {@code ChiTietNhanVien}: quan hệ 1–1 với {@link NhanVienDTO}.
 * <p>
 * Lưu thông tin liên hệ và định danh bổ sung; tách khỏi bảng nhân viên chính để giảm độ rộng bản ghi khi tra cứu danh sách.
 * </p>
 */
public class ChiTietNhanVienDTO {

    private String maNV;
    private String cccd;
    private String diaChi;
    private String sdt;
    private String email;

    /** Constructor mặc định. */
    public ChiTietNhanVienDTO() {
    }

    /**
     * Constructor đầy đủ tham số.
     *
     * @param maNV   mã nhân viên (PK, FK)
     * @param cccd   căn cước công dân (UNIQUE)
     * @param diaChi nơi thường trú
     * @param sdt    số điện thoại
     * @param email  email
     */
    public ChiTietNhanVienDTO(String maNV, String cccd, String diaChi,
            String sdt, String email) {
        this.maNV = maNV;
        this.cccd = cccd;
        this.diaChi = diaChi;
        this.sdt = sdt;
        this.email = email;
    }

    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "ChiTietNhanVienDTO{" +
                "maNV='" + maNV + '\'' +
                ", cccd='" + cccd + '\'' +
                ", diaChi='" + diaChi + '\'' +
                ", sdt='" + sdt + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
