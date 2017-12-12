package com.example.dudusdkdemo;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.czl.chatClient.DuduClient;
import com.czl.chatClient.DuduSDK;
import com.czl.chatClient.audio.DuduMixAudo;
import com.czl.chatClient.audio.MultiAudioMixer;
import com.czl.chatClient.audio.ShortData;
import com.czl.chatClient.audio.Speex;
import com.czl.chatClient.audio.TimesData;
import com.czl.chatClient.bean.DuduUser;
import com.czl.chatClient.bean.NettyServer;
import com.czl.chatClient.bean.Pushmessage;
import com.czl.chatClient.bean.Responbean;
import com.czl.chatClient.login.onConnetCallBack;
import com.czl.chatClient.receiver.ZVAHandler;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.netty.channel.Channel;

public class SpeexMainActivity extends Activity implements MultiAudioMixer.OnAudioMixListener {
	private short[] mAudioRecordData;
	private short[] mAudioTrackData;

	private Button mStart;
	private Button mStop;
	private File mAudioFile;
	private AudioRecord mAudioRecord;
	private AudioTrack mAudioTrack;
	private DuduMixAudo mixAudo;


	private Speex speex;
	    DuduUser user1=getUser("user10005","设备号"+10005);
//	DuduUser user1=getUser("user10001","设备号"+10001);//上面是10005  下面就用10001  这样当10005 登录成功  主动给10005 发一条消息

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        DuduUser user=getUser("user10207","设备号"+10207); // 注意 userId 前面拼接 "user" 字符串   比如  uid=10001  那么 第一个参数  应该是  user10001; 设备号 是为了 让用户单点登录用的
//		DuduUser user=getUser("user10005","设备号"+10005);
//		DuduUser user=getUser("user10001","设备号"+10001);
		loginNSbyUid(user);
		init();
		registerListener();
		mixAudo=new DuduMixAudo(this,speex);
	}

	private void init() {
		mStart = (Button) findViewById(R.id.start);
		mStop = (Button) findViewById(R.id.stop);
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			File file = new File(Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/audio/");
			if (!file.exists()) {
				file.mkdirs();
			}
			mAudioFile = new File(file, System.currentTimeMillis() + ".spx");
		}

		try {
			int sampleRateInHz = 8000;
			int recordBufferSizeInBytes = AudioRecord.getMinBufferSize(
					sampleRateInHz, AudioFormat.CHANNEL_IN_MONO,
					AudioFormat.ENCODING_PCM_16BIT);
			Log.d("TAG", "recordBufferSizeInBytes=" + recordBufferSizeInBytes);
			mAudioRecordData = new short[recordBufferSizeInBytes];
			mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
					sampleRateInHz, AudioFormat.CHANNEL_IN_MONO,
					AudioFormat.ENCODING_PCM_16BIT, recordBufferSizeInBytes);

			int trackBufferSizeInBytes = AudioTrack.getMinBufferSize(
					sampleRateInHz, AudioFormat.CHANNEL_OUT_MONO,
					AudioFormat.ENCODING_PCM_16BIT);
			mAudioTrackData = new short[trackBufferSizeInBytes];
			Log.d("TAG", "trackBufferSizeInBytes" + trackBufferSizeInBytes);
			mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
					sampleRateInHz, AudioFormat.CHANNEL_OUT_MONO,
					AudioFormat.ENCODING_PCM_16BIT, trackBufferSizeInBytes,
					AudioTrack.MODE_STREAM);
			mAudioTrack.setStereoVolume(AudioTrack.getMinVolume(),
					AudioTrack.getMaxVolume());
			speex = new Speex();
			speex.init();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}

	}

	private String loginNSbyUid(DuduUser user) {
		NettyServer server=new NettyServer();
		server.setIp("192.168.13.31");
		server.setPort(10001);

		DuduSDK.init(new DuduSDK.Builder().setUser(user).setMsServer(server));
		DuduSDK.setDebug(true);
		DuduClient client = DuduClient.getInstance();
		client.login(user, new onConnetCallBack() {
			@Override
			public void onConnectSucess(Channel channel) {
//                DuduUser user1=getUser("user10001","设备号"+10001);//上面是10005  下面就用10001  这样当10005 登录成功  主动给10005 发一条消息
//                DuduUser user1=getUser("user10005","设备号"+10005);
                DuduClient.getInstance().sendDuduMessage(user1,"发送一条消息||||||"+"!!!!|||");
//                byte[] aubyte="发送一条消息||||||".getBytes();
//                  DuduClient.getInstance().sendAudoiByte(user1,aubyte,"发送一条消息||||||"+"!!!!|||");
			}

			@Override
			public void onConnectFailed() {

			}

			@Override
			public boolean isReconnect() {
				return false;
			}

			@Override
			public void disconnect(Channel channel) {

			}

			@Override
			public void addLisenter(DuduClient duduClient) {

				duduClient.addLisenter(new ZVAHandler() {
					@Override
					public void onServerRecive(String s) {
						Log.e("Dudu_SDK","发送成功"+s);
					}

					@Override
					public void onReceiveRespons(Responbean responbean) {

					}

					@Override
					public void onReciveAudioMessage(DuduUser duduUser, byte[] bytes, String s) {
						List<Integer> sizeList=JSONObject.parseArray(s,Integer.class);
						TimesData data=new TimesData(duduUser,bytes,sizeList);
						try {
							mixAudo.auMessage(data);
						} catch (IOException e) {
							e.printStackTrace();
						}
						mixAudo.play();
					}

					@Override
					public void onReciveMessage(DuduUser duduUser, String s) {
						DuduUser userResone=null;
						if(DuduClient.getInstance().getCurrentUser().getUserid().equals("user1001")) {
							userResone=getUser("user10005","设备号"+10005);
						}else {
							userResone=getUser("user10001","设备号"+10001);
						}
						Log.e("DuduSDK","userResone:"+s);
						if(("发送一条消息||||||"+"!!!!|||").equals(s))
							DuduClient.getInstance().sendDuduMessage(userResone,"收到用户("+duduUser.getUserid()+")一条消息  消息内容____ "+s);
					}

					@Override
					public void onReceiveOfflineMessage(List<Pushmessage> lists) {

					}

					@Override
					public void onReceivePushMessage(Pushmessage Impmsg) {

					}
				});
			}
		});
		return null;
	}


	public DuduUser getUser(String uid, String diviceId) {
		DuduUser user = new DuduUser();
		user.setUserid(uid);
		user.setDiviceid(diviceId);
		user.setUrl("http://120.76.194.73:8888/group2/M00/00/13/eEzCSVjeIK3TAMmHAAAi7yL8j4o462.png");
		user.setUsername("測試"+uid);
		return user;
	}

	@Override
	public void onMixing(byte[] data) throws IOException {
		mAudioTrack.write(data, 0, data.length);
		mAudioTrack.play();
	}

	@Override
	public void onMixError(int errorCode) {

	}

	@Override
	public void onMixComplete() {

	}

	private static final class Data {
		private int mSize;
		private byte[] mBuffer;
	}
	private void registerListener() {

		mStart.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);

				new Thread(new Runnable() {
					public void run() {
						List<ShortData> list=new ArrayList<ShortData>();
						try {
							mAudioRecord.startRecording();
							DataOutputStream dos = new DataOutputStream(
									new BufferedOutputStream(
											new FileOutputStream(mAudioFile)));
							int sizeInShorts = speex.getFrameSize();
							short[] audioData = new short[sizeInShorts];
							int sizeInBytes = speex.getFrameSize();
							while (mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
								int number = mAudioRecord.read(audioData, 0,
										sizeInShorts);
								short[] dst = new short[sizeInBytes];
								if(mAudioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING){
									return;
								}
								System.arraycopy(audioData, 0, dst, 0, number);
								byte[] encoded = new byte[sizeInBytes];
								ShortData data=new ShortData();
								int count = speex.encode(dst, 0, encoded,
										number);
								Log.e("语音消息",encoded.length+"!!!!"+count);
								if (count > 0) {
									data.setByteBuffer(encoded);
									data.setmSize(count);
									list.add(data);
									if(list.size()==10){
										ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
										List<Integer> sizeList=new ArrayList<>();
										for(int i=0;i<list.size();i++){
											swapStream.write(list.get(i).getByteBuffer(),0,list.get(i).getmSize());
											sizeList.add(list.get(i).getmSize());
										}
										list.clear();
										DuduClient.getInstance().sendAudoiByte(user1, swapStream.toByteArray(), JSONObject.toJSONString(sizeList));
									}
								}
							}
							dos.flush();
							dos.close();
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}).start();
			}
		});

		mStop.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				if (mAudioRecord != null
						&& mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
					mAudioRecord.stop();
					// speex.close();
				}
			}
		});

	}
}
