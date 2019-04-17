package cn.rongcloud.pojo;

import cn.rongcloud.common.ApiException;
import cn.rongcloud.common.ErrorEnum;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by weiqinxiao on 2019/2/28.
 */
public enum RoleEnum {
    RoleAssistant("RoleAssistant", 1),
    RoleConnector("RoleConnector", 2),
    RoleAudience("RoleAudience", 3);

    private @Getter
    @Setter(AccessLevel.PRIVATE) String msg;
    private @Getter
    @Setter(AccessLevel.PRIVATE) int value;

    RoleEnum(String msg, int value) {
        this.msg = msg;
        this.value = value;
    }

    public static RoleEnum getEnumByValue(int v) {
        for(RoleEnum item : RoleEnum.values()) {
            if(item.getValue() == v) {
                return item;
            }
        }

        throw new ApiException(ErrorEnum.ERR_REQUEST_PARA_ERR, v + " not valid role");
    }
}
