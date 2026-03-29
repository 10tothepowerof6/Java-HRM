package com.hrm.dao;

import com.hrm.dto.DanhMucThuongDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Lớp truy cập dữ liệu (DAO) cho bảng DanhMucThuong trên SQL Server.
 * <p>
 * Nhiệm vụ: đóng gói các câu lệnh SQL (truy vấn, thêm, sửa, xóa) qua JDBC.
 * Không đặt quy tắc nghiệp vụ phức tạp tại đây — kiểm tra và luồng xử lý thuộc tầng BUS.
 * </p>
 */
public class DanhMucThuongDAO {

    public ArrayList<DanhMucThuongDTO> getAll() {
        ArrayList<DanhMucThuongDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM DanhMucThuong";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                DanhMucThuongDTO dto = new DanhMucThuongDTO();
                dto.setMaThuong(rs.getString("MaThuong"));
                dto.setTenLoaiThuong(rs.getNString("TenLoaiThuong"));
                dto.setMoTa(rs.getNString("MoTa"));
                list.add(dto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public DanhMucThuongDTO getById(String maThuong) {
        String sql = "SELECT * FROM DanhMucThuong WHERE MaThuong = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maThuong);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    DanhMucThuongDTO dto = new DanhMucThuongDTO();
                    dto.setMaThuong(rs.getString("MaThuong"));
                    dto.setTenLoaiThuong(rs.getNString("TenLoaiThuong"));
                    dto.setMoTa(rs.getNString("MoTa"));
                    return dto;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insert(DanhMucThuongDTO dto) {
        String sql = "INSERT INTO DanhMucThuong (MaThuong, TenLoaiThuong, MoTa) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, dto.getMaThuong());
            ps.setNString(2, dto.getTenLoaiThuong());
            ps.setNString(3, dto.getMoTa());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(DanhMucThuongDTO dto) {
        String sql = "UPDATE DanhMucThuong SET TenLoaiThuong = ?, MoTa = ? WHERE MaThuong = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setNString(1, dto.getTenLoaiThuong());
            ps.setNString(2, dto.getMoTa());
            ps.setString(3, dto.getMaThuong());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(String maThuong) {
        String sql = "DELETE FROM DanhMucThuong WHERE MaThuong = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maThuong);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
