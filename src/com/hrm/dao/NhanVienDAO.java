package com.hrm.dao;

import com.hrm.dto.NhanVienDTO;
import java.sql.*;
import java.util.ArrayList;

/**
 * Lớp truy cập dữ liệu (DAO) cho bảng NhanVien trên SQL Server.
 * <p>
 * Nhiệm vụ: đóng gói các câu lệnh SQL (truy vấn, thêm, sửa, xóa) qua JDBC.
 * Không đặt quy tắc nghiệp vụ phức tạp tại đây — kiểm tra và luồng xử lý thuộc tầng BUS.
 * </p>
 */
public class NhanVienDAO {

    /**
     * Lấy toàn bộ danh sách nhân viên.
     * @return ArrayList<NhanVienDTO>
     */
    public ArrayList<NhanVienDTO> getAll() {
        ArrayList<NhanVienDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM NhanVien";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                NhanVienDTO nv = new NhanVienDTO();
                nv.setMaNV(rs.getString("MaNV"));
                nv.setHo(rs.getNString("Ho"));
                nv.setTen(rs.getNString("Ten"));
                nv.setGioiTinh(rs.getNString("GioiTinh"));
                nv.setNgaySinh(rs.getDate("NgaySinh"));
                nv.setNgayBatDau(rs.getDate("NgayBatDau"));
                nv.setTrangThai(rs.getNString("TrangThai"));
                nv.setMaPB(rs.getString("MaPB"));
                nv.setMaCV(rs.getString("MaCV"));
                list.add(nv);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy nhân viên theo mã.
     * @param maNV Mã nhân viên
     * @return NhanVienDTO hoặc null nếu không tìm thấy
     */
    public NhanVienDTO getById(String maNV) {
        String sql = "SELECT * FROM NhanVien WHERE MaNV = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    NhanVienDTO nv = new NhanVienDTO();
                    nv.setMaNV(rs.getString("MaNV"));
                    nv.setHo(rs.getNString("Ho"));
                    nv.setTen(rs.getNString("Ten"));
                    nv.setGioiTinh(rs.getNString("GioiTinh"));
                    nv.setNgaySinh(rs.getDate("NgaySinh"));
                    nv.setNgayBatDau(rs.getDate("NgayBatDau"));
                    nv.setTrangThai(rs.getNString("TrangThai"));
                    nv.setMaPB(rs.getString("MaPB"));
                    nv.setMaCV(rs.getString("MaCV"));
                    return nv;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Kiểm tra mã nhân viên đã tồn tại chưa.
     */
    public boolean checkMaNVExists(String maNV) {
        String sql = "SELECT 1 FROM NhanVien WHERE MaNV = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Lấy danh sách nhân viên theo phòng ban.
     */
    public ArrayList<NhanVienDTO> getByPhongBan(String maPB) {
        ArrayList<NhanVienDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM NhanVien WHERE MaPB = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPB);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    NhanVienDTO nv = new NhanVienDTO();
                    nv.setMaNV(rs.getString("MaNV"));
                    nv.setHo(rs.getNString("Ho"));
                    nv.setTen(rs.getNString("Ten"));
                    nv.setGioiTinh(rs.getNString("GioiTinh"));
                    nv.setNgaySinh(rs.getDate("NgaySinh"));
                    nv.setNgayBatDau(rs.getDate("NgayBatDau"));
                    nv.setTrangThai(rs.getNString("TrangThai"));
                    nv.setMaPB(rs.getString("MaPB"));
                    nv.setMaCV(rs.getString("MaCV"));
                    list.add(nv);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Thêm mới nhân viên.
     */
    public boolean insert(NhanVienDTO nv) {
        String sql = "INSERT INTO NhanVien (MaNV, Ho, Ten, GioiTinh, NgaySinh, NgayBatDau, TrangThai, MaPB, MaCV) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nv.getMaNV());
            ps.setNString(2, nv.getHo());
            ps.setNString(3, nv.getTen());
            ps.setNString(4, nv.getGioiTinh());
            ps.setDate(5, new java.sql.Date(nv.getNgaySinh().getTime()));
            ps.setDate(6, new java.sql.Date(nv.getNgayBatDau().getTime()));
            ps.setNString(7, nv.getTrangThai());
            ps.setString(8, nv.getMaPB());
            ps.setString(9, nv.getMaCV());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Cập nhật thông tin nhân viên.
     */
    public boolean update(NhanVienDTO nv) {
        String sql = "UPDATE NhanVien SET Ho = ?, Ten = ?, GioiTinh = ?, NgaySinh = ?, NgayBatDau = ?, TrangThai = ?, MaPB = ?, MaCV = ? WHERE MaNV = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, nv.getHo());
            ps.setNString(2, nv.getTen());
            ps.setNString(3, nv.getGioiTinh());
            ps.setDate(4, new java.sql.Date(nv.getNgaySinh().getTime()));
            ps.setDate(5, new java.sql.Date(nv.getNgayBatDau().getTime()));
            ps.setNString(6, nv.getTrangThai());
            ps.setString(7, nv.getMaPB());
            ps.setString(8, nv.getMaCV());
            ps.setString(9, nv.getMaNV());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Xóa nhân viên theo mã.
     */
    public boolean delete(String maNV) {
        String sql = "DELETE FROM NhanVien WHERE MaNV = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
