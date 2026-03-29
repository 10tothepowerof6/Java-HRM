package com.hrm.dao;

import com.hrm.dto.HopDongDTO;
import java.sql.*;
import java.util.ArrayList;

/**
 * Lớp truy cập dữ liệu (DAO) cho bảng HopDong trên SQL Server.
 * <p>
 * Nhiệm vụ: đóng gói các câu lệnh SQL (truy vấn, thêm, sửa, xóa) qua JDBC.
 * Không đặt quy tắc nghiệp vụ phức tạp tại đây — kiểm tra và luồng xử lý thuộc tầng BUS.
 * </p>
 */
public class HopDongDAO {

    public ArrayList<HopDongDTO> getAll() {
        ArrayList<HopDongDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM HopDong";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                HopDongDTO dto = new HopDongDTO();
                dto.setMaHD(rs.getString("MaHD"));
                dto.setMaNV(rs.getString("MaNV"));
                dto.setLoaiHD(rs.getNString("LoaiHD"));
                dto.setNgayBatDau(rs.getDate("NgayBatDau"));
                dto.setNgayKetThuc(rs.getDate("NgayKetThuc"));
                dto.setLuongHopDong(rs.getBigDecimal("LuongHopDong"));
                dto.setTrangThai(rs.getNString("TrangThai"));
                dto.setGhiChu(rs.getNString("GhiChu"));
                list.add(dto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public HopDongDTO getById(String maHD) {
        String sql = "SELECT * FROM HopDong WHERE MaHD = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    HopDongDTO dto = new HopDongDTO();
                    dto.setMaHD(rs.getString("MaHD"));
                    dto.setMaNV(rs.getString("MaNV"));
                    dto.setLoaiHD(rs.getNString("LoaiHD"));
                    dto.setNgayBatDau(rs.getDate("NgayBatDau"));
                    dto.setNgayKetThuc(rs.getDate("NgayKetThuc"));
                    dto.setLuongHopDong(rs.getBigDecimal("LuongHopDong"));
                    dto.setTrangThai(rs.getNString("TrangThai"));
                    dto.setGhiChu(rs.getNString("GhiChu"));
                    return dto;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insert(HopDongDTO dto) {
        String sql = "INSERT INTO HopDong (MaHD, MaNV, LoaiHD, NgayBatDau, NgayKetThuc, LuongHopDong, TrangThai, GhiChu) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dto.getMaHD());
            ps.setString(2, dto.getMaNV());
            ps.setNString(3, dto.getLoaiHD());
            ps.setDate(4, new java.sql.Date(dto.getNgayBatDau().getTime()));
            if (dto.getNgayKetThuc() != null) {
                ps.setDate(5, new java.sql.Date(dto.getNgayKetThuc().getTime()));
            } else {
                ps.setNull(5, Types.DATE);
            }
            ps.setBigDecimal(6, dto.getLuongHopDong());
            ps.setNString(7, dto.getTrangThai());
            ps.setNString(8, dto.getGhiChu());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(HopDongDTO dto) {
        String sql = "UPDATE HopDong SET MaNV = ?, LoaiHD = ?, NgayBatDau = ?, NgayKetThuc = ?, LuongHopDong = ?, TrangThai = ?, GhiChu = ? WHERE MaHD = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dto.getMaNV());
            ps.setNString(2, dto.getLoaiHD());
            ps.setDate(3, new java.sql.Date(dto.getNgayBatDau().getTime()));
            if (dto.getNgayKetThuc() != null) {
                ps.setDate(4, new java.sql.Date(dto.getNgayKetThuc().getTime()));
            } else {
                ps.setNull(4, Types.DATE);
            }
            ps.setBigDecimal(5, dto.getLuongHopDong());
            ps.setNString(6, dto.getTrangThai());
            ps.setNString(7, dto.getGhiChu());
            ps.setString(8, dto.getMaHD());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(String maHD) {
        String sql = "DELETE FROM HopDong WHERE MaHD = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maHD);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
