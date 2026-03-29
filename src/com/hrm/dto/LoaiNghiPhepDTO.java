package com.hrm.dto;

/**
 * Đối tượng chuyển tải dữ liệu (DTO) ánh xạ bảng LoaiNghiPhep trong cơ sở dữ liệu.
 * <p>
 * Dùng truyền dữ liệu giữa DAO, BUS và Swing; không chứa logic truy cập CSDL.
 * </p>
 */
public class LoaiNghiPhepDTO {

    private String maLoaiNP; // PK
    private String tenLoai;
    private int soNgayToiDa;
    private String moTa;

    public LoaiNghiPhepDTO() {
    }

    public LoaiNghiPhepDTO(String maLoaiNP, String tenLoai, int soNgayToiDa, String moTa) {
        this.maLoaiNP = maLoaiNP;
        this.tenLoai = tenLoai;
        this.soNgayToiDa = soNgayToiDa;
        this.moTa = moTa;
    }

    public String getMaLoaiNP() {
        return maLoaiNP;
    }

    public void setMaLoaiNP(String maLoaiNP) {
        this.maLoaiNP = maLoaiNP;
    }

    public String getTenLoai() {
        return tenLoai;
    }

    public void setTenLoai(String tenLoai) {
        this.tenLoai = tenLoai;
    }

    public int getSoNgayToiDa() {
        return soNgayToiDa;
    }

    public void setSoNgayToiDa(int soNgayToiDa) {
        this.soNgayToiDa = soNgayToiDa;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    @Override
    public String toString() {
        return "LoaiNghiPhepDTO{"
                + "maLoaiNP='" + maLoaiNP + '\''
                + ", tenLoai='" + tenLoai + '\''
                + ", soNgayToiDa=" + soNgayToiDa
                + ", moTa='" + moTa + '\''
                + '}';
    }
}

