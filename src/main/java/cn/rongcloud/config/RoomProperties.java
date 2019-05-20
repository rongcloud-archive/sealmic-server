package cn.rongcloud.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by weiqinxiao on 2019/2/28.
 */
@Data
@Component
@ConfigurationProperties(prefix = "cn.rongcloud.room")
public class RoomProperties {
    private int maxCount;
    private long delayTtl;
    private int posCount;
    private int maxMember;
    private long userIMOfflineKickTtl;
}
