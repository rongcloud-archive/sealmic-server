package cn.rongcloud.mic.room.enums;

public enum MicPositionState {
    /**
     * 正常
     */
    NORMAL(0),
    /**
     * 已锁定
     */
    LOCKED(1),
    /**
     * 闭麦，禁止说话
     */
    CLOSEMIKE(2);

    private int value;

    MicPositionState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
