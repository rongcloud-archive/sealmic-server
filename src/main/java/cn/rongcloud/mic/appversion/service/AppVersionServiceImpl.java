package cn.rongcloud.mic.appversion.service;

import cn.rongcloud.mic.appversion.dao.AppVersionDao;
import cn.rongcloud.mic.appversion.model.TAppVersion;
import cn.rongcloud.mic.appversion.pojos.ReqAppVersionCreate;
import cn.rongcloud.mic.appversion.pojos.ReqAppVersionUpdate;
import cn.rongcloud.mic.appversion.pojos.RespAppVersion;
import cn.rongcloud.mic.common.rest.RestResult;
import cn.rongcloud.mic.common.rest.RestResultCode;
import cn.rongcloud.common.utils.DateTimeUtils;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by sunyinglong on 2020/6/16
 */
@Slf4j
@Service
public class AppVersionServiceImpl implements AppVersionService {

    @Autowired
    private AppVersionDao appVersionDao;

    @Override
    public RestResult publishAppVersion(ReqAppVersionCreate data) {
        TAppVersion appVersion = appVersionDao.findTAppVersionByPlatformEqualsAndVersionCodeEquals(data.getPlatform(), data.getVersionCode());
        if (appVersion != null) {
            return RestResult.generic(RestResultCode.ERR_APP_VERSION_ALREADY_EXIST);
        }
        appVersion = new TAppVersion();
        appVersion.setPlatform(data.getPlatform());
        appVersion.setVersion(data.getVersion());
        appVersion.setVersionCode(data.getVersionCode());
        appVersion.setDownloadUrl(data.getDownloadUrl());
        appVersion.setReleaseNote(data.getReleaseNote());
        appVersion.setForceUpgrade(data.getForceUpgrade());
        Date date = DateTimeUtils.currentUTC();
        appVersion.setCreateDt(date);
        appVersion.setUpdateDt(date);
        appVersionDao.save(appVersion);
        return RestResult.success();
    }

    @Override
    public RestResult updateAppVersion(String platform, Long versionCode, ReqAppVersionUpdate data) {
        //检查版本是否存在
        TAppVersion appVersion = appVersionDao.findTAppVersionByPlatformEqualsAndVersionCodeEquals(platform, versionCode);
        if (appVersion == null) {
            return RestResult.generic(RestResultCode.ERR_APP_VERSION_NOT_EXIST);
        }
        if (data.getForceUpgrade() != null) {
            appVersion.setForceUpgrade(data.getForceUpgrade());
        }
        if (data.getDownloadUrl() != null) {
            appVersion.setDownloadUrl(data.getDownloadUrl());
        }
        if (data.getReleaseNote() != null) {
            appVersion.setReleaseNote(data.getReleaseNote());
        }
        appVersionDao.save(appVersion);
        return RestResult.success();
    }

    @Override
    public RestResult deleteAppVersion(String platform, Long versionCode) {
        //检查版本是否存在
        TAppVersion appVersion = appVersionDao.findTAppVersionByPlatformEqualsAndVersionCodeEquals(platform, versionCode);
        if (appVersion == null) {
            return RestResult.generic(RestResultCode.ERR_APP_VERSION_NOT_EXIST);
        }
        appVersionDao.delete(appVersion);
        return RestResult.success();
    }

    @Override
    public RestResult getLatestAppVersion(String platform, Long versionCode) {
        //检查版本是否存在
        List<TAppVersion> appVersions = appVersionDao.findTAppVersionsByPlatformEqualsAndVersionCodeAfter(platform, versionCode);
        if (appVersions == null || appVersions.isEmpty()) {
            return RestResult.generic(RestResultCode.ERR_APP_NO_NEW_VERSIONS);
        }

        TAppVersion lastVersion = appVersions.get(0);
        for (TAppVersion version : appVersions) {
            if (version.getForceUpgrade() != null && version.getForceUpgrade()) {
                lastVersion.setForceUpgrade(true);
                break;
            }
        }

        RespAppVersion out = new RespAppVersion();
        out.setDownloadUrl(lastVersion.getDownloadUrl());
        out.setForceUpgrade(lastVersion.getForceUpgrade());
        out.setPlatform(lastVersion.getPlatform());
        out.setReleaseNote(lastVersion.getReleaseNote());
        out.setVersion(lastVersion.getVersion());
        out.setVersionCode(lastVersion.getVersionCode());

        return RestResult.success(out);
    }
}
