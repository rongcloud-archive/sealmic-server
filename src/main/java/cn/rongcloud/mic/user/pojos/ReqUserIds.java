package cn.rongcloud.mic.user.pojos;

import java.util.List;
import javax.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * Created by sunyinglong on 2020/6/8
 */
@Data
public class ReqUserIds {

    @NotEmpty(message = "userIds should not be empty")
    private List<String> userIds;
}
