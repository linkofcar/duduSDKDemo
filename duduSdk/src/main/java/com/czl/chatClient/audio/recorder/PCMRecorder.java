package com.czl.chatClient.audio.recorder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.czl.chatClient.Constants;
import com.czl.chatClient.DuduClient;
import com.czl.chatClient.audio.Codec.DuduCodecServer;
import com.czl.chatClient.audio.Codec.SampleSpeexAudioCoder;
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
    private DuduCodecServer codecServer;

    private PCMRecorder() {
        codecServer=new SampleSpeexAudioCoder();
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


    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process
                .THREAD_PRIORITY_URGENT_AUDIO);

        int recordBuff = AudioRecord.getMinBufferSize(Constants.SAMPLERATEINHZ,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                Constants.SAMPLERATEINHZ, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, recordBuff);
        codecServer.init(Constants.SAMPLERATEINHZ);
        short[] rawData = new short[Constants.RAW_PACKAGE_SIZE];
        audioRecord.startRecording();
        byte[] outbuffer=null;
        while (isRunning) {
            audioRecord.read(rawData, 0, Constants.RAW_PACKAGE_SIZE);
            short[] play=FramDataManager.get().getEchoData();
            if (play!= null) {
                outbuffer  =  codecServer.AECCancelEncode(rawData,play,Constants.RAW_PACKAGE_SIZE);
            } else {
                outbuffer  =  codecServer.encode(rawData,Constants.RAW_PACKAGE_SIZE);
            }
            DuduClient.getInstance().sendAudoiByte(toUsers, outbuffer, "@#!");
        }
        audioRecord.stop();
        audioRecord.release();
        audioRecord = null;
        codecServer.destory();
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
