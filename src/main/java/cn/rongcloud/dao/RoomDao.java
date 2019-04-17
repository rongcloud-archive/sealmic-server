package cn.rongcloud.dao;

import cn.rongcloud.pojo.Room;
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
public interface RoomDao extends JpaRepository<Room, Long> {
    public List<Room> findByRid(String rid);

    @Transactional
    @Modifying
    public int deleteByRid(String rid);

    public boolean existsByRid(String rid);
    public boolean existsByRidAndCreatorUid(String rid, String creatorUid);

    @Transactional
    @Modifying
    @Query(value = "update t_room set bg_id=?2 where rid=?1", nativeQuery = true)
    public int updateBgByRid(String rid, int bgId);
}