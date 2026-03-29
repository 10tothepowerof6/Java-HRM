package com.hrm.dao;

import com.hrm.dto.BangLuongThangDTO;
import java.sql.*;
import java.util.ArrayList;

/**
 * Lớp truy cập dữ liệu (DAO) cho bảng BangLuongThang trên SQL Server.
 * <p>
 * Nhiệm vụ: đóng gói các câu lệnh SQL (truy vấn, thêm, sửa, xóa) qua JDBC.
 * Không đặt quy tắc nghiệp vụ phức tạp tại đây — kiểm tra và luồng xử lý thuộc tầng BUS.
 * </p>
 */
public class BangLuongThangDAO {

    public ArrayList<BangLuongThangDTO> getAll() {
        ArrayList<BangLuongThangDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM BangLuongThang";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                BangLuongThangDTO dto = new BangLuongThangDTO();
                dto.setMaBangLuong(rs.getString("MaBangLuong"));
                dto.setMaNV(rs.getString("MaNV"));
                dto.setThang(rs.getInt("Thang"));
                dto.setNam(rs.getInt("Nam"));
                dto.setSoNgayCong(rs.getInt("SoNgayCong"));
                dto.setLuongCoBan(rs.getBigDecimal("LuongCoBan"));
                dto.setHeSoLuong(rs.getBigDecimal("HeSoLuong"));
                dto.setTongPhuCap(rs.getBigDecimal("TongPhuCap"));
                dto.setTongThuong(rs.getBigDecimal("TongThuong"));
                dto.setKhauTruBHXH(rs.getBigDecimal("KhauTruBHXH"));
                dto.setKhauTruBHYT(rs.getBigDecimal("KhauTruBHYT"));
                dto.setKhauTruBHTN(rs.getBigDecimal("KhauTruBHTN"));
                dto.setThueTNCN(rs.getBigDecimal("ThueTNCN"));
                dto.setThucLanh(rs.getBigDecimal("ThucLanh"));
                list.add(dto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public BangLuongThangDTO getById(String maBangLuong) {
        String sql = "SELECT * FROM BangLuongThang WHERE MaBangLuong = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maBangLuong);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BangLuongThangDTO dto = new BangLuongThangDTO();
                    dto.setMaBangLuong(rs.getString("MaBangLuong"));
                    dto.setMaNV(rs.getString("MaNV"));
                    dto.setThang(rs.getInt("Thang"));
                    dto.setNam(rs.getInt("Nam"));
                    dto.setSoNgayCong(rs.getInt("SoNgayCong"));
                    dto.setLuongCoBan(rs.getBigDecimal("LuongCoBan"));
                    dto.setHeSoLuong(rs.getBigDecimal("HeSoLuong"));
                    dto.setTongPhuCap(rs.getBigDecimal("TongPhuCap"));
                    dto.setTongThuong(rs.getBigDecimal("TongThuong"));
                    dto.setKhauTruBHXH(rs.getBigDecimal("KhauTruBHXH"));
                    dto.setKhauTruBHYT(rs.getBigDecimal("KhauTruBHYT"));
                    dto.setKhauTruBHTN(rs.getBigDecimal("KhauTruBHTN"));
                    dto.setThueTNCN(rs.getBigDecimal("ThueTNCN"));
                    dto.setThucLanh(rs.getBigDecimal("ThucLanh"));
                    return dto;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insert(BangLuongThangDTO dto) {
        String sql = "INSERT INTO BangLuongThang (MaBangLuong, MaNV, Thang, Nam, SoNgayCong, LuongCoBan, HeSoLuong, TongPhuCap, TongThuong, KhauTruBHXH, KhauTruBHYT, KhauTruBHTN, ThueTNCN, ThucLanh) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dto.getMaBangLuong());
            ps.setString(2, dto.getMaNV());
            ps.setInt(3, dto.getThang());
            ps.setInt(4, dto.getNam());
            ps.setInt(5, dto.getSoNgayCong());
            ps.setBigDecimal(6, dto.getLuongCoBan());
            ps.setBigDecimal(7, dto.getHeSoLuong());
            ps.setBigDecimal(8, dto.getTongPhuCap());
            ps.setBigDecimal(9, dto.getTongThuong());
            ps.setBigDecimal(10, dto.getKhauTruBHXH());
            ps.setBigDecimal(11, dto.getKhauTruBHYT());
            ps.setBigDecimal(12, dto.getKhauTruBHTN());
            ps.setBigDecimal(13, dto.getThueTNCN());
            ps.setBigDecimal(14, dto.getThucLanh());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(BangLuongThangDTO dto) {
        String sql = "UPDATE BangLuongThang SET MaNV = ?, Thang = ?, Nam = ?, SoNgayCong = ?, LuongCoBan = ?, HeSoLuong = ?, TongPhuCap = ?, TongThuong = ?, KhauTruBHXH = ?, KhauTruBHYT = ?, KhauTruBHTN = ?, ThueTNCN = ?, ThucLanh = ? WHERE MaBangLuong = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dto.getMaNV());
            ps.setInt(2, dto.getThang());
            ps.setInt(3, dto.getNam());
            ps.setInt(4, dto.getSoNgayCong());
            ps.setBigDecimal(5, dto.getLuongCoBan());
            ps.setBigDecimal(6, dto.getHeSoLuong());
            ps.setBigDecimal(7, dto.getTongPhuCap());
            ps.setBigDecimal(8, dto.getTongThuong());
            ps.setBigDecimal(9, dto.getKhauTruBHXH());
            ps.setBigDecimal(10, dto.getKhauTruBHYT());
            ps.setBigDecimal(11, dto.getKhauTruBHTN());
            ps.setBigDecimal(12, dto.getThueTNCN());
            ps.setBigDecimal(13, dto.getThucLanh());
            ps.setString(14, dto.getMaBangLuong());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(String maBangLuong) {
        String sql = "DELETE FROM BangLuongThang WHERE MaBangLuong = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maBangLuong);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteAll() {
        String sql = "DELETE FROM BangLuongThang";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return ps.executeUpdate() >= 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
