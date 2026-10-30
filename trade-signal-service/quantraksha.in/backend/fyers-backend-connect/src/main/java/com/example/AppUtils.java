package com.example;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AppUtils {

    public static String getFormattedDate(Date date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }
}
