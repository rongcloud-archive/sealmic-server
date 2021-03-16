package cn.rongcloud.mic.room.enums;

public enum MemberChangeType {
    /**
     * 被踢
     */
    KICK(0);

    private int value;

    MemberChangeType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
