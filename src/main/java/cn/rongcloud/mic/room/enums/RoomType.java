package cn.rongcloud.mic.room.enums;

public enum RoomType {
    /**
     * 官方
     */
    OFFICIAL(0),
    /**
     * 自建
     */
    CUSTOM(1);

    private int value;

    RoomType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
