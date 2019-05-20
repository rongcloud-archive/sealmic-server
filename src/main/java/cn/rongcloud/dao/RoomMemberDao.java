package cn.rongcloud.dao;

import cn.rongcloud.pojo.RoomMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by weiqinxiao on 2019/2/25.
 */
@Repository
public interface RoomMemberDao extends JpaRepository<RoomMember, Long> {
    public List<RoomMember> findByRid(String rid);

    public List<RoomMember> findByRidAndUid(String rid, String uid);

    public List<RoomMember> findByUid(String uid);

    public int countByRid(String rid);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "update t_room_member set role=?3 where rid=?1 and uid=?2", nativeQuery = true)
    public int updateRoleByRidAndUid(String rid, String uid, int role);

    @Transactional
    @Modifying
    public int deleteByRidAndUid(String rid, String uid);

    @Transactional
    @Modifying
    public int deleteByRid(String rid);

    public boolean existsByRidAndUid(String rid, String uid);
}