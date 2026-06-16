package com.nhom6.qlbh.controller.banOnline;

import com.nhom6.qlbh.model.DonHangOnline;
import com.nhom6.qlbh.model.NenTangOnline;
import com.nhom6.qlbh.service.BanOnlineService;
import com.nhom6.qlbh.util.AlertUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class BanOnlineController {

    @FXML private ComboBox<NenTangOnline> cboNenTang;
    @FXML private TableView<DonHangOnline> tblDHO;
    @FXML private TableColumn<DonHangOnline, String> colMaDHO, colNenTang, colThoiGian, colMaHD,
                                                      colTrangThai, colDiaChi, colDonVi, colTrangThaiGiao;
    @FXML private TableColumn<DonHangOnline, Void>   colAction;
    @FXML private Label lblCount;

    private final BanOnlineService service = new BanOnlineService();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final List<String> GIAO_SEQUENCE =
        List.of("CHO_LAY", "DANG_GIAO", "DA_GIAO");

    @FXML
    public void initialize() {
        // Load nền tảng vào filter ComboBox
        try {
            List<NenTangOnline> ntList = service.getNenTangOnlines();
            NenTangOnline all = new NenTangOnline(0, "Tất cả");
            ntList.add(0, all);
            cboNenTang.setItems(FXCollections.observableArrayList(ntList));
            cboNenTang.getSelectionModel().selectFirst();
        } catch (Exception e) { /* ignored */ }

        setupTable();
        loadData();
    }

    private void setupTable() {
        colMaDHO.setCellValueFactory(c      -> new SimpleStringProperty(c.getValue().getMaDHO()));
        colNenTang.setCellValueFactory(c    -> new SimpleStringProperty(c.getValue().getTenNT()));
        colThoiGian.setCellValueFactory(c   -> {
            var dt = c.getValue().getThoiGian();
            return new SimpleStringProperty(dt != null ? dt.format(FMT) : "");
        });
        colMaHD.setCellValueFactory(c       -> new SimpleStringProperty(
            c.getValue().getMaHD() != null ? c.getValue().getMaHD() : "—"));
        colTrangThai.setCellValueFactory(c  -> new SimpleStringProperty(c.getValue().getTrangThai()));
        colDiaChi.setCellValueFactory(c     -> new SimpleStringProperty(
            c.getValue().getDiaChiGiao() != null ? c.getValue().getDiaChiGiao() : "—"));
        colDonVi.setCellValueFactory(c      -> new SimpleStringProperty(
            c.getValue().getDonViVanChuyen() != null ? c.getValue().getDonViVanChuyen() : "—"));
        colTrangThaiGiao.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTrangThaiGiaoLabel()));

        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnCapNhat = new Button("Cập nhật VC");
            {
                btnCapNhat.getStyleClass().add("btn-edit");
                btnCapNhat.setOnAction(e -> {
                    DonHangOnline d = getTableView().getItems().get(getIndex());
                    capNhatVanChuyen(d);
                });
            }
            private final HBox box = new HBox(btnCapNhat);
            { box.setSpacing(4); }

            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                DonHangOnline d = getTableView().getItems().get(getIndex());
                btnCapNhat.setDisable(d.getMaVD() == null || "DA_GIAO".equals(d.getTrangThaiGiao()));
                setGraphic(box);
            }
        });
    }

    private void loadData() {
        try {
            List<DonHangOnline> list = service.findAll();
            NenTangOnline filter = cboNenTang.getValue();
            if (filter != null && filter.getMaNT() != 0) {
                list = list.stream().filter(d -> d.getMaNT() == filter.getMaNT()).toList();
            }
            tblDHO.setItems(FXCollections.observableArrayList(list));
            lblCount.setText(list.size() + " đơn hàng online");
        } catch (Exception e) { AlertUtil.error("Lỗi", e.getMessage()); }
    }

    @FXML public void onFilter() { loadData(); }

    @FXML
    private void onTaoDon() {
        try {
            Parent form = FXMLLoader.load(getClass().getResource("/fxml/banOnline/ban-online-form.fxml"));
            ((StackPane) tblDHO.getScene().getRoot().lookup("#contentArea")).getChildren().setAll(form);
        } catch (Exception e) { AlertUtil.error("Lỗi mở form", e.getMessage()); }
    }

    private void capNhatVanChuyen(DonHangOnline d) {
        if (d.getMaVD() == null) return;
        String cur = d.getTrangThaiGiao();
        int idx = GIAO_SEQUENCE.indexOf(cur);
        if (idx < 0 || idx >= GIAO_SEQUENCE.size() - 1) {
            AlertUtil.info("Thông báo", "Đơn hàng đã hoàn thành giao hàng."); return;
        }
        String next = GIAO_SEQUENCE.get(idx + 1);
        try {
            service.capNhatVanChuyen(d.getMaVD(), next);
            loadData();
        } catch (Exception e) { AlertUtil.error("Lỗi", e.getMessage()); }
    }
}
