package com.czl.chatClient.audio;

/**
 * Created by Administrator on 2017\12\9 0009.
 */

public class ShortData {
    private int mSize;
    private short[] mBuffer;
    private byte[] byteBuffer;

    public int getmSize() {
        return mSize;
    }

    public void setmSize(int mSize) {
        this.mSize = mSize;
    }

    public short[] getmBuffer() {
        return mBuffer;
    }

    public void setmBuffer(short[] mBuffer) {
        this.mBuffer = mBuffer;
    }

    public byte[] getByteBuffer() {
        return byteBuffer;
    }

    public void setByteBuffer(byte[] byteBuffer) {
        this.byteBuffer = byteBuffer;
    }
}
