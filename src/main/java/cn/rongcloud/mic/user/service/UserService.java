package cn.rongcloud.mic.user.service;


import cn.rongcloud.common.jwt.JwtUser;
import cn.rongcloud.mic.common.rest.RestResult;
import cn.rongcloud.mic.user.model.TUser;
import cn.rongcloud.mic.user.pojos.ReqLogin;
import cn.rongcloud.mic.user.pojos.ReqUserIds;
import cn.rongcloud.mic.user.pojos.ReqVisitorLogin;

/**
 * Created by sunyinglong on 2020/6/3
 */
public interface UserService {
    RestResult login(ReqLogin data);

    RestResult visitorLogin(ReqVisitorLogin loginData);

    RestResult refreshIMToken(JwtUser jwtUser);

    RestResult sendCode(String mobile);

    TUser getUserInfo(String uid);

    RestResult batchGetUsersInfo(ReqUserIds data, JwtUser jwtUser);

}
