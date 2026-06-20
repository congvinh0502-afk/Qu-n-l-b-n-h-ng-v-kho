package com.nhom6.qlbh.controller.hanghoa;

import com.nhom6.qlbh.model.LoaiSanPham;
import com.nhom6.qlbh.model.SanPham;
import com.nhom6.qlbh.service.SanPhamService;
import com.nhom6.qlbh.dao.LoaiSanPhamDAO;
import com.nhom6.qlbh.util.AlertUtil;
import com.nhom6.qlbh.util.ExcelUtil;
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
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SanPhamController {

    @FXML private TextField txtSearch;
    @FXML private ComboBox<LoaiSanPham> cboLoai;
    @FXML private CheckBox chkSapHet;
    @FXML private Button btnThem;
    @FXML private Button btnXuatExcel;
    @FXML private TableView<SanPham> tblSanPham;
    @FXML private TableColumn<SanPham, String>  colMaSP;
    @FXML private TableColumn<SanPham, String>  colTenSP;
    @FXML private TableColumn<SanPham, String>  colLoai;
    @FXML private TableColumn<SanPham, String>  colGiaVon;
    @FXML private TableColumn<SanPham, String>  colGiaBan;
    @FXML private TableColumn<SanPham, Integer> colTonKho;
    @FXML private TableColumn<SanPham, Integer> colMucTon;
    @FXML private TableColumn<SanPham, String>  colTrangThai;
    @FXML private TableColumn<SanPham, Void>    colAction;
    @FXML private Label lblCount;

    private final SanPhamService service = new SanPhamService();
    private final LoaiSanPhamDAO loaiDAO = new LoaiSanPhamDAO();

    @FXML
    public void initialize() {
        setupColumns();
        loadCategories();
        loadData();
    }

    private void setupColumns() {
        tblSanPham.setEditable(true);

        colMaSP.setCellValueFactory(new PropertyValueFactory<>("maSP"));
        colTenSP.setCellValueFactory(new PropertyValueFactory<>("tenSP"));
        colLoai.setCellValueFactory(new PropertyValueFactory<>("tenLoai"));
        colGiaVon.setCellValueFactory(c -> new SimpleStringProperty(FormatUtil.currency(c.getValue().getGiaVon())));
        colGiaBan.setCellValueFactory(c -> new SimpleStringProperty(FormatUtil.currency(c.getValue().getGiaBan())));
        colTonKho.setCellValueFactory(new PropertyValueFactory<>("tonKho"));

        // Cột Mức tối thiểu — có thể sửa trực tiếp
        colMucTon.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getMucTonToiThieu()).asObject());
        colMucTon.setEditable(true);
        colMucTon.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colMucTon.setOnEditCommit(event -> {
            SanPham sp = event.getRowValue();
            Integer newVal = event.getNewValue();
            if (newVal == null || newVal < 0) {
                AlertUtil.warn("Giá trị không hợp lệ", "Mức tồn tối thiểu phải >= 0.");
                tblSanPham.refresh();
                return;
            }
            try {
                service.updateMucTon(sp.getMaSP(), newVal);
                sp.setMucTonToiThieu(newVal);
            } catch (Exception e) {
                AlertUtil.error("Lỗi cập nhật", e.getMessage());
            }
            tblSanPham.refresh();
        });

        colTrangThai.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTrangThaiLabel()));
        colTrangThai.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setText(null); setStyle(""); return; }
                setText(val);
                setStyle("Đang KD".equals(val)
                    ? "-fx-text-fill: #16a34a; -fx-font-weight: bold;"
                    : "-fx-text-fill: #dc2626; -fx-font-weight: bold;");
            }
        });

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
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });

        // Tô màu dòng sắp hết tồn kho
        tblSanPham.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(SanPham sp, boolean empty) {
                super.updateItem(sp, empty);
                if (empty || sp == null) {
                    setStyle("");
                } else if (sp.getTonKho() == 0) {
                    setStyle("-fx-background-color: #ffcdd2;");
                } else if (sp.isSapHet()) {
                    setStyle("-fx-background-color: #fff3e0;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    private void loadCategories() {
        try {
            List<LoaiSanPham> list = loaiDAO.findAll();
            LoaiSanPham all = new LoaiSanPham(0, "— Tất cả —");
            list.add(0, all);
            cboLoai.setItems(FXCollections.observableArrayList(list));
            cboLoai.getSelectionModel().selectFirst();
        } catch (Exception e) {
            AlertUtil.error("Lỗi tải danh mục", e.getMessage());
        }
    }

    private void loadData() {
        try {
            List<SanPham> list;
            if (chkSapHet != null && chkSapHet.isSelected()) {
                list = service.findSapHet();
            } else {
                list = service.findAll();
            }
            tblSanPham.setItems(FXCollections.observableArrayList(list));
            lblCount.setText(list.size() + " sản phẩm");
        } catch (Exception e) {
            AlertUtil.error("Lỗi tải dữ liệu", e.getMessage());
        }
    }

    @FXML
    private void onSearch() {
        try {
            List<SanPham> list;
            if (chkSapHet.isSelected()) {
                list = service.findSapHet();
            } else {
                String kw = txtSearch.getText();
                LoaiSanPham sel = cboLoai.getValue();
                Integer maLoai = (sel == null || sel.getMaLoai() == 0) ? null : sel.getMaLoai();
                list = service.search(kw, maLoai);
            }
            tblSanPham.setItems(FXCollections.observableArrayList(list));
            lblCount.setText(list.size() + " sản phẩm");
        } catch (Exception e) {
            AlertUtil.error("Lỗi tìm kiếm", e.getMessage());
        }
    }

    @FXML
    private void onThem() {
        openDialog(null, true);
    }

    private void openDialog(SanPham sp, boolean isNew) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/hanghoa/san-pham-dialog.fxml"));
            Parent root = loader.load();
            SanPhamDialogController ctrl = loader.getController();
            ctrl.setSanPham(sp, isNew);

            Stage stage = new Stage();
            stage.setTitle(isNew ? "Thêm sản phẩm" : "Sửa sản phẩm");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

            if (ctrl.isSaved()) loadData();
        } catch (Exception e) {
            AlertUtil.error("Lỗi mở form", e.getMessage());
        }
    }

    @FXML
    private void onXuatExcel() {
        List<SanPham> items = tblSanPham.getItems();
        if (items == null || items.isEmpty()) {
            AlertUtil.warn("Không có dữ liệu", "Bảng hiện tại không có dữ liệu để xuất."); return;
        }
        FileChooser fc = new FileChooser();
        fc.setTitle("Lưu file Excel");
        fc.setInitialFileName("hang-hoa-" + LocalDate.now() + ".xlsx");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"));
        File file = fc.showSaveDialog(btnXuatExcel.getScene().getWindow());
        if (file == null) return;
        try {
            String[] headers = {"Mã SP", "Tên sản phẩm", "Loại", "Giá vốn", "Giá bán",
                                "Tồn kho", "Mức tối thiểu", "Trạng thái"};
            List<String[]> rows = new ArrayList<>();
            for (SanPham sp : items) {
                rows.add(new String[]{
                    sp.getMaSP(), sp.getTenSP(), sp.getTenLoai(),
                    FormatUtil.currency(sp.getGiaVon()), FormatUtil.currency(sp.getGiaBan()),
                    String.valueOf(sp.getTonKho()), String.valueOf(sp.getMucTonToiThieu()),
                    sp.getTrangThaiLabel()
                });
            }
            ExcelUtil.exportSheet(headers, rows, "Hàng hóa", file);
            AlertUtil.info("Xuất thành công", "Đã xuất " + items.size() + " sản phẩm:\n" + file.getAbsolutePath());
        } catch (Exception e) {
            AlertUtil.error("Lỗi xuất Excel", e.getMessage());
        }
    }

    private void onXoa(SanPham sp) {
        Optional<ButtonType> res = AlertUtil.confirm(
            "Xác nhận xóa",
            "Xóa sản phẩm \"" + sp.getTenSP() + "\"?");
        if (res.isEmpty() || res.get() != ButtonType.OK) return;
        try {
            service.xoa(sp.getMaSP());
            loadData();
        } catch (Exception e) {
            AlertUtil.error("Không thể xóa", e.getMessage());
        }
    }
}
