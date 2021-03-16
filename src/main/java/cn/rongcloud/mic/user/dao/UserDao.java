package cn.rongcloud.mic.user.dao;

import cn.rongcloud.mic.user.model.TUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by sunyinglong on 2020/05/25.
 */
@Repository
public interface UserDao extends JpaRepository<TUser, Long> {

    TUser findTUserByMobileEquals(String mobile);

    TUser findTUserByDeviceIdEqualsAndTypeEquals(String deviceId, Integer type);

    TUser findTUserByUidEquals(String uid);
}