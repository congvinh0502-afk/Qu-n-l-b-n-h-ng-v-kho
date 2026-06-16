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

    public static void exportBaoCao(List<PhanTichSanPham> spList,
                                    List<DoanhThuNgay> doanhThuList,
                                    File file) throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            // ---- Style: header ----
            CellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            Font hFont = wb.createFont();
            hFont.setBold(true);
            hFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(hFont);

            // ---- Style: currency ----
            CellStyle currStyle = wb.createCellStyle();
            DataFormat df = wb.createDataFormat();
            currStyle.setDataFormat(df.getFormat("#,##0"));

            // ---- Sheet 1: Phân tích sản phẩm ----
            Sheet sheet1 = wb.createSheet("Phân tích sản phẩm");
            String[] headers1 = {"Mã SP", "Tên sản phẩm", "SL bán", "SL trả", "SL thực", "Doanh thu (VNĐ)", "Lợi nhuận gộp (VNĐ)"};
            createHeaderRow(sheet1, headers1, headerStyle);
            int rowIdx = 1;
            for (PhanTichSanPham sp : spList) {
                Row row = sheet1.createRow(rowIdx++);
                row.createCell(0).setCellValue(sp.getMaSP());
                row.createCell(1).setCellValue(sp.getTenSP());
                row.createCell(2).setCellValue(sp.getSoLuongBan());
                row.createCell(3).setCellValue(sp.getSoLuongTra());
                row.createCell(4).setCellValue(sp.getSoLuongThuc());
                Cell cDT = row.createCell(5);
                cDT.setCellValue(sp.getDoanhThu().doubleValue());
                cDT.setCellStyle(currStyle);
                Cell cLN = row.createCell(6);
                cLN.setCellValue(sp.getLoiNhuanGop().doubleValue());
                cLN.setCellStyle(currStyle);
            }
            for (int i = 0; i < headers1.length; i++) sheet1.autoSizeColumn(i);

            // ---- Sheet 2: Doanh thu theo ngày ----
            Sheet sheet2 = wb.createSheet("Doanh thu theo ngày");
            String[] headers2 = {"Ngày", "Doanh thu thuần (VNĐ)", "Số hóa đơn"};
            createHeaderRow(sheet2, headers2, headerStyle);
            rowIdx = 1;
            for (DoanhThuNgay d : doanhThuList) {
                Row row = sheet2.createRow(rowIdx++);
                row.createCell(0).setCellValue(d.getNgay().format(DATE_FMT));
                Cell cDT = row.createCell(1);
                cDT.setCellValue(d.getDoanhThuThuan().doubleValue());
                cDT.setCellStyle(currStyle);
                row.createCell(2).setCellValue(d.getSoHoaDon());
            }
            for (int i = 0; i < headers2.length; i++) sheet2.autoSizeColumn(i);

            try (FileOutputStream out = new FileOutputStream(file)) {
                wb.write(out);
            }
        }
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
