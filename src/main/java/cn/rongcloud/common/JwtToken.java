package cn.rongcloud.common;

import lombok.Data;

/**
 * Created by weiqinxiao on 2019/2/27.
 */
@Data
public class JwtToken {
    private String tokenId;
    private String userId;
    private String roomId;
    private String token;
    private long issuedTime;//UTC时间戳(ms)
    private long expiredTime;//UTC时间戳(ms)
}
