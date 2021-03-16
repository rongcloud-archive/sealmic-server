package cn.rongcloud.mic.room.pojos;

import java.util.List;
import lombok.Data;

/**
 * Created by sunyinglong on 2020/6/3
 */
@Data
public class ResRoomList {

    private Long totalCount;
    private List<ResRoomInfo> rooms;

}
