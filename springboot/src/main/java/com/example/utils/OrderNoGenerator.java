package com.example.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class OrderNoGenerator {
    public static String generate() {
        String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String random = String.format("%06d", new Random().nextInt(999999));
        return "RC" + dateStr + random;
    }
}
