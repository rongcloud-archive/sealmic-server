package cn.rongcloud.mic.room.dao;

import cn.rongcloud.mic.room.model.TRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by sunyinglong on 2020/05/25.
 */
@Repository
public interface RoomDao extends JpaRepository<TRoom, Long> {

    TRoom findTRoomByUidEquals(String roomUid);

    @Transactional
    @Modifying
    @Query(value = "update TRoom t  set t.allowedJoinRoom=?2, t.allowedFreeJoinMic=?3 where t.uid=?1")
    int updateRoomSetting(String roomId, boolean allowedJoinRoom, boolean allowedFreeJoinMic);

    @Modifying
    @Transactional
    @Query(value = "delete from TRoom t where t.uid=?1")
    void deleteTRoomByUidEquals(String uid);

}