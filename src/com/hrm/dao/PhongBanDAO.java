package com.hrm.dao;

import com.hrm.dto.PhongBanDTO;
import java.sql.*;
import java.util.ArrayList;

/**
 * Lớp truy cập dữ liệu (DAO) cho bảng PhongBan trên SQL Server.
 * <p>
 * Nhiệm vụ: đóng gói các câu lệnh SQL (truy vấn, thêm, sửa, xóa) qua JDBC.
 * Không đặt quy tắc nghiệp vụ phức tạp tại đây — kiểm tra và luồng xử lý thuộc tầng BUS.
 * </p>
 */
public class PhongBanDAO {

    public ArrayList<PhongBanDTO> getAll() {
        ArrayList<PhongBanDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM PhongBan";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                PhongBanDTO pb = new PhongBanDTO();
                pb.setMaPB(rs.getString("MaPB"));
                pb.setTenPB(rs.getNString("TenPB"));
                pb.setMaTruongPB(rs.getString("MaTruongPB"));
                pb.setNgayThanhLap(rs.getDate("NgayThanhLap"));
                pb.setEmail(rs.getString("Email"));
                list.add(pb);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public PhongBanDTO getById(String maPB) {
        String sql = "SELECT * FROM PhongBan WHERE MaPB = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPB);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PhongBanDTO pb = new PhongBanDTO();
                    pb.setMaPB(rs.getString("MaPB"));
                    pb.setTenPB(rs.getNString("TenPB"));
                    pb.setMaTruongPB(rs.getString("MaTruongPB"));
                    pb.setNgayThanhLap(rs.getDate("NgayThanhLap"));
                    pb.setEmail(rs.getString("Email"));
                    return pb;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insert(PhongBanDTO pb) {
        String sql = "INSERT INTO PhongBan (MaPB, TenPB, MaTruongPB, NgayThanhLap, Email) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pb.getMaPB());
            ps.setNString(2, pb.getTenPB());
            ps.setString(3, pb.getMaTruongPB());
            if (pb.getNgayThanhLap() != null) {
                ps.setDate(4, new java.sql.Date(pb.getNgayThanhLap().getTime()));
            } else {
                ps.setNull(4, Types.DATE);
            }
            ps.setString(5, pb.getEmail());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(PhongBanDTO pb) {
        String sql = "UPDATE PhongBan SET TenPB = ?, MaTruongPB = ?, NgayThanhLap = ?, Email = ? WHERE MaPB = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, pb.getTenPB());
            ps.setString(2, pb.getMaTruongPB());
            if (pb.getNgayThanhLap() != null) {
                ps.setDate(3, new java.sql.Date(pb.getNgayThanhLap().getTime()));
            } else {
                ps.setNull(3, Types.DATE);
            }
            ps.setString(4, pb.getEmail());
            ps.setString(5, pb.getMaPB());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(String maPB) {
        String sql = "DELETE FROM PhongBan WHERE MaPB = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPB);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
