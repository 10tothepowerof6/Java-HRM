package com.hrm.dto;

import java.math.BigDecimal;
import java.util.Date;

/**
 * DTO đại diện cho bảng ThamSo trong CSDL.
 * Bảng tham số hệ thống chứa các hằng số cấu hình động
 * (tỷ lệ BHXH, BHYT, mức lương cơ sở...).
 *
 * <p>
 * <b>Lưu ý:</b> Các giá trị trong bảng này KHÔNG ĐƯỢC hardcode
 * trong code Java. Mọi tính toán liên quan (tính lương, khấu trừ)
 * phải lấy giá trị từ bảng này thông qua ThamSoBUS.
 * 
 *
 * 
 */
public class ThamSoDTO {

    private String maThamSo;
    private String tenThamSo;
    private BigDecimal giaTri;
    private String moTa;
    private Date ngayCapNhat;

    /** Constructor mặc định. */
    public ThamSoDTO() {
    }

    /**
     * Constructor đầy đủ tham số.
     *
     * @param maThamSo    mã tham số (PK)
     * @param tenThamSo   tên tham số (UNIQUE), VD: "TyLeBHXH", "MucLuongCoSo"
     * @param giaTri      giá trị (DECIMAL)
     * @param moTa        mô tả
     * @param ngayCapNhat ngày cập nhật gần nhất
     */
    public ThamSoDTO(String maThamSo, String tenThamSo, BigDecimal giaTri,
            String moTa, Date ngayCapNhat) {
        this.maThamSo = maThamSo;
        this.tenThamSo = tenThamSo;
        this.giaTri = giaTri;
        this.moTa = moTa;
        this.ngayCapNhat = ngayCapNhat;
    }

    public String getMaThamSo() {
        return maThamSo;
    }

    public void setMaThamSo(String maThamSo) {
        this.maThamSo = maThamSo;
    }

    public String getTenThamSo() {
        return tenThamSo;
    }

    public void setTenThamSo(String tenThamSo) {
        this.tenThamSo = tenThamSo;
    }

    public BigDecimal getGiaTri() {
        return giaTri;
    }

    public void setGiaTri(BigDecimal giaTri) {
        this.giaTri = giaTri;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public Date getNgayCapNhat() {
        return ngayCapNhat;
    }

    public void setNgayCapNhat(Date ngayCapNhat) {
        this.ngayCapNhat = ngayCapNhat;
    }

    @Override
    public String toString() {
        return "ThamSoDTO{" +
                "maThamSo='" + maThamSo + '\'' +
                ", tenThamSo='" + tenThamSo + '\'' +
                ", giaTri=" + giaTri +
                ", moTa='" + moTa + '\'' +
                ", ngayCapNhat=" + ngayCapNhat +
                '}';
    }
}
