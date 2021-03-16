package cn.rongcloud.mic.room.controller;

import cn.rongcloud.common.jwt.filter.JwtFilter;
import cn.rongcloud.common.jwt.JwtUser;
import cn.rongcloud.common.utils.GsonUtil;
import cn.rongcloud.mic.common.rest.RestResult;
import cn.rongcloud.mic.room.pojos.ReqBroadcastMessage;
import cn.rongcloud.mic.room.pojos.ReqMicStateSet;
import cn.rongcloud.mic.room.pojos.ReqRoomCreate;
import cn.rongcloud.mic.room.pojos.ReqRoomId;
import cn.rongcloud.mic.room.pojos.ReqRoomMicAccept;
import cn.rongcloud.mic.room.pojos.ReqRoomMicInvite;
import cn.rongcloud.mic.room.pojos.ReqRoomMicKick;
import cn.rongcloud.mic.room.pojos.ReqRoomMicReject;
import cn.rongcloud.mic.room.pojos.ReqRoomSetting;
import cn.rongcloud.mic.room.pojos.ReqRoomStatusSync;
import cn.rongcloud.mic.room.pojos.ReqRoomUserGag;
import cn.rongcloud.mic.room.pojos.ReqRoomUserKick;
import cn.rongcloud.mic.room.pojos.ReqTakeOverHost;
import cn.rongcloud.mic.room.pojos.ReqTransferHost;
import cn.rongcloud.mic.room.service.RoomService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/room")
@Slf4j
public class RoomController {

    @Autowired
    RoomService roomService;

    /**
     * @api {post} /room/create 创建房间
     * @apiDescription 访问权限: 只有登录用户才可访问，游客无权限访问该接口
     * @apiVersion 1.0.0
     * @apiName createRoom
     * @apiGroup 房间模块
     * @apiUse token_msg
     * @apiParam {String} name 房间名称
     * @apiParam {String} themePictureUrl 房间主题图片地址
     * @apiParamExample 请求参数示例:
     * POST /room/create HTTP/1.1
     * Host: api-cn.ronghub.com
     * Authorization: authorization
     * Content-Type: application/json;charset=UTF-8
     * {
     * 	   "name": "test",
     * 	   "themePictureUrl": "xxxx"
     * }
     * @apiSuccess (返回结果) {String} roomId 房间id
     * @apiSuccess (返回结果) {String} roomName 房间名称
     * @apiSuccess (返回结果) {String} themePictureUrl 房间主题图片地址
     * @apiSuccess (返回结果) {Timestamp} createDt 创建时间
     * @apiSuccessExample {json} 服务响应示例:
     * {
     *      "code": 10000,
     *      "msg": "success",
     *      "data": {
     *           "roomId": "2akJS6N5QOYsCKf5LhpgqY",
     *           "roomName": "房间名称",
     *           "themePictureUrl": "xxxxx",
     *           "createDt": 1555406087939
     *      }
     * }
     */
    @PostMapping(value = "/create")
    public RestResult createRoom(@RequestBody ReqRoomCreate data, @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser)
        throws Exception {
        log.info("create room, operator:{}, data:{}", jwtUser.getUserId(), GsonUtil.toJson(data));
        return roomService.createRoom(data, jwtUser);
    }

    /**
     * @api {get} /room/list 获取房间列表
     * @apiVersion 1.0.0
     * @apiName getRoomList
     * @apiGroup 房间模块
     * @apiUse token_msg
     * @apiParam {String} fromRoomId 起始位置(Query 参数，需拼接到 url 之后), 初始查询参数值可以为空
     * @apiParam {Int} size 返回记录数(Query 参数，需拼接到 url 之后)
     * @apiParamExample 请求参数示例:
     * GET /room/list?fromRoomId=xxxxxxxx&size=10 HTTP/1.1
     * Host: api-cn.ronghub.com
     * Authorization: authorization
     * Content-Type: application/json;charset=UTF-8
     * @apiSuccess (返回结果) {Int} totalCount 总记录数
     * @apiSuccess (返回结果) {Array} room 房间信息
     * @apiSuccess (房间信息) {String} roomId 房间id
     * @apiSuccess (房间信息) {String} roomName 房间名称
     * @apiSuccess (房间信息) {String} themePictureUrl 房间主题图片地址
     * @apiSuccess (房间信息) {boolean} allowedFreeJoinMic 是否允许观众自由上麦
     * @apiSuccess (房间信息) {boolean} allowedJoinRoom 是否用户加入房间
     * @apiSuccess (房间信息) {Timestamp} updateDt 更新时间
     * @apiSuccessExample {json} 服务响应示例:
     * {
     * 	"code": 10000,
     * 	"msg": "success",
     * 	"data": {
     * 		"totalCount": 2,
     * 		"rooms": [{
     * 			"roomId": "2akJS6N5QOYsCKf5LhpgqY",
     * 			"roomName": "名称1",
     * 			"themePictureUrl": "xxxxxx",
     * 			"allowedJoinRoom": true,
     * 		    "allowedFreeJoinMic": true,
     * 			"updateDt": 1555406087939
     *                }, {
     * 			"roomId": "3saDkSLFMdnsseOksdakJS6",
     * 			"roomName": "名称2",
     * 			"themePictureUrl": "xxxxxx",
     *          "allowedJoinRoom": true,
     * 		    "allowedFreeJoinMic": true,
     * 			"updateDt": 1555406087939
     *        }]
     *    }
     * }
     */
    @GetMapping(value = "/list")
    public RestResult getRoomList(@RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser,
        @RequestParam(value = "fromRoomId") String fromRoomId, @RequestParam(value = "size", defaultValue = "10") Integer size){
        log.info("get room list, operator:{}, from:{}, size:{}", jwtUser.getUserId(), fromRoomId, size);
        return roomService.getRoomList(fromRoomId, size);
    }

    /**
     * @api {get} /room/{id} 获取房间信息
     * @apiVersion 1.0.0
     * @apiName getRoomDetail
     * @apiGroup 房间模块
     * @apiUse token_msg
     * @apiParam {Int} roomId 房间id (Path 参数，需替换 url 地址中的 {roomId} 变量)
     * @apiParamExample 请求参数示例:
     * GET /room/xxxxxx HTTP/1.1
     * Host: api-cn.ronghub.com
     * Authorization: authorization
     * Content-Type: application/json;charset=UTF-8
     * @apiSuccess (返回结果) {String} roomId 房间id
     * @apiSuccess (返回结果) {String} roomName 房间名称
     * @apiSuccess (返回结果) {String} themePictureUrl 房间主题图片地址
     * @apiSuccess (房间信息) {boolean} allowedFreeJoinMic 是否允许观众自由上麦
     * @apiSuccess (房间信息) {boolean} allowedJoinRoom 是否用户加入房间
     * @apiSuccess (房间信息) {Timestamp} updateDt 更新时间
     * @apiSuccessExample {json} 服务响应示例:
     * {
     * 	"code": 10000,
     * 	"msg": "success",
     * 	"data": {
     * 			"roomId": "3saDkSLFMdnsseOksdakJS6",
     * 			"roomName": "名称2",
     * 			"themePictureUrl": "xxxxxx",
     *          "allowedJoinRoom": true,
     * 		    "allowedFreeJoinMic": true,
     * 			"updateDt": 1555406087939
     *      }
     * }
     */
    @GetMapping(value = "/{roomId}")
    public RestResult getRoomDetail(@PathVariable("roomId") String roomId, @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser){
        log.info("get room info, operator:{}, roomId:{}", jwtUser.getUserId(),roomId);
        return roomService.getRoomDetail(roomId);
    }

    /**
     * @api {put} /room/setting 房间设置
     * @apiDescription 访问权限: 只有主持人有权限操作
     * @apiVersion 1.0.0
     * @apiName roomSetting
     * @apiGroup 房间模块
     * @apiUse token_msg
     * @apiParam {String} roomId 房间id
     * @apiParam {boolean} allowedJoinRoom 是否允许观众加入
     * @apiParam {boolean} allowedFreeJoinMic 是否允许观众自由上麦
     * @apiParamExample 请求参数示例:
     * PUT /room/setting HTTP/1.1
     * Host: api-cn.ronghub.com
     * Authorization: authorization
     * Content-Type: application/json;charset=UTF-8
     * {
     *       "roomId": "xxxxxxx",
     *       "allowedJoinRoom": true,
     *       "allowedFreeJoinMic": true
     * }
     * @apiSuccess (返回结果) {Int} code 返回码，10000 为正常。
     * @apiSuccessExample {json} 服务响应示例:
     * {
     *      "code": 10000,
     *      "msg": "success"
     * }
     */
    @PutMapping(value = "/setting")
    public RestResult roomSetting(@RequestBody ReqRoomSetting data, @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser){
        log.info("room setting, operator:{}, data:{}", jwtUser.getUserId(), GsonUtil.toJson(data));
        return roomService.roomSetting(data, jwtUser);
    }

    /**
     * @api {get} /room/{roomId}/members 获取房间成员列表
     * @apiVersion 1.0.0
     * @apiName roomMembers
     * @apiGroup 房间模块
     * @apiUse token_msg
     * @apiParam {Int} roomId 房间id (Path 参数，需替换 url 地址中的 {roomId} 变量)
     * @apiParamExample 请求参数示例:
     * GET /room/setting/xxxxxxx/members HTTP/1.1
     * Host: api-cn.ronghub.com
     * Authorization: authorization
     * Content-Type: application/json;charset=UTF-8
     * @apiSuccess (返回结果) {String} userId 用户id
     * @apiSuccess (返回结果) {String} userName 用户名称
     * @apiSuccess (返回结果) {String} portrait 头像
     * @apiSuccessExample {json} 服务响应示例:
     * {
     * 	"code": 10000,
     * 	"msg": "success",
     * 	"data": [{
     * 				"userId": "2akJS6N5QOYsCKf5LhpgqY",
     * 			    "userName": "李晓明",
     * 			    "portrait": "xxxxxxxx"
     *                        },
     *            {
     * 				"userId": "sIl1nG5AQD8h-O7A2zlN5Q",
     * 				"userName": "张三",
     * 				"portrait": "xxxxx"
     *            }
     * 		]
     * }
     */
    @GetMapping(value = "/{roomId}/members")
    public RestResult getRoomMembers(@PathVariable("roomId") String roomId, @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser)
        throws Exception {
        log.info("get room members, operator:{}, roomId:{}", jwtUser.getUserId(),roomId);
        return roomService.getRoomMembers(roomId);
    }

    /**
     * @api {get} /room/{roomId}/mic/apply/members 获取排麦成员
     * @apiVersion 1.0.0
     * @apiName roomApplyMicMembers
     * @apiGroup 房间模块
     * @apiUse token_msg
     * @apiParam {String} roomId 房间id (Path 参数，需替换 url 地址中的 {roomId} 变量)
     * @apiParamExample 请求参数示例:
     * GET /room/xxxxxx/mic/apply/members HTTP/1.1
     * Host: api-cn.ronghub.com
     * Authorization: authorization
     * Content-Type: application/json;charset=UTF-8
     * @apiSuccess (返回结果) {String} userId 用户id
     * @apiSuccess (返回结果) {String} userName 用户名称
     * @apiSuccess (返回结果) {String} portrait 头像
     * @apiSuccessExample {json} 服务响应示例:
     * {
     * 	"code": 10000,
     * 	"msg": "success",
     * 	"data": [{
     * 				"userId": "2akJS6N5QOYsCKf5LhpgqY",
     *      	    "userName": "李晓明",
     *              "portrait": "xxxxxxxx"
     *            },
     *            {
     * 				"userId": "sIl1nG5AQD8h-O7A2zlN5Q",
     * 			    "userName": "张三",
     * 				"portrait": "xxxxx"
     *            }
     * 		]
     * }
     */
    @GetMapping(value = "/{roomId}/mic/apply/members")
    public RestResult getMicApplyMembers(@PathVariable("roomId") String roomId, @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser){
        log.info("get mic apply members, operator:{}, data:{}", jwtUser.getUserId(), roomId);
        return roomService.getMicApplyMembers(roomId, jwtUser);

    }

    /**
     * @api {post} /room/gag 用户禁言设置
     * @apiDescription 访问权限: 只有主持人有权限操作
     * @apiVersion 1.0.0
     * @apiName roomUserGag
     * @apiGroup 房间模块
     * @apiUse token_msg
     * @apiParam {String} roomId 房间id
     * @apiParam {String} operation 操作，add:禁言, remove:解除禁言
     * @apiParam {Array} userIds 用户id
     * @apiParamExample 请求参数示例:
     * POST /room/xxxxxxx/user/gag HTTP/1.1
     * Host: api-cn.ronghub.com
     * Authorization: authorization
     * {
     *     "roomId": "xxxxxxx",
     *     "userIds": ["xxxxxxx","yyyyyyy"],
     *     "operation": "add"
     * }
     * @apiSuccess (返回结果) {Int} code 返回码，10000 为正常。
     * @apiSuccessExample {json} 服务响应示例:
     * {
     *      "code": 10000,
     *      "msg": "success"
     * }
     */
    @PostMapping(value = "/gag")
    public RestResult roomUserGag(@RequestBody ReqRoomUserGag data, @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser)
        throws Exception {
        log.info("room user gag, operator:{}, data:{}", jwtUser.getUserId(), GsonUtil.toJson(data));
        return roomService.roomUserGag(data, jwtUser);
    }

    /**
     * @api {get} /room/{roomId}/gag/members 查询禁言用户列表
     * @apiDescription 访问权限: 只有主持人有权限操作
     * @apiVersion 1.0.0
     * @apiName queryGagRoomUsers
     * @apiGroup 房间模块
     * @apiUse token_msg
     * @apiParam {String} roomId 房间id，(Path 参数，需替换 url 地址中的 {roomId} 变量)
     * @apiParam {Array} userIds 用户id
     * @apiParamExample 请求参数示例:
     * GET /room/xxxxxx/gag/members HTTP/1.1
     * Host: api-cn.ronghub.com
     * Authorization: authorization
     * @apiSuccess (返回结果) {String} userId 用户id
     * @apiSuccess (返回结果) {String} userName 用户名称
     * @apiSuccess (返回结果) {String} portrait 头像
     * @apiSuccessExample {json} 服务响应示例:
     * {
     * 	"code": 10000,
     * 	"msg": "success",
     * 	"data": [{
     * 				"userId": "2akJS6N5QOYsCKf5LhpgqY",
     * 			    "userName": "李晓明",
     * 			    "portrait": "xxxxxxxx"
     *                        },
     *            {
     * 				"userId": "sIl1nG5AQD8h-O7A2zlN5Q",
     * 				"userName": "张三",
     * 				"portrait": "xxxxx"
     *            }
     * 		]
     * }
     */
    @GetMapping(value = "/{roomId}/gag/members")
    public RestResult queryGagRoomUsers(@PathVariable("roomId") String roomId, @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser) {
        log.info("query room gag users , operator:{}, roomId:{}", jwtUser.getUserId(), roomId);
        return roomService.queryGagRoomUsers(roomId, jwtUser);
    }


    /**
     * @api {post} /room/kick 将用户踢出房间
     * @apiDescription 访问权限: 只有主持人有权限操作
     * @apiVersion 1.0.0
     * @apiName roomUserRemove
     * @apiGroup 房间模块
     * @apiUse token_msg
     * @apiParam {String} roomId 房间id
     * @apiParam {Array} userIds 用户id
     * @apiParamExample 请求参数示例:
     * POST /room/kick HTTP/1.1
     * Host: api-cn.ronghub.com
     * Authorization: authorization
     * {
     *     "roomId": "xxxxxx",
     *     "userIds":["xxxxxxx","yyyyyyy"]
     * }
     * @apiSuccess (返回结果) {Int} code 返回码，10000 为正常。
     * @apiSuccessExample {json} 服务响应示例:
     * {
     *      "code": 10000,
     *      "msg": "success"
     * }
     */
    @PostMapping(value = "/kick")
    public RestResult roomUserKick(@RequestBody ReqRoomUserKick data, @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser)
        throws Exception {
        log.info("kick room user , operator:{}, data:{}", jwtUser.getUserId(), GsonUtil.toJson(data));
        return roomService.roomUserKick(data, jwtUser);
    }

    /**
     * @api {post} /room/mic/apply/accept 同意用户上麦
     * @apiDescription 访问权限: 只有主持人有权限操作
     * @apiVersion 1.0.0
     * @apiName roomMicAccept
     * @apiGroup 麦位模块
     * @apiUse token_msg
     * @apiParam {String} roomId 房间id
     * @apiParam {String} userId 用户id
     * @apiParamExample 请求参数示例:
     * POST /room/xxxxxxx/mic/apply/accept HTTP/1.1
     * Host: api-cn.ronghub.com
     * Authorization: authorization
     * {
     *     "roomId":"xxxxx",
     *     "userId":"xxxxx"
     * }
     * @apiSuccess (返回结果) {Int} code 返回码，10000 为正常。
     * @apiSuccessExample {json} 服务响应示例:
     * {
     *      "code": 10000,
     *      "msg": "success"
     * }
     */
    @PostMapping(value = "/mic/apply/accept")
    public RestResult roomMicAccept(@RequestBody ReqRoomMicAccept data, @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser)
        throws Exception {
        log.info("accept mic apply, operator:{}, data:{}", jwtUser.getUserId(), GsonUtil.toJson(data));
        return roomService.roomMicAccept(data, jwtUser);
    }

    /**
     * @api {post} /room/mic/apply/reject 拒绝用户上麦
     * @apiDescription 访问权限: 只有主持人有权限操作
     * @apiVersion 1.0.0
     * @apiName roomMicReject
     * @apiGroup 麦位模块
     * @apiUse token_msg
     * @apiParam {String} roomId 房间id
     * @apiParam {String} userId 用户id
     * @apiParamExample 请求参数示例:
     * POST /room/xxxxxxx/mic/apply/reject HTTP/1.1
     * Host: api-cn.ronghub.com
     * Authorization: authorization
     * {
     *     "roomId": "xxxxx",
     *     "userId":"xxxxx"
     * }
     * @apiSuccess (返回结果) {Int} code 返回码，10000 为正常。
     * @apiSuccessExample {json} 服务响应示例:
     * {
     *      "code": 10000,
     *      "msg": "success"
     * }
     */
    @PostMapping(value = "/mic/apply/reject")
    public RestResult roomMicReject(@RequestBody ReqRoomMicReject data, @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser)
        throws Exception {
        log.info("reject mic apply, operator:{}, data:{}", jwtUser.getUserId(), GsonUtil.toJson(data));
        return roomService.roomMicReject(data, jwtUser);
    }


    /**
     * @api {put} /room/mic/state 麦位状态设置
     * @apiDescription 访问权限: 主持人、主播，主持人可以设置所有麦位状态，主播只可设置自己麦位状态
     * @apiVersion 1.0.0
     * @apiName roomMicChange
     * @apiGroup 麦位模块
     * @apiUse token_msg
     * @apiParam {String} roomId 房间id
     * @apiParam {Int} state 麦位状态, 0:正常, 1:麦位锁定, 2:闭麦
     * @apiParam {Int} position 操作目标麦位
     * @apiParamExample 请求参数示例:
     * PUT /room/xxxxxxx/mic/change HTTP/1.1
     * Host: api-cn.ronghub.com
     * Authorization: authorization
     * Content-Type: application/json;charset=UTF-8
     * {
     *     "roomId":  "xxxxx",
     * 	   "state": 0,
     * 	   "position": 0
     * }
     * @apiSuccess (返回结果) {Int} code 返回码，10000 为正常。
     * @apiSuccessExample {json} 服务响应示例:
     * {
     *      "code": 10000,
     *      "msg": "success"
     * }
     */
    @PutMapping(value = "/mic/state")
    public RestResult setMicState(@RequestBody ReqMicStateSet data, @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser)
        throws Exception {
        log.info("set mic state, operator:{}, data:{}", jwtUser.getUserId(), GsonUtil.toJson(data));
        return roomService.setMicState(data, jwtUser);
    }

    /**
     * @api {post} /room/mic/invite 邀请用户连麦
     * @apiDescription 访问权限: 只有主持人有权限操作
     * @apiVersion 1.0.0
     * @apiName roomMicInvite
     * @apiGroup 麦位模块
     * @apiUse token_msg
     * @apiParam {Int} roomId 房间id
     * @apiParam {String} userId 用户id
     * @apiParamExample 请求参数示例:
     * POST /room/mic/invite HTTP/1.1
     * Host: api-cn.ronghub.com
     * Authorization: authorization
     * Content-Type: application/json;charset=UTF-8
     * {
     *     "roomId:"xxxxx",
     *     "userId":"xxxxx"
     * }
     * @apiSuccess (返回结果) {Int} code 返回码，10000 为正常。
     * @apiSuccessExample {json} 服务响应示例:
     * {
     *      "code": 10000,
     *      "msg": "success"
     * }
     */
    @PostMapping(value = "/mic/invite")
    public RestResult micInvite(@RequestBody ReqRoomMicInvite data, @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser)
        throws Exception {
        log.info("mic invite, operator:{}, data:{}", jwtUser.getUserId(), GsonUtil.toJson(data));
        return roomService.micInvite(data, jwtUser);
    }

    /**
     * @api {post} /room/mic/kick 踢用户下麦
     * @apiDescription 访问权限: 只有主持人有权限操作
     * @apiVersion 1.0.0
     * @apiName roomMicKick
     * @apiGroup 麦位模块
     * @apiUse token_msg
     * @apiParam {String} roomId 房间id
     * @apiParam {String} userId 用户id
     * @apiParamExample 请求参数示例:
     * POST /room/mic/kick HTTP/1.1
     * Host: api-cn.ronghub.com
     * Authorization: authorization
     * Content-Type: application/json;charset=UTF-8
     * {
     *     "roomId":"xxxxx",
     *     "userId":"xxxxx"
     * }
     * @apiSuccess (返回结果) {Int} code 返回码，10000 为正常。
     * @apiSuccessExample {json} 服务响应示例:
     * {
     *      "code": 10000,
     *      "msg": "success"
     * }
     */
    @PostMapping(value = "/mic/kick")
    public RestResult micKick(@RequestBody ReqRoomMicKick data, @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser)
        throws Exception {
        log.info("mic kick, operator:{}, data:{}", jwtUser.getUserId(), GsonUtil.toJson(data));
        return roomService.micKick(data, jwtUser);
    }

    /**
     * @api {post} /room/mic/quit 主播下麦
     * @apiDescription 访问权限: 主持人、主播
     * @apiVersion 1.0.0
     * @apiName roomMicQuit
     * @apiGroup 麦位模块
     * @apiUse token_msg
     * @apiParam {String} roomId 房间id
     * @apiParamExample 请求参数示例:
     * POST /room/mic/quit HTTP/1.1
     * Host: api-cn.ronghub.com
     * Authorization: authorization
     * Content-Type: application/json;charset=UTF-8
     * {
     *     "roomId":"xxxxx"
     * }
     * @apiSuccess (返回结果) {Int} code 返回码，10000 为正常。
     * @apiSuccessExample {json} 服务响应示例:
     * {
     *      "code": 10000,
     *      "msg": "success"
     * }
     */
    @PostMapping(value = "/mic/quit")
    public RestResult micQuit(@RequestBody ReqRoomId data, @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser)
        throws Exception {
        log.info("quit mic, operator:{}, data:{}", jwtUser.getUserId(), data.getRoomId());
        return roomService.micQuit(data, jwtUser);
    }

    /**
     * @api {post} /room/mic/takeOverHost 接管主持人
     * @apiVersion 1.0.0
     * @apiName takeOverHost
     * @apiGroup 麦位模块
     * @apiUse token_msg
     * @apiParam {String} roomId 房间id
     * @apiParamExample 请求参数示例:
     * POST /room/mic/takeOverHost HTTP/1.1
     * Host: api-cn.ronghub.com
     * Authorization: authorization
     * Content-Type: application/json;charset=UTF-8
     * {
     *     "roomId":"xxxxx"
     * }
     * @apiSuccess (返回结果) {Int} code 返回码，10000 为正常。
     * @apiSuccessExample {json} 服务响应示例:
     * {
     *      "code": 10000,
     *      "msg": "success"
     * }
     */
    @PostMapping(value = "/mic/takeOverHost")
    public RestResult takeOverHost(@RequestBody ReqRoomId data, @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser)
        throws Exception {
        log.info("takeOverHost, operator:{}, data:{}", jwtUser.getUserId(), data.getRoomId());
        return roomService.takeOverHost(data, jwtUser);
    }

    /**
     * @api {post} /room/mic/takeOverHost/accept 主持人同意接管
     * @apiDescription 访问权限: 只有主持人有权限操作
     * @apiVersion 1.0.0
     * @apiName takeOverHostAccept
     * @apiGroup 麦位模块
     * @apiUse token_msg
     * @apiParam {String} roomId 房间id
     * @apiParam {String} userId 用户id
     * @apiParamExample 请求参数示例:
     * POST /room/mic/takeOverHost/accept HTTP/1.1
     * Host: api-cn.ronghub.com
     * Authorization: authorization
     * Content-Type: application/json;charset=UTF-8
     * {
     *     "roomId":"xxxxx",
     *     "userId":"xxxx"
     * }
     * @apiSuccess (返回结果) {Int} code 返回码，10000 为正常。
     * @apiSuccessExample {json} 服务响应示例:
     * {
     *      "code": 10000,
     *      "msg": "success"
     * }
     */
    @PostMapping(value = "/mic/takeOverHost/accept")
    public RestResult takeOverHostAccept(@RequestBody ReqTakeOverHost data, @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser)
        throws Exception {
        log.info("accept takeOverHost, operator:{}, data:{}", jwtUser.getUserId(), GsonUtil.toJson(data));
        return roomService.acceptTakeOverHost(data, jwtUser);
    }

    /**
     * @api {post} /room/mic/takeOverHost/reject 主持人拒绝接管
     * @apiDescription 访问权限: 只有主持人有权限操作
     * @apiVersion 1.0.0
     * @apiName takeOverHostReject
     * @apiGroup 麦位模块
     * @apiUse token_msg
     * @apiParam {String} roomId 房间id
     * @apiParamExample 请求参数示例:
     * POST /room/mic/takeOverHost/reject HTTP/1.1
     * Host: api-cn.ronghub.com
     * Authorization: authorization
     * Content-Type: application/json;charset=UTF-8
     * {
     *     "roomId":"xxxxx",
     *     "userId":"xxxxx"
     * }
     * @apiSuccess (返回结果) {Int} code 返回码，10000 为正常。
     * @apiSuccessExample {json} 服务响应示例:
     * {
     *      "code": 10000,
     *      "msg": "success"
     * }
     */
    @PostMapping(value = "/mic/takeOverHost/reject")
    public RestResult takeOverHostReject(@RequestBody ReqTakeOverHost data, @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser)
        throws Exception {
        log.info("reject takeOverHost, operator:{}, data:{}", jwtUser.getUserId(), data.getRoomId());
        return roomService.rejectTakeOverHost(data, jwtUser);
    }

    /**
     * @api {post} /room/mic/transferHost 转让主持人
     * @apiDescription 访问权限: 只有主持人有权限操作
     * @apiVersion 1.0.0
     * @apiName transferHost
     * @apiGroup 麦位模块
     * @apiUse token_msg
     * @apiParam {String} roomId 房间id
     * @apiParamExample 请求参数示例:
     * POST /room/mic/transferHost HTTP/1.1
     * Host: api-cn.ronghub.com
     * Authorization: authorization
     * Content-Type: application/json;charset=UTF-8
     * {
     *     "roomId":"xxxxx",
     *     "userId":"xxxxx",
     * }
     * @apiSuccess (返回结果) {Int} code 返回码，10000 为正常。
     * @apiSuccessExample {json} 服务响应示例:
     * {
     *      "code": 10000,
     *      "msg": "success"
     * }
     */
    @PostMapping(value = "/mic/transferHost")
    public RestResult transferHost(@RequestBody ReqTransferHost data, @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser)
        throws Exception {
        log.info("transferHost, operator:{}, data:{}", jwtUser.getUserId(), GsonUtil.toJson(data));
        return roomService.transferHost(data, jwtUser);
    }

    /**
     * @api {post} /room/mic/transferHost/accept 转让主持人同意
     * @apiDescription 访问权限: 主播
     * @apiVersion 1.0.0
     * @apiName transferHostAccept
     * @apiGroup 麦位模块
     * @apiUse token_msg
     * @apiParam {String} roomId 房间id
     * @apiParamExample 请求参数示例:
     * POST /room/mic/transferHost/accept HTTP/1.1
     * Host: api-cn.ronghub.com
     * Authorization: authorization
     * Content-Type: application/json;charset=UTF-8
     * {
     *     "roomId":"xxxxx"
     * }
     * @apiSuccess (返回结果) {Int} code 返回码，10000 为正常。
     * @apiSuccessExample {json} 服务响应示例:
     * {
     *      "code": 10000,
     *      "msg": "success"
     * }
     */
    @PostMapping(value = "/mic/transferHost/accept")
    public RestResult transferHostAccept(@RequestBody ReqRoomId data, @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser)
        throws Exception {
        log.info("accept transferHost, operator:{}, data:{}", jwtUser.getUserId(), data.getRoomId());
        return roomService.acceptTransferHost(data, jwtUser);
    }

    /**
     * @api {post} /room/mic/transferHost/reject 转让主持人拒绝
     * @apiDescription 访问权限: 主播
     * @apiVersion 1.0.0
     * @apiName transferHostReject
     * @apiGroup 麦位模块
     * @apiUse token_msg
     * @apiParam {String} roomId 房间id
     * @apiParamExample 请求参数示例:
     * POST /room/mic/transferHost/reject HTTP/1.1
     * Host: api-cn.ronghub.com
     * Authorization: authorization
     * Content-Type: application/json;charset=UTF-8
     * {
     *     "roomId":"xxxxx"
     * }
     * @apiSuccess (返回结果) {Int} code 返回码，10000 为正常。
     * @apiSuccessExample {json} 服务响应示例:
     * {
     *      "code": 10000,
     *      "msg": "success"
     * }
     */
    @PostMapping(value = "/mic/transferHost/reject")
    public RestResult transferHostReject(@RequestBody ReqRoomId data, @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser)
        throws Exception {
        log.info("reject transferHost, operator:{}, data:{}", jwtUser.getUserId(), data.getRoomId());
        return roomService.rejectTransferHost(data, jwtUser);
    }

    /**
     * @api {post} /room/mic/apply 申请排麦
     * @apiDescription 访问权限: 只有观众有权限操作
     * @apiVersion 1.0.0
     * @apiName roomMicApply
     * @apiGroup 麦位模块
     * @apiUse token_msg
     * @apiParam {String} roomId 房间id
     * @apiParamExample 请求参数示例:
     * POST /room/mic/apply HTTP/1.1
     * Host: api-cn.ronghub.com
     * Authorization: authorization
     * Content-Type: application/json;charset=UTF-8
     * {
     *     "roomId":"xxxxx"
     * }
     * @apiSuccess (返回结果) {Int} code 返回码，10000 为正常。
     * @apiSuccessExample {json} 服务响应示例:
     * {
     *      "code": 10000,
     *      "msg": "success"
     * }
     */
    @PostMapping(value = "/mic/apply")
    public RestResult roomMicApply(@RequestBody ReqRoomId data, @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser)
        throws Exception {
        log.info("apply mic, operator:{}, roomId:{}", jwtUser.getUserId(), data.getRoomId());
        return roomService.roomMicApply(data, jwtUser);
    }

    /**
     * @api {post} /room/message/broadcast 发送聊天室广播消息
     * @apiVersion 1.0.0
     * @apiName roomMessageBroadcast
     * @apiGroup 消息模块
     * @apiUse token_msg
     * @apiParam {String} fromUserId 发送人用户 Id。
     * @apiParam {String} objectName 消息类型，参考融云消息类型表.消息标志；可自定义消息类型，长度不超过 32 个字符，您在自定义消息时需要注意，不要以 "RC:" 开头，以避免与融云系统内置消息的 ObjectName 重名。（必传）
     * @apiParam {String} content 发送消息内容，单条消息最大 128k，内置消息以 JSON 方式进行数据序列化，消息中可选择是否携带用户信息，详见融云内置消息结构详解；如果 objectName 为自定义消息类型，该参数可自定义格式，不限于 JSON。（必传）
     * @apiParamExample 请求参数示例:
     * POST /room/message/broadcast HTTP/1.1
     * Host: api-cn.ronghub.com
     * Authorization: authorization
     * Content-Type: application/json;charset=UTF-8
     * {
     *     "fromUserId": "xxxxx",
     *     "objectName": "RC:TxtMsg"
     *     "content":"xxxxx"
     * }
     * @apiSuccess (返回结果) {Int} code 返回码，10000 为正常。
     * @apiSuccessExample {json} 服务响应示例:
     * {
     *      "code": 10000,
     *      "msg": "success"
     * }
     */
    @PostMapping(value = "/message/broadcast")
    public RestResult messageBroadcast(@RequestBody ReqBroadcastMessage data, @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser)
        throws Exception {
        log.info("message broadcast, operator:{}, data:{}", jwtUser.getUserId(), GsonUtil.toJson(data));
        return roomService.messageBroadcast(data, jwtUser);
    }


    /**
     * @api {post} /room/status_sync IM 聊天室状态同步
     * @apiVersion 1.0.0
     * @apiName roomStatusSync
     * @apiGroup 房间模块
     * @apiUse token_msg
     * @apiParam {String} chatRoomId 聊天室 Id。
     * @apiParam {String[]} userIds 用户 Id 数据。
     * @apiParam {String} status 操作状态：0：直接调用接口 1：触发融云退出聊天室机制将用户踢出（聊天室中用户在离线 30 秒后有新消息产生时或离线后聊天室中产生 30 条消息时会被自动退出聊天室，此状态需要聊天室中有新消息时才会进行同步）2：用户被封禁 3：触发融云销毁聊天室机制自动销毁
     * @apiParam {String} type 聊天室事件类型：0 创建聊天室、1 加入聊天室、2 退出聊天室、3 销毁聊天室
     * @apiParam {Long} time 发生时间。
     * @apiParamExample 请求参数示例:
     * POST /room/status_sync HTTP/1.1
     * Host: api-cn.ronghub.com
     * Authorization: authorization
     * Content-Type: application/json;charset=UTF-8
     * [
     *   {
     *     "chatRoomId":"destory_11",
     *     "userIds":["gggg"],
     *     "status":0,
     *     "type":1,
     *     "time":1574476797772
     *   },
     *   {
     *     "chatRoomId":"destory_12",
     *     "userIds":[],
     *     "status":0,
     *     "type":0,
     *     "time":1574476797772
     *   }
     * ]
     * @apiSuccess (返回结果) {Int} code 返回码，10000 为正常。
     * @apiSuccessExample {json} 服务响应示例:
     * {
     *      "code": 10000,
     *      "msg": "success"
     * }
     */
    @PostMapping(value = "/status_sync")
    public RestResult statusSync(@RequestBody List<ReqRoomStatusSync> data,
        @RequestParam(value = "signTimestamp") Long signTimestamp,
        @RequestParam(value = "nonce") String nonce,
        @RequestParam(value = "signature") String signature) {
        log.info("chartroom status sync, data:{}", GsonUtil.toJson(data));
        roomService.chrmStatusSync(data, signTimestamp, nonce, signature);
        return RestResult.success();
    }

}
