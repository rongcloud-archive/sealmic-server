package cn.rongcloud.dao;

import cn.rongcloud.pojo.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Created by weiqinxiao on 2019/2/25.
 */
@Repository
public interface UserDao extends JpaRepository<UserInfo, Long> {
    //will query with "select * from TABLE where user_id = 'userId'"
    public List<UserInfo> findByUid(String uid);

    @Modifying
    @Transactional
    public int deleteByUid(String uid);
}