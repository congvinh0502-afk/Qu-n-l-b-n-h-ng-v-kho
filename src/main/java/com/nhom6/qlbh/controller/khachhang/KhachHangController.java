package com.nhom6.qlbh.controller.khachhang;

import com.nhom6.qlbh.model.KhachHang;
import com.nhom6.qlbh.service.KhachHangService;
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
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

public class KhachHangController {

    @FXML private TextField txtSearch;
    @FXML private TableView<KhachHang> tblKhachHang;
    @FXML private TableColumn<KhachHang, String> colMaKH;
    @FXML private TableColumn<KhachHang, String> colTenKH;
    @FXML private TableColumn<KhachHang, String> colDienThoai;
    @FXML private TableColumn<KhachHang, String> colTongBan;
    @FXML private TableColumn<KhachHang, String> colTongBanTruTra;
    @FXML private TableColumn<KhachHang, Void>   colAction;
    @FXML private Label lblCount;

    private final KhachHangService service = new KhachHangService();

    @FXML
    public void initialize() {
        colMaKH.setCellValueFactory(new PropertyValueFactory<>("maKH"));
        colTenKH.setCellValueFactory(new PropertyValueFactory<>("tenKH"));
        colDienThoai.setCellValueFactory(new PropertyValueFactory<>("dienThoai"));
        colTongBan.setCellValueFactory(c -> new SimpleStringProperty(FormatUtil.currency(c.getValue().getTongBan())));
        colTongBanTruTra.setCellValueFactory(c -> new SimpleStringProperty(FormatUtil.currency(c.getValue().getTongBanTruTra())));

        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("Sửa");
            private final Button btnDel  = new Button("Xóa");
            private final HBox box = new HBox(6, btnEdit, btnDel);
            {
                btnEdit.getStyleClass().add("btn-edit");
                btnDel.getStyleClass().add("btn-delete");
                btnEdit.setOnAction(e -> openDialog(getTableView().getItems().get(getIndex()), false));
                btnDel.setOnAction(e -> onXoa(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });

        loadData();
    }

    private void loadData() {
        try {
            List<KhachHang> list = service.findAll();
            tblKhachHang.setItems(FXCollections.observableArrayList(list));
            lblCount.setText(list.size() + " khách hàng");
        } catch (Exception e) {
            AlertUtil.error("Lỗi tải dữ liệu", e.getMessage());
        }
    }

    @FXML private void onSearch() {
        try {
            List<KhachHang> list = service.search(txtSearch.getText());
            tblKhachHang.setItems(FXCollections.observableArrayList(list));
            lblCount.setText(list.size() + " khách hàng");
        } catch (Exception e) {
            AlertUtil.error("Lỗi tìm kiếm", e.getMessage());
        }
    }

    @FXML private void onThem() { openDialog(null, true); }

    private void openDialog(KhachHang kh, boolean isNew) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/khachhang/khach-hang-dialog.fxml"));
            Parent root = loader.load();
            KhachHangDialogController ctrl = loader.getController();
            ctrl.setKhachHang(kh, isNew);

            Stage stage = new Stage();
            stage.setTitle(isNew ? "Thêm khách hàng" : "Sửa khách hàng");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

            if (ctrl.isSaved()) loadData();
        } catch (Exception e) {
            AlertUtil.error("Lỗi mở form", e.getMessage());
        }
    }

    private void onXoa(KhachHang kh) {
        Optional<ButtonType> res = AlertUtil.confirm("Xác nhận xóa",
            "Xóa khách hàng \"" + kh.getTenKH() + "\"?");
        if (res.isEmpty() || res.get() != ButtonType.OK) return;
        try {
            service.xoa(kh.getMaKH());
            loadData();
        } catch (Exception e) {
            AlertUtil.error("Không thể xóa", e.getMessage());
        }
    }
}
