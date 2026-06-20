package com.nhom6.qlbh.controller.banOnline;

import com.nhom6.qlbh.model.DonHangOnline;
import com.nhom6.qlbh.model.NenTangOnline;
import com.nhom6.qlbh.service.BanOnlineService;
import com.nhom6.qlbh.util.AlertUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BanOnlineController {

    @FXML private HBox platformCardsArea;
    @FXML private ComboBox<NenTangOnline> cboNenTang;
    @FXML private TableView<DonHangOnline> tblDHO;
    @FXML private TableColumn<DonHangOnline, String> colMaDHO, colNenTang, colThoiGian, colMaHD,
                                                      colTrangThai, colDiaChi, colDonVi, colTrangThaiGiao;
    @FXML private TableColumn<DonHangOnline, Void>   colAction;
    @FXML private Label lblCount;

    private final BanOnlineService service = new BanOnlineService();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final List<String> GIAO_SEQUENCE = List.of("CHO_LAY", "DANG_GIAO", "DA_GIAO");

    private List<NenTangOnline> platforms = new ArrayList<>();
    private final Map<Integer, String> platformColors = new HashMap<>();

    @FXML
    public void initialize() {
        loadPlatforms();
        setupTable();
        loadData();
    }

    private void loadPlatforms() {
        try {
            platforms = service.getNenTangOnlines();
            platformColors.clear();
            for (NenTangOnline nt : platforms) {
                String color = nt.getMauSac() != null ? nt.getMauSac() : "#607d8b";
                if ("#000000".equals(color)) color = "#424242";
                platformColors.put(nt.getMaNT(), color);
            }
            buildPlatformCards();

            List<NenTangOnline> items = new ArrayList<>();
            items.add(new NenTangOnline(0, "Tất cả"));
            items.addAll(platforms);
            cboNenTang.setItems(FXCollections.observableArrayList(items));
            cboNenTang.getSelectionModel().selectFirst();
        } catch (Exception e) { /* ignored */ }
    }

    private void buildPlatformCards() {
        platformCardsArea.getChildren().clear();
        for (NenTangOnline nt : platforms) {
            platformCardsArea.getChildren().add(createCard(nt));
        }
    }

    private VBox createCard(NenTangOnline nt) {
        String color = platformColors.getOrDefault(nt.getMaNT(), "#607d8b");
        String initial = nt.getTenNT().length() > 0 ? nt.getTenNT().substring(0, 1).toUpperCase() : "?";

        Label lblIcon = new Label(initial);
        lblIcon.setStyle("-fx-font-size:22px; -fx-font-weight:bold; -fx-text-fill:white;");
        StackPane circle = new StackPane(lblIcon);
        circle.setStyle("-fx-background-color:" + color + "; -fx-background-radius:28; " +
                        "-fx-min-width:56; -fx-min-height:56; -fx-max-width:56; -fx-max-height:56;");
        circle.setAlignment(Pos.CENTER);

        Label lblName = new Label(nt.getTenNT());
        lblName.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:#212121;");

        boolean ketNoi = nt.isKetNoi();
        Label lblStatus = new Label(ketNoi ? "✔ Đã kết nối" : "✘ Chưa kết nối");
        lblStatus.setStyle(ketNoi
            ? "-fx-text-fill:#16a34a; -fx-font-size:11px; -fx-font-weight:bold;"
            : "-fx-text-fill:#9e9e9e; -fx-font-size:11px;");

        int newStatus = ketNoi ? 0 : 1;
        Button btnToggle = new Button(ketNoi ? "Ngắt kết nối" : "Kết nối");
        btnToggle.getStyleClass().add(ketNoi ? "btn-delete" : "btn-search");
        btnToggle.setPrefWidth(120);
        btnToggle.setOnAction(e -> {
            try {
                service.toggleKetNoi(nt.getMaNT(), newStatus);
                loadPlatforms();
            } catch (Exception ex) { AlertUtil.error("Lỗi", ex.getMessage()); }
        });

        VBox card = new VBox(8, circle, lblName, lblStatus, btnToggle);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color:white; -fx-background-radius:10; " +
                      "-fx-border-color:#e0e0e0; -fx-border-radius:10; -fx-border-width:1; " +
                      "-fx-padding:14 20; -fx-min-width:155; " +
                      "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),4,0,0,2);");
        return card;
    }

    private void setupTable() {
        colMaDHO.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMaDHO()));

        // Nền tảng — icon màu + tên
        colNenTang.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTenNT()));
        colNenTang.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                setGraphic(null); setText(null);
                if (empty || val == null || getIndex() >= getTableView().getItems().size()) return;
                DonHangOnline d = getTableView().getItems().get(getIndex());
                String color = platformColors.getOrDefault(d.getMaNT(), "#607d8b");
                String initial = val.length() > 0 ? val.substring(0, 1).toUpperCase() : "?";
                Label dot = new Label(initial);
                dot.setStyle("-fx-text-fill:white; -fx-font-size:9px; -fx-font-weight:bold;");
                StackPane badge = new StackPane(dot);
                badge.setStyle("-fx-background-color:" + color + "; -fx-background-radius:9; " +
                               "-fx-min-width:18; -fx-min-height:18; -fx-max-width:18; -fx-max-height:18;");
                badge.setAlignment(Pos.CENTER);
                Label name = new Label(val);
                name.setStyle("-fx-font-size:12px;");
                HBox hb = new HBox(5, badge, name);
                hb.setAlignment(Pos.CENTER_LEFT);
                setGraphic(hb);
            }
        });

        colThoiGian.setCellValueFactory(c -> {
            var dt = c.getValue().getThoiGian();
            return new SimpleStringProperty(dt != null ? dt.format(FMT) : "");
        });
        colMaHD.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getMaHD() != null ? c.getValue().getMaHD() : "—"));
        colTrangThai.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTrangThai()));
        colDiaChi.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getDiaChiGiao() != null ? c.getValue().getDiaChiGiao() : "—"));
        colDonVi.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getDonViVanChuyen() != null ? c.getValue().getDonViVanChuyen() : "—"));

        colTrangThaiGiao.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTrangThaiGiaoLabel()));
        colTrangThaiGiao.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setText(null); setStyle(""); return; }
                setText(val);
                setStyle(switch (val) {
                    case "Đã giao"   -> "-fx-text-fill:#16a34a; -fx-font-weight:bold;";
                    case "Đang giao" -> "-fx-text-fill:#1565c0; -fx-font-weight:bold;";
                    case "Hoàn trả"  -> "-fx-text-fill:#c62828; -fx-font-weight:bold;";
                    default          -> "-fx-text-fill:#e65100;";
                });
            }
        });

        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Cập nhật VC");
            { btn.getStyleClass().add("btn-edit");
              btn.setOnAction(e -> capNhatVanChuyen(getTableView().getItems().get(getIndex()))); }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                DonHangOnline d = getTableView().getItems().get(getIndex());
                btn.setDisable(d.getMaVD() == null || "DA_GIAO".equals(d.getTrangThaiGiao()));
                setGraphic(btn);
            }
        });
    }

    private void loadData() {
        try {
            List<DonHangOnline> list = service.findAll();
            NenTangOnline filter = cboNenTang.getValue();
            if (filter != null && filter.getMaNT() != 0)
                list = list.stream().filter(d -> d.getMaNT() == filter.getMaNT()).toList();
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
