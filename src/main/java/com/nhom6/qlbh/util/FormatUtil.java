package com.nhom6.qlbh.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class FormatUtil {

    private static final NumberFormat CURRENCY =
        NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    static {
        CURRENCY.setMinimumFractionDigits(0);
        CURRENCY.setMaximumFractionDigits(0);
    }

    public static String currency(BigDecimal value) {
        if (value == null) return "0";
        return CURRENCY.format(value);
    }

    public static String currency(long value) {
        return CURRENCY.format(value);
    }

    public static BigDecimal parseCurrency(String text) {
        if (text == null || text.isBlank()) return BigDecimal.ZERO;
        try {
            String clean = text.replaceAll("[^0-9]", "");
            return new BigDecimal(clean);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}
