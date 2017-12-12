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

    /**
     * <p>start to mix , you can call {@link #setOnAudioMixListener(OnAudioMixListener)} before this method to get mixed data.
     */
//    public void mixAudios(File[] rawAudioFiles){
//
//        final int fileSize = rawAudioFiles.length;
//
//        FileInputStream[] audioFileStreams = new FileInputStream[fileSize];
//        File audioFile = null;
//
//        FileInputStream inputStream;
//        byte[][] allAudioBytes = new byte[fileSize][];
//        boolean[] streamDoneArray = new boolean[fileSize];
//        byte[] buffer = new byte[512];
//        int offset;
//
//        try {
//
//            for (int fileIndex = 0; fileIndex < fileSize; ++fileIndex) {
//                audioFile = rawAudioFiles[fileIndex];
//                audioFileStreams[fileIndex] = new FileInputStream(audioFile);
//            }
//
//            while(true){
//
//                for(int streamIndex = 0 ; streamIndex < fileSize ; ++streamIndex){
//
//                    inputStream = audioFileStreams[streamIndex];
//                    if(!streamDoneArray[streamIndex] && (offset = inputStream.read(buffer)) != -1){
//                        allAudioBytes[streamIndex] = Arrays.copyOf(buffer,buffer.length);
//                    }else{
//                        streamDoneArray[streamIndex] = true;
//                        allAudioBytes[streamIndex] = new byte[512];
//                    }
//                }
//
//                byte[] mixBytes = mixRawAudioBytes(allAudioBytes);
//                if(mixBytes != null && mOnAudioMixListener != null){
//                    mOnAudioMixListener.onMixing(mixBytes);
//                }
//
//                boolean done = true;
//                for(boolean streamEnd : streamDoneArray){
//                    if(!streamEnd){
//                        done = false;
//                    }
//                }
//
//                if(done){
//                    if(mOnAudioMixListener != null)
//                        mOnAudioMixListener.onMixComplete();
//                    break;
//                }
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            if(mOnAudioMixListener != null)
//                mOnAudioMixListener.onMixError(1);
//        }finally{
//            try {
//                for(FileInputStream in : audioFileStreams){
//                    if(in != null)
//                        in.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    public void mixAudios(List<byte[]> rawAudioFiles) throws  IOException{

        byte[] mixBytes = mixRawAudioBytes(rawAudioFiles);
        if(mixBytes != null && mOnAudioMixListener != null){
            mOnAudioMixListener.onMixing(mixBytes);
        }
        if(mOnAudioMixListener != null)
            mOnAudioMixListener.onMixComplete();
    }

//    public  List<ShortData> decode(byte[] mixBytes) throws IOException{
//        List<ShortData> list=new ArrayList<ShortData>();
//        DataInputStream dis = new DataInputStream(
//                new BufferedInputStream(BytesUtil.byte2Input(mixBytes)));
//        int subSize=speex.getFrameSize();
//        int count = mixBytes.length % subSize == 0 ? mixBytes.length / subSize: mixBytes.length / subSize + 1;
//        for(int i=0;i<count;i++) {
//            short[] lin = new short[subSize];
//            byte[] encoded = new byte[subSize];
//            ShortData data = new ShortData();
//            dis.read(encoded,0,encoded.length);
//            int size = speex.decode(encoded, lin,
//                    encoded.length);
//            if (size > 0) {
//                data.setmBuffer(lin);
//                data.setmSize(size);
//                list.add(data);
//            }
//
//        }
//        return list;
//    }

    public static Object[] splitAry(byte[] ary, int subSize) {
        int count = ary.length % subSize == 0 ? ary.length / subSize: ary.length / subSize + 1;

        List<List<Byte>> subAryList = new ArrayList<List<Byte>>();

        for (int i = 0; i < count; i++) {
            int index = i * subSize;
            List<Byte> list = new ArrayList<Byte>();
            int j = 0;
            while (j < subSize && index < ary.length) {
                list.add(ary[index++]);
                j++;
            }
            subAryList.add(list);
        }

        Object[] subAry = new Object[subAryList.size()];

        for(int i = 0; i < subAryList.size(); i++){
            List<Byte> subList = subAryList.get(i);
            byte[] subAryItem = new byte[subList.size()];
            for(int j = 0; j < subList.size(); j++){
                subAryItem[j] = subList.get(j);
            }
            subAry[i] = subAryItem;
        }

        return subAry;
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

    public Speex getSpeex() {
        return speex;
    }

    public void setSpeex(Speex speex) {
        this.speex = speex;
    }
}
