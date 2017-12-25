package com.czl.chatClient.audio;

import android.util.Log;

/**
 * Created by Tong on 2017/8/8.
 */

public class Speex {
	static {
		try {
			Log.e("JNI", "Trying to load libspeex.so");
			System.loadLibrary("Speex");
		} catch (UnsatisfiedLinkError ule) {
			Log.e("JNI", "WARNING:Gould not load libspeex.so");
		}
	}

	/*
     * quality 1 : 4kbps (very noticeable artifacts, usually intelligible) 2 :
     * 6kbps (very noticeable artifacts, good intelligibility) 4 : 8kbps
     * (noticeable artifacts sometimes) 6 : 11kpbs (artifacts usually only
     * noticeable with headphones) 8 : 15kbps (artifacts not usually noticeable)
     */
	private static final int DEFAULT_COMPRESSION = 8;

	public Speex() {

	}

	public void init() {
		open(DEFAULT_COMPRESSION);
	}

	public native int open(int compression);

	public native int getFrameSize();

	public native int decode(byte encoded[], short lin[], int size);

	public native int encode(short lin[], int offset, byte encoded[], int size);

	public native void close();

	public native int initSpeexAec(int frameSize, int filterLength, int
			sampleRate);

	public native int speexAec(short[] record, short[] play, short[] aec);

	public native int exitSpeexDsp();
}
