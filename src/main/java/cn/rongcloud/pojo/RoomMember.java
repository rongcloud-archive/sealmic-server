package cn.rongcloud.pojo;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by weiqinxiao on 2019/2/28.
 */
@Entity
@Table(name = "t_room_member")
public class RoomMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private @Getter @Setter String uid;
    private @Getter @Setter String rid;
    private @Getter @Setter int role;
    private @Getter @Setter Date joinDt;
}
