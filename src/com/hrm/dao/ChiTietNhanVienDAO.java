package com.hrm.dao;

import com.hrm.dto.ChiTietNhanVienDTO;
import java.sql.*;
import java.util.ArrayList;

/**
 * Lớp truy cập dữ liệu (DAO) cho bảng ChiTietNhanVien trên SQL Server.
 * <p>
 * Nhiệm vụ: đóng gói các câu lệnh SQL (truy vấn, thêm, sửa, xóa) qua JDBC.
 * Không đặt quy tắc nghiệp vụ phức tạp tại đây — kiểm tra và luồng xử lý thuộc tầng BUS.
 * </p>
 */
public class ChiTietNhanVienDAO {

    public ArrayList<ChiTietNhanVienDTO> getAll() {
        ArrayList<ChiTietNhanVienDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM ChiTietNhanVien";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                ChiTietNhanVienDTO ct = new ChiTietNhanVienDTO();
                ct.setMaNV(rs.getString("MaNV"));
                ct.setCccd(rs.getString("CCCD"));
                ct.setDiaChi(rs.getNString("DiaChi"));
                ct.setSdt(rs.getString("SDT"));
                ct.setEmail(rs.getString("Email"));
                list.add(ct);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public ChiTietNhanVienDTO getById(String maNV) {
        String sql = "SELECT * FROM ChiTietNhanVien WHERE MaNV = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ChiTietNhanVienDTO ct = new ChiTietNhanVienDTO();
                    ct.setMaNV(rs.getString("MaNV"));
                    ct.setCccd(rs.getString("CCCD"));
                    ct.setDiaChi(rs.getNString("DiaChi"));
                    ct.setSdt(rs.getString("SDT"));
                    ct.setEmail(rs.getString("Email"));
                    return ct;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insert(ChiTietNhanVienDTO ct) {
        String sql = "INSERT INTO ChiTietNhanVien (MaNV, CCCD, DiaChi, SDT, Email) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ct.getMaNV());
            ps.setString(2, ct.getCccd());
            ps.setNString(3, ct.getDiaChi());
            ps.setString(4, ct.getSdt());
            ps.setString(5, ct.getEmail());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(ChiTietNhanVienDTO ct) {
        String sql = "UPDATE ChiTietNhanVien SET CCCD = ?, DiaChi = ?, SDT = ?, Email = ? WHERE MaNV = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ct.getCccd());
            ps.setNString(2, ct.getDiaChi());
            ps.setString(3, ct.getSdt());
            ps.setString(4, ct.getEmail());
            ps.setString(5, ct.getMaNV());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(String maNV) {
        String sql = "DELETE FROM ChiTietNhanVien WHERE MaNV = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
