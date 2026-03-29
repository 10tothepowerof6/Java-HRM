package com.hrm.dao;

import com.hrm.dto.LoaiNghiPhepDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Lớp truy cập dữ liệu (DAO) cho bảng LoaiNghiPhep trên SQL Server.
 * <p>
 * Nhiệm vụ: đóng gói các câu lệnh SQL (truy vấn, thêm, sửa, xóa) qua JDBC.
 * Không đặt quy tắc nghiệp vụ phức tạp tại đây — kiểm tra và luồng xử lý thuộc tầng BUS.
 * </p>
 */
public class LoaiNghiPhepDAO {

    public ArrayList<LoaiNghiPhepDTO> getAll() {
        ArrayList<LoaiNghiPhepDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM LoaiNghiPhep";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                LoaiNghiPhepDTO dto = new LoaiNghiPhepDTO();
                dto.setMaLoaiNP(rs.getString("MaLoaiNP"));
                dto.setTenLoai(rs.getNString("TenLoai"));
                dto.setSoNgayToiDa(rs.getInt("SoNgayToiDa"));
                dto.setMoTa(rs.getNString("MoTa"));
                list.add(dto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public LoaiNghiPhepDTO getById(String maLoaiNP) {
        String sql = "SELECT * FROM LoaiNghiPhep WHERE MaLoaiNP = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maLoaiNP);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    LoaiNghiPhepDTO dto = new LoaiNghiPhepDTO();
                    dto.setMaLoaiNP(rs.getString("MaLoaiNP"));
                    dto.setTenLoai(rs.getNString("TenLoai"));
                    dto.setSoNgayToiDa(rs.getInt("SoNgayToiDa"));
                    dto.setMoTa(rs.getNString("MoTa"));
                    return dto;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insert(LoaiNghiPhepDTO dto) {
        String sql = "INSERT INTO LoaiNghiPhep (MaLoaiNP, TenLoai, SoNgayToiDa, MoTa) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dto.getMaLoaiNP());
            ps.setNString(2, dto.getTenLoai());
            ps.setInt(3, dto.getSoNgayToiDa());
            ps.setNString(4, dto.getMoTa());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(LoaiNghiPhepDTO dto) {
        String sql = "UPDATE LoaiNghiPhep SET TenLoai = ?, SoNgayToiDa = ?, MoTa = ? WHERE MaLoaiNP = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, dto.getTenLoai());
            ps.setInt(2, dto.getSoNgayToiDa());
            ps.setNString(3, dto.getMoTa());
            ps.setString(4, dto.getMaLoaiNP());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(String maLoaiNP) {
        String sql = "DELETE FROM LoaiNghiPhep WHERE MaLoaiNP = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maLoaiNP);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}

