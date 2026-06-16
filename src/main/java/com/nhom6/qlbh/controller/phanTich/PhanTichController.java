package com.nhom6.qlbh.controller.phanTich;

import com.nhom6.qlbh.model.DoanhThuNgay;
import com.nhom6.qlbh.model.PhanTichSanPham;
import com.nhom6.qlbh.service.BaoCaoService;
import com.nhom6.qlbh.util.AlertUtil;
import com.nhom6.qlbh.util.ExcelUtil;
import com.nhom6.qlbh.util.FormatUtil;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class PhanTichController {

    @FXML private ComboBox<String> cboRange;
    @FXML private ComboBox<String> cboTopBy;

    // KPI labels
    @FXML private Label lblTongDoanhThu;
    @FXML private Label lblTongLoiNhuan;
    @FXML private Label lblSoHoaDon;
    @FXML private Label lblSoSanPham;

    // Charts
    @FXML private LineChart<String, Number> lineChart;
    @FXML private BarChart<String, Number>  barChart;
    @FXML private Label lblBarTitle;

    // Table
    @FXML private TableView<PhanTichSanPham>       tblPhanTich;
    @FXML private TableColumn<PhanTichSanPham, String>  colMaSP, colTenSP, colDoanhThu, colLoiNhuan;
    @FXML private TableColumn<PhanTichSanPham, Integer> colSLBan, colSLTra, colSLThuc;

    @FXML private Button btnXuatExcel;

    private final BaoCaoService service = new BaoCaoService();
    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("dd/MM");

    // Cache for Excel export
    private List<DoanhThuNgay>     lastDoanhThu;
    private List<PhanTichSanPham>  lastPhanTich;

    @FXML
    public void initialize() {
        cboRange.setItems(FXCollections.observableArrayList("7 ngày gần nhất", "14 ngày gần nhất", "30 ngày gần nhất"));
        cboRange.getSelectionModel().selectFirst();

        cboTopBy.setItems(FXCollections.observableArrayList("Doanh thu", "Số lượng bán", "Lợi nhuận gộp"));
        cboTopBy.getSelectionModel().selectFirst();

        setupTable();
        loadAll();
    }

    private void setupTable() {
        colMaSP.setCellValueFactory(c  -> new SimpleStringProperty(c.getValue().getMaSP()));
        colTenSP.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTenSP()));
        colSLBan.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getSoLuongBan()).asObject());
        colSLTra.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getSoLuongTra()).asObject());
        colSLThuc.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getSoLuongThuc()).asObject());
        colDoanhThu.setCellValueFactory(c  -> new SimpleStringProperty(FormatUtil.currency(c.getValue().getDoanhThu())));
        colLoiNhuan.setCellValueFactory(c  -> new SimpleStringProperty(FormatUtil.currency(c.getValue().getLoiNhuanGop())));
    }

    private void loadAll() {
        try {
            loadKpi();
            loadLineChart();
            loadBarChart();
            loadTable();
        } catch (Exception e) {
            AlertUtil.error("Lỗi tải dữ liệu", e.getMessage());
        }
    }

    @FXML
    public void onRefresh() { loadAll(); }

    // ------------------------------------------------------------------ KPI
    private void loadKpi() {
        Map<String, Object> kpi = service.getKpiTongQuan();
        BigDecimal dt = (BigDecimal) kpi.getOrDefault("tongDoanhThu", BigDecimal.ZERO);
        BigDecimal ln = (BigDecimal) kpi.getOrDefault("tongLoiNhuan", BigDecimal.ZERO);
        long soHD    = ((Number) kpi.getOrDefault("soHoaDon",  0L)).longValue();
        long soSP    = ((Number) kpi.getOrDefault("soSanPham", 0L)).longValue();

        lblTongDoanhThu.setText(FormatUtil.currency(dt) + " đ");
        lblTongLoiNhuan.setText(FormatUtil.currency(ln) + " đ");
        lblSoHoaDon.setText(String.valueOf(soHD));
        lblSoSanPham.setText(String.valueOf(soSP));
    }

    // --------------------------------------------------------------- LineChart
    private void loadLineChart() {
        int days = switch (cboRange.getSelectionModel().getSelectedIndex()) {
            case 1  -> 14;
            case 2  -> 30;
            default -> 7;
        };
        LocalDate to   = LocalDate.now();
        LocalDate from = to.minusDays(days - 1);

        List<DoanhThuNgay> data = service.getDoanhThuNgay(from, to);
        lastDoanhThu = data;

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Doanh thu");

        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            LocalDate fd = d;
            DoanhThuNgay dtn = data.stream()
                .filter(x -> x.getNgay().equals(fd))
                .findFirst().orElse(null);
            double revenue = dtn != null ? dtn.getDoanhThuThuan().doubleValue() : 0;
            series.getData().add(new XYChart.Data<>(d.format(DAY_FMT), revenue));
        }

        lineChart.getData().clear();
        lineChart.getData().add(series);
    }

    // ---------------------------------------------------------------- BarChart
    private void loadBarChart() {
        String topByLabel = cboTopBy.getValue();
        String orderBy = switch (topByLabel) {
            case "Số lượng bán"  -> "SoLuongThuc";
            case "Lợi nhuận gộp" -> "LoiNhuanGop";
            default              -> "DoanhThu";
        };
        lblBarTitle.setText("Top 10 SP — " + topByLabel);

        List<PhanTichSanPham> top = service.getTopSanPham(10, orderBy);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(topByLabel);
        for (PhanTichSanPham sp : top) {
            double val = switch (orderBy) {
                case "SoLuongThuc" -> sp.getSoLuongThuc();
                case "LoiNhuanGop" -> sp.getLoiNhuanGop().doubleValue();
                default            -> sp.getDoanhThu().doubleValue();
            };
            series.getData().add(new XYChart.Data<>(sp.getMaSP(), val));
        }

        barChart.getData().clear();
        barChart.getData().add(series);
    }

    // ------------------------------------------------------------------- Table
    private void loadTable() {
        List<PhanTichSanPham> list = service.getAllPhanTich();
        lastPhanTich = list;
        tblPhanTich.setItems(FXCollections.observableArrayList(list));
    }

    // --------------------------------------------------------------- Export
    @FXML
    public void onXuatExcel() {
        if (lastPhanTich == null || lastDoanhThu == null) {
            AlertUtil.warn("Chưa có dữ liệu", "Nhấn Làm mới để tải dữ liệu trước.");
            return;
        }
        FileChooser fc = new FileChooser();
        fc.setTitle("Lưu file Excel");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"));
        fc.setInitialFileName("bao-cao-" + LocalDate.now() + ".xlsx");
        File file = fc.showSaveDialog(btnXuatExcel.getScene().getWindow());
        if (file == null) return;

        try {
            ExcelUtil.exportBaoCao(lastPhanTich, lastDoanhThu, file);
            AlertUtil.info("Xuất thành công", "Đã lưu file:\n" + file.getAbsolutePath());
        } catch (Exception e) {
            AlertUtil.error("Lỗi xuất Excel", e.getMessage());
        }
    }
}
