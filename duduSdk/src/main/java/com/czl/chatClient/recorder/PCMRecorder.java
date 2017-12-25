package com.czl.chatClient.recorder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.czl.chatClient.Constants;
import com.czl.chatClient.DuduClient;
import com.czl.chatClient.audio.Speex;
import com.czl.chatClient.bean.DuduUser;
import com.czl.chatClient.utils.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tongchenfei on 2017/8/8.
 */

public class PCMRecorder implements Runnable {
    private List<DuduUser> toUsers;

    private PCMRecorder() {

    }

    private static PCMRecorder instance = new PCMRecorder();

    public static PCMRecorder get() {
        return instance;
    }

    private AudioRecord audioRecord;
    private boolean isRunning = false;

    public void startRecord() {
        if (!isRunning) {
            isRunning = true;
            new Thread(this).start();
        }
    }

    public void stopRecord() {
        isRunning = false;
    }

    private Speex speex = new Speex();


    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process
                .THREAD_PRIORITY_URGENT_AUDIO);

        int recordBuff = AudioRecord.getMinBufferSize(Constants.SAMPLERATEINHZ,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                Constants.SAMPLERATEINHZ, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, recordBuff);

        speex.init();
        speex.initSpeexAec(speex.getFrameSize(), speex.getFrameSize()*25, Constants.SAMPLERATEINHZ);

        short[] rawData = new short[Constants.RAW_PACKAGE_SIZE];
        short[] echoCancelData = new short[Constants.RAW_PACKAGE_SIZE];
        byte[] speexEncode = new byte[Constants.RAW_PACKAGE_SIZE];
        audioRecord.startRecording();
        int iSize=0;
        OutData datas=null;
        byte[] outbuffer=null;
        List<Integer> list=new ArrayList<>();
        while (isRunning) {
            audioRecord.read(rawData, 0, Constants.RAW_PACKAGE_SIZE);
            if (FramDataManager.get().getEchoData() != null) {
                speex.speexAec(rawData, FramDataManager.get().getEchoData(),
                        echoCancelData);
//                datas=encodeByte(echoCancelData,speexEncode);
                iSize=speex.encode(rawData, 0, speexEncode, 0);
//                FramDataManager.get().addSpeexData(speexEncode, iSize);
            } else {
                iSize=speex.encode(rawData, 0, speexEncode, 0);
//                datas=encodeByte(echoCancelData,speexEncode);
//                FramDataManager.get().addSpeexData(speexEncode, iSize);
            }
            list.add(iSize);
            outbuffer=new byte[iSize];
            System.arraycopy(speexEncode, 0, outbuffer, 0, iSize);
            DuduClient.getInstance().sendAudoiByte(toUsers, outbuffer, "@#!");
        }
        audioRecord.stop();
        audioRecord.release();
        audioRecord = null;
        speex.exitSpeexDsp();
    }

    private OutData encodeByte(short[] echoCancelData, byte[] speexEncode) {
        int frame=speex.getFrameSize();
        int nsamples = (echoCancelData.length - 1) /frame  + 1;
        int iSize=0;
        Log.e("TIMES_"+nsamples);
        List<Integer> list=new ArrayList<>();
        for(int i=0;i<nsamples;i++){
            short[] dst=new short[Constants.RAW_PACKAGE_SIZE];
            byte[] dstencode=new byte[Constants.RAW_PACKAGE_SIZE];
            System.arraycopy(echoCancelData, i*frame, dst, 0, Constants.RAW_PACKAGE_SIZE);
            int size=speex.encode(dst, 0, dstencode, 0);
            System.arraycopy(dstencode, 0, speexEncode, iSize, size);
            list.add(size);
            iSize+=size;
        }
        byte[]  buffer =new byte[iSize];
        System.arraycopy(speexEncode, 0, buffer, 0, iSize);
        for(byte b:buffer){
            Log.e(b+"@@@@@@");
        }
        OutData data=new OutData();
        data.setSizeList(list);
        data.setBuffer(buffer);
       return data;
    }

    public class OutData{
        private byte[] buffer;
        private List<Integer> sizeList;

        public byte[] getBuffer() {
            return buffer;
        }

        public void setBuffer(byte[] buffer) {
            this.buffer = buffer;
        }

        public List<Integer> getSizeList() {
            return sizeList;
        }

        public void setSizeList(List<Integer> sizeList) {
            this.sizeList = sizeList;
        }
    }

    public List<DuduUser> getToUser() {
        return toUsers;
    }
    public void setToUser(DuduUser toUser) {

      List<DuduUser> toUsers=new ArrayList<>();
       toUsers.add(toUser);
       setToUser(toUsers);
    }

    public void setToUser(List<DuduUser> toUsers) {
        this.toUsers = toUsers;
    }
}
