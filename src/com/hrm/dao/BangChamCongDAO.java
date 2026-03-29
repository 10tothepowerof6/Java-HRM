package com.hrm.dao;

import com.hrm.dto.BangChamCongDTO;
import java.sql.*;
import java.util.ArrayList;

/**
 * Lớp truy cập dữ liệu (DAO) cho bảng BangChamCong trên SQL Server.
 * <p>
 * Nhiệm vụ: đóng gói các câu lệnh SQL (truy vấn, thêm, sửa, xóa) qua JDBC.
 * Không đặt quy tắc nghiệp vụ phức tạp tại đây — kiểm tra và luồng xử lý thuộc tầng BUS.
 * </p>
 */
public class BangChamCongDAO {

    public ArrayList<BangChamCongDTO> getAll() {
        ArrayList<BangChamCongDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM BangChamCong";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                BangChamCongDTO dto = new BangChamCongDTO();
                dto.setMaChamCong(rs.getString("MaChamCong"));
                dto.setMaNV(rs.getString("MaNV"));
                dto.setNgayLamViec(rs.getDate("NgayLamViec"));
                dto.setGioVao(rs.getTime("GioVao"));
                dto.setGioRa(rs.getTime("GioRa"));
                dto.setTrangThai(rs.getNString("TrangThai"));
                list.add(dto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public BangChamCongDTO getById(String maChamCong) {
        String sql = "SELECT * FROM BangChamCong WHERE MaChamCong = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maChamCong);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BangChamCongDTO dto = new BangChamCongDTO();
                    dto.setMaChamCong(rs.getString("MaChamCong"));
                    dto.setMaNV(rs.getString("MaNV"));
                    dto.setNgayLamViec(rs.getDate("NgayLamViec"));
                    dto.setGioVao(rs.getTime("GioVao"));
                    dto.setGioRa(rs.getTime("GioRa"));
                    dto.setTrangThai(rs.getNString("TrangThai"));
                    return dto;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insert(BangChamCongDTO dto) {
        String sql = "INSERT INTO BangChamCong (MaChamCong, MaNV, NgayLamViec, GioVao, GioRa, TrangThai) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dto.getMaChamCong());
            ps.setString(2, dto.getMaNV());
            ps.setDate(3, new java.sql.Date(dto.getNgayLamViec().getTime()));
            ps.setTime(4, dto.getGioVao());
            ps.setTime(5, dto.getGioRa());
            ps.setNString(6, dto.getTrangThai());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(BangChamCongDTO dto) {
        String sql = "UPDATE BangChamCong SET MaNV = ?, NgayLamViec = ?, GioVao = ?, GioRa = ?, TrangThai = ? WHERE MaChamCong = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dto.getMaNV());
            ps.setDate(2, new java.sql.Date(dto.getNgayLamViec().getTime()));
            ps.setTime(3, dto.getGioVao());
            ps.setTime(4, dto.getGioRa());
            ps.setNString(5, dto.getTrangThai());
            ps.setString(6, dto.getMaChamCong());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(String maChamCong) {
        String sql = "DELETE FROM BangChamCong WHERE MaChamCong = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maChamCong);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
