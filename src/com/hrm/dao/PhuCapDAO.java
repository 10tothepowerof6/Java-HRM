package com.hrm.dao;

import com.hrm.dto.PhuCapDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

/**
 * Lớp truy cập dữ liệu (DAO) cho bảng PhuCap trên SQL Server.
 * <p>
 * Nhiệm vụ: đóng gói các câu lệnh SQL (truy vấn, thêm, sửa, xóa) qua JDBC.
 * Không đặt quy tắc nghiệp vụ phức tạp tại đây — kiểm tra và luồng xử lý thuộc tầng BUS.
 * </p>
 */
public class PhuCapDAO {

    public ArrayList<PhuCapDTO> getAll() {
        ArrayList<PhuCapDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM PhuCap";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                PhuCapDTO dto = new PhuCapDTO();
                dto.setMaPC(rs.getString("MaPC"));
                dto.setMaNV(rs.getString("MaNV"));
                dto.setMaLoaiPC(rs.getString("MaLoaiPC"));
                dto.setSoTien(rs.getBigDecimal("SoTien"));
                dto.setNgayApDung(rs.getDate("NgayApDung"));
                dto.setNgayKetThuc(rs.getDate("NgayKetThuc"));
                list.add(dto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public PhuCapDTO getById(String maPC) {
        String sql = "SELECT * FROM PhuCap WHERE MaPC = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maPC);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PhuCapDTO dto = new PhuCapDTO();
                    dto.setMaPC(rs.getString("MaPC"));
                    dto.setMaNV(rs.getString("MaNV"));
                    dto.setMaLoaiPC(rs.getString("MaLoaiPC"));
                    dto.setSoTien(rs.getBigDecimal("SoTien"));
                    dto.setNgayApDung(rs.getDate("NgayApDung"));
                    dto.setNgayKetThuc(rs.getDate("NgayKetThuc"));
                    return dto;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean insert(PhuCapDTO dto) {
        String sql = "INSERT INTO PhuCap (MaPC, MaNV, MaLoaiPC, SoTien, NgayApDung, NgayKetThuc) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, dto.getMaPC());
            ps.setString(2, dto.getMaNV());
            ps.setString(3, dto.getMaLoaiPC());
            ps.setBigDecimal(4, dto.getSoTien());
            ps.setDate(5, new java.sql.Date(dto.getNgayApDung().getTime()));

            if (dto.getNgayKetThuc() == null) {
                ps.setNull(6, Types.DATE);
            } else {
                ps.setDate(6, new java.sql.Date(dto.getNgayKetThuc().getTime()));
            }

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean update(PhuCapDTO dto) {
        String sql = "UPDATE PhuCap SET MaNV = ?, MaLoaiPC = ?, SoTien = ?, NgayApDung = ?, NgayKetThuc = ? "
                + "WHERE MaPC = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, dto.getMaNV());
            ps.setString(2, dto.getMaLoaiPC());
            ps.setBigDecimal(3, dto.getSoTien());
            ps.setDate(4, new java.sql.Date(dto.getNgayApDung().getTime()));

            if (dto.getNgayKetThuc() == null) {
                ps.setNull(5, Types.DATE);
            } else {
                ps.setDate(5, new java.sql.Date(dto.getNgayKetThuc().getTime()));
            }

            ps.setString(6, dto.getMaPC());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean delete(String maPC) {
        String sql = "DELETE FROM PhuCap WHERE MaPC = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPC);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
