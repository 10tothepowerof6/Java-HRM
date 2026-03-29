package com.hrm.dao;

import com.hrm.dto.ChiTietThuongDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Lớp truy cập dữ liệu (DAO) cho bảng ChiTietThuong trên SQL Server.
 * <p>
 * Nhiệm vụ: đóng gói các câu lệnh SQL (truy vấn, thêm, sửa, xóa) qua JDBC.
 * Không đặt quy tắc nghiệp vụ phức tạp tại đây — kiểm tra và luồng xử lý thuộc tầng BUS.
 * </p>
 */
public class ChiTietThuongDAO {

    public ArrayList<ChiTietThuongDTO> getAll() {
        ArrayList<ChiTietThuongDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM ChiTietThuong";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ChiTietThuongDTO dto = new ChiTietThuongDTO();
                dto.setMaCTT(rs.getString("MaCTT"));
                dto.setMaNV(rs.getString("MaNV"));
                dto.setMaThuong(rs.getString("MaThuong"));
                dto.setSoTien(rs.getBigDecimal("SoTien"));
                dto.setNgayThuong(rs.getDate("NgayThuong"));
                dto.setGhiChu(rs.getNString("GhiChu"));
                list.add(dto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public ChiTietThuongDTO getById(String maCTT) {
        String sql = "SELECT * FROM ChiTietThuong WHERE MaCTT = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maCTT);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ChiTietThuongDTO dto = new ChiTietThuongDTO();
                    dto.setMaCTT(rs.getString("MaCTT"));
                    dto.setMaNV(rs.getString("MaNV"));
                    dto.setMaThuong(rs.getString("MaThuong"));
                    dto.setSoTien(rs.getBigDecimal("SoTien"));
                    dto.setNgayThuong(rs.getDate("NgayThuong"));
                    dto.setGhiChu(rs.getNString("GhiChu"));
                    return dto;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean insert(ChiTietThuongDTO dto) {
        String sql = "INSERT INTO ChiTietThuong (MaCTT, MaNV, MaThuong, SoTien, NgayThuong, GhiChu) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, dto.getMaCTT());
            ps.setString(2, dto.getMaNV());
            ps.setString(3, dto.getMaThuong());
            ps.setBigDecimal(4, dto.getSoTien());
            ps.setDate(5, new java.sql.Date(dto.getNgayThuong().getTime()));
            ps.setNString(6, dto.getGhiChu());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean update(ChiTietThuongDTO dto) {
        String sql = "UPDATE ChiTietThuong SET MaNV = ?, MaThuong = ?, SoTien = ?, NgayThuong = ?, GhiChu = ? "
                + "WHERE MaCTT = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, dto.getMaNV());
            ps.setString(2, dto.getMaThuong());
            ps.setBigDecimal(3, dto.getSoTien());
            ps.setDate(4, new java.sql.Date(dto.getNgayThuong().getTime()));
            ps.setNString(5, dto.getGhiChu());
            ps.setString(6, dto.getMaCTT());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean delete(String maCTT) {
        String sql = "DELETE FROM ChiTietThuong WHERE MaCTT = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maCTT);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
