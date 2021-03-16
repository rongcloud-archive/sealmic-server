package cn.rongcloud.mic.user.service;

import cn.rongcloud.common.im.IMHelper;
import cn.rongcloud.common.im.pojos.IMTokenInfo;
import cn.rongcloud.common.jwt.JwtTokenHelper;
import cn.rongcloud.common.jwt.JwtUser;
import cn.rongcloud.mic.common.rest.RestException;
import cn.rongcloud.mic.common.rest.RestResult;
import cn.rongcloud.mic.common.rest.RestResultCode;
import cn.rongcloud.common.sms.SMSHelper;
import cn.rongcloud.common.sms.pojos.SMSCodeVerifyResult;
import cn.rongcloud.common.sms.pojos.SMSSendCodeResult;
import cn.rongcloud.common.sms.pojos.VerificationCodeInfo;
import cn.rongcloud.common.utils.DateTimeUtils;
import cn.rongcloud.common.utils.GsonUtil;
import cn.rongcloud.common.utils.IdentifierUtils;
import cn.rongcloud.mic.constant.CustomerConstant;
import cn.rongcloud.mic.user.dao.UserDao;
import cn.rongcloud.mic.user.enums.UserType;
import cn.rongcloud.mic.user.model.TUser;
import cn.rongcloud.mic.user.pojos.ReqLogin;
import cn.rongcloud.mic.user.pojos.ReqUserIds;
import cn.rongcloud.mic.user.pojos.ReqVisitorLogin;
import cn.rongcloud.mic.user.pojos.ResLogin;
import cn.rongcloud.mic.user.pojos.ResRefreshToken;
import cn.rongcloud.mic.room.pojos.RespRoomUser;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Created by sunyinglong on 2020/6/3
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserDao userDao;

    @Autowired
    IMHelper imHelper;

    @Autowired
    SMSHelper smsHelper;

    @Autowired
    JwtTokenHelper jwtTokenHelper;

    @Value("${sealmic.login.sms_verify}")
    private boolean loginSMSVerify;

    @Value("${sms.templateId}")
    private String smsTemplateId;

    @Resource
    RedisTemplate<String, Object> redisTemplate;

    @Override
    public RestResult login(ReqLogin data) {
        //检查手机号格式
        if (!isMobile(data.getMobile())) {
            return RestResult.generic(RestResultCode.ERR_USER_INVALID_PHONE_NUMBER);
        }

        Date curDate = DateTimeUtils.currentUTC();
        //根据手机号检查用户是否存在
        TUser user = userDao.findTUserByMobileEquals(data.getMobile());
        if (user == null) {
            user = new TUser();
            user.setUid(IdentifierUtils.uuid36());
            user.setName(data.getUserName());
            user.setMobile(data.getMobile());
            user.setDeviceId(data.getDeviceId());
            user.setPortrait(data.getPortrait());
            user.setType(UserType.USER.getValue());
            user.setCreateDt(curDate);
            user.setUpdateDt(curDate);
            //用户信息保存至数据库
            userDao.save(user);
        }

        //校验短信验证码是否开启
        if (loginSMSVerify) {
            if (data.getVerifyCode() == null) {
                throw new RestException(RestResult.generic(RestResultCode.ERR_USER_VERIFY_CODE_EMPTY));
            }
            RestResultCode result = verifyCode(data.getMobile(), data.getVerifyCode());
            if (result.getCode() != RestResultCode.ERR_SUCCESS.getCode()) {
                return RestResult.generic(result);
            }
        }

        //构建 jwtUser 对象
        JwtUser jwtUser = build(user);

        //封装登录返回结果
        ResLogin loginResult = build(jwtUser);

        return RestResult.success(loginResult);
    }

    @Override
    public RestResult visitorLogin(ReqVisitorLogin data) {
        TUser user = userDao.findTUserByDeviceIdEqualsAndTypeEquals(data.getDeviceId(), UserType.VISITOR.getValue());
        Date curDate = DateTimeUtils.currentUTC();
        if (user == null) {
            user = new TUser();
            user.setUid(IdentifierUtils.uuid36());
            user.setName(data.getUserName());
            user.setDeviceId(data.getDeviceId());
            user.setPortrait(data.getPortrait());
            user.setType(UserType.VISITOR.getValue());
            user.setCreateDt(curDate);
            user.setUpdateDt(curDate);
            //保存游客信息至数据库
            userDao.save(user);
        }

        //构建 jwtUser 对象
        JwtUser jwtUser = build(user);

        //封装登录返回结果
        ResLogin loginResult = build(jwtUser);

        return RestResult.success(loginResult);
    }

    private JwtUser build(TUser user) {
        JwtUser jwtUser = new JwtUser();
        jwtUser.setUserId(user.getUid());
        jwtUser.setUserName(user.getName());
        jwtUser.setPortrait(user.getPortrait());
        jwtUser.setType(user.getType());
        return jwtUser;
    }
    private ResLogin build(JwtUser jwtUser){
        ResLogin loginResult = new ResLogin();
        loginResult.setAuthorization(jwtTokenHelper.createJwtToken(jwtUser).getToken());
        loginResult.setImToken(getIMToken(jwtUser));
        loginResult.setUserName(jwtUser.getUserName());
        loginResult.setPortrait(jwtUser.getPortrait());
        loginResult.setType(jwtUser.getType());
        loginResult.setUserId(jwtUser.getUserId());
        return loginResult;
    }

    @Override
    public RestResult refreshIMToken(JwtUser jwtUser) {
        ResRefreshToken refreshToken = new ResRefreshToken();
        refreshToken.setImToken(refreshToken(jwtUser));
        return RestResult.success(refreshToken);
    }

    @Override
    public RestResult sendCode(String mobile) {
        if (!loginSMSVerify) {
            //TOTO 需要优化
            return RestResult.success();
        }
        return RestResult.generic(sendVerifyCode(mobile));
    }

    @Override
    public TUser getUserInfo(String uid) {
        if (StringUtils.isEmpty(uid)) {
            return null;
        }
        TUser user = (TUser) redisTemplate.opsForValue().get(getRedisUserInfoKey(uid));
        if (user == null) {
            user = userDao.findTUserByUidEquals(uid);
            log.info("get user from db, userId:{}", uid);
        }
        log.info("get user from redis, userId:{}", uid);
        if (user != null) {
            redisTemplate.opsForValue().set(getRedisUserInfoKey(uid), user);
        }
        return user;
    }

    @Override
    public RestResult batchGetUsersInfo(ReqUserIds data, JwtUser jwtUser) {

        List<RespRoomUser> outs = new ArrayList<>();
        for (String userId: data.getUserIds()) {
            RespRoomUser out = new RespRoomUser();
            TUser user =  getUserInfo(userId);
            if (user != null) {
                out.setUserId(userId);
                out.setUserName(user.getName());
                out.setPortrait(user.getPortrait());
                outs.add(out);
            }
        }
        return RestResult.success(outs);
    }

    public String getIMToken(JwtUser jwtUser) {

        //先从缓存查，token是否存在
        String token = (String) redisTemplate.opsForHash().get(getRedisUserIMTokenKey(), jwtUser.getUserId());
        if (token != null) {
            return token;
        }
        return refreshToken(jwtUser);
    }

    private String refreshToken(JwtUser jwtUser) {
        //从 IM 重新获取 token
        try {
            IMTokenInfo tokenInfo = imHelper.getToken(jwtUser.getUserId(), jwtUser.getUserName(), jwtUser.getPortrait());
            if (!tokenInfo.isSuccess()) {
                throw new RestException(RestResult.generic(RestResultCode.ERR_USER_IM_TOKEN_ERROR, tokenInfo.getErrorMessage()));
            }
            //将 IM token 放入 Redis 缓存
            redisTemplate.opsForHash().put(getRedisUserIMTokenKey(), jwtUser.getUserId(), tokenInfo.getToken());

            return tokenInfo.getToken();
        } catch (Exception e) {
            log.info("request token error, e:{}", e.getMessage());
        }
        return null;
    }

    private String getRedisUserInfoKey(String userId) {
        return CustomerConstant.SERVICENAME + "|user_info|" + userId;
    }

    private String getRedisUserIMTokenKey() {
        return CustomerConstant.SERVICENAME + "|user_im_token";
    }

    private RestResultCode sendVerifyCode(String mobile) {
        //检查手机号格式
        if (!isMobile(mobile)) {
            return RestResultCode.ERR_USER_INVALID_PHONE_NUMBER;
        }

        try {
            VerificationCodeInfo codeInfo = (VerificationCodeInfo) redisTemplate.opsForValue().get(getRedisSMSVerifyCodeKey(mobile));
            if (codeInfo != null) {
                Date now = new Date();
                // Todo Over frequency time should read from config
                if (now.getTime() - codeInfo.getCreateDt().getTime() < 60 * 1000) {
                    return RestResultCode.ERR_USER_SEND_CODE_OVER_FREQUENCY;
                }
            } else {
                codeInfo = new VerificationCodeInfo();
            }
            codeInfo.setCreateDt(new Date());

            String sessionId = null;
            String templateId = smsTemplateId;
            SMSSendCodeResult sendCodeResult = smsHelper.sendCode(mobile, templateId, "+86", null, null);
            if (sendCodeResult == null) {
                log.info("---sendCodeResult is null");
                return RestResultCode.ERR_USER_FAILURE_EXTERNAL;
            }
            if (sendCodeResult.getCode() == 200 && sendCodeResult.getSessionId() != null) {
                log.info("---sendCodeResult: " + GsonUtil.toJson(sendCodeResult));
                sessionId = sendCodeResult.getSessionId();
            } else {
                log.error("sms code error, code = {}, msg = {} ", sendCodeResult.getCode(), sendCodeResult.getErrorMessage());
                return RestResultCode.ERR_USER_FAILURE_EXTERNAL;
            }

            codeInfo.setSessionId(sessionId);
            redisTemplate.opsForValue().set(getRedisSMSVerifyCodeKey(mobile), codeInfo, 15, TimeUnit.MINUTES);
            return RestResultCode.ERR_SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return RestResultCode.ERR_USER_FAILURE_EXTERNAL;
        }
    }

    private RestResultCode verifyCode(String mobile, String code) {

        if (!isMobile(mobile)) {
            return RestResultCode.ERR_USER_INVALID_PHONE_NUMBER;
        }

        VerificationCodeInfo codeInfo = (VerificationCodeInfo) redisTemplate.opsForValue().get(getRedisSMSVerifyCodeKey(mobile));
        if (codeInfo == null) {
            return RestResultCode.ERR_USER_NOT_SEND_CODE;
        }

        SMSCodeVerifyResult codeResult = smsHelper.verifyCode(codeInfo.getSessionId(), code);
        if (!(codeResult.getCode() == 200 && codeResult.isSuccess())) {
            log.info("verify code error, sessionId = {}, code = {} , error = {}, msg = {}",
                codeInfo.getSessionId(), code, codeResult.getCode(), codeResult.getErrorMessage());
            return RestResultCode.ERR_USER_VERIFY_CODE_INVALID;
        }

        redisTemplate.delete(getRedisSMSVerifyCodeKey(mobile));

        return RestResultCode.ERR_SUCCESS;
    }

    private String getRedisSMSVerifyCodeKey(String mobile) {
        return CustomerConstant.SERVICENAME + "_sms_verifycode_" + mobile;
    }

    /**
     * 是否为手机号
     */
    public static boolean isMobile(String str) {
        final String regex = "^((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(17[013678])|(18[0,1,2,3,5-9]))\\d{8}$";
        //对比
        Pattern pat = Pattern.compile(regex.trim());
        Matcher mat = pat.matcher(str.trim());
        return mat.matches();
    }
}
