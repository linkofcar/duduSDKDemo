package com.czl.chatClient.audio.Codec;

import com.czl.chatClient.audio.Speex;
import com.czl.chatClient.utils.Log;

/**
 * Created by Administrator on 2017/12/26.
 */

public class SampleSpeexAudioCoder implements DuduCodecServer{
    private boolean isInitAEC=false;

    public SampleSpeexAudioCoder() {
    }

    @Override
    public byte[] encode(short[] rec,int frameSize) {
        byte[] speexEncode = new byte[frameSize];
        int   iSize=Speex.get().encode(rec, 0, speexEncode, 0);
        byte[]    outbuffer=new byte[iSize];
        System.arraycopy(speexEncode, 0, outbuffer, 0, iSize);
        return outbuffer;
    }

    @Override
    public short[] decode(byte[] rec,int size) {
        short[]  rawData = new short[Speex.get().getFrameSize()];
        int iSize=Speex.get().decode(rec, rawData, size);
        short[] outBuffer=new short[iSize];
        System.arraycopy(rawData, 0, outBuffer, 0, iSize);
        return outBuffer;
    }

    @Override
    public byte[] AECCancelEncode(short[] rec, short[] play,int frameSize) {
        if(!isInitAEC){
            Log.e("编码器 未初始化");
            return  null;
        }
        short[] echoCancelData = new short[frameSize];
        Speex.get().speexAec(rec,play, echoCancelData);
        return encode(echoCancelData,frameSize);
    }

    @Override
    public void init(int smapleRate) {
        if(!isInitAEC){
            isInitAEC=true;
            Speex.get().initSpeexAec(getFrameSize(),getFrameSize()*25,smapleRate);
        }
    }

    @Override
    public void destory() {
        Speex.get().exitSpeexDsp();
        isInitAEC=false;
    }

    @Override
    public int getFrameSize() {
        return Speex.get().getFrameSize();
    }
}
