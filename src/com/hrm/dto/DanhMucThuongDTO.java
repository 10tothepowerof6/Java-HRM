package com.hrm.dto;

/**
 * Đối tượng chuyển tải dữ liệu (DTO) ánh xạ bảng DanhMucThuong trong cơ sở dữ liệu.
 * <p>
 * Dùng truyền dữ liệu giữa DAO, BUS và Swing; không chứa logic truy cập CSDL.
 * </p>
 */
public class DanhMucThuongDTO {

    private String maThuong; // PK
    private String tenLoaiThuong;
    private String moTa;

    public DanhMucThuongDTO() {
    }

    public DanhMucThuongDTO(String maThuong, String tenLoaiThuong, String moTa) {
        this.maThuong = maThuong;
        this.tenLoaiThuong = tenLoaiThuong;
        this.moTa = moTa;
    }

    public String getMaThuong() {
        return maThuong;
    }

    public void setMaThuong(String maThuong) {
        this.maThuong = maThuong;
    }

    public String getTenLoaiThuong() {
        return tenLoaiThuong;
    }

    public void setTenLoaiThuong(String tenLoaiThuong) {
        this.tenLoaiThuong = tenLoaiThuong;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    @Override
    public String toString() {
        return "DanhMucThuongDTO{"
                + "maThuong='" + maThuong + '\''
                + ", tenLoaiThuong='" + tenLoaiThuong + '\''
                + ", moTa='" + moTa + '\''
                + '}';
    }
}
