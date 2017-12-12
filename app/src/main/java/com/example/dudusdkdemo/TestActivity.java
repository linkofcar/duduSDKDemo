package com.example.dudusdkdemo;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.czl.chatClient.Constants;
import com.czl.chatClient.DuduClient;
import com.czl.chatClient.DuduSDK;
import com.czl.chatClient.bean.DuduUser;
import com.czl.chatClient.bean.NettyServer;
import com.czl.chatClient.bean.Pushmessage;
import com.czl.chatClient.bean.Responbean;
import com.czl.chatClient.login.onConnetCallBack;
import com.czl.chatClient.receiver.ZVAHandler;

import java.util.List;

import io.netty.channel.Channel;

public class TestActivity extends Activity {
    private AudioRecord mAudioRecord;
    private AudioTrack mAudioTrack;

    private Button mStart;
    private Button mStop;
    private  Button sendTextMsg;

    private int DEFAULT_SIZE=1024;
    private int  index=0;
    private Handler mHandler;
//    DuduUser user1=getUser("user10005","设备号"+10005);
        DuduUser user1=getUser("user10001","设备号"+10001);//上面是10005  下面就用10001  这样当10005 登录成功  主动给10005 发一条消息
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHandler=new Handler();
        initRecorderAndroidTranker();
        initView();

//        DuduUser user=getUser("user10001","设备号"+Constants.SEPORATE+10001); // 注意 userId 前面拼接 "user" 字符串   比如  uid=10001  那么 第一个参数  应该是  user10001; 设备号 是为了 让用户单点登录用的
        DuduUser user=getUser("user10005","设备号"+10005);
        loginNSbyUid(user);
    }

    private void initView() {
        mStart = findViewById(R.id.start);
        mStop= findViewById(R.id.stop);
        sendTextMsg=findViewById(R.id.send_test_msg);
        sendTextMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                index++;
                // 发送 一般的文字消息  请调用
                /**
                 * 第一个 参数 @{user}  发送给 某给用户
                 * 第二个参数 @{JsonString}  建议 是 一个json  字符串   (根据需要 设置 json里面的参数)
                 * 第三个参数  @{saveToCache}  是否 保存 离线消息  默认false 不保存
                 *
                 */
                // DuduClient.getInstance().sendDuduMessage(DuduUser user,String  jsonString)
                /**
                 *  重载 群发 方法
                 *  @{users} 第一个参数 想要发送的 用户列表
                 *  @{ jsonString} 建议 是 一个json  字符串   (根据需要 设置 json里面的参数)
                 *
                 */
//              DuduClient.getInstance().sendDuduMessage(List<DuduUser> users, String  jsonString);

//                DuduUser user1=getUser("user10001","设备号"+10001);//上面是10005  下面就用10001  这样当10005 登录成功  主动给10005 发一条消息
//                DuduUser user1=getUser("user10005","设备号"+10005);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        DuduClient.getInstance().sendDuduMessage(user1,"第   "+index+"  \n\n+||||| 测试消息");
                        // 保存离线消息  当 用户 {user1} 处于离线状态时  消息会保存 到数据库。。。 当它 上线时  onReceiveOfflineMessage(List<Pushmessage> lists) 方法 会被调用
//                        DuduClient.getInstance().sendDuduMessage(user1,"第   "+index+"   测试消息",true);
                    }
                }).start();

            }
        });


        mStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        startRecording();
                    }
                }).start();
            }
        });
        mStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                        stopRecording();
            }
        });
    }

    private void stopRecording() {
        if (mAudioRecord != null
                && mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
            mAudioRecord.stop();
            // speex.close();
        }
    }

    private void startRecording() {
        if(mAudioRecord==null){
            return;
        }
        mAudioRecord.startRecording();
        int sizeInShorts = DEFAULT_SIZE;
        byte[] audioData = new byte[sizeInShorts];
        while (mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
            int number = mAudioRecord.read(audioData, 0,
                    sizeInShorts);
            // 注意  此处 size=1024 供测试  实际使用 应该 在10kb 左右 数据太小服务器 压力会很大  （另 字节流应该经过 压缩处理  压缩率 也是 消息延迟的 重要原因。）
            if(number>0) {
                // 发送  语音字节流的方法
                /**
                 * 第一个 参数 @{user}  发送给 某给用户
                 * 第二个参数 @{byes}  语音 字节流 数组   建议 编码压缩 后 的 流。
                 * 第三个参数 @(discribtionString)  消息描述 --字符串(建议 使用一个JsonString  便于扩展) 可以为空 。比如 你 想在 这条语音消息里 附加 一个 消息的时间
                 *
                 */
                DuduClient.getInstance().sendAudoiByte(user1, audioData, "语音消息");
               }
            }
    }

    private void initRecorderAndroidTranker() {

        int sampleRateInHz = 8000;
        int recordBufferSizeInBytes = AudioRecord.getMinBufferSize(
                sampleRateInHz, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        // 初始化 录音设备
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                sampleRateInHz, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, recordBufferSizeInBytes);

        int trackBufferSizeInBytes = AudioTrack.getMinBufferSize(
                sampleRateInHz, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
       //初始化 语音播放器
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRateInHz, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, trackBufferSizeInBytes,
                AudioTrack.MODE_STREAM);
        mAudioTrack.setStereoVolume(AudioTrack.getMinVolume(),
                AudioTrack.getMaxVolume());
    }

    private String loginNSbyUid(final DuduUser user) {
        // 初始化流程 略有变化  支持配置MS 地址及 NS 地址  配置NS 地址后  配置 的MS地址   失效（sdk 始终连接NS 地址） （二选一  优先NS 连接）
        NettyServer msServer=new NettyServer();
        msServer.setIp("192.168.13.31");
        msServer.setPort(10001);

        DuduSDK.Builder builder= new DuduSDK.Builder();
        builder.setUser(user);
        builder.setMsServer(msServer);
//        builder.setNsServer(nsServer)
        DuduSDK.init(builder);
        DuduSDK.setDebug(true);
        DuduClient client = DuduClient.getInstance();
        client.login(user, new onConnetCallBack() {
            @Override
            public void onConnectSucess(Channel channel) {


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
                       // 播放字节流
                        mAudioTrack.write(bytes, 0, bytes.length);
                        mAudioTrack.play();
                    }

                    @Override
                    public void onReciveMessage(final DuduUser fromUser, final String s) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(TestActivity.this,fromUser.getUserid()+"  发来消息：  "+s,Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void onReceiveOfflineMessage(List<Pushmessage> lists) {
                        for(Pushmessage pushmessage:lists){
                            Log.e("Dudu_SDK","接收到离线消息:"+pushmessage.toString());
                        }
                    }

                    @Override
                    public void onReceivePushMessage(Pushmessage Impmsg) {
                        Log.e("Dudu_SDK","接收到推送消息:"+Impmsg.toString());
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
}
