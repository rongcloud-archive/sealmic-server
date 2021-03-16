package cn.rongcloud.mic.room.enums;

public enum TakeOverHostCmd {
    /**
     * 接管申请
     */
    APPLY(0),
    /**
     * 拒绝
     */
    REJECT(1),
    /**
     * 接受
     */
    ACCEPT(2);

    private int value;

    TakeOverHostCmd(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
