package cn.rongcloud.permission;

import cn.rongcloud.common.ApiException;
import cn.rongcloud.common.ErrorEnum;
import cn.rongcloud.common.JwtUser;
import cn.rongcloud.dao.RoomMemberDao;
import cn.rongcloud.filter.JwtFilter;
import cn.rongcloud.pojo.RoleEnum;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by weiqinxiao on 2019/2/26.
 */
@Slf4j
@Aspect
@Component
public class AuthChecker {
    @Autowired
    RoomMemberDao roomMemberDao;

    @Before("@annotation(permAnno)")
    public void checkPermission(JoinPoint joinPoint, DeclarePermissions permAnno) throws ApiException {
        log.debug("called checkPermission:" + joinPoint.getSignature());
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        JwtUser jwtUser = (JwtUser) request.getAttribute(JwtFilter.JWT_AUTH_DATA);
        if (null == jwtUser) {
            throw new ApiException(ErrorEnum.ERR_INVALID_AUTH);
        }
        //Authorization
        RoleEnum[] permEnums = permAnno.value();
        if (permEnums.length > 0) {//进行权限验证
            List<RoleEnum> perms = new LinkedList<>();
            for (RoleEnum permEnum : permEnums) {
                perms.add(RoleEnum.getEnumByValue(permEnum.getValue()));
            }
        }
    }
}
