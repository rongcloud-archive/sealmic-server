package cn.rongcloud.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "cn.rongcloud.jwt")
public class JwtProperties {
	private String secret;
	private Long ttlInMilliSec;
}
