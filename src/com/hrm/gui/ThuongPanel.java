package com.hrm.gui;

import com.hrm.bus.ChiTietThuongBUS;
import com.hrm.bus.DanhMucThuongBUS;
import com.hrm.bus.NhanVienBUS;
import com.hrm.dto.ChiTietThuongDTO;
import com.hrm.dto.DanhMucThuongDTO;
import com.hrm.dto.NhanVienDTO;
import com.hrm.security.PermissionHelper;
import com.hrm.util.ExcelHelper;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * Panel quản lý thưởng theo mô hình master–detail: danh mục thưởng và chi tiết tiền thưởng theo nhân viên.
 * <p>
 * Xuất/nhập Excel khi đủ quyền CRUD module {@code thuong}; double-click mở sửa nếu được phép.
 * </p>
 */
public class ThuongPanel extends JPanel {

    private static final Color PRIMARY = new Color(0, 82, 155);
    private static final Color PRIMARY_DARK = new Color(0, 51, 102);
    private static final Color BG_CONTENT = new Color(30, 41, 59);
    private static final Color BG_TOOLBAR = new Color(22, 33, 52);
    private static final Color TEXT_WHITE = new Color(248, 250, 252);
    private static final Color TEXT_MUTED = new Color(148, 163, 184);
    private static final Color TABLE_ROW_ALT = new Color(22, 33, 52);
    private static final Color TABLE_SEL = new Color(0, 82, 155);
    private static final Color BTN_DANGER = new Color(185, 28, 28);
    private static final Color BTN_SECONDARY = new Color(55, 65, 81);
    private static final Color FIELD_BG = new Color(15, 23, 42);
    private static final Color FIELD_BORDER = new Color(71, 85, 105);

    private final DanhMucThuongBUS danhMucThuongBUS;
    private final ChiTietThuongBUS chiTietThuongBUS;
    private final NhanVienBUS nhanVienBUS;

    private JTable tblDanhMuc;
    private JTable tblChiTiet;
    private DefaultTableModel modelDanhMuc;
    private DefaultTableModel modelChiTiet;
    private JTextField txtSearch;
    private JLabel lblStatus;

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public ThuongPanel() {
        danhMucThuongBUS = new DanhMucThuongBUS();
        chiTietThuongBUS = new ChiTietThuongBUS();
        nhanVienBUS = new NhanVienBUS();

        initUI();
        refreshData();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                refreshData();
            }
        });
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(BG_CONTENT);
        add(createToolbar(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(0, 4));
        toolbar.setBackground(BG_TOOLBAR);
        toolbar.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        // ── Hàng 1: Search ──
        JPanel rowSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        rowSearch.setOpaque(false);

        txtSearch = new JTextField(22);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setForeground(TEXT_WHITE);
        txtSearch.setBackground(FIELD_BG);
        txtSearch.setCaretColor(TEXT_WHITE);
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BORDER),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        txtSearch.setToolTipText("Tìm mã/tên thưởng, mã nhân viên, ghi chú...");
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { loadMasterTable(); }

            @Override
            public void removeUpdate(DocumentEvent e) { loadMasterTable(); }

            @Override
            public void changedUpdate(DocumentEvent e) { loadMasterTable(); }
        });
        rowSearch.add(txtSearch);

        // ── Hàng 2: CRUD trái | Excel phải ──
        JPanel rowActions = new JPanel(new BorderLayout());
        rowActions.setOpaque(false);

        JPanel crudPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        crudPanel.setOpaque(false);

        JButton btnAddDM = createStyledButton("+ Danh mục", PRIMARY);
        btnAddDM.addActionListener(e -> openAddDanhMucDialog());
        crudPanel.add(btnAddDM);
        JButton btnEditDM = createStyledButton("Sửa DM", BTN_SECONDARY);
        btnEditDM.addActionListener(e -> openEditDanhMucDialog());
        crudPanel.add(btnEditDM);
        JButton btnDeleteDM = createStyledButton("Xóa DM", BTN_DANGER);
        btnDeleteDM.addActionListener(e -> deleteSelectedDanhMuc());
        crudPanel.add(btnDeleteDM);

        crudPanel.add(Box.createHorizontalStrut(10));

        JButton btnAddCT = createStyledButton("+ Chi tiết", PRIMARY);
        btnAddCT.addActionListener(e -> openAddChiTietDialog());
        crudPanel.add(btnAddCT);
        JButton btnEditCT = createStyledButton("Sửa CT", BTN_SECONDARY);
        btnEditCT.addActionListener(e -> openEditChiTietDialog());
        crudPanel.add(btnEditCT);
        JButton btnDeleteCT = createStyledButton("Xóa CT", BTN_DANGER);
        btnDeleteCT.addActionListener(e -> deleteSelectedChiTiet());
        crudPanel.add(btnDeleteCT);

        JPanel excelPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        excelPanel.setOpaque(false);

        JButton btnExport = createStyledButton("Xuất Excel", BTN_SECONDARY);
        btnExport.setToolTipText("Xuất danh mục & chi tiết thưởng ra file .xls");
        btnExport.addActionListener(e -> exportExcel());
        excelPanel.add(btnExport);

        JButton btnImport = createStyledButton("Nhập Excel", BTN_SECONDARY);
        btnImport.setToolTipText("Nhập từ file .xls");
        btnImport.addActionListener(e -> importExcel());
        excelPanel.add(btnImport);

        rowActions.add(crudPanel, BorderLayout.WEST);
        rowActions.add(excelPanel, BorderLayout.EAST);

        toolbar.add(rowSearch, BorderLayout.NORTH);
        toolbar.add(rowActions, BorderLayout.SOUTH);

        PermissionHelper.applyVisible(btnAddDM, PermissionHelper.canAdd("thuong"));
        PermissionHelper.applyVisible(btnEditDM, PermissionHelper.canEdit("thuong"));
        PermissionHelper.applyVisible(btnDeleteDM, PermissionHelper.canDelete("thuong"));
        PermissionHelper.applyVisible(btnAddCT, PermissionHelper.canAdd("thuong"));
        PermissionHelper.applyVisible(btnEditCT, PermissionHelper.canEdit("thuong"));
        PermissionHelper.applyVisible(btnDeleteCT, PermissionHelper.canDelete("thuong"));
        PermissionHelper.applyVisible(btnExport, PermissionHelper.canExcel("thuong"));
        PermissionHelper.applyVisible(btnImport, PermissionHelper.canExcel("thuong"));

        return toolbar;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(TEXT_WHITE);
        btn.setBackground(bgColor);
        btn.setPreferredSize(new Dimension(100, 34));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        Color hoverColor = bgColor.brighter();
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { btn.setBackground(hoverColor); }

            @Override
            public void mouseExited(MouseEvent e) { btn.setBackground(bgColor); }
        });
        return btn;
    }

    private JSplitPane createMainContent() {
        modelDanhMuc = new DefaultTableModel(new String[]{"Mã thưởng", "Tên loại thưởng", "Mô tả"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblDanhMuc = createStyledTable(modelDanhMuc);
        tblDanhMuc.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    loadDetailTable();
                }
            }
        });
        tblDanhMuc.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tblDanhMuc.getSelectedRow() != -1
                        && PermissionHelper.canEdit("thuong")) {
                    openEditDanhMucDialog();
                }
            }
        });

        modelChiTiet = new DefaultTableModel(new String[]{"Mã CTT", "Nhân viên", "Số tiền", "Ngày thưởng", "Ghi chú"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblChiTiet = createStyledTable(modelChiTiet);
        tblChiTiet.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tblChiTiet.getSelectedRow() != -1
                        && PermissionHelper.canEdit("thuong")) {
                    openEditChiTietDialog();
                }
            }
        });

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(BG_CONTENT);
        top.setBorder(BorderFactory.createEmptyBorder(8, 10, 4, 10));
        JLabel lblTop = new JLabel("Danh mục thưởng");
        lblTop.setForeground(TEXT_WHITE);
        lblTop.setFont(new Font("Segoe UI", Font.BOLD, 13));
        top.add(lblTop, BorderLayout.NORTH);
        top.add(createStyledScrollPane(tblDanhMuc), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(BG_CONTENT);
        bottom.setBorder(BorderFactory.createEmptyBorder(4, 10, 8, 10));
        JLabel lblBottom = new JLabel("Chi tiết thưởng theo danh mục");
        lblBottom.setForeground(TEXT_WHITE);
        lblBottom.setFont(new Font("Segoe UI", Font.BOLD, 13));
        bottom.add(lblBottom, BorderLayout.NORTH);
        bottom.add(createStyledScrollPane(tblChiTiet), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, bottom);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setDividerSize(8);
        splitPane.setResizeWeight(0.42);
        splitPane.setBackground(BG_CONTENT);
        return splitPane;
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setForeground(TEXT_WHITE);
        table.setBackground(BG_CONTENT);
        table.setSelectionBackground(TABLE_SEL);
        table.setSelectionForeground(TEXT_WHITE);
        table.setRowHeight(34);
        table.setGridColor(new Color(40, 50, 70));
        table.setShowGrid(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 36));
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setBackground(PRIMARY_DARK);
                lbl.setForeground(TEXT_WHITE);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(40, 50, 70)),
                        BorderFactory.createEmptyBorder(0, 8, 0, 8)
                ));
                lbl.setHorizontalAlignment(SwingConstants.LEFT);
                return lbl;
            }
        });

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (isSelected) {
                    c.setBackground(TABLE_SEL);
                    c.setForeground(TEXT_WHITE);
                } else {
                    c.setBackground(row % 2 == 0 ? BG_CONTENT : TABLE_ROW_ALT);
                    c.setForeground(TEXT_WHITE);
                }
                ((JLabel) c).setHorizontalAlignment(SwingConstants.LEFT);
                if (table == tblChiTiet && column == 2) {
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.RIGHT);
                    c.setForeground(new Color(96, 165, 250));
                }
                ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return c;
            }
        });
        return table;
    }

    private JScrollPane createStyledScrollPane(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BG_CONTENT);
        scrollPane.getViewport().setOpaque(true);
        scrollPane.setBackground(BG_CONTENT);
        return scrollPane;
    }

    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        statusBar.setBackground(BG_TOOLBAR);
        statusBar.setPreferredSize(new Dimension(0, 30));
        statusBar.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        lblStatus = new JLabel("");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatus.setForeground(TEXT_MUTED);
        statusBar.add(lblStatus);
        return statusBar;
    }

    private void refreshData() {
        danhMucThuongBUS.loadData();
        chiTietThuongBUS.loadData();
        nhanVienBUS.loadData();
        loadMasterTable();
    }

    private void loadMasterTable() {
        String keyword = txtSearch == null ? "" : txtSearch.getText().trim().toLowerCase();
        modelDanhMuc.setRowCount(0);

        ArrayList<DanhMucThuongDTO> masters = danhMucThuongBUS.getList();
        ArrayList<DanhMucThuongDTO> filtered = new ArrayList<>();
        for (DanhMucThuongDTO dm : masters) {
            String ma = dm.getMaThuong() == null ? "" : dm.getMaThuong().toLowerCase();
            String ten = dm.getTenLoaiThuong() == null ? "" : dm.getTenLoaiThuong().toLowerCase();
            String moTa = dm.getMoTa() == null ? "" : dm.getMoTa().toLowerCase();
            if (keyword.isEmpty() || ma.contains(keyword) || ten.contains(keyword) || moTa.contains(keyword)) {
                filtered.add(dm);
                modelDanhMuc.addRow(new Object[]{dm.getMaThuong(), dm.getTenLoaiThuong(), dm.getMoTa()});
            }
        }

        if (!filtered.isEmpty()) {
            tblDanhMuc.setRowSelectionInterval(0, 0);
        } else {
            loadDetailTable();
        }
        lblStatus.setText("Danh mục thưởng: " + filtered.size());
    }

    private void loadDetailTable() {
        modelChiTiet.setRowCount(0);
        String maThuong = getSelectedMaThuong();
        String keyword = txtSearch == null ? "" : txtSearch.getText().trim();
        ArrayList<ChiTietThuongDTO> details = chiTietThuongBUS.search(keyword, maThuong);

        for (ChiTietThuongDTO ct : details) {
            String nhanVien = getNhanVienDisplay(ct.getMaNV());
            String soTien = ct.getSoTien() == null ? "0 ₫" : currencyFormat.format(ct.getSoTien());
            String ngayThuong = ct.getNgayThuong() == null ? "" : sdf.format(ct.getNgayThuong());
            modelChiTiet.addRow(new Object[]{
                ct.getMaCTT(),
                nhanVien,
                soTien,
                ngayThuong,
                ct.getGhiChu()
            });
        }
        String selectedText = maThuong == null ? "(chưa chọn danh mục)" : maThuong;
        lblStatus.setText("Danh mục: " + selectedText + " | Chi tiết thưởng: " + details.size());
    }

    private String getSelectedMaThuong() {
        int row = tblDanhMuc.getSelectedRow();
        if (row < 0) return null;
        Object value = modelDanhMuc.getValueAt(row, 0);
        return value == null ? null : String.valueOf(value);
    }

    private String getNhanVienDisplay(String maNV) {
        if (maNV == null) return "";
        NhanVienDTO nv = nhanVienBUS.getById(maNV);
        if (nv != null) {
            return nv.getMaNV() + " - " + nv.getHo() + " " + nv.getTen();
        }
        return maNV;
    }

    private void openAddDanhMucDialog() {
        DanhMucThuongDialog dialog = new DanhMucThuongDialog(
                (javax.swing.JFrame) SwingUtilities.getWindowAncestor(this),
                "Thêm Danh mục thưởng",
                null,
                danhMucThuongBUS
        );
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshData();
        }
    }

    private void openEditDanhMucDialog() {
        int row = tblDanhMuc.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn danh mục thưởng để sửa!", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String maThuong = String.valueOf(modelDanhMuc.getValueAt(row, 0));
        DanhMucThuongDTO dto = danhMucThuongBUS.getById(maThuong);
        if (dto == null) return;

        DanhMucThuongDialog dialog = new DanhMucThuongDialog(
                (javax.swing.JFrame) SwingUtilities.getWindowAncestor(this),
                "Sửa Danh mục thưởng",
                dto,
                danhMucThuongBUS
        );
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshData();
        }
    }

    private void deleteSelectedDanhMuc() {
        int row = tblDanhMuc.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn danh mục thưởng để xóa!", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maThuong = String.valueOf(modelDanhMuc.getValueAt(row, 0));
        ArrayList<ChiTietThuongDTO> listByCategory = chiTietThuongBUS.getByMaThuong(maThuong);
        if (!listByCategory.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Không thể xóa danh mục vì đang có chi tiết thưởng tham chiếu tới mã này.",
                    "Chặn xóa",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa danh mục thưởng " + maThuong + "?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            danhMucThuongBUS.delete(maThuong);
            refreshData();
        }
    }

    private void openAddChiTietDialog() {
        String selectedMaThuong = getSelectedMaThuong();
        if (selectedMaThuong == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn danh mục thưởng trước khi thêm chi tiết!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ChiTietThuongDialog dialog = new ChiTietThuongDialog(
                (javax.swing.JFrame) SwingUtilities.getWindowAncestor(this),
                "Thêm Chi tiết thưởng",
                null,
                chiTietThuongBUS,
                danhMucThuongBUS,
                nhanVienBUS,
                selectedMaThuong
        );
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshData();
            selectMasterById(selectedMaThuong);
        }
    }

    private void openEditChiTietDialog() {
        int row = tblChiTiet.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn chi tiết thưởng để sửa!", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maCTT = String.valueOf(modelChiTiet.getValueAt(row, 0));
        ChiTietThuongDTO dto = chiTietThuongBUS.getById(maCTT);
        if (dto == null) return;

        ChiTietThuongDialog dialog = new ChiTietThuongDialog(
                (javax.swing.JFrame) SwingUtilities.getWindowAncestor(this),
                "Sửa Chi tiết thưởng",
                dto,
                chiTietThuongBUS,
                danhMucThuongBUS,
                nhanVienBUS,
                dto.getMaThuong()
        );
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshData();
            selectMasterById(dto.getMaThuong());
        }
    }

    private void deleteSelectedChiTiet() {
        int row = tblChiTiet.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn chi tiết thưởng để xóa!", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String maCTT = String.valueOf(modelChiTiet.getValueAt(row, 0));
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa chi tiết thưởng " + maCTT + "?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            chiTietThuongBUS.delete(maCTT);
            refreshData();
        }
    }

    private void selectMasterById(String maThuong) {
        if (maThuong == null) return;
        for (int i = 0; i < modelDanhMuc.getRowCount(); i++) {
            Object value = modelDanhMuc.getValueAt(i, 0);
            if (value != null && maThuong.equalsIgnoreCase(String.valueOf(value))) {
                tblDanhMuc.setRowSelectionInterval(i, i);
                break;
            }
        }
    }

    // =========================================================================
    //  EXPORT / IMPORT EXCEL
    // =========================================================================
    private static final String[] CTT_HEADERS = {
        "Mã CTT", "Mã NV", "Mã thưởng", "Số tiền", "Ngày thưởng (dd/MM/yyyy)", "Ghi chú"
    };

    private void exportExcel() {
        java.util.List<Object[]> rows = new java.util.ArrayList<>();
        for (ChiTietThuongDTO ct : chiTietThuongBUS.getList()) {
            rows.add(new Object[]{
                ct.getMaCTT(), ct.getMaNV(), ct.getMaThuong(),
                ct.getSoTien(), ct.getNgayThuong(),
                ct.getGhiChu()
            });
        }

        if (rows.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu chi tiết thưởng để xuất!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        HSSFWorkbook wb = ExcelHelper.createWorkbook("ChiTietThuong", CTT_HEADERS, rows);
        ExcelHelper.saveWithDialog(SwingUtilities.getWindowAncestor(this), wb, "ChiTietThuong");
    }

    private void importExcel() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "File Excel cần header:\nMã CTT | Mã NV | Mã thưởng | Số tiền | Ngày thưởng (dd/MM/yyyy) | Ghi chú\n\n" +
                "Bạn muốn tải file mẫu trước?",
                "Nhập Excel — Chi tiết thưởng",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            HSSFWorkbook tmpl = ExcelHelper.createTemplate("ChiTietThuong", CTT_HEADERS);
            ExcelHelper.saveWithDialog(SwingUtilities.getWindowAncestor(this), tmpl, "Mau_ChiTietThuong");
            return;
        }
        if (confirm == JOptionPane.CANCEL_OPTION) return;

        java.util.List<String[]> data = ExcelHelper.openAndRead(this, CTT_HEADERS.length);
        if (data == null) return;

        java.util.List<String> errors = new java.util.ArrayList<>();
        int success = 0;

        for (int i = 0; i < data.size(); i++) {
            String[] row = data.get(i);
            int line = i + 2;
            try {
                String maCTT = row[0].trim();
                String maNV = row[1].trim();
                String maThuong = row[2].trim();
                java.math.BigDecimal soTien = ExcelHelper.parseMoney(row[3]);
                java.util.Date ngay = ExcelHelper.parseDate(row[4]);
                String ghiChu = row[5].trim();

                if (maCTT.isEmpty()) { errors.add("Dòng " + line + ": Mã CTT trống"); continue; }
                if (maNV.isEmpty()) { errors.add("Dòng " + line + ": Mã NV trống"); continue; }
                if (maThuong.isEmpty()) { errors.add("Dòng " + line + ": Mã thưởng trống"); continue; }
                if (soTien == null) { errors.add("Dòng " + line + ": Số tiền không hợp lệ"); continue; }
                if (ngay == null) { errors.add("Dòng " + line + ": Ngày thưởng không hợp lệ"); continue; }
                if (nhanVienBUS.getById(maNV) == null) { errors.add("Dòng " + line + ": Mã NV '" + maNV + "' không tồn tại"); continue; }
                if (danhMucThuongBUS.getById(maThuong) == null) { errors.add("Dòng " + line + ": Mã thưởng '" + maThuong + "' không tồn tại"); continue; }

                ChiTietThuongDTO dto = new ChiTietThuongDTO(maCTT, maNV, maThuong, soTien, ngay, ghiChu);
                chiTietThuongBUS.add(dto);
                success++;
            } catch (Exception ex) {
                errors.add("Dòng " + line + ": " + ex.getMessage());
            }
        }

        refreshData();
        ExcelHelper.showImportErrors(this, errors, success);
    }
}

