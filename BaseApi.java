

/**
 * @api API 接口鉴权规则
 * @apiDescription SealMic Server 采用基于 token 的鉴权机制。客户端登录，服务端生成一个 Authorization 认证信息返回给客户端，每次请求 API 接口时，均需要在 HTTP Request Header 里面携带 Authorization 信息，具体如下：
 * @apiVersion 1.0.0
 * @apiName authenticationRule
 * @apiGroup 接口说明
 * @apiParam {String} Authorization 鉴权信息,  游客登录接口或用户登录接口下发的 authorization
 */

/**
 * @api API 错误码汇总
 * @apiVersion 1.0.0
 * @apiName errorCode
 * @apiGroup 接口说明
 * @apiParam {String} 10000 操作成功
 * @apiParam {String} 10001 系统内部错误
 * @apiParam {String} 10002 请求参数缺失或无效
 * @apiParam {String} 10003 认证信息无效或已过期
 * @apiParam {String} 10004 无权限操作
 * @apiParam {String} 10005 错误的请求
 * @apiParam {String} 20000 获取 IM Token 失败
 * @apiParam {String} 20001 发送短信请求过于频繁
 * @apiParam {String} 20002 短信发送失败
 * @apiParam {String} 20003 手机号无效
 * @apiParam {String} 20004 短信验证码尚未发送
 * @apiParam {String} 20005 短信验证码无效
 * @apiParam {String} 20006 验证码不能为空
 * @apiParam {String} 30000 房间创建失败
 * @apiParam {String} 30001 房间不存在
 * @apiParam {String} 30002 用户id个数不能超过 20
 * @apiParam {String} 30003 封禁用户失败
 * @apiParam {String} 30004 用户不在房间
 * @apiParam {String} 30005 用户已在麦位
 * @apiParam {String} 30006 用户已在排麦列表
 * @apiParam {String} 30007 用户没有申请排麦
 * @apiParam {String} 30008 用户不在麦位
 * @apiParam {String} 30009 没有可用麦位
 * @apiParam {String} 30010 您已是主持人
 * @apiParam {String} 30011 主持人转让信息已失效
 * @apiParam {String} 30012 禁言用户失败
 * @apiParam {String} 30013 接管主持人信息已失效
 * @apiParam {String} 40000 版本已存在
 * @apiParam {String} 40001 版本不存在
 * @apiParam {String} 40002 没有新版本
 */

/**
 * @apiDefine token_msg 全局配置token鉴权请求头
 * @apiHeader (鉴权请求头) {String}  Authorization 登录认证信息
 */
