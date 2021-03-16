package cn.rongcloud.mic.room.enums;

public enum TransferHostCmd {
    /**
     * 转让申请
     */
    APPLY(0),
    /**
     * 拒绝
     */
    REJECT(1),
    /**
     * 同意
     */
    ACCEPT(2);

    private int value;

    TransferHostCmd(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
