package com.hrm.dto;

import java.math.BigDecimal;

/**
 * DTO đại diện cho bảng BangLuongThang trong CSDL.
 * Chứa thông tin bảng tính lương hàng tháng của nhân viên.
 *
 * <p>
 * <b>Công thức:</b><br>
 * {@code ThucLanh = LuongCoBan + TongPhuCap + TongThuong
 *                  - KhauTruBHXH - KhauTruBHYT - KhauTruBHTN - ThueTNCN}
 * </p>
 *
 * <p>
 * <b>Snapshot:</b> HeSoLuong và LuongCoBan được snapshot tại thời điểm
 * tính lương để dữ liệu lịch sử không bị thay đổi khi cập nhật hệ số mới.
 * </p>
 *
 * <p>
 * <b>UNIQUE Constraint:</b> (MaNV, Thang, Nam) — Mỗi NV chỉ có 1 bảng lương
 * cho mỗi tháng/năm.
 * </p>
 *
 * 
 */
public class BangLuongThangDTO {

    private String maBangLuong;
    private String maNV;
    private int thang;
    private int nam;
    private int soNgayCong;
    private BigDecimal luongCoBan;
    private BigDecimal heSoLuong;
    private BigDecimal tongPhuCap;
    private BigDecimal tongThuong;
    private BigDecimal khauTruBHXH;
    private BigDecimal khauTruBHYT;
    private BigDecimal khauTruBHTN;
    private BigDecimal thueTNCN;
    private BigDecimal thucLanh;

    /** Constructor mặc định. */
    public BangLuongThangDTO() {
    }

    /**
     * Constructor đầy đủ tham số.
     *
     * @param maBangLuong mã bảng lương (PK)
     * @param maNV        mã nhân viên (FK)
     * @param thang       tháng (1-12)
     * @param nam         năm (VD: 2026)
     * @param soNgayCong  số ngày công thực tế
     * @param luongCoBan  lương cơ bản = HeSoLuong × MucLuongCoSo
     * @param heSoLuong   hệ số lương (snapshot)
     * @param tongPhuCap  tổng phụ cấp
     * @param tongThuong  tổng thưởng
     * @param khauTruBHXH khấu trừ BHXH
     * @param khauTruBHYT khấu trừ BHYT
     * @param khauTruBHTN khấu trừ BH thất nghiệp
     * @param thueTNCN    thuế thu nhập cá nhân
     * @param thucLanh    thực lãnh (net salary)
     */
    public BangLuongThangDTO(String maBangLuong, String maNV, int thang,
            int nam, int soNgayCong, BigDecimal luongCoBan,
            BigDecimal heSoLuong, BigDecimal tongPhuCap,
            BigDecimal tongThuong, BigDecimal khauTruBHXH,
            BigDecimal khauTruBHYT, BigDecimal khauTruBHTN,
            BigDecimal thueTNCN, BigDecimal thucLanh) {
        this.maBangLuong = maBangLuong;
        this.maNV = maNV;
        this.thang = thang;
        this.nam = nam;
        this.soNgayCong = soNgayCong;
        this.luongCoBan = luongCoBan;
        this.heSoLuong = heSoLuong;
        this.tongPhuCap = tongPhuCap;
        this.tongThuong = tongThuong;
        this.khauTruBHXH = khauTruBHXH;
        this.khauTruBHYT = khauTruBHYT;
        this.khauTruBHTN = khauTruBHTN;
        this.thueTNCN = thueTNCN;
        this.thucLanh = thucLanh;
    }

    public String getMaBangLuong() {
        return maBangLuong;
    }

    public void setMaBangLuong(String maBangLuong) {
        this.maBangLuong = maBangLuong;
    }

    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public int getThang() {
        return thang;
    }

    public void setThang(int thang) {
        this.thang = thang;
    }

    public int getNam() {
        return nam;
    }

    public void setNam(int nam) {
        this.nam = nam;
    }

    public int getSoNgayCong() {
        return soNgayCong;
    }

    public void setSoNgayCong(int soNgayCong) {
        this.soNgayCong = soNgayCong;
    }

    public BigDecimal getLuongCoBan() {
        return luongCoBan;
    }

    public void setLuongCoBan(BigDecimal luongCoBan) {
        this.luongCoBan = luongCoBan;
    }

    public BigDecimal getHeSoLuong() {
        return heSoLuong;
    }

    public void setHeSoLuong(BigDecimal heSoLuong) {
        this.heSoLuong = heSoLuong;
    }

    public BigDecimal getTongPhuCap() {
        return tongPhuCap;
    }

    public void setTongPhuCap(BigDecimal tongPhuCap) {
        this.tongPhuCap = tongPhuCap;
    }

    public BigDecimal getTongThuong() {
        return tongThuong;
    }

    public void setTongThuong(BigDecimal tongThuong) {
        this.tongThuong = tongThuong;
    }

    public BigDecimal getKhauTruBHXH() {
        return khauTruBHXH;
    }

    public void setKhauTruBHXH(BigDecimal khauTruBHXH) {
        this.khauTruBHXH = khauTruBHXH;
    }

    public BigDecimal getKhauTruBHYT() {
        return khauTruBHYT;
    }

    public void setKhauTruBHYT(BigDecimal khauTruBHYT) {
        this.khauTruBHYT = khauTruBHYT;
    }

    public BigDecimal getKhauTruBHTN() {
        return khauTruBHTN;
    }

    public void setKhauTruBHTN(BigDecimal khauTruBHTN) {
        this.khauTruBHTN = khauTruBHTN;
    }

    public BigDecimal getThueTNCN() {
        return thueTNCN;
    }

    public void setThueTNCN(BigDecimal thueTNCN) {
        this.thueTNCN = thueTNCN;
    }

    public BigDecimal getThucLanh() {
        return thucLanh;
    }

    public void setThucLanh(BigDecimal thucLanh) {
        this.thucLanh = thucLanh;
    }

    @Override
    public String toString() {
        return "BangLuongThangDTO{" +
                "maBangLuong='" + maBangLuong + '\'' +
                ", maNV='" + maNV + '\'' +
                ", thang=" + thang +
                ", nam=" + nam +
                ", soNgayCong=" + soNgayCong +
                ", luongCoBan=" + luongCoBan +
                ", heSoLuong=" + heSoLuong +
                ", tongPhuCap=" + tongPhuCap +
                ", tongThuong=" + tongThuong +
                ", khauTruBHXH=" + khauTruBHXH +
                ", khauTruBHYT=" + khauTruBHYT +
                ", khauTruBHTN=" + khauTruBHTN +
                ", thueTNCN=" + thueTNCN +
                ", thucLanh=" + thucLanh +
                '}';
    }
}
