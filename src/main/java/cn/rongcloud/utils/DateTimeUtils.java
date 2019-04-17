package cn.rongcloud.utils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Date;

/**
 * Created by weiqinxiao on 2019/2/28.
 */
public class DateTimeUtils {
    public static Date currentUTC() {
        return new DateTime(DateTimeZone.UTC).toDate();
    }
}
