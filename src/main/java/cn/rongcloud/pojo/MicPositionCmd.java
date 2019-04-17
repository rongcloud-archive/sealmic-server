package cn.rongcloud.pojo;

/**
 * Created by weiqinxiao on 2019/3/25.
 */
public enum MicPositionCmd {
    Carry, // 0 抱麦
    Lock, //1 锁麦
    Unlock,//2 解锁麦
    Forbidden,//3 禁麦
    Unforbidden,//4 解禁
    Kick, //5 踢麦

    Up,//6 上麦
    Down,//7 下麦
    Change,//8跳麦
}
