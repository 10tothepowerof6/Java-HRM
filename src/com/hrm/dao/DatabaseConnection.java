package com.hrm.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton class quản lý kết nối cơ sở dữ liệu (JDBC).
 *
 * <p>
 * <b>Design Pattern:</b> Singleton — đảm bảo chỉ có DUY NHẤT 1 instance
 * kết nối tới database trong toàn bộ vòng đời ứng dụng, tránh tạo
 * nhiều connection gây resource leak.
 * </p>
 *
 * <p>
 * <b>Cách sử dụng:</b>
 * </p>
 * 
 * <pre>{@code
 * Connection conn = DatabaseConnection.getInstance().getConnection();
 * PreparedStatement ps = conn.prepareStatement("SELECT * FROM NhanVien");
 * }</pre>
 *
 * <p>
 * <b>Lưu ý:</b> Cần thay đổi DB_URL, USER, PASSWORD cho phù hợp
 * với môi trường triển khai thực tế. Hiện tại đang cấu hình cho SQL Server.
 * </p>
 *
 * 
 */
public class DatabaseConnection {

    // ==================== CẤU HÌNH KẾT NỐI ====================
    // Thay đổi các giá trị dưới đây cho phù hợp với môi trường của bạn.

    /** JDBC URL kết nối tới SQL Server. */
    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=QuanLyNhanSu;encrypt=true;trustServerCertificate=true;";

    /** Tên đăng nhập SQL Server. */
    private static final String USER = "sa";

    /** Mật khẩu SQL Server. */
    private static final String PASSWORD = "123123";

    // ==================== SINGLETON ====================

    /** Instance duy nhất của class. */
    private static DatabaseConnection instance;

    /** Đối tượng Connection JDBC. */
    private Connection connection;

    /**
     * Constructor private — chỉ được gọi từ bên trong class.
     * Load JDBC Driver và tạo kết nối tới database.
     */
    private DatabaseConnection() {
        try {
            // Load SQL Server JDBC Driver
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            // Tạo kết nối
            connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            System.out.println("[DatabaseConnection] Kết nối database thành công.");
        } catch (ClassNotFoundException e) {
            System.err.println("[DatabaseConnection] Không tìm thấy JDBC Driver!");
            System.err.println("Hãy đảm bảo sqljdbc4.jar nằm trong thư mục lib/");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("[DatabaseConnection] Kết nối database thất bại!");
            System.err.println("Kiểm tra lại DB_URL, USER, PASSWORD.");
            e.printStackTrace();
        }
    }

    /**
     * Lấy instance duy nhất của DatabaseConnection (Singleton).
     * Nếu instance chưa tồn tại hoặc connection đã bị đóng, tạo mới.
     *
     * @return instance duy nhất
     */
    public static DatabaseConnection getInstance() {
        try {
            if (instance == null || instance.getConnection().isClosed()) {
                instance = new DatabaseConnection();
            }
        } catch (SQLException e) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Lấy đối tượng Connection đang mở.
     *
     * @return Connection JDBC
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Đóng kết nối database. Nên gọi khi thoát ứng dụng.
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DatabaseConnection] Đã đóng kết nối database.");
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseConnection] Lỗi khi đóng kết nối.");
            e.printStackTrace();
        }
    }
}
