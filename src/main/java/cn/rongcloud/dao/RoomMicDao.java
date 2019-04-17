package cn.rongcloud.dao;

import cn.rongcloud.pojo.RoomMicPositionInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockModeType;
import java.util.List;

/**
 * Created by weiqinxiao on 2019/2/25.
 */
@Repository
public interface RoomMicDao extends JpaRepository<RoomMicPositionInfo, Long> {
    public RoomMicPositionInfo findByRidAndUid(String rid, String uid);

    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    public RoomMicPositionInfo findByRidAndPosition(String rid, int position);

    @Query(value = "select * from t_room_mic where rid=?1 order by position ASC", nativeQuery = true)
    public List<RoomMicPositionInfo> findByRidOrderAsc(String rid);

    @Transactional
    @Modifying
    public int deleteByRid(String rid);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "update t_room_mic set state=?3 where rid=?1 and position=?2", nativeQuery = true)
    public int updateStateByRidAndPosition(String rid, int position, int state);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "update t_room_mic set uid=?3 where rid=?1 and position=?2", nativeQuery = true)
    public int updateUidByRidAndPosition(String rid, int position, String uid);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "update t_room_mic set state=?3, uid=?4 where rid=?1 and position=?2", nativeQuery = true)
    public int updateStateAndUidByRidAndPosition(String rid, int position, int state, String uid);

}