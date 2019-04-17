package cn.rongcloud.filter;

import cn.rongcloud.common.JwtTokenHelper;
import cn.rongcloud.common.JwtUser;
import cn.rongcloud.common.UserAgentTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Slf4j
@Component
public class JwtFilter extends GenericFilterBean {
    public static final String JWT_AUTH_DATA = "JWT_AUTH_DATA";
    public static final String USER_AGENT_TYPE = "USER_AGENT_TYPE";

    @Autowired
    private JwtTokenHelper tokenHelper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void doFilter(final ServletRequest req,
                         final ServletResponse res,
                         final FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) req;
        //HttpServletResponse httpRes = (HttpServletResponse) res; 
        log.debug("doFilter: " + httpReq.getRequestURL().toString());

        if (null == tokenHelper) {
            tokenHelper = (JwtTokenHelper) ((WebApplicationContext) WebApplicationContextUtils.getWebApplicationContext(httpReq.getServletContext())).getBean(JwtTokenHelper.class);
        }

        if (null == stringRedisTemplate) {
            stringRedisTemplate = (StringRedisTemplate) ((WebApplicationContext) WebApplicationContextUtils.getWebApplicationContext(httpReq.getServletContext())).getBean(StringRedisTemplate.class);
        }

        final String token = httpReq.getHeader("Authorization");
        if (token != null) {
            try {
                JwtUser user = tokenHelper.checkJwtToken(token, stringRedisTemplate);
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
        log.info("the request UA: {}", userAgent);
        if (null != userAgent) {
            UserAgentTypeEnum type = UserAgentTypeEnum.getEnumByUserAgent(userAgent);
            httpReq.setAttribute(USER_AGENT_TYPE, type);
        }

        chain.doFilter(req, res);
    }
}
