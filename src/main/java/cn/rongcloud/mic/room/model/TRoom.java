package cn.rongcloud.mic.room.model;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

/**
 * Created by sunyinglong on 2020/5/25
 */
@Entity
@Table(name = "t_room")
@Data
public class TRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String uid;
    private String name;
    @Column(name = "theme_picture_url")
    private String themePictureUrl;
    @Column(name = "allowed_join_room")
    private boolean allowedJoinRoom;
    @Column(name = "allowed_free_join_mic")
    private boolean allowedFreeJoinMic;
    private Integer type;
    private Date createDt;
    private Date updateDt;

}
