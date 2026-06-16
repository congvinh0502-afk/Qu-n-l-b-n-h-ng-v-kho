package com.nhom6.qlbh.controller.nhacc;

import com.nhom6.qlbh.model.NhaCungCap;
import com.nhom6.qlbh.service.NhaCungCapService;
import com.nhom6.qlbh.util.AlertUtil;
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

public class NhaCungCapController {

    @FXML private TextField txtSearch;
    @FXML private TableView<NhaCungCap> tblNhaCungCap;
    @FXML private TableColumn<NhaCungCap, String> colMaNCC;
    @FXML private TableColumn<NhaCungCap, String> colTenNCC;
    @FXML private TableColumn<NhaCungCap, String> colDienThoai;
    @FXML private TableColumn<NhaCungCap, String> colDiaChi;
    @FXML private TableColumn<NhaCungCap, Void>   colAction;
    @FXML private Label lblCount;

    private final NhaCungCapService service = new NhaCungCapService();

    @FXML
    public void initialize() {
        colMaNCC.setCellValueFactory(new PropertyValueFactory<>("maNCC"));
        colTenNCC.setCellValueFactory(new PropertyValueFactory<>("tenNCC"));
        colDienThoai.setCellValueFactory(new PropertyValueFactory<>("dienThoai"));
        colDiaChi.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getDiaChi()));

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
            List<NhaCungCap> list = service.findAll();
            tblNhaCungCap.setItems(FXCollections.observableArrayList(list));
            lblCount.setText(list.size() + " nhà cung cấp");
        } catch (Exception e) {
            AlertUtil.error("Lỗi tải dữ liệu", e.getMessage());
        }
    }

    @FXML private void onSearch() {
        try {
            List<NhaCungCap> list = service.search(txtSearch.getText());
            tblNhaCungCap.setItems(FXCollections.observableArrayList(list));
            lblCount.setText(list.size() + " nhà cung cấp");
        } catch (Exception e) {
            AlertUtil.error("Lỗi tìm kiếm", e.getMessage());
        }
    }

    @FXML private void onThem() { openDialog(null, true); }

    private void openDialog(NhaCungCap ncc, boolean isNew) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/nhacc/nha-cung-cap-dialog.fxml"));
            Parent root = loader.load();
            NhaCungCapDialogController ctrl = loader.getController();
            ctrl.setNhaCungCap(ncc, isNew);

            Stage stage = new Stage();
            stage.setTitle(isNew ? "Thêm nhà cung cấp" : "Sửa nhà cung cấp");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

            if (ctrl.isSaved()) loadData();
        } catch (Exception e) {
            AlertUtil.error("Lỗi mở form", e.getMessage());
        }
    }

    private void onXoa(NhaCungCap ncc) {
        Optional<ButtonType> res = AlertUtil.confirm("Xác nhận xóa",
            "Xóa nhà cung cấp \"" + ncc.getTenNCC() + "\"?");
        if (res.isEmpty() || res.get() != ButtonType.OK) return;
        try {
            service.xoa(ncc.getMaNCC());
            loadData();
        } catch (Exception e) {
            AlertUtil.error("Không thể xóa", e.getMessage());
        }
    }
}
