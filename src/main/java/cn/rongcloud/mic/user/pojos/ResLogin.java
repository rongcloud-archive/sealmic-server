package cn.rongcloud.mic.user.pojos;

import lombok.Data;

/**
 * Created by sunyinglong on 2020/5/25.
 */
@Data
public class ResLogin {
    private String userId;
    private String userName;
    private String portrait;
    private Integer type;
    private String authorization;
    private String imToken;
}
