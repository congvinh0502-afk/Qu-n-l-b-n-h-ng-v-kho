package com.nhom6.qlbh.controller.banhang;

import com.nhom6.qlbh.model.TraHang;
import com.nhom6.qlbh.service.TraHangService;
import com.nhom6.qlbh.util.AlertUtil;
import com.nhom6.qlbh.util.FormatUtil;
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

public class TraHangController {

    @FXML private TableView<TraHang> tblTH;
    @FXML private TableColumn<TraHang, String> colMaTra, colMaHD, colKhach, colNV, colThoiGian, colCanTra, colDaTra, colLyDo;
    @FXML private TableColumn<TraHang, Void> colAction;
    @FXML private Label lblCount;

    private final TraHangService service = new TraHangService();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        colMaTra.setCellValueFactory(new PropertyValueFactory<>("maTra"));
        colMaHD.setCellValueFactory(new PropertyValueFactory<>("maHD"));
        colKhach.setCellValueFactory(new PropertyValueFactory<>("tenKH"));
        colNV.setCellValueFactory(new PropertyValueFactory<>("tenNV"));
        colThoiGian.setCellValueFactory(c -> {
            var dt = c.getValue().getThoiGian();
            return new SimpleStringProperty(dt != null ? dt.format(FMT) : "");
        });
        colCanTra.setCellValueFactory(c -> new SimpleStringProperty(FormatUtil.currency(c.getValue().getCanTraKhach())));
        colDaTra.setCellValueFactory(c  -> new SimpleStringProperty(FormatUtil.currency(c.getValue().getDaTraKhach())));
        colLyDo.setCellValueFactory(new PropertyValueFactory<>("lyDo"));

        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Xem");
            { btn.getStyleClass().add("btn-edit"); btn.setOnAction(e -> showDetail(getTableView().getItems().get(getIndex()))); }
            @Override protected void updateItem(Void v, boolean empty) { super.updateItem(v, empty); setGraphic(empty ? null : btn); }
        });

        loadData();
    }

    public void loadData() {
        try {
            List<TraHang> list = service.findAll();
            tblTH.setItems(FXCollections.observableArrayList(list));
            lblCount.setText(list.size() + " phiếu trả");
        } catch (Exception e) { AlertUtil.error("Lỗi", e.getMessage()); }
    }

    @FXML
    private void onTaoPhieu() {
        try {
            Parent form = FXMLLoader.load(getClass().getResource("/fxml/banhang/tra-hang-form.fxml"));
            ((StackPane) tblTH.getScene().getRoot().lookup("#contentArea")).getChildren().setAll(form);
        } catch (Exception e) { AlertUtil.error("Lỗi mở form", e.getMessage()); }
    }

    private void showDetail(TraHang th) {
        try {
            var ct = service.findChiTiet(th.getMaTra());
            StringBuilder sb = new StringBuilder("Phiếu: ").append(th.getMaTra())
                .append("  HĐ: ").append(th.getMaHD() != null ? th.getMaHD() : "—")
                .append("  KH: ").append(th.getTenKH()).append("\n\n");
            for (var c : ct)
                sb.append(String.format("%-10s %-30s SL:%3d  Giá: %s%n",
                    c.getMaSP(), c.getTenSP(), c.getSoLuong(), FormatUtil.currency(c.getDonGia())));
            sb.append("\nLý do: ").append(th.getLyDo());
            TextArea ta = new TextArea(sb.toString());
            ta.setEditable(false); ta.setPrefSize(500, 280);
            ta.setStyle("-fx-font-family:monospace;-fx-font-size:12px;");
            Stage s = new Stage(); s.setTitle("Chi tiết " + th.getMaTra());
            s.initModality(Modality.APPLICATION_MODAL);
            s.setScene(new Scene(ta)); s.setResizable(false); s.show();
        } catch (Exception e) { AlertUtil.error("Lỗi", e.getMessage()); }
    }
}
