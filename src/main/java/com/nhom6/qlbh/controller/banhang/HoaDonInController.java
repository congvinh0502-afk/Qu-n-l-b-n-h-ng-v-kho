package com.nhom6.qlbh.controller.banhang;

import com.nhom6.qlbh.model.ChiTietHD;
import com.nhom6.qlbh.model.HoaDon;
import com.nhom6.qlbh.util.AlertUtil;
import com.nhom6.qlbh.util.FormatUtil;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PageLayout;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HoaDonInController {

    @FXML private Label  lblMaHD;
    @FXML private VBox   printContainer;
    @FXML private Button btnIn;

    private Node invoiceNode;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter D_FMT  = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void setData(HoaDon hd, List<ChiTietHD> chiTiet) {
        lblMaHD.setText(hd.getMaHD());
        invoiceNode = buildInvoice(hd, chiTiet);
        printContainer.getChildren().setAll(invoiceNode);
    }

    // ── BUILD INVOICE NODE ───────────────────────────────────────────────────
    private Node buildInvoice(HoaDon hd, List<ChiTietHD> chiTiet) {
        VBox invoice = new VBox(0);
        invoice.setPadding(new Insets(30));
        invoice.setStyle("-fx-background-color:white;" +
                         "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.18),10,0,2,3);");
        invoice.setPrefWidth(620);
        invoice.setMaxWidth(620);

        // ── Header ───────────────────────────────────────────────────────────
        VBox header = new VBox(3);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0, 0, 12, 0));
        header.getChildren().addAll(
            lbl("CỬA HÀNG QUẢN LÝ BÁN HÀNG & KHO",
                "-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#1565c0;"),
            lbl("Nhóm 6 – Đồ án Cơ sở dữ liệu",
                "-fx-font-size:10px;-fx-text-fill:#757575;"),
            lbl("HÓA ĐƠN BÁN HÀNG",
                "-fx-font-size:18px;-fx-font-weight:bold;-fx-padding:8 0 2 0;"),
            lbl("Số: " + hd.getMaHD(),
                "-fx-font-size:11px;-fx-text-fill:#616161;")
        );
        invoice.getChildren().addAll(header, sep());

        // ── Info ─────────────────────────────────────────────────────────────
        GridPane info = new GridPane();
        info.setHgap(12); info.setVgap(6);
        info.setPadding(new Insets(10, 0, 10, 0));
        info.getColumnConstraints().addAll(
            fixedCol(120), fixedCol(190), fixedCol(80), fixedCol(180));

        String dateStr = hd.getThoiGian() != null ? hd.getThoiGian().format(DT_FMT) : "—";
        addInfoRow(info, 0, "Khách hàng:", hd.getTenKH(),       "Ngày:",        dateStr);
        addInfoRow(info, 1, "Nhân viên:",  hd.getTenNV(),       "Trạng thái:",  hd.getTrangThaiLabel());
        invoice.getChildren().addAll(info, sep());

        // ── Products table ───────────────────────────────────────────────────
        GridPane tbl = new GridPane();
        tbl.setPadding(new Insets(8, 0, 8, 0));
        // STT | TenSP | SL | DonGia | ThanhTien
        tbl.getColumnConstraints().addAll(
            fixedCol(36), growCol(220), fixedCol(42), fixedCol(115), fixedCol(127));

        String[] thdr = {"#", "Tên sản phẩm", "SL", "Đơn giá (đ)", "Thành tiền (đ)"};
        for (int i = 0; i < thdr.length; i++) {
            Label l = lbl(thdr[i],
                "-fx-font-weight:bold;-fx-font-size:11px;-fx-padding:5 6 5 6;" +
                "-fx-background-color:#e3f2fd;");
            l.setMaxWidth(Double.MAX_VALUE);
            GridPane.setFillWidth(l, true);
            l.setAlignment(i >= 2 ? Pos.CENTER_RIGHT : (i == 0 ? Pos.CENTER : Pos.CENTER_LEFT));
            tbl.add(l, i, 0);
        }

        for (int r = 0; r < chiTiet.size(); r++) {
            ChiTietHD ct = chiTiet.get(r);
            String bg = r % 2 == 0 ? "white" : "#fafafa";
            String[] vals = {
                String.valueOf(r + 1),
                ct.getTenSP(),
                String.valueOf(ct.getSoLuong()),
                FormatUtil.currency(ct.getDonGia()),
                FormatUtil.currency(ct.getThanhTien())
            };
            for (int c = 0; c < vals.length; c++) {
                Label l = lbl(vals[c],
                    "-fx-font-size:11px;-fx-padding:4 6 4 6;-fx-background-color:" + bg + ";");
                l.setMaxWidth(Double.MAX_VALUE);
                l.setAlignment(c >= 2 ? Pos.CENTER_RIGHT : (c == 0 ? Pos.CENTER : Pos.CENTER_LEFT));
                tbl.add(l, c, r + 1);
            }
        }
        invoice.getChildren().addAll(tbl, sep());

        // ── Summary ───────────────────────────────────────────────────────────
        VBox summary = new VBox(5);
        summary.setPadding(new Insets(8, 0, 8, 0));
        addSumRow(summary, "Tổng tiền hàng:",  FormatUtil.currency(hd.getTongTienHang())   + " đ", false);
        if (hd.getGiamGia().compareTo(BigDecimal.ZERO) > 0)
            addSumRow(summary, "Giảm giá:",     "- " + FormatUtil.currency(hd.getGiamGia()) + " đ", false);
        addSumRow(summary, "Tổng sau giảm:",   FormatUtil.currency(hd.getTongSauGiamGia()) + " đ", true);
        addSumRow(summary, "Đã thanh toán:",   FormatUtil.currency(hd.getDaThanhToan())    + " đ", false);
        if (hd.getConNo().compareTo(BigDecimal.ZERO) > 0)
            addSumRow(summary, "Còn nợ:",       FormatUtil.currency(hd.getConNo())          + " đ", false);
        invoice.getChildren().addAll(summary, sep());

        // ── Footer ────────────────────────────────────────────────────────────
        HBox footer = new HBox();
        footer.setPadding(new Insets(14, 0, 0, 0));

        VBox footLeft = new VBox(3);
        footLeft.getChildren().addAll(
            lbl("Ngày in: " + LocalDate.now().format(D_FMT),
                "-fx-font-size:10px;-fx-text-fill:#757575;"),
            lbl("Phần mềm: QLBH Nhóm 6",
                "-fx-font-size:9px;-fx-text-fill:#bdbdbd;")
        );

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox footRight = new VBox(0);
        footRight.setAlignment(Pos.CENTER);
        footRight.getChildren().addAll(
            lbl("Người bán hàng",
                "-fx-font-size:10px;-fx-font-weight:bold;"),
            lbl("(Ký và ghi rõ họ tên)",
                "-fx-font-size:9px;-fx-text-fill:#9e9e9e;"),
            lbl(hd.getTenNV(),
                "-fx-font-size:10px;-fx-text-fill:#424242;-fx-padding:28 0 0 0;")
        );

        footer.getChildren().addAll(footLeft, spacer, footRight);
        invoice.getChildren().add(footer);

        return invoice;
    }

    // ── ACTION HANDLERS ──────────────────────────────────────────────────────
    @FXML
    private void onIn() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) {
            AlertUtil.error("Lỗi máy in",
                "Không tìm thấy máy in.\n" +
                "Trên Windows: cài 'Microsoft Print to PDF' để xuất PDF.");
            return;
        }
        Stage stage = (Stage) btnIn.getScene().getWindow();
        if (!job.showPrintDialog(stage)) return;

        // Scale invoice to fit printable area if needed
        PageLayout layout = job.getJobSettings().getPageLayout();
        double pw = layout.getPrintableWidth();
        double nw = invoiceNode.getBoundsInLocal().getWidth();
        Scale scale = null;
        if (nw > 0 && nw > pw) {
            scale = new Scale(pw / nw, pw / nw);
            invoiceNode.getTransforms().add(scale);
        }

        boolean ok = job.printPage(invoiceNode);
        if (scale != null) invoiceNode.getTransforms().remove(scale);

        if (ok) {
            job.endJob();
            AlertUtil.info("Thành công", "Đã gửi lệnh in / lưu PDF thành công.");
        } else {
            AlertUtil.error("Lỗi in", "Không in được. Vui lòng thử lại.");
        }
    }

    @FXML
    private void onDong() {
        ((Stage) btnIn.getScene().getWindow()).close();
    }

    // ── HELPERS ──────────────────────────────────────────────────────────────
    private static Label lbl(String text, String style) {
        Label l = new Label(text); l.setStyle(style); return l;
    }

    private static Region sep() {
        Region r = new Region();
        r.setPrefHeight(1); r.setMaxWidth(Double.MAX_VALUE);
        r.setStyle("-fx-background-color:#e0e0e0;");
        VBox.setMargin(r, new Insets(4, 0, 4, 0));
        return r;
    }

    private static ColumnConstraints fixedCol(double w) {
        ColumnConstraints c = new ColumnConstraints(w); return c;
    }

    private static ColumnConstraints growCol(double pref) {
        ColumnConstraints c = new ColumnConstraints();
        c.setPrefWidth(pref); c.setHgrow(Priority.ALWAYS); return c;
    }

    private static void addInfoRow(GridPane grid, int row,
                                   String k1, String v1, String k2, String v2) {
        grid.add(lbl(k1, "-fx-font-weight:bold;-fx-font-size:11px;"), 0, row);
        grid.add(lbl(v1, "-fx-font-size:11px;"),                       1, row);
        grid.add(lbl(k2, "-fx-font-weight:bold;-fx-font-size:11px;"), 2, row);
        grid.add(lbl(v2, "-fx-font-size:11px;"),                       3, row);
    }

    private static void addSumRow(VBox box, String label, String value, boolean bold) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_RIGHT);
        String weight = bold ? "-fx-font-weight:bold;" : "";
        Label lk = lbl(label, "-fx-font-size:11px;-fx-min-width:150px;-fx-alignment:CENTER_RIGHT;" +
                               "-fx-padding:0 8 0 0;" + weight);
        Label lv = lbl(value, "-fx-font-size:" + (bold ? "12" : "11") + "px;" +
                               "-fx-min-width:130px;-fx-alignment:CENTER_RIGHT;" + weight);
        row.getChildren().addAll(lk, lv);
        box.getChildren().add(row);
    }
}
