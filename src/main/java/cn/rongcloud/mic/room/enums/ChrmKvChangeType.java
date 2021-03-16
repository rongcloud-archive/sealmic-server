package cn.rongcloud.mic.room.enums;

public enum ChrmKvChangeType {
    /**
     * 上麦
     */
    JOIN(1),
    /**
     * 下麦
     */
    QUIT(2),
    /**
     * 观众接管主持人
     */
    AUDIENCE_TAKEOVER_HOST(3),
    /**
     * 主播接管主持人
     */
    ANCHOR_TAKEOVER_HOST(4),
    /**
     * 转让主持人
     */
    TRANSFER_HOST(5),
    /**
     * 麦位状态设置
     */
    STATE_SET(6);

    private int value;

    ChrmKvChangeType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
