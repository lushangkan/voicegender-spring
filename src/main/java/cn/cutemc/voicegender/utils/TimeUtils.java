package cn.cutemc.voicegender.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtils {

    public static String formatTime(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        return dateFormat.format(date);
    }

}
