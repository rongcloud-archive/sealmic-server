package cn.rongcloud.common.utils;

/**
 * Created by sunyinglong on 2020/6/25
 */
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;
import org.apache.commons.lang.RandomStringUtils;

public class IdentifierUtils {

    private IdentifierUtils() {
        throw new IllegalStateException("IdentifierUtils Utility class");
    }

    public static String random(int length) {
        return RandomStringUtils.randomAlphanumeric(length);
    }

    public static String uuid() {
        return uuid24();
    }

    public static String uuid24() {
        UUID uuid = UUID.randomUUID();
        return base64Encode(uuid.getMostSignificantBits()) + base64Encode(
                uuid.getLeastSignificantBits());
    }

    public static String uuid32() {
        return uuid36().replace("-", "");
    }

    public static String uuid36() {
        return UUID.randomUUID().toString();
    }

    private static String base64Encode(long value) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES).putLong(value);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer.array());
    }

}