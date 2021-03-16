package cn.rongcloud.mic.appversion.controller;

import cn.rongcloud.mic.appversion.pojos.ReqAppVersionCreate;
import cn.rongcloud.mic.appversion.pojos.ReqAppVersionUpdate;
import cn.rongcloud.mic.appversion.service.AppVersionService;
import cn.rongcloud.common.utils.GsonUtil;
import cn.rongcloud.mic.common.rest.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by sunyinglong on 2020/6/25
 */
@RestController
@RequestMapping("/appversion")
@Slf4j
public class AppVersionController {

    @Autowired
    private AppVersionService appVersionService;


    /**
     * @api {get} /appversion/latest 获取 App 最新版本
     * @apiVersion 1.0.0
     * @apiName appVersionLatest
     * @apiGroup APP版本管理
     * @apiParam {String} platform Query参数, Android或iOS
     * @apiParam {Long} versionCode Query参数，版本标识
     * @apiParamExample 请求参数示例:
     * GET /appversion/latest?platform=Android&versionCode=20200617 HTTP/1.1
     * Host: api-cn.ronghub.com
     * Content-Type: application/json;charset=UTF-8
     * @apiSuccess (返回结果) {String} platform Android或iOS
     * @apiSuccess (返回结果) {String} downloadUrl 安装包下载地址
     * @apiSuccess (返回结果) {String} version 版本号
     * @apiSuccess (返回结果) {String} versionCode 版本标识
     * @apiSuccess (返回结果) {Boolean} forceUpgrade 是否强制更新
     * @apiSuccess (返回结果) {String} releaseNote 版本描述
     * @apiSuccessExample {json} 服务响应示例:
     * {
     * 	"code": 10000,
     * 	"msg": "success",
     * 	"data": {
     * 		"platform": "Android Q",
     * 		"downloadUrl": "http://www.baidu.com",
     * 		"version": "2.1.1",
     * 	    "versionCode": 20200617,
     * 		"forceUpgrade": true,
     * 		"releaseNote": "版本描述"
     *   }
     * }
     */
    @GetMapping(value = "/latest")
    public RestResult appVersionLatest(@RequestParam(value = "platform", required = true) String platform,
        @RequestParam(value = "versionCode", required = true) Long versionCode){
        log.info("get latest app version, platform:{}, versionCode:{}", platform, versionCode);
        return appVersionService.getLatestAppVersion(platform, versionCode);
    }

    /**
     * @api {post} /appversion/publish 发布版本
     * @apiVersion 1.0.0
     * @apiName createAppVersion
     * @apiGroup APP版本管理
     * @apiUse token_msg
     * @apiParam {String} platform Android 或 iOS
     * @apiParam {String} downloadUrl 安装包下载地址
     * @apiParam {String} version 供客户端展示的版本名称，例如: 1.0.0
     * @apiParam {Long} versionCode 版本标识
     * @apiParam {boolean} forceUpgrade 是否强制更新
     * @apiParam {String} releaseNote 版本描述
     * @apiParamExample 请求参数示例:
     * POST /appversion/publish HTTP/1.1
     * Host: api-cn.ronghub.com
     * Content-Type: application/json;charset=UTF-8
     * Authorization: authorization
     * {
     *      "platform": "Android",
     *      "downloadUrl": "http://www.baidu.com",
     *      "version": "2.1.1",
     *      "versionCode": 202006171047,
     *      "forceUpgrade":true,
     *      "releaseNote":"版本描述"
     * }
     * @apiSuccess (返回结果) {Int} code 返回码，10000 为正常。
     * @apiSuccessExample {json} 服务响应示例:
     * {
     *      "code": 10000,
     *      "msg": "success"
     * }
     */
    @PostMapping(value = "/publish")
    public RestResult createAppVersion(@RequestBody ReqAppVersionCreate data){
        log.info("publish app version, data:{}", GsonUtil.toJson(data));
        return appVersionService.publishAppVersion(data);
    }

    /**
     * @api {put} /appversion/update 更新版本信息
     * @apiVersion 1.0.0
     * @apiName updateAppVersion
     * @apiGroup APP版本管理
     * @apiUse token_msg
     * @apiParam {String} platform Query参数，Android 或 iOS，查询条件不允许修改
     * @apiParam {Long} versionCode Query参数，版本号，查询条件不允许修改
     * @apiParam {String} downloadUrl 安装包下载地址
     * @apiParam {boolean} forceUpgrade 是否强制更新
     * @apiParam {String} releaseNote 版本描述
     * @apiParamExample 请求参数示例:
     * PUT /appversion/appversion/update?platform=Android&versionCode=2.1.1 HTTP/1.1
     * Host: api-cn.ronghub.com
     * Content-Type: application/json;charset=UTF-8
     * Authorization: authorization
     * {
     *      "downloadUrl": "http://www.baidu.com",
     *      "forceUpgrade": true,
     *      "releaseNote":"版本描述"
     * }
     * @apiSuccess (返回结果) {Int} code 返回码，10000 为正常。
     * @apiSuccessExample {json} 服务响应示例:
     * {
     *      "code": 10000,
     *      "msg": "success"
     * }
     */
    @PutMapping(value = "/update")
    public RestResult updateAppVersion(
        @RequestBody ReqAppVersionUpdate data,
        @RequestParam(value = "platform", required = true) String platform,
        @RequestParam(value = "versionCode", required = true) Long versionCode){
        log.info("update app version, platform:{}, versionCode:{}, data:{}", platform, versionCode, GsonUtil.toJson(data));
        return appVersionService.updateAppVersion(platform, versionCode, data);
    }

    /**
     * @api {delete} /appversion 删除版本
     * @apiVersion 1.0.0
     * @apiName deleteAppVersion
     * @apiGroup APP版本管理
     * @apiParam {String} platform Query参数，Android 或 iOS
     * @apiParam {Long} versionCode Query参数，版本号
     * @apiParamExample 请求参数示例:
     * DELETE /appversion/appversion?platform=Android&versionCode=20200617 HTTP/1.1
     * Host: api-cn.ronghub.com
     * Content-Type: application/json;charset=UTF-8
     * Authorization: authorization
     * @apiSuccess (返回结果) {Int} code 返回码，10000 为正常。
     * @apiSuccessExample {json} 服务响应示例:
     * {
     *      "code": 10000,
     *      "msg": "success"
     * }
     */
    @DeleteMapping(value = "/delete")
    public RestResult deleteAppVersion(@RequestParam(value = "platform", required = true) String platform,
        @RequestParam(value = "versionCode", required = true) Long versionCode){
        log.info("delete app version, platform:{}, versionCode:{}", platform, versionCode);
        return appVersionService.deleteAppVersion(platform, versionCode);
    }
}
