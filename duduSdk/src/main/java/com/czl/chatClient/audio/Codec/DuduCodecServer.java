package com.czl.chatClient.audio.Codec;

/**
 * Created by Administrator on 2017/12/26.
 */

public interface DuduCodecServer {
    public byte[] encode(short[] rec,int frameSize);

    public short[] decode(byte[] rec,int size);

    public byte[] AECCancelEncode(short[] rec,short[] play,int frameSize);

    public void init(int smapleRate);

    public void destory();

    public int getFrameSize();
}
