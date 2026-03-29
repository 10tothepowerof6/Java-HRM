package com.hrm.util;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Tiện ích đọc/ghi file Excel .xls (HSSF) cho toàn bộ ứng dụng HRM.
 * Lớp này KHÔNG chứa nghiệp vụ — chỉ xử lý I/O workbook và chuyển đổi kiểu dữ liệu.
 */
public final class ExcelHelper {

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("dd/MM/yyyy");
    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("HH:mm");

    private ExcelHelper() {}

    // =========================================================================
    //  EXPORT
    // =========================================================================

    /**
     * Tạo workbook mới với 1 sheet chứa header + dữ liệu.
     *
     * @param sheetName  tên sheet
     * @param headers    mảng tên cột (dòng 1)
     * @param rows       danh sách dòng dữ liệu — mỗi dòng là Object[]
     * @return HSSFWorkbook đã ghi dữ liệu (chưa lưu file)
     */
    public static HSSFWorkbook createWorkbook(String sheetName, String[] headers, List<Object[]> rows) {
        HSSFWorkbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet(sheetName);

        CellStyle headerStyle = buildHeaderStyle(wb);
        CellStyle dateStyle = buildDateStyle(wb);
        CellStyle moneyStyle = buildMoneyStyle(wb);
        CellStyle decimalStyle = buildDecimalStyle(wb);

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 1;
        for (Object[] data : rows) {
            Row row = sheet.createRow(rowIdx++);
            for (int col = 0; col < data.length; col++) {
                Cell cell = row.createCell(col);
                setCellValue(cell, data[col], dateStyle, moneyStyle, decimalStyle);
            }
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            int w = sheet.getColumnWidth(i);
            if (w < 3000) sheet.setColumnWidth(i, 3000);
        }

        return wb;
    }

    /**
     * Thêm 1 sheet vào workbook đã có.
     */
    public static void addSheet(HSSFWorkbook wb, String sheetName, String[] headers, List<Object[]> rows) {
        Sheet sheet = wb.createSheet(sheetName);
        CellStyle headerStyle = buildHeaderStyle(wb);
        CellStyle dateStyle = buildDateStyle(wb);
        CellStyle moneyStyle = buildMoneyStyle(wb);
        CellStyle decimalStyle = buildDecimalStyle(wb);

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 1;
        for (Object[] data : rows) {
            Row row = sheet.createRow(rowIdx++);
            for (int col = 0; col < data.length; col++) {
                Cell cell = row.createCell(col);
                setCellValue(cell, data[col], dateStyle, moneyStyle, decimalStyle);
            }
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            int w = sheet.getColumnWidth(i);
            if (w < 3000) sheet.setColumnWidth(i, 3000);
        }
    }

    /**
     * Mở hộp thoại "Save As" và ghi workbook ra file .xls.
     *
     * @return true nếu ghi thành công, false nếu người dùng huỷ hoặc lỗi
     */
    public static boolean saveWithDialog(Component parent, HSSFWorkbook wb, String suggestedName) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Xuất file Excel");
        fc.setFileFilter(new FileNameExtensionFilter("Excel 97-2003 (.xls)", "xls"));
        fc.setSelectedFile(new File(suggestedName + ".xls"));

        if (fc.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return false;
        }

        File file = fc.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".xls")) {
            file = new File(file.getAbsolutePath() + ".xls");
        }

        try (FileOutputStream fos = new FileOutputStream(file)) {
            wb.write(fos);
            JOptionPane.showMessageDialog(parent,
                    "Xuất thành công!\n" + file.getAbsolutePath(),
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(parent,
                    "Lỗi ghi file: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            closeQuietly(wb);
        }
    }

    // =========================================================================
    //  IMPORT
    // =========================================================================

    /**
     * Mở hộp thoại chọn file .xls, đọc sheet đầu tiên và trả về danh sách dòng dữ liệu (String[]).
     * Dòng đầu tiên (header) bị bỏ qua.
     *
     * @param parent    component cha cho dialog
     * @param expected  số cột mong đợi (để validate), hoặc -1 nếu không kiểm tra
     * @return null nếu người dùng huỷ hoặc lỗi; danh sách rỗng nếu file rỗng
     */
    public static List<String[]> openAndRead(Component parent, int expected) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Nhập file Excel");
        fc.setFileFilter(new FileNameExtensionFilter("Excel 97-2003 (.xls)", "xls"));

        if (fc.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        return readFile(fc.getSelectedFile(), expected, parent);
    }

    /**
     * Đọc file .xls sheet đầu tiên → danh sách String[]. Bỏ qua header (row 0).
     */
    public static List<String[]> readFile(File file, int expectedCols, Component parent) {
        List<String[]> result = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook wb = new HSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheetAt(0);
            if (sheet == null) return result;

            int lastRow = sheet.getLastRowNum();
            for (int i = 1; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                int cols = expectedCols > 0 ? expectedCols : row.getLastCellNum();
                String[] values = new String[cols];
                for (int c = 0; c < cols; c++) {
                    values[c] = getCellString(row.getCell(c));
                }
                result.add(values);
            }

        } catch (Exception ex) {
            if (parent != null) {
                JOptionPane.showMessageDialog(parent,
                        "Lỗi đọc file Excel: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
            return null;
        }

        return result;
    }

    /**
     * Tạo workbook chỉ chứa header (template trống) để tải về.
     */
    public static HSSFWorkbook createTemplate(String sheetName, String[] headers) {
        return createWorkbook(sheetName, headers, new ArrayList<>());
    }

    // =========================================================================
    //  PARSE HELPERS (dùng khi import — chuyển String → kiểu Java)
    // =========================================================================

    public static Date parseDate(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try { return DATE_FMT.parse(s.trim()); } catch (Exception e) { return null; }
    }

    public static java.sql.Time parseTime(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            Date d = TIME_FMT.parse(s.trim());
            return new java.sql.Time(d.getTime());
        } catch (Exception e) { return null; }
    }

    public static BigDecimal parseMoney(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            String clean = s.trim().replace(",", "").replace("₫", "").replace(" ", "");
            return new BigDecimal(clean);
        } catch (Exception e) { return null; }
    }

    public static int parseInt(String s, int defaultVal) {
        if (s == null || s.trim().isEmpty()) return defaultVal;
        try { return Integer.parseInt(s.trim().replace(".0", "")); } catch (Exception e) { return defaultVal; }
    }

    public static String formatDate(Date d) {
        return d != null ? DATE_FMT.format(d) : "";
    }

    public static String formatTime(java.sql.Time t) {
        return t != null ? TIME_FMT.format(t) : "";
    }

    // =========================================================================
    //  IMPORT ERROR REPORT
    // =========================================================================

    /**
     * Hiển thị danh sách lỗi import trong JTextArea scrollable.
     */
    public static void showImportErrors(Component parent, List<String> errors, int successCount) {
        StringBuilder sb = new StringBuilder();
        sb.append("Nhập thành công: ").append(successCount).append(" dòng\n");
        sb.append("Lỗi: ").append(errors.size()).append(" dòng\n\n");
        for (String e : errors) {
            sb.append(e).append("\n");
        }

        JTextArea ta = new JTextArea(sb.toString());
        ta.setEditable(false);
        ta.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new java.awt.Dimension(500, 300));

        JOptionPane.showMessageDialog(parent, sp,
                "Kết quả nhập Excel", JOptionPane.INFORMATION_MESSAGE);
    }

    // =========================================================================
    //  INTERNAL STYLES & HELPERS
    // =========================================================================

    private static CellStyle buildHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private static CellStyle buildDateStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        CreationHelper ch = wb.getCreationHelper();
        style.setDataFormat(ch.createDataFormat().getFormat("dd/MM/yyyy"));
        return style;
    }

    private static CellStyle buildMoneyStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        CreationHelper ch = wb.getCreationHelper();
        style.setDataFormat(ch.createDataFormat().getFormat("#,##0"));
        return style;
    }

    /** Định dạng số thập phân (hệ số lương, v.v.) — tránh dùng #,##0 làm tròn 1.5 → 2. */
    private static CellStyle buildDecimalStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        CreationHelper ch = wb.getCreationHelper();
        style.setDataFormat(ch.createDataFormat().getFormat("0.##########"));
        return style;
    }

    private static void setCellValue(Cell cell, Object val, CellStyle dateStyle,
                                     CellStyle moneyStyle, CellStyle decimalStyle) {
        if (val == null) {
            cell.setBlank();
        } else if (val instanceof String) {
            cell.setCellValue((String) val);
        } else if (val instanceof BigDecimal) {
            BigDecimal bd = (BigDecimal) val;
            cell.setCellValue(bd.doubleValue());
            // Chỉ tiền tệ / số nguyên dùng #,##0; hệ số & BigDecimal có phần lẻ giữ thập phân
            if (bd.stripTrailingZeros().scale() <= 0) {
                cell.setCellStyle(moneyStyle);
            } else {
                cell.setCellStyle(decimalStyle);
            }
        } else if (val instanceof Number) {
            double d = ((Number) val).doubleValue();
            cell.setCellValue(d);
        } else if (val instanceof Date) {
            cell.setCellValue((Date) val);
            cell.setCellStyle(dateStyle);
        } else if (val instanceof java.sql.Time) {
            cell.setCellValue(TIME_FMT.format((java.sql.Time) val));
        } else {
            cell.setCellValue(val.toString());
        }
    }

    private static String getCellString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return DATE_FMT.format(cell.getDateCellValue());
                }
                double num = cell.getNumericCellValue();
                if (num == Math.floor(num) && !Double.isInfinite(num)) {
                    return String.valueOf((long) num);
                }
                return String.valueOf(num);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try { return String.valueOf(cell.getNumericCellValue()); }
                catch (Exception e) { return cell.getStringCellValue(); }
            default:
                return "";
        }
    }

    private static void closeQuietly(Workbook wb) {
        try { wb.close(); } catch (Exception ignored) {}
    }
}
