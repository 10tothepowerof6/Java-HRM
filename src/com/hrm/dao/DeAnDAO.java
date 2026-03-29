package com.hrm.dao;

import com.hrm.dto.DeAnDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

/**
 * Lớp truy cập dữ liệu (DAO) cho bảng DeAn trên SQL Server.
 * <p>
 * Nhiệm vụ: đóng gói các câu lệnh SQL (truy vấn, thêm, sửa, xóa) qua JDBC.
 * Không đặt quy tắc nghiệp vụ phức tạp tại đây — kiểm tra và luồng xử lý thuộc tầng BUS.
 * </p>
 */
public class DeAnDAO {

    public ArrayList<DeAnDTO> getAll() {
        ArrayList<DeAnDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM DeAn";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                DeAnDTO dto = new DeAnDTO();
                dto.setMaDA(rs.getString("MaDA"));
                dto.setTenDA(rs.getNString("TenDA"));
                dto.setNgayBatDau(rs.getDate("NgayBatDau"));
                dto.setNgayKetThuc(rs.getDate("NgayKetThuc"));
                dto.setMaPB(rs.getString("MaPB"));
                dto.setVonDeAn(rs.getBigDecimal("VonDeAn"));
                dto.setTrangThai(rs.getNString("TrangThai"));
                list.add(dto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public DeAnDTO getById(String maDA) {
        String sql = "SELECT * FROM DeAn WHERE MaDA = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maDA);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    DeAnDTO dto = new DeAnDTO();
                    dto.setMaDA(rs.getString("MaDA"));
                    dto.setTenDA(rs.getNString("TenDA"));
                    dto.setNgayBatDau(rs.getDate("NgayBatDau"));
                    dto.setNgayKetThuc(rs.getDate("NgayKetThuc"));
                    dto.setMaPB(rs.getString("MaPB"));
                    dto.setVonDeAn(rs.getBigDecimal("VonDeAn"));
                    dto.setTrangThai(rs.getNString("TrangThai"));
                    return dto;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean insert(DeAnDTO da) {
        String sql = "INSERT INTO DeAn (MaDA, TenDA, NgayBatDau, NgayKetThuc, MaPB, VonDeAn, TrangThai) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, da.getMaDA());
            ps.setNString(2, da.getTenDA());
            ps.setDate(3, new java.sql.Date(da.getNgayBatDau().getTime()));

            if (da.getNgayKetThuc() == null) {
                ps.setNull(4, Types.DATE);
            } else {
                ps.setDate(4, new java.sql.Date(da.getNgayKetThuc().getTime()));
            }

            ps.setString(5, da.getMaPB());
            ps.setBigDecimal(6, da.getVonDeAn());
            ps.setNString(7, da.getTrangThai());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean update(DeAnDTO da) {
        String sql = "UPDATE DeAn SET TenDA = ?, NgayBatDau = ?, NgayKetThuc = ?, MaPB = ?, VonDeAn = ?, "
                + "TrangThai = ? WHERE MaDA = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setNString(1, da.getTenDA());
            ps.setDate(2, new java.sql.Date(da.getNgayBatDau().getTime()));

            if (da.getNgayKetThuc() == null) {
                ps.setNull(3, Types.DATE);
            } else {
                ps.setDate(3, new java.sql.Date(da.getNgayKetThuc().getTime()));
            }

            ps.setString(4, da.getMaPB());
            ps.setBigDecimal(5, da.getVonDeAn());
            ps.setNString(6, da.getTrangThai());
            ps.setString(7, da.getMaDA());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean delete(String maDA) {
        String sql = "DELETE FROM DeAn WHERE MaDA = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maDA);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}
