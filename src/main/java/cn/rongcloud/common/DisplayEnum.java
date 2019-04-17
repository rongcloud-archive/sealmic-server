package cn.rongcloud.common;

// Display 定义格式如下：
// display://type=1?userId=xxx?uri=xxxxx
// 0，1，3 时，userId 有效，对应此人的 id，uri 无效
// 2 时，展示白板，必须携带白板 uri
// 4 时，清空当前 room 的 display

public enum DisplayEnum {
    Assistant,//0
    Teacher,//1
    WhiteBoard,//2
    Screen,//3
    None, //4
}