package cn.rongcloud.pojo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Created by weiqinxiao on 2019/3/21.
 */
@Data
public class RoomResult extends RoomBaseResult {
    private List<MicPositionResult> micPositions;
    private List<AudienceResult> audiences;

    @Data
    public static class MicPositionResult extends AudienceResult {
        int state;
        int position;
    }

    @Data
    public static class AudienceResult {
        String userId;
        Date joinDt;
        int role;
    }
}
