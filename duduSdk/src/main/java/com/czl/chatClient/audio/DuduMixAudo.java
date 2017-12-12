package com.czl.chatClient.audio;

import android.util.Log;

import com.czl.chatClient.AppServerType;
import com.czl.chatClient.bean.DuduUser;
import com.czl.chatClient.bean.NettyMessage;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/12/8.
 */

public class DuduMixAudo {
    private Map<String,List<byte[]>> map=new HashMap<String, List<byte[]>>();
    private Map<String,List<byte[]>> timemap=new HashMap<String, List<byte[]>>();
    private boolean isplaying=false;
    MultiAudioMixer mixer;
    private Speex mSpeex;

    public DuduMixAudo(MultiAudioMixer.OnAudioMixListener mOnAudioMixListener,Speex speex) {
        mixer= MultiAudioMixer.createAudioMixer();
        mixer.setOnAudioMixListener(mOnAudioMixListener);
        mixer.setSpeex(speex);
        this.mSpeex=speex;
    }


    private String getMapkey(NettyMessage message) {
//        "dd-MMM-yyyy HH:mm:ss:SSS"
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");

        return formatter.format(new Date(message.getConobj().getMsgTime()));
    }

    public void  play(){
        try {
            if(isplaying){
                return;
            }
            timemap.putAll(map);
            map.clear();
            while (timemap.size() > 0&&!isplaying) {
                isplaying=true;
                List<byte[]> list=convertByte(timemap);
                mixer.mixAudios(list);
                timemap.clear();
            }
            isplaying=false;
        }catch (Exception e){
            e.printStackTrace();
        }
   }

    private void readMapbyte(MultiAudioMixer mixer,List<NettyMessage> msgs) throws Exception {
        Log.e("DuduMixAudo",msgs.size()+"!@@@@@@@@@");
        Map<String,List<byte[]>> bytesMap=new HashMap<String, List<byte[]>>();
       for(NettyMessage msg:msgs){
          List<byte[]> usersiglbytes= bytesMap.get(msg.getConobj().getFrom().getUserid());
          if(usersiglbytes==null){
            usersiglbytes=new LinkedList<byte[]>();
            bytesMap.put(msg.getConobj().getFrom().getUserid(),usersiglbytes);
          }
           usersiglbytes.add(msg.getContent());
       }
        List<byte[]> allmixbytes=convertByte(map);
        mixer.mixAudios(allmixbytes);
    }

    List<byte[]> convertByte(Map<String ,List<byte[]>> bytesMap) {
        List<byte[]> allmixbytes=new ArrayList<byte[]>();
        for(List<byte[]> userbytes:bytesMap.values()){
            Log.e("DuduMixAudo",userbytes.size()+"!!");
            int lengh=0;
            for(byte[] bytes:userbytes){
                lengh+=bytes.length;
            }
            byte[] result = new byte[lengh];
            int offset=0;
            for (byte[] array : userbytes) {
                System.arraycopy(array, 0, result, offset, array.length);
                offset += array.length;
            }
            Log.e("DuduMixAudo",result.length+"@@");
            allmixbytes.add(result);
        }
        return  allmixbytes;
    }

    public void auMessage(TimesData data) throws IOException{
        byte[] bytes=decode(data);
        String uid=data.getFromUser().getUserid();
        Log.e("DuduMixAudo",uid+"   key   "+uid);
        List<byte[]> frombytes=map.get(uid);
        if(frombytes==null){
            frombytes=new LinkedList<byte[]>();
            map.put(uid,frombytes);
        }else {
            Log.e("DuduMixAudo",uid+"已有数据");
        }
        frombytes.add(bytes);
    }

    /**
     * 解码
     * @param data
     * @return
     * @throws IOException
     */
    private byte[] decode(TimesData data) throws IOException{
        List<ShortData> userbytes=new ArrayList<>();
        DataInputStream dis = new DataInputStream(
                new BufferedInputStream(BytesUtil.byte2Input(data.getBytedata())));
        int len = 0;
        for (int mSize : data.getSizeList()) {
            ShortData sdata=new ShortData();
            byte[] encoded = new byte[mSize];
            len = dis.read(encoded, 0, mSize);
            if (len != -1) {
                short[] lin = new short[mSpeex
                        .getFrameSize()];
                int size = mSpeex.decode(encoded, lin,
                        encoded.length);
                if(size>0) {
                    sdata.setmBuffer(lin);
                    sdata.setmSize(size);
                    userbytes.add(sdata);
                }
            }
        }

        int lengh=0;
        for(ShortData bytes:userbytes){
            lengh+=bytes.getmSize();
        }
        short[] result = new short[lengh];
        int offset=0;
        for (ShortData array : userbytes) {
            System.arraycopy(array.getmBuffer(), 0, result, offset, array.getmSize());
            offset += array.getmSize();
        }
        return BytesUtil.Shorts2Bytes(result);
    }
}
