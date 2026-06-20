package com.nhom6.qlbh.util;

import com.nhom6.qlbh.model.DoanhThuNgay;
import com.nhom6.qlbh.model.PhanTichSanPham;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExcelUtil {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Xuất một bảng bất kỳ ra file .xlsx.
     * Header in đậm nền xanh, đóng băng dòng tiêu đề, tự co cột.
     */
    public static void exportSheet(String[] headers, List<String[]> rows,
                                   String sheetName, File file) throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet(sheetName);

            CellStyle hStyle = buildHeaderStyle(wb);
            CellStyle numStyle = buildNumStyle(wb);

            // Header row + freeze
            Row hRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell c = hRow.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(hStyle);
            }
            sheet.createFreezePane(0, 1);

            // Data rows
            for (int r = 0; r < rows.size(); r++) {
                Row row = sheet.createRow(r + 1);
                String[] data = rows.get(r);
                for (int col = 0; col < data.length; col++) {
                    String val = data[col] != null ? data[col] : "";
                    // Nếu giá trị là số thuần (không có dấu phân cách đặc biệt), ghi dạng số
                    try {
                        double num = Double.parseDouble(val.replace(",", ""));
                        Cell cell = row.createCell(col);
                        cell.setCellValue(num);
                        cell.setCellStyle(numStyle);
                    } catch (NumberFormatException e) {
                        row.createCell(col).setCellValue(val);
                    }
                }
            }

            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

            try (FileOutputStream out = new FileOutputStream(file)) {
                wb.write(out);
            }
        }
    }

    /** Xuất báo cáo phân tích (2 sheet: sản phẩm + doanh thu theo ngày) */
    public static void exportBaoCao(List<PhanTichSanPham> spList,
                                    List<DoanhThuNgay> doanhThuList,
                                    File file) throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            CellStyle hStyle   = buildHeaderStyle(wb);
            CellStyle numStyle = buildNumStyle(wb);

            // ---- Sheet 1: Phân tích sản phẩm ----
            Sheet sheet1 = wb.createSheet("Phân tích sản phẩm");
            String[] h1 = {"Mã SP", "Tên sản phẩm", "SL bán", "SL trả", "SL thực",
                           "Doanh thu (VNĐ)", "Lợi nhuận gộp (VNĐ)"};
            createHeaderRow(sheet1, h1, hStyle);
            sheet1.createFreezePane(0, 1);

            int rowIdx = 1;
            for (PhanTichSanPham sp : spList) {
                Row row = sheet1.createRow(rowIdx++);
                row.createCell(0).setCellValue(sp.getMaSP());
                row.createCell(1).setCellValue(sp.getTenSP());
                row.createCell(2).setCellValue(sp.getSoLuongBan());
                row.createCell(3).setCellValue(sp.getSoLuongTra());
                row.createCell(4).setCellValue(sp.getSoLuongThuc());
                Cell cDT = row.createCell(5); cDT.setCellValue(sp.getDoanhThu().doubleValue());     cDT.setCellStyle(numStyle);
                Cell cLN = row.createCell(6); cLN.setCellValue(sp.getLoiNhuanGop().doubleValue()); cLN.setCellStyle(numStyle);
            }
            for (int i = 0; i < h1.length; i++) sheet1.autoSizeColumn(i);

            // ---- Sheet 2: Doanh thu theo ngày ----
            Sheet sheet2 = wb.createSheet("Doanh thu theo ngày");
            String[] h2 = {"Ngày", "Doanh thu thuần (VNĐ)", "Số hóa đơn"};
            createHeaderRow(sheet2, h2, hStyle);
            sheet2.createFreezePane(0, 1);

            rowIdx = 1;
            for (DoanhThuNgay d : doanhThuList) {
                Row row = sheet2.createRow(rowIdx++);
                row.createCell(0).setCellValue(d.getNgay().format(DATE_FMT));
                Cell cDT = row.createCell(1); cDT.setCellValue(d.getDoanhThuThuan().doubleValue()); cDT.setCellStyle(numStyle);
                row.createCell(2).setCellValue(d.getSoHoaDon());
            }
            for (int i = 0; i < h2.length; i++) sheet2.autoSizeColumn(i);

            try (FileOutputStream out = new FileOutputStream(file)) {
                wb.write(out);
            }
        }
    }

    private static CellStyle buildHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        return style;
    }

    private static CellStyle buildNumStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        DataFormat df = wb.createDataFormat();
        style.setDataFormat(df.getFormat("#,##0"));
        return style;
    }

    private static void createHeaderRow(Sheet sheet, String[] headers, CellStyle style) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
    }
}
