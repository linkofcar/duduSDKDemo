package com.czl.chatClient.audio;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/12/8.
 */

public abstract class MultiAudioMixer {
    private  OnAudioMixListener mOnAudioMixListener;
    private Speex speex;

    public static MultiAudioMixer createAudioMixer(){
        return new AverageAudioMixer();
    }

    public void setOnAudioMixListener(OnAudioMixListener l){
        this.mOnAudioMixListener = l;
    }

    public void mixAudios(List<byte[]> rawAudioFiles) throws  IOException{

        byte[] mixBytes = mixRawAudioBytes(rawAudioFiles);
        if(mixBytes != null && mOnAudioMixListener != null){
            mOnAudioMixListener.onMixing(mixBytes);
        }
        if(mOnAudioMixListener != null)
            mOnAudioMixListener.onMixComplete();
    }


    abstract byte[] mixRawAudioBytes(List<byte[]> data);

    public interface OnAudioMixListener{
        /**
         * invoke when mixing, if you want to stop the mixing process, you can throw an AudioMixException
         * @param data
         * @throws AudioMixException
         */
        void onMixing( byte[] data) throws IOException;

        void onMixError(int errorCode);

        /**
         * invoke when mix success
         */
        void onMixComplete();
    }

    public static class AudioMixException extends IOException{
        private static final long serialVersionUID = -1344782236320621800L;

        public AudioMixException(String msg){
            super(msg);
        }
    }

    /**
     * 平均值算法
     * @author Darcy
     */
    private static class AverageAudioMixer extends MultiAudioMixer{

        @Override
        byte[] mixRawAudioBytes(List<byte[]> bMulRoadAudioes) {

            if (bMulRoadAudioes == null || bMulRoadAudioes.size() == 0)
                return null;

            byte[] realMixAudio = bMulRoadAudioes.get(0);

            if(bMulRoadAudioes.size() == 1)
                return realMixAudio;

            for(int rw = 0 ; rw < bMulRoadAudioes.size();  ++rw){
                if(bMulRoadAudioes.get(rw).length != realMixAudio.length){
                    Log.e("app", "column of the road of audio + " + rw +" is diffrent.");
                    return null;
                }
            }

            int row = bMulRoadAudioes.size();
            int coloum = realMixAudio.length / 2;
            short[][] sMulRoadAudioes = new short[row][coloum];

            for (int r = 0; r < row; ++r) {
                for (int c = 0; c < coloum; ++c) {
                    sMulRoadAudioes[r][c] = (short) ((bMulRoadAudioes.get(r)[c * 2] & 0xff) | (bMulRoadAudioes.get(r)[c * 2 + 1] & 0xff) << 8);
                }
            }

            short[] sMixAudio = new short[coloum];
            int mixVal;
            int sr = 0;
            for (int sc = 0; sc < coloum; ++sc) {
                mixVal = 0;
                sr = 0;
                for (; sr < row; ++sr) {
                    mixVal += sMulRoadAudioes[sr][sc];
                }
                sMixAudio[sc] = (short) (mixVal / row);
            }

            for (sr = 0; sr < coloum; ++sr) {
                realMixAudio[sr * 2] = (byte) (sMixAudio[sr] & 0x00FF);
                realMixAudio[sr * 2 + 1] = (byte) ((sMixAudio[sr] & 0xFF00) >> 8);
            }

            return realMixAudio;
        }

    }
}
