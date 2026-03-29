package com.hrm.dao;

import com.hrm.dto.ChucVuDTO;
import java.sql.*;
import java.util.ArrayList;

/**
 * Lớp truy cập dữ liệu (DAO) cho bảng ChucVu trên SQL Server.
 * <p>
 * Nhiệm vụ: đóng gói các câu lệnh SQL (truy vấn, thêm, sửa, xóa) qua JDBC.
 * Không đặt quy tắc nghiệp vụ phức tạp tại đây — kiểm tra và luồng xử lý thuộc tầng BUS.
 * </p>
 */
public class ChucVuDAO {

    public ArrayList<ChucVuDTO> getAll() {
        ArrayList<ChucVuDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM ChucVu";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                ChucVuDTO cv = new ChucVuDTO();
                cv.setMaCV(rs.getString("MaCV"));
                cv.setTenCV(rs.getNString("TenCV"));
                cv.setHeSoLuong(rs.getBigDecimal("HeSoLuong"));
                list.add(cv);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public ChucVuDTO getById(String maCV) {
        String sql = "SELECT * FROM ChucVu WHERE MaCV = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maCV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ChucVuDTO cv = new ChucVuDTO();
                    cv.setMaCV(rs.getString("MaCV"));
                    cv.setTenCV(rs.getNString("TenCV"));
                    cv.setHeSoLuong(rs.getBigDecimal("HeSoLuong"));
                    return cv;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insert(ChucVuDTO cv) {
        String sql = "INSERT INTO ChucVu (MaCV, TenCV, HeSoLuong) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cv.getMaCV());
            ps.setNString(2, cv.getTenCV());
            ps.setBigDecimal(3, cv.getHeSoLuong());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(ChucVuDTO cv) {
        String sql = "UPDATE ChucVu SET TenCV = ?, HeSoLuong = ? WHERE MaCV = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, cv.getTenCV());
            ps.setBigDecimal(2, cv.getHeSoLuong());
            ps.setString(3, cv.getMaCV());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(String maCV) {
        String sql = "DELETE FROM ChucVu WHERE MaCV = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maCV);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
