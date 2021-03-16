package cn.rongcloud.mic.appversion.dao;

import cn.rongcloud.mic.appversion.model.TAppVersion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Created by sunyinglong on 2020/6/15.
 */
@Repository
public interface AppVersionDao extends JpaRepository<TAppVersion, Long> {
    TAppVersion findTAppVersionByPlatformEqualsAndVersionCodeEquals(String platform,
        Long versionCode);

    @Query("select v from TAppVersion v  where v.platform=?1 and v.versionCode > ?2 order by v.versionCode desc ")
    List<TAppVersion> findTAppVersionsByPlatformEqualsAndVersionCodeAfter(String platform,
        Long versionCode);
}