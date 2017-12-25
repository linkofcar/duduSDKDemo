package com.example.dudusdkdemo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.czl.chatClient.Constants;
import com.czl.chatClient.DuduClient;
import com.czl.chatClient.DuduSDK;
import com.czl.chatClient.audio.DuduMixAudo;
import com.czl.chatClient.audio.MultiAudioMixer;
import com.czl.chatClient.audio.Speex;
import com.czl.chatClient.bean.DuduUser;
import com.czl.chatClient.bean.NettyServer;
import com.czl.chatClient.bean.Pushmessage;
import com.czl.chatClient.bean.Responbean;
import com.czl.chatClient.login.onConnetCallBack;
import com.czl.chatClient.receiver.ZVAHandler;
import com.czl.chatClient.recorder.PCMRecorder;
import com.czl.chatClient.recorder.PCMTranker;
import com.czl.chatClient.recorder.FramDataManager;

import java.io.File;
import java.io.IOException;
import java.util.List;

import io.netty.channel.Channel;

public class SpeexMainActivity extends Activity implements MultiAudioMixer.OnAudioMixListener {
	private Button mStart;
	private Button mStop;
	private File mAudioFile;
	private DuduMixAudo mixAudo;
	private short[] newbyte;


	private Speex speex;
//	    DuduUser user1=getUser("user10005","设备号"+10005);
	DuduUser user1=getUser("user10001","设备号"+10001);//上面是10005  下面就用10001  这样当10005 登录成功  主动给10005 发一条消息

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
//        DuduUser user=getUser("user10207","设备号"+10207); // 注意 userId 前面拼接 "user" 字符串   比如  uid=10001  那么 第一个参数  应该是  user10001; 设备号 是为了 让用户单点登录用的
		DuduUser user=getUser("user10005","设备号"+10005);
//		DuduUser user=getUser("user10001","设备号"+10001);
		loginNSbyUid(user);
		init();
		registerListener();
		mixAudo=new DuduMixAudo(this,speex);
		PCMRecorder.get().setToUser(user1);
	}

	private void init() {
		mStart = (Button) findViewById(R.id.start);
		mStop = (Button) findViewById(R.id.stop);
		speex = new Speex();
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
						Log.e("Dudu_SDK", "发送成功" + s);
					}

					@Override
					public void onReceiveRespons(Responbean responbean) {

					}

					@Override
					public void onReciveAudioMessage(DuduUser duduUser, byte[] bytes, String s) {
//						List<Integer> list=JSONObject.parseArray(s,Integer.class);
						int offset = 0;
						for (int i = 0; i < Constants.SEND_TIMES; i++) {
							byte[] timebyte = new byte[bytes.length / Constants.SEND_TIMES];
							System.arraycopy(bytes, offset, timebyte, 0, i);
							FramDataManager.get().addSpeexData(bytes, bytes.length);
							offset += i;
						}
						PCMTranker.get().startPlay();
					}

					@Override
					public void onReciveMessage(DuduUser duduUser, String s) {
						DuduUser userResone = null;
						if (DuduClient.getInstance().getCurrentUser().getUserid().equals("user1001")) {
							userResone = getUser("user10005", "设备号" + 10005);
						} else {
							userResone = getUser("user10001", "设备号" + 10001);
						}
						Log.e("DuduSDK", "userResone:" + s);
						if (("发送一条消息||||||" + "!!!!|||").equals(s))
							DuduClient.getInstance().sendDuduMessage(userResone, "收到用户(" + duduUser.getUserid() + ")一条消息  消息内容____ " + s);
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
	public void onMixing(short[] data) throws IOException {


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
						PCMRecorder.get().startRecord();
			}
		});

		mStop.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				PCMRecorder.get().stopRecord();
				PCMTranker.get().stopPlay();
			}
		});

	}
}
