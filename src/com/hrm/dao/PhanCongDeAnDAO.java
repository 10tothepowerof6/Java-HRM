package com.hrm.dao;

import com.hrm.dto.PhanCongDeAnDTO;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

/**
 * Lớp truy cập dữ liệu (DAO) cho bảng PhanCongDeAn trên SQL Server.
 * <p>
 * Nhiệm vụ: đóng gói các câu lệnh SQL (truy vấn, thêm, sửa, xóa) qua JDBC.
 * Không đặt quy tắc nghiệp vụ phức tạp tại đây — kiểm tra và luồng xử lý thuộc tầng BUS.
 * </p>
 */
public class PhanCongDeAnDAO {

    public ArrayList<PhanCongDeAnDTO> getAll() {
        ArrayList<PhanCongDeAnDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM PhanCongDeAn";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                PhanCongDeAnDTO dto = new PhanCongDeAnDTO();
                dto.setMaNV(rs.getString("MaNV"));
                dto.setMaDA(rs.getString("MaDA"));
                dto.setNgayBatDau(rs.getDate("NgayBatDau"));
                dto.setNgayKetThuc(rs.getDate("NgayKetThuc"));
                dto.setPhuCapDeAn(rs.getBigDecimal("PhuCapDeAn"));
                list.add(dto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public PhanCongDeAnDTO getById(String maNV, String maDA) {
        String sql = "SELECT * FROM PhanCongDeAn WHERE MaNV = ? AND MaDA = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maNV);
            ps.setString(2, maDA);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PhanCongDeAnDTO dto = new PhanCongDeAnDTO();
                    dto.setMaNV(rs.getString("MaNV"));
                    dto.setMaDA(rs.getString("MaDA"));
                    dto.setNgayBatDau(rs.getDate("NgayBatDau"));
                    dto.setNgayKetThuc(rs.getDate("NgayKetThuc"));
                    dto.setPhuCapDeAn(rs.getBigDecimal("PhuCapDeAn"));
                    return dto;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insert(PhanCongDeAnDTO dto) {
        String sql = "INSERT INTO PhanCongDeAn (MaNV, MaDA, NgayBatDau, NgayKetThuc, PhuCapDeAn) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, dto.getMaNV());
            ps.setString(2, dto.getMaDA());
            ps.setDate(3, new java.sql.Date(dto.getNgayBatDau().getTime()));

            if (dto.getNgayKetThuc() == null) {
                ps.setNull(4, Types.DATE);
            } else {
                ps.setDate(4, new java.sql.Date(dto.getNgayKetThuc().getTime()));
            }

            ps.setBigDecimal(5, dto.getPhuCapDeAn());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean update(PhanCongDeAnDTO dto) {
        String sql = "UPDATE PhanCongDeAn SET NgayBatDau = ?, NgayKetThuc = ?, PhuCapDeAn = ? "
                + "WHERE MaNV = ? AND MaDA = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, new java.sql.Date(dto.getNgayBatDau().getTime()));

            if (dto.getNgayKetThuc() == null) {
                ps.setNull(2, Types.DATE);
            } else {
                ps.setDate(2, new java.sql.Date(dto.getNgayKetThuc().getTime()));
            }

            ps.setBigDecimal(3, dto.getPhuCapDeAn());

            ps.setString(4, dto.getMaNV());
            ps.setString(5, dto.getMaDA());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean delete(String maNV, String maDA) {
        String sql = "DELETE FROM PhanCongDeAn WHERE MaNV = ? AND MaDA = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maNV);
            ps.setString(2, maDA);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
