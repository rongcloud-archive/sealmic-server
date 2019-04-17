package cn.rongcloud.controller;

import cn.rongcloud.common.ApiException;
import cn.rongcloud.common.BaseResponse;
import cn.rongcloud.common.JwtUser;
import cn.rongcloud.filter.JwtFilter;
import cn.rongcloud.pojo.LoginResult;
import cn.rongcloud.pojo.ReqLoginData;
import cn.rongcloud.pojo.ReqRoomData;
import cn.rongcloud.service.UserService;
import cn.rongcloud.utils.CheckUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by weiqinxiao on 2019/2/25.
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;

    @RequestMapping(value = "/refresh-token", method = RequestMethod.POST)
    public BaseResponse<String> refreshToken(@RequestBody ReqRoomData reqUserData,
                                             @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA, required = false) JwtUser jwtUser)
            throws ApiException, Exception {
        CheckUtils.checkArgument(jwtUser != null, "Invalid or expired authorization ");

        String token = userService.refreshToken(jwtUser.getUserId(), jwtUser.getUserName());
        BaseResponse<String> response = new BaseResponse<>();
        response.setData(token);
        return response;
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public BaseResponse<LoginResult> login(@RequestBody ReqLoginData loginData,
                                           @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA, required = false) JwtUser jwtUser)
            throws ApiException, Exception {
        LoginResult loginResult = userService.login(loginData.getDeviceId(), jwtUser);
        BaseResponse<LoginResult> response = new BaseResponse<>();
        response.setData(loginResult);
        return response;
    }
}
