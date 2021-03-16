package cn.rongcloud.common.utils;

import java.util.Date;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * Created by sunyinglong on 2020/6/25
 */
public class DateTimeUtils {
    private DateTimeUtils() {}
    public static Date currentUTC() {
        return new DateTime(DateTimeZone.UTC).toDate();
    }
}
