package cn.rongcloud.service.Impl;

import cn.rongcloud.common.*;
import cn.rongcloud.im.IMHelper;
import cn.rongcloud.pojo.IMTokenInfo;
import cn.rongcloud.pojo.LoginResult;
import cn.rongcloud.service.UserService;
import cn.rongcloud.dao.UserDao;
import cn.rongcloud.utils.DateTimeUtils;
import cn.rongcloud.utils.IdentifierUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Created by weiqinxiao on 2019/2/25.
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserDao userDao;

    @Autowired
    IMHelper imHelper;

    @Autowired
    JwtTokenHelper jwtTokenHelper;

    @Override
    public LoginResult login(String deviceId, JwtUser jwtUser) throws ApiException, Exception {
        log.info("login: {}, jwtUser={}", deviceId, jwtUser);
        LoginResult loginResult = new LoginResult();
        Date curDate = DateTimeUtils.currentUTC();
        String userId;
        String userName = curDate.getTime() + "";
        if (jwtUser == null) {
            userId = IdentifierUtils.uuid();
            jwtUser = new JwtUser();
            jwtUser.setUserId(userId);
            jwtUser.setUserName(userName);
            loginResult.setAuthorization(jwtTokenHelper.createJwtToken(jwtUser).getToken());
        } else {
            userId = jwtUser.getUserId();
            userName = jwtUser.getUserName();
        }
        IMTokenInfo tokenInfo = imHelper.getToken(userId, userName, "");
        if (tokenInfo.isSuccess()) {
            loginResult.setImToken(tokenInfo.getToken());
        } else {
            throw new ApiException(ErrorEnum.ERR_IM_TOKEN_ERROR, tokenInfo.getErrorMessage());
        }
        loginResult.setUserName(userName);
        loginResult.setUserId(userId);
        return loginResult;
    }

    @Override
    public String refreshToken(String userId, String name) throws ApiException, Exception  {
        log.info("refresh token: {}, {}", userId, name);
        IMTokenInfo tokenInfo = imHelper.getToken(userId, name, "");
        if (tokenInfo.isSuccess()) {
            return tokenInfo.getToken();
        } else {
            throw new ApiException(ErrorEnum.ERR_IM_TOKEN_ERROR, tokenInfo.getErrorMessage());
        }
    }
}
