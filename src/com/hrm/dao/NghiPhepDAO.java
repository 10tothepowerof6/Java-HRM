package com.hrm.dao;

import com.hrm.dto.NghiPhepDTO;
import java.sql.*;
import java.util.ArrayList;

/**
 * Lớp truy cập dữ liệu (DAO) cho bảng NghiPhep trên SQL Server.
 * <p>
 * Nhiệm vụ: đóng gói các câu lệnh SQL (truy vấn, thêm, sửa, xóa) qua JDBC.
 * Không đặt quy tắc nghiệp vụ phức tạp tại đây — kiểm tra và luồng xử lý thuộc tầng BUS.
 * </p>
 */
public class NghiPhepDAO {

    public ArrayList<NghiPhepDTO> getAll() {
        ArrayList<NghiPhepDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM NghiPhep";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                NghiPhepDTO dto = new NghiPhepDTO();
                dto.setMaNP(rs.getString("MaNP"));
                dto.setMaNV(rs.getString("MaNV"));
                dto.setMaLoaiNP(rs.getString("MaLoaiNP"));
                dto.setTuNgay(rs.getDate("TuNgay"));
                dto.setDenNgay(rs.getDate("DenNgay"));
                dto.setSoNgay(rs.getInt("SoNgay"));
                dto.setLyDo(rs.getNString("LyDo"));
                dto.setTrangThai(rs.getNString("TrangThai"));
                list.add(dto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public NghiPhepDTO getById(String maNP) {
        String sql = "SELECT * FROM NghiPhep WHERE MaNP = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNP);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    NghiPhepDTO dto = new NghiPhepDTO();
                    dto.setMaNP(rs.getString("MaNP"));
                    dto.setMaNV(rs.getString("MaNV"));
                    dto.setMaLoaiNP(rs.getString("MaLoaiNP"));
                    dto.setTuNgay(rs.getDate("TuNgay"));
                    dto.setDenNgay(rs.getDate("DenNgay"));
                    dto.setSoNgay(rs.getInt("SoNgay"));
                    dto.setLyDo(rs.getNString("LyDo"));
                    dto.setTrangThai(rs.getNString("TrangThai"));
                    return dto;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insert(NghiPhepDTO dto) {
        String sql = "INSERT INTO NghiPhep (MaNP, MaNV, MaLoaiNP, TuNgay, DenNgay, SoNgay, LyDo, TrangThai) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dto.getMaNP());
            ps.setString(2, dto.getMaNV());
            ps.setString(3, dto.getMaLoaiNP());
            ps.setDate(4, new java.sql.Date(dto.getTuNgay().getTime()));
            ps.setDate(5, new java.sql.Date(dto.getDenNgay().getTime()));
            ps.setInt(6, dto.getSoNgay());
            ps.setNString(7, dto.getLyDo());
            ps.setNString(8, dto.getTrangThai());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(NghiPhepDTO dto) {
        String sql = "UPDATE NghiPhep SET MaNV = ?, MaLoaiNP = ?, TuNgay = ?, DenNgay = ?, SoNgay = ?, LyDo = ?, TrangThai = ? WHERE MaNP = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dto.getMaNV());
            ps.setString(2, dto.getMaLoaiNP());
            ps.setDate(3, new java.sql.Date(dto.getTuNgay().getTime()));
            ps.setDate(4, new java.sql.Date(dto.getDenNgay().getTime()));
            ps.setInt(5, dto.getSoNgay());
            ps.setNString(6, dto.getLyDo());
            ps.setNString(7, dto.getTrangThai());
            ps.setString(8, dto.getMaNP());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(String maNP) {
        String sql = "DELETE FROM NghiPhep WHERE MaNP = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNP);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
