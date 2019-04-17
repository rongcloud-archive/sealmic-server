package cn.rongcloud.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by weiqinxiao on 2019/2/28.
 */
@Data
@Component
@ConfigurationProperties(prefix = "cn.rongcloud.im")
public class IMProperties {
    private String appKey;
    private String secret;
    private String host;
}
