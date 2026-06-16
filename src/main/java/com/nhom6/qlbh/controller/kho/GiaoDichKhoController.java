package com.nhom6.qlbh.controller.kho;

import com.nhom6.qlbh.model.ChiTietGDK;
import com.nhom6.qlbh.model.GiaoDichKho;
import com.nhom6.qlbh.service.GiaoDichKhoService;
import com.nhom6.qlbh.util.AlertUtil;
import com.nhom6.qlbh.util.FormatUtil;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class GiaoDichKhoController {

    @FXML private ComboBox<String> cboLoaiFilter;
    @FXML private TableView<GiaoDichKho> tblGDK;
    @FXML private TableColumn<GiaoDichKho, String>  colMaGD;
    @FXML private TableColumn<GiaoDichKho, String>  colLoaiGD;
    @FXML private TableColumn<GiaoDichKho, String>  colNCC;
    @FXML private TableColumn<GiaoDichKho, String>  colNV;
    @FXML private TableColumn<GiaoDichKho, String>  colThoiGian;
    @FXML private TableColumn<GiaoDichKho, Integer> colSoMH;
    @FXML private TableColumn<GiaoDichKho, String>  colTongTien;
    @FXML private TableColumn<GiaoDichKho, String>  colGhiChu;
    @FXML private TableColumn<GiaoDichKho, Void>    colAction;
    @FXML private Label lblCount;

    private final GiaoDichKhoService service = new GiaoDichKhoService();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        cboLoaiFilter.setItems(FXCollections.observableArrayList(
            "— Tất cả —", "NHAP", "TRA_NHAP", "KIEM_KHO", "XUAT_HUY"));
        cboLoaiFilter.getSelectionModel().selectFirst();

        colMaGD.setCellValueFactory(new PropertyValueFactory<>("maGD"));
        colLoaiGD.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLoaiGDLabel()));
        colNCC.setCellValueFactory(new PropertyValueFactory<>("tenNCC"));
        colNV.setCellValueFactory(new PropertyValueFactory<>("tenNV"));
        colThoiGian.setCellValueFactory(c -> {
            var dt = c.getValue().getThoiGian();
            return new SimpleStringProperty(dt != null ? dt.format(FMT) : "");
        });
        colSoMH.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getSoMatHang()).asObject());
        colTongTien.setCellValueFactory(c -> new SimpleStringProperty(FormatUtil.currency(c.getValue().getTongTien())));
        colGhiChu.setCellValueFactory(new PropertyValueFactory<>("ghiChu"));

        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Xem");
            { btn.getStyleClass().add("btn-edit"); btn.setOnAction(e -> showDetail(getTableView().getItems().get(getIndex()))); }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : btn);
            }
        });

        loadData();
    }

    public void loadData() {
        try {
            String sel = cboLoaiFilter.getValue();
            String loai = (sel == null || sel.startsWith("—")) ? null : sel;
            List<GiaoDichKho> list = service.findAll(loai);
            tblGDK.setItems(FXCollections.observableArrayList(list));
            lblCount.setText(list.size() + " phiếu");
        } catch (Exception e) {
            AlertUtil.error("Lỗi tải dữ liệu", e.getMessage());
        }
    }

    @FXML private void onSearch() { loadData(); }

    @FXML
    private void onTaoPhieu() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/kho/giao-dich-kho-form.fxml"));
            Parent form = loader.load();
            StackPane contentArea = (StackPane) tblGDK.getScene().getRoot().lookup("#contentArea");
            contentArea.getChildren().setAll(form);
        } catch (Exception e) {
            AlertUtil.error("Lỗi mở form", e.getMessage());
        }
    }

    private void showDetail(GiaoDichKho gdk) {
        try {
            List<ChiTietGDK> ct = service.findChiTiet(gdk.getMaGD());
            StringBuilder sb = new StringBuilder();
            sb.append("Phiếu: ").append(gdk.getMaGD())
              .append("  |  Loại: ").append(gdk.getLoaiGDLabel()).append("\n\n");
            for (ChiTietGDK c : ct) {
                sb.append(String.format("%-10s %-30s SL: %5d  Đơn giá: %s%n",
                    c.getMaSP(), c.getTenSP(), c.getSoLuong(), FormatUtil.currency(c.getDonGiaNhap())));
            }

            TextArea ta = new TextArea(sb.toString());
            ta.setEditable(false); ta.setPrefSize(520, 280);
            ta.setStyle("-fx-font-family: monospace; -fx-font-size: 12px;");

            Stage stage = new Stage();
            stage.setTitle("Chi tiết phiếu " + gdk.getMaGD());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(ta));
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            AlertUtil.error("Lỗi", e.getMessage());
        }
    }
}
