package cn.rongcloud.pojo;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by weiqinxiao on 2019/3/21.
 */
@Entity
@Table(name = "t_room_mic")
public class RoomMicPositionInfo implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private @Getter @Setter String uid;
    private @Getter @Setter String rid;
    private @Getter @Setter int state;
    private @Getter @Setter int position;

    @Override
    public String toString() {
        return "RoomMicPositionInfo{" +
                "uid='" + uid + '\'' +
                ", rid='" + rid + '\'' +
                ", state=" + state +
                ", position=" + position +
                '}';
    }
}
