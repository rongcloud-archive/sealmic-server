package cn.rongcloud.mic.user.controller;

import cn.rongcloud.common.jwt.filter.JwtFilter;
import cn.rongcloud.common.jwt.JwtUser;
import cn.rongcloud.common.utils.GsonUtil;
import cn.rongcloud.mic.common.rest.RestResult;
import cn.rongcloud.mic.user.pojos.ReqUserIds;
import cn.rongcloud.mic.user.pojos.ReqLogin;
import cn.rongcloud.mic.user.pojos.ReqSendCode;
import cn.rongcloud.mic.user.pojos.ReqVisitorLogin;
import cn.rongcloud.mic.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    UserService userService;

    /**
     * @api {post} /user/visitorLogin 游客登录
     * @apiVersion 1.0.0
     * @apiName visitorLogin
     * @apiGroup 用户模块
     * @apiParam {String} userName 用户名称
     * @apiParam {String} portrait 头像
     * @apiParam {String} deviceId 设备ID
     * @apiParamExample 请求参数示例:
     * POST /user/visitorLogin HTTP/1.1
     * Host: api-cn.ronghub.com
     * Content-Type: application/json;charset=UTF-8
     * {
     * 	   "userName": "秦时明月",
     * 	   "portrait":"http://xxx:xxx/portrait.png",
     * 	   "deviceId": "xxxxxxxx"
     * }
     * @apiSuccess (返回结果) {String} userId 用户ID
     * @apiSuccess (返回结果) {String} userName 用户名称
     * @apiSuccess (返回结果) {String} portrait 用户头像
     * @apiSuccess (返回结果) {String} imToken IM 连接 token
     * @apiSuccess (返回结果) {String} authorization 认证信息
     * @apiSuccess (返回结果) {String} type 用户类型，1 注册用户 0 游客
     * @apiSuccessExample {json} 服务响应示例:
     * {
     *      "code": 10000,
     *      "msg": "success",
     *      "data": {
     *           "userId": "2akJS6N5QOYsCKf5LhpgqY",
     *           "userName": "秦时明月",
     *           "portrait": "http://xxx:xxx/portrait.png",
     *           "imToken": "xxxxxx",
     *           "authorization": "xxxxxxx",
     *           "type": 0
     *      }
     * }
     */
    @PostMapping(value = "/visitorLogin")
    public RestResult visitorLogin(@RequestBody ReqVisitorLogin loginData) {
        log.info("visitor login: {}", GsonUtil.toJson(loginData));
        return userService.visitorLogin(loginData);
    }

    /**
     * @api {post} /user/refreshToken 刷新 token
     * @apiVersion 1.0.0
     * @apiName userRefreshToken
     * @apiGroup 用户模块
     * @apiUse token_msg
     * @apiParamExample 请求参数示例:
     * POST /user/refreshToken HTTP/1.1
     * Host: api-cn.ronghub.com
     * Authorization: authorization
     * Content-Type: application/json;charset=UTF-8
     * @apiSuccess (返回结果) {String} imToken IM 连接 token
     * @apiSuccessExample {json} 服务响应示例:
     * {
     *      "code": 10000,
     *      "msg": "success",
     *      "data": {
     *           "imToken": "xxxxxx"
     *      }
     * }
     */
    @PostMapping(value = "/refreshToken")
    public RestResult refreshToken(@RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser) {
        log.info("refreshToken, jwtUser: {}", GsonUtil.toJson(jwtUser));
        return userService.refreshIMToken(jwtUser);
    }

    /**
     * @api {post} /user/sendCode 发送短信验证码
     * @apiVersion 1.0.0
     * @apiName userSendCode
     * @apiGroup 用户模块
     * @apiParam {String} mobile 手机号
     * @apiParamExample 请求参数示例:
     * POST /user/sendCode HTTP/1.1
     * Host: api-cn.ronghub.com
     * Content-Type: application/json;charset=UTF-8
     * {
     * 	   "mobile": "13333333333"
     * }
     * @apiSuccessExample {json} 服务响应示例:
     * {
     *      "code": 10000,
     *      "msg": "success"
     * }
     */
    @PostMapping(value = "/sendCode")
    public RestResult sendCode(@RequestBody ReqSendCode data){
        log.info("sendCode, data: {}", GsonUtil.toJson(data));
        return userService.sendCode(data.getMobile());
    }

    /**
     * @api {post} /user/login 用户登录
     * @apiVersion 1.0.0
     * @apiName userLogin
     * @apiGroup 用户模块
     * @apiParam {String} mobile 手机号
     * @apiParam {String} verifyCode 验证码(开启短信验证时，必填)
     * @apiParam {String} userName 用户名称
     * @apiParam {String} portrait 头像
     * @apiParam {String} deviceId 设备ID
     * @apiParamExample 请求参数示例:
     * POST /user/login HTTP/1.1
     * Host: api-cn.ronghub.com
     * Content-Type: application/json;charset=UTF-8
     * {
     * 	   "mobile": "13300000000",
     * 	   "verifyCode": "1234",
     * 	   "userName": "秦时明月",
     *     "portrait": "http://xxx:xxx/portrait.png",
     *     "deviceId": "xxxxxxxx"
     * }
     * @apiSuccess (返回结果) {String} userId 用户ID
     * @apiSuccess (返回结果) {String} userName 用户名称
     * @apiSuccess (返回结果) {String} portrait 用户头像
     * @apiSuccess (返回结果) {String} imToken IM 连接 token
     * @apiSuccess (返回结果) {String} authorization 认证信息
     * @apiSuccess (返回结果) {String} type 用户类型，1 注册用户 0 游客
     * @apiSuccessExample {json} 服务响应示例:
     * {
     *      "code": 10000,
     *      "msg": "success",
     *      "data": {
     *           "userId": "2akJS6N5QOYsCKf5LhpgqY",
     *           "userName": "秦时明月",
     *           "portrait": "http://xxx:xxx/portrait.png",
     *           "imToken": "xxxxxx",
     *           "authorization": "xxxxxxx",
     *           "type": 1
     *      }
     * }
     */
    @PostMapping(value = "/login")
    public RestResult login(@RequestBody ReqLogin data) {
        log.info("user login: {}", GsonUtil.toJson(data));
        return userService.login(data);
    }

    /**
     * @api {post} /user/batch 批量获取用户信息
     * @apiVersion 1.0.0
     * @apiName batchGetUserInfo
     * @apiGroup 用户模块
     * @apiParam {Array} ids 用户id
     * @apiParamExample 请求参数示例:
     * POST /user/batch HTTP/1.1
     * Host: api-cn.ronghub.com
     * Content-Type: application/json;charset=UTF-8
     * {
     *   "userIds":["xxxx", "xxxxxxx"]
     * }
     * @apiSuccess (返回结果) {String} userId 用户ID
     * @apiSuccess (返回结果) {String} userName 用户名称
     * @apiSuccess (返回结果) {String} portrait 用户头像
     * @apiSuccessExample {json} 服务响应示例:
     * {
     *      "code": 10000,
     *      "msg": "success",
     *      "data": [{
     *           "userId": "2akJS6N5QOYsCKf5LhpgqY",
     *           "userName": "秦时明月",
     *           "portrait": "http://xxx:xxx/portrait.png"
     *      }]
     * }
     */
    @PostMapping(value = "/batch")
    public RestResult batch(@RequestBody ReqUserIds data, @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser) {
        log.info("batch get user, operator: {}, data:{}", jwtUser.getUserId(), GsonUtil.toJson(data));
        return userService.batchGetUsersInfo(data, jwtUser);
    }

}
