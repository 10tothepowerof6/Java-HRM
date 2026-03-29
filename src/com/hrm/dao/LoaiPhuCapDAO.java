package com.hrm.dao;

import com.hrm.dto.LoaiPhuCapDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Lớp truy cập dữ liệu (DAO) cho bảng LoaiPhuCap trên SQL Server.
 * <p>
 * Nhiệm vụ: đóng gói các câu lệnh SQL (truy vấn, thêm, sửa, xóa) qua JDBC.
 * Không đặt quy tắc nghiệp vụ phức tạp tại đây — kiểm tra và luồng xử lý thuộc tầng BUS.
 * </p>
 */
public class LoaiPhuCapDAO {

    public ArrayList<LoaiPhuCapDTO> getAll() {
        ArrayList<LoaiPhuCapDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM LoaiPhuCap";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                LoaiPhuCapDTO dto = new LoaiPhuCapDTO();
                dto.setMaLoaiPC(rs.getString("MaLoaiPC"));
                dto.setTenLoaiPC(rs.getNString("TenLoaiPC"));
                dto.setMoTa(rs.getNString("MoTa"));
                list.add(dto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public LoaiPhuCapDTO getById(String maLoaiPC) {
        String sql = "SELECT * FROM LoaiPhuCap WHERE MaLoaiPC = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maLoaiPC);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    LoaiPhuCapDTO dto = new LoaiPhuCapDTO();
                    dto.setMaLoaiPC(rs.getString("MaLoaiPC"));
                    dto.setTenLoaiPC(rs.getNString("TenLoaiPC"));
                    dto.setMoTa(rs.getNString("MoTa"));
                    return dto;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insert(LoaiPhuCapDTO dto) {
        String sql = "INSERT INTO LoaiPhuCap (MaLoaiPC, TenLoaiPC, MoTa) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, dto.getMaLoaiPC());
            ps.setNString(2, dto.getTenLoaiPC());
            ps.setNString(3, dto.getMoTa());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(LoaiPhuCapDTO dto) {
        String sql = "UPDATE LoaiPhuCap SET TenLoaiPC = ?, MoTa = ? WHERE MaLoaiPC = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setNString(1, dto.getTenLoaiPC());
            ps.setNString(2, dto.getMoTa());
            ps.setString(3, dto.getMaLoaiPC());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(String maLoaiPC) {
        String sql = "DELETE FROM LoaiPhuCap WHERE MaLoaiPC = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maLoaiPC);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
