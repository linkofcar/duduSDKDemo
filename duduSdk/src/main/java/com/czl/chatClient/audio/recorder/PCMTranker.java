package com.czl.chatClient.audio.recorder;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.czl.chatClient.Constants;
import com.czl.chatClient.audio.Codec.DuduCodecServer;
import com.czl.chatClient.audio.Codec.SampleSpeexAudioCoder;
import com.czl.chatClient.utils.Log;

/**
 * Created by tongchenfei on 2017/8/8.
 */

public class PCMTranker implements Runnable {
    private String TAG=PCMTranker.class.getSimpleName();
    private DuduCodecServer codecServer;
    private PCMTranker() {
        codecServer=new SampleSpeexAudioCoder();
    }

    private static PCMTranker instance = new PCMTranker();

    public static PCMTranker get() {
        return instance;
    }

    private AudioTrack audioTrack;
    public boolean isRunning = false;


    public void startPlay() {
        if (!isRunning) {
            isRunning = true;
            new Thread(this).start();
        }
    }

    public void stopPlay() {
        isRunning = false;
    }

    @Override
    public void run() {
        Log.e(TAG, "开始播放" + isRunning);
        int playBuff = AudioTrack.getMinBufferSize(Constants.SAMPLERATEINHZ,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, Constants
                .SAMPLERATEINHZ, AudioFormat.CHANNEL_OUT_MONO, AudioFormat
                .ENCODING_PCM_16BIT, playBuff, AudioTrack.MODE_STREAM);
        short[] rawData;
        byte[] speexEncode;
        int index=1;
        while (isRunning) {
            if (FramDataManager.get().canRead()) {
                speexEncode = FramDataManager.get().getSpeexData();
                if(speexEncode!=null) {
                    rawData = codecServer.decode(speexEncode, speexEncode
                            .length);
                    audioTrack.write(rawData, 0, rawData.length);
                    FramDataManager.get().addEchoData(rawData, rawData.length);
                    if(audioTrack.getPlayState()!=AudioTrack.PLAYSTATE_PLAYING) {
                        audioTrack.play();
                    }
                }
            }else {
                try {
                    Thread.sleep(500*index);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(index>=6){
                    isRunning=false;
                }
                index++;
            }
        }
        FramDataManager.get().clear();
        audioTrack.stop();
        audioTrack.release();
        audioTrack = null;
        Log.e(TAG, "播放结束");
    }
}
