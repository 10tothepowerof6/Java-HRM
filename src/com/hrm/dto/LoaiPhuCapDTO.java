package com.hrm.dto;

/**
 * Đối tượng chuyển tải dữ liệu (DTO) ánh xạ bảng LoaiPhuCap trong cơ sở dữ liệu.
 * <p>
 * Dùng truyền dữ liệu giữa DAO, BUS và Swing; không chứa logic truy cập CSDL.
 * </p>
 */
public class LoaiPhuCapDTO {

    private String maLoaiPC; // PK
    private String tenLoaiPC;
    private String moTa;

    public LoaiPhuCapDTO() {
    }

    public LoaiPhuCapDTO(String maLoaiPC, String tenLoaiPC, String moTa) {
        this.maLoaiPC = maLoaiPC;
        this.tenLoaiPC = tenLoaiPC;
        this.moTa = moTa;
    }

    public String getMaLoaiPC() {
        return maLoaiPC;
    }

    public void setMaLoaiPC(String maLoaiPC) {
        this.maLoaiPC = maLoaiPC;
    }

    public String getTenLoaiPC() {
        return tenLoaiPC;
    }

    public void setTenLoaiPC(String tenLoaiPC) {
        this.tenLoaiPC = tenLoaiPC;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    @Override
    public String toString() {
        return "LoaiPhuCapDTO{"
                + "maLoaiPC='" + maLoaiPC + '\''
                + ", tenLoaiPC='" + tenLoaiPC + '\''
                + ", moTa='" + moTa + '\''
                + '}';
    }
}
