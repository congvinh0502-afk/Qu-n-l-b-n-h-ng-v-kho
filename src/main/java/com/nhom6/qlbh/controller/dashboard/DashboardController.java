package com.nhom6.qlbh.controller.dashboard;

import com.nhom6.qlbh.model.DoanhThuNgay;
import com.nhom6.qlbh.model.PhanTichSanPham;
import com.nhom6.qlbh.service.BaoCaoService;
import com.nhom6.qlbh.util.FormatUtil;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class DashboardController {

    @FXML private Label lblNgayHienTai;
    @FXML private Label lblDoanhThuHomNay;
    @FXML private Label lblSoHDHomNay;
    @FXML private Label lblSoHangSapHet;
    @FXML private Label lblSoKhachHang;

    @FXML private BarChart<String, Number>  barChart7Ngay;

    @FXML private TableView<PhanTichSanPham> tblTop5;
    @FXML private TableColumn<PhanTichSanPham, String>  topColTen, topColDT;
    @FXML private TableColumn<PhanTichSanPham, Integer> topColSL;

    private final BaoCaoService service = new BaoCaoService();
    private static final DateTimeFormatter DAY_FMT  = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter DATE_FULL = DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy");

    @FXML
    public void initialize() {
        lblNgayHienTai.setText(LocalDate.now().format(DATE_FULL));
        setupTable();
        loadAll();
    }

    private void setupTable() {
        topColTen.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTenSP()));
        topColSL.setCellValueFactory(c  -> new SimpleIntegerProperty(c.getValue().getSoLuongThuc()).asObject());
        topColDT.setCellValueFactory(c  -> new SimpleStringProperty(FormatUtil.currency(c.getValue().getDoanhThu())));
    }

    @FXML public void onRefresh() { loadAll(); }

    private void loadAll() {
        loadKpi();
        loadBarChart();
        loadTop5();
    }

    private void loadKpi() {
        try {
            Map<String, Object> m = service.getDashboardHomNay();
            BigDecimal dt  = (BigDecimal) m.getOrDefault("doanhThuHomNay", BigDecimal.ZERO);
            long soHD      = ((Number) m.getOrDefault("soHDHomNay",    0L)).longValue();
            long sapHet    = ((Number) m.getOrDefault("soHangSapHet",  0L)).longValue();
            long khach     = ((Number) m.getOrDefault("soKhachHang",   0L)).longValue();

            lblDoanhThuHomNay.setText(FormatUtil.currency(dt));
            lblSoHDHomNay.setText(String.valueOf(soHD));
            lblSoHangSapHet.setText(String.valueOf(sapHet));
            lblSoKhachHang.setText(String.valueOf(khach));

            // Tô đỏ nếu có hàng sắp hết
            if (sapHet > 0) lblSoHangSapHet.setStyle("-fx-text-fill:#c62828;-fx-font-size:28px;-fx-font-weight:bold;");
        } catch (Exception e) {
            lblDoanhThuHomNay.setText("—");
        }
    }

    private void loadBarChart() {
        try {
            LocalDate to   = LocalDate.now();
            LocalDate from = to.minusDays(6);
            List<DoanhThuNgay> data = service.getDoanhThuNgay(from, to);

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Doanh thu");

            for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
                LocalDate fd = d;
                DoanhThuNgay dtn = data.stream()
                    .filter(x -> x.getNgay().equals(fd)).findFirst().orElse(null);
                double revenue = dtn != null ? dtn.getDoanhThuThuan().doubleValue() : 0;
                series.getData().add(new XYChart.Data<>(d.format(DAY_FMT), revenue));
            }

            barChart7Ngay.getData().clear();
            barChart7Ngay.getData().add(series);
        } catch (Exception ignored) {}
    }

    private void loadTop5() {
        try {
            List<PhanTichSanPham> top = service.getTopSanPham(5, "SoLuongThuc");
            tblTop5.setItems(FXCollections.observableArrayList(top));
        } catch (Exception ignored) {}
    }
}
