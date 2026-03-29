package com.hrm.dao;

import com.hrm.dto.ThamSoDTO;
import java.sql.*;
import java.util.ArrayList;

/**
 * Lớp truy cập dữ liệu (DAO) cho bảng ThamSo trên SQL Server.
 * <p>
 * Nhiệm vụ: đóng gói các câu lệnh SQL (truy vấn, thêm, sửa, xóa) qua JDBC.
 * Không đặt quy tắc nghiệp vụ phức tạp tại đây — kiểm tra và luồng xử lý thuộc tầng BUS.
 * </p>
 */
public class ThamSoDAO {

    public ArrayList<ThamSoDTO> getAll() {
        ArrayList<ThamSoDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM ThamSo";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                ThamSoDTO dto = new ThamSoDTO();
                dto.setMaThamSo(rs.getString("MaThamSo"));
                dto.setTenThamSo(rs.getNString("TenThamSo"));
                dto.setGiaTri(rs.getBigDecimal("GiaTri"));
                dto.setMoTa(rs.getNString("MoTa"));
                dto.setNgayCapNhat(rs.getDate("NgayCapNhat"));
                list.add(dto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public ThamSoDTO getById(String maThamSo) {
        String sql = "SELECT * FROM ThamSo WHERE MaThamSo = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maThamSo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ThamSoDTO dto = new ThamSoDTO();
                    dto.setMaThamSo(rs.getString("MaThamSo"));
                    dto.setTenThamSo(rs.getNString("TenThamSo"));
                    dto.setGiaTri(rs.getBigDecimal("GiaTri"));
                    dto.setMoTa(rs.getNString("MoTa"));
                    dto.setNgayCapNhat(rs.getDate("NgayCapNhat"));
                    return dto;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Lấy giá trị tham số dựa trên TenThamSo (ví dụ: 'TyLeBHXH') */
    public ThamSoDTO getByTen(String tenThamSo) {
        String sql = "SELECT * FROM ThamSo WHERE TenThamSo = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, tenThamSo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ThamSoDTO dto = new ThamSoDTO();
                    dto.setMaThamSo(rs.getString("MaThamSo"));
                    dto.setTenThamSo(rs.getNString("TenThamSo"));
                    dto.setGiaTri(rs.getBigDecimal("GiaTri"));
                    dto.setMoTa(rs.getNString("MoTa"));
                    dto.setNgayCapNhat(rs.getDate("NgayCapNhat"));
                    return dto;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insert(ThamSoDTO dto) {
        String sql = "INSERT INTO ThamSo (MaThamSo, TenThamSo, GiaTri, MoTa, NgayCapNhat) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dto.getMaThamSo());
            ps.setNString(2, dto.getTenThamSo());
            ps.setBigDecimal(3, dto.getGiaTri());
            ps.setNString(4, dto.getMoTa());
            ps.setDate(5, new java.sql.Date(dto.getNgayCapNhat().getTime()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(ThamSoDTO dto) {
        String sql = "UPDATE ThamSo SET TenThamSo = ?, GiaTri = ?, MoTa = ?, NgayCapNhat = ? WHERE MaThamSo = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, dto.getTenThamSo());
            ps.setBigDecimal(2, dto.getGiaTri());
            ps.setNString(3, dto.getMoTa());
            ps.setDate(4, new java.sql.Date(dto.getNgayCapNhat().getTime()));
            ps.setString(5, dto.getMaThamSo());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(String maThamSo) {
        String sql = "DELETE FROM ThamSo WHERE MaThamSo = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maThamSo);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
