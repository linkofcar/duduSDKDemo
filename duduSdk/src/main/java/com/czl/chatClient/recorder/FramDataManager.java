package com.czl.chatClient.recorder;

import com.czl.chatClient.utils.Log;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by tongchenfei on 2017/8/8.
 */

public class FramDataManager {
    private static Queue<byte[]> queue = new ConcurrentLinkedQueue<byte[]>();
    private FramDataManager() {
        raws = new LinkedList<>();
    }

    private static FramDataManager instance = new FramDataManager();

    public static FramDataManager get() {
        return instance;
    }

    private short[] echo;

    private LinkedList<short[]> raws;


    public void addRawData(short[] raw) {
        short[] buff = raw.clone();
        raws.add(buff);
    }

    public short[] getRawData() {
        if (raws.size() > 0) {
            return raws.pop();
        } else {
            return null;
        }
    }

    public void clear() {
        raws.clear();
    }

    public boolean canRead() {
        boolean read=(raws.size() > 0 ||queue.iterator().hasNext());
        return read;
    }

    public int rawSize() {
        return raws.size();
    }

    public void addEchoData(short[] echoData, int size) {
        echo = new short[size];
        System.arraycopy(echoData, 0, echo, 0, size);
    }

    public short[] getEchoData() {
        return echo;
    }

    public void addSpeexData(byte[] speexData, int size) {
//        byte[] buff = new byte[size];
//        System.arraycopy(speexData, 0, buff, 0, size);
        Log.e(speexData.length+"!!!!!!!");
        queue.add(speexData);
    }

    public byte[] getSpeexData() {
        return  queue.poll();
    }

    public void clearSpeexs() {
        queue.clear();
    }

    private boolean isEchoCancel = true;

    public boolean isEchoCancel() {
        return isEchoCancel;
    }

    public void setEchoCancel(boolean echoCancel) {
        isEchoCancel = echoCancel;
        if (!isEchoCancel) {
            echo = null;
        }
    }
}
