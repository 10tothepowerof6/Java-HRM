package com.hrm.dto;

import java.util.Date;

/**
 * Đối tượng chuyển tải dữ liệu (DTO) ánh xạ bảng {@code NghiPhep} — đơn xin nghỉ theo khoảng ngày và loại phép.
 * <p>
 * Trạng thái luồng: Chờ duyệt / Đã duyệt / Từ chối. Tầng {@link com.hrm.bus.NghiPhepBUS} khi duyệt có thể kiểm tra
 * tổng ngày phép theo năm so với cấu hình {@link LoaiNghiPhepDTO}.
 * </p>
 */
public class NghiPhepDTO {

    private String maNP;
    private String maNV;
    private String maLoaiNP;
    private Date tuNgay;
    private Date denNgay;
    private int soNgay;
    private String lyDo;
    private String trangThai;

    /** Constructor mặc định. */
    public NghiPhepDTO() {
    }

    /**
     * Constructor đầy đủ tham số.
     *
     * @param maNP      mã nghỉ phép (PK)
     * @param maNV      mã nhân viên (FK)
     * @param maLoaiNP  mã loại nghỉ phép (FK)
     * @param tuNgay    ngày bắt đầu nghỉ
     * @param denNgay   ngày kết thúc nghỉ
     * @param soNgay    số ngày nghỉ (tính tự động = denNgay - tuNgay + 1)
     * @param lyDo      lý do nghỉ phép
     * @param trangThai trạng thái (Chờ duyệt / Đã duyệt / Từ chối)
     */
    public NghiPhepDTO(String maNP, String maNV, String maLoaiNP,
            Date tuNgay, Date denNgay, int soNgay,
            String lyDo, String trangThai) {
        this.maNP = maNP;
        this.maNV = maNV;
        this.maLoaiNP = maLoaiNP;
        this.tuNgay = tuNgay;
        this.denNgay = denNgay;
        this.soNgay = soNgay;
        this.lyDo = lyDo;
        this.trangThai = trangThai;
    }

    public String getMaNP() {
        return maNP;
    }

    public void setMaNP(String maNP) {
        this.maNP = maNP;
    }

    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public String getMaLoaiNP() {
        return maLoaiNP;
    }

    public void setMaLoaiNP(String maLoaiNP) {
        this.maLoaiNP = maLoaiNP;
    }

    public Date getTuNgay() {
        return tuNgay;
    }

    public void setTuNgay(Date tuNgay) {
        this.tuNgay = tuNgay;
    }

    public Date getDenNgay() {
        return denNgay;
    }

    public void setDenNgay(Date denNgay) {
        this.denNgay = denNgay;
    }

    public int getSoNgay() {
        return soNgay;
    }

    public void setSoNgay(int soNgay) {
        this.soNgay = soNgay;
    }

    public String getLyDo() {
        return lyDo;
    }

    public void setLyDo(String lyDo) {
        this.lyDo = lyDo;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    @Override
    public String toString() {
        return "NghiPhepDTO{" +
                "maNP='" + maNP + '\'' +
                ", maNV='" + maNV + '\'' +
                ", maLoaiNP='" + maLoaiNP + '\'' +
                ", tuNgay=" + tuNgay +
                ", denNgay=" + denNgay +
                ", soNgay=" + soNgay +
                ", lyDo='" + lyDo + '\'' +
                ", trangThai='" + trangThai + '\'' +
                '}';
    }
}
