package cn.rongcloud.common.jwt.filter;

import cn.rongcloud.common.jwt.enums.UserAgentTypeEnum;
import cn.rongcloud.common.jwt.JwtTokenHelper;
import cn.rongcloud.common.jwt.JwtUser;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.GenericFilterBean;

/**
 * Created by sunyinglong on 2020/6/25
 */
@Slf4j
@Component
public class JwtFilter extends GenericFilterBean {
    public static final String JWT_AUTH_DATA = "JWT_AUTH_DATA";
    public static final String USER_AGENT_TYPE = "USER_AGENT_TYPE";

    @Autowired
    private JwtTokenHelper tokenHelper;

    @Override
    public void doFilter(final ServletRequest req,
                         final ServletResponse res,
                         final FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) req;

        log.debug("doFilter: " + httpReq.getRequestURL().toString());

        if (null == tokenHelper) {
            tokenHelper = ((WebApplicationContext) WebApplicationContextUtils.getWebApplicationContext(httpReq.getServletContext())).getBean(JwtTokenHelper.class);
        }

        final String token = httpReq.getHeader("Authorization");
        if (token != null) {
            try {
                JwtUser user = tokenHelper.checkJwtToken(token);
                if (null != user) {
                    httpReq.setAttribute(JWT_AUTH_DATA, user);
                }
            } catch (Exception e) {
                log.error("caught error when check token:", e);
            }
        } else {
            log.error("not found Authorization");
        }

        String userAgent = httpReq.getHeader("user-agent");
        log.info("the request IP:{}, UA: {}", getIpAddr(httpReq), userAgent);
        if (null != userAgent) {
            UserAgentTypeEnum type = UserAgentTypeEnum.getEnumByUserAgent(userAgent);
            httpReq.setAttribute(USER_AGENT_TYPE, type);
        }

        chain.doFilter(req, res);
    }

    public  String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1";
        }
        if (ip.split(",").length > 1) {
            ip = ip.split(",")[0];
        }
        return ip;
    }
}
