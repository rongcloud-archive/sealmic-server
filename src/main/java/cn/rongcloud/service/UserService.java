package cn.rongcloud.service;

import cn.rongcloud.common.ApiException;
import cn.rongcloud.common.JwtUser;
import cn.rongcloud.pojo.LoginResult;
import cn.rongcloud.pojo.UserInfo;

/**
 * Created by weiqinxiao on 2019/2/25.
 */
public interface UserService {
    public LoginResult login(String deviceId, JwtUser jwtUser) throws ApiException, Exception;

    public String refreshToken(String userId, String name) throws ApiException, Exception;
}
