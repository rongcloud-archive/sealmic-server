package cn.rongcloud.utils;

/**
 * Created by weiqinxiao on 2019/2/28.
 */
public class CheckUtils {
    public static void checkArgument(boolean condition, String message) throws IllegalArgumentException {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
