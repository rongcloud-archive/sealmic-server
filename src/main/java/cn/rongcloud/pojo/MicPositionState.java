package cn.rongcloud.pojo;

/**
 * Created by weiqinxiao on 2019/3/25.
 */
public enum MicPositionState {
    Idle(0x0),
    Locked(0x1),
    Forbidden(0x2),
    Hold(0x4)
    ;

    int value;
    MicPositionState(int v) {
        value = v;
    }

    public int getValue() {
        return value;
    }
}
