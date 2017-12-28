package com.czl.chatClient.audio.codec;

/**
 *  音频编解码接口
 */

public interface DuduCodecServer {
    //音频压缩方法
    public byte[] encode(short[] rec,int frameSize);
    //音频 解压方法
    public short[] decode(byte[] rec,int size);
    // 音频回声消除方法
    public short[] AECCanceler(short[] rec,short[] play,int frameSize);
    // 音频压缩及回声消除方法
    public byte[] AECCancelEncode(short[] rec,short[] play,int frameSize);
    // 初始化 回声消除
    public void init(int smapleRate);
    //销毁 回声消除
    public void destory();
    // 每一帧的大小
    public int getFrameSize();
}
