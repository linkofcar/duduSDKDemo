package com.czl.chatClient.recorder;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.czl.chatClient.Constants;
import com.czl.chatClient.audio.Speex;
import com.czl.chatClient.utils.Log;

/**
 * Created by tongchenfei on 2017/8/8.
 */

public class PCMTranker implements Runnable {
    private String TAG=PCMTranker.class.getSimpleName();
    private PCMTranker() {

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

    private Speex speex = new Speex();

    @Override
    public void run() {
        Log.e(TAG, "开始播放" + isRunning);
        int playBuff = AudioTrack.getMinBufferSize(Constants.SAMPLERATEINHZ,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, Constants
                .SAMPLERATEINHZ, AudioFormat.CHANNEL_OUT_MONO, AudioFormat
                .ENCODING_PCM_16BIT, playBuff, AudioTrack.MODE_STREAM);
        speex.init();
        short[] rawData;
        byte[] speexEncode;
        while (isRunning) {
            if (FramDataManager.get().canRead()) {
                rawData = new short[speex.getFrameSize()];
                speexEncode = FramDataManager.get().getSpeexData();
                if(speexEncode!=null) {
                    int iSize = speex.decode(speexEncode, rawData, speexEncode
                            .length);
                    Log.e("rawData" + iSize);
                    audioTrack.write(rawData, 0, iSize);
                    FramDataManager.get().addEchoData(rawData, iSize);
                    if(audioTrack.getPlayState()!=AudioTrack.PLAYSTATE_PLAYING) {
                        audioTrack.play();
                    }
                }
            }
        }
        FramDataManager.get().clear();
        audioTrack.stop();
        audioTrack.release();
        audioTrack = null;
        Log.e(TAG, "播放结束");
    }
}
