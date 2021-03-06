package com.czl.chatClient.utils;

import android.util.Log;

import com.czl.chatClient.Constants;
import com.czl.chatClient.audio.ShortData;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017\12\9 0009.
 */

public class BytesUtil {
    public static final InputStream byte2Input(byte[] buf) {
        return new ByteArrayInputStream(buf);
    }

    public static final byte[] input2byte(InputStream inStream)
            throws IOException {
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[100];
        int rc = 0;
        while ((rc = inStream.read(buff, 0, 100)) > 0) {
            swapStream.write(buff, 0, rc);
        }
        byte[] in2b = swapStream.toByteArray();
        return in2b;
    }


    private String TAG = "BytesTransUtil";

    public static boolean testCPU() {
        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            // System.out.println("is big ending");
            return true;
        } else {
            // System.out.println("is little ending");
            return false;
        }
    }

    public static byte[] getBytes(short s, boolean bBigEnding) {
        byte[] buf = new byte[2];
        if (bBigEnding)
            for (int i = buf.length - 1; i >= 0; i--) {
                buf[i] = (byte) (s & 0x00ff);
                s >>= 8;
            }
        else
            for (int i = 0; i < buf.length; i++) {
                buf[i] = (byte) (s & 0x00ff);
                s >>= 8;
            }
        return buf;
    }

    public byte[] getBytes(int s, boolean bBigEnding) {
        byte[] buf = new byte[4];
        if (bBigEnding) {
            for (int i = buf.length - 1; i >= 0; i--) {
                buf[i] = (byte) (s & 0x000000ff);
                s >>= 8;
            }
        } else {
            System.out.println("1");
            for (int i = 0; i < buf.length; i++) {
                buf[i] = (byte) (s & 0x000000ff);
                s >>= 8;
            }
        }
        return buf;
    }

    public byte[] getBytes(long s, boolean bBigEnding) {
        byte[] buf = new byte[8];
        if (bBigEnding)
            for (int i = buf.length - 1; i >= 0; i--) {
                buf[i] = (byte) (s & 0x00000000000000ff);
                s >>= 8;
            }
        else
            for (int i = 0; i < buf.length; i++) {
                buf[i] = (byte) (s & 0x00000000000000ff);
                s >>= 8;
            }
        return buf;
    }

    public static short getShort(byte[] buf, boolean bBigEnding) {
        if (buf == null) {
            throw new IllegalArgumentException("byte array is null!");
        }
        if (buf.length > 2) {
            throw new IllegalArgumentException("byte array size > 2 !");
        }
        short r = 0;
        if (bBigEnding) {
            for (int i = 0; i < buf.length; i++) {
                r <<= 8;
                r |= (buf[i] & 0x00ff);
            }
        } else {
            for (int i = buf.length - 1; i >= 0; i--) {
                r <<= 8;
                r |= (buf[i] & 0x00ff);
            }
        }

        return r;
    }

    public int getInt(byte[] buf, boolean bBigEnding) {
        if (buf == null) {
            throw new IllegalArgumentException("byte array is null!");
        }
        if (buf.length > 4) {
            throw new IllegalArgumentException("byte array size > 4 !");
        }
        int r = 0;
        if (bBigEnding) {
            for (int i = 0; i < buf.length; i++) {
                r <<= 8;
                r |= (buf[i] & 0x000000ff);
            }
        } else {
            for (int i = buf.length - 1; i >= 0; i--) {
                r <<= 8;
                r |= (buf[i] & 0x000000ff);
            }
        }
        return r;
    }

    public long getLong(byte[] buf, boolean bBigEnding) {
        if (buf == null) {
            throw new IllegalArgumentException("byte array is null!");
        }
        if (buf.length > 8) {
            throw new IllegalArgumentException("byte array size > 8 !");
        }
        long r = 0;
        if (bBigEnding) {
            for (int i = 0; i < buf.length; i++) {
                r <<= 8;
                r |= (buf[i] & 0x00000000000000ff);
            }
        } else {
            for (int i = buf.length - 1; i >= 0; i--) {
                r <<= 8;
                r |= (buf[i] & 0x00000000000000ff);
            }
        }
        return r;
    }

    /*----------------------------------------------------------*/
    /* 对转换进行一个简单的封装 */
    /*----------------------------------------------------------*/
    public byte[] getBytes(int i) {
        return getBytes(i, this.testCPU());
    }

    public static byte[] getBytes(short s) {
        return getBytes(s, testCPU());
    }

    public byte[] getBytes(long l) {
        return getBytes(l, this.testCPU());
    }

    public int getInt(byte[] buf) {
        return getInt(buf, this.testCPU());
    }

    public static short getShort(byte[] buf) {
        return getShort(buf, testCPU());
    }

    public long getLong(byte[] buf) {
        return getLong(buf, this.testCPU());
    }

    /****************************************/
    public  static short[] Bytes2Shorts(byte[] buf) {
        byte bLength = 2;
        short[] s = new short[buf.length / bLength];
        for (int iLoop = 0; iLoop < s.length; iLoop++) {
            byte[] temp = new byte[bLength];
            for (int jLoop = 0; jLoop < bLength; jLoop++) {
                temp[jLoop] = buf[iLoop * bLength + jLoop];
            }
            s[iLoop] = getShort(temp);
        }
        return s;
    }

    public static byte[] Shorts2Bytes(short[] s) {
        byte bLength = 2;
        byte[] buf = new byte[s.length * bLength];
        for (int iLoop = 0; iLoop < s.length; iLoop++) {
            byte[] temp = getBytes(s[iLoop]);
            for (int jLoop = 0; jLoop < bLength; jLoop++) {
                buf[iLoop * bLength + jLoop] = temp[jLoop];
            }
        }
        return buf;
    }


    public static List<short[]> convertByte(Map<String ,List<short[]>> bytesMap) {
        List<short[]> allmixbytes=new ArrayList<short[]>();
        for(List<short[]> userbytes:bytesMap.values()){
            int lengh=0;
            for(short[] bytes:userbytes){
                lengh+=bytes.length;
            }
            short[] result = new short[lengh];
            int offset=0;
            for (short[] array : userbytes) {
                System.arraycopy(array, 0, result, offset, array.length);
                offset += array.length;
            }
            allmixbytes.add(result);
        }
        return  allmixbytes;
    }


    public static byte[] convertByte(List<byte[]>  userbytes) {
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
        return result;
    }
    public static List<byte[]> convertByte(byte[] bytes,int times) {
        List<byte[]> list=new ArrayList<>();
        int lengh=bytes.length/ times;
        int offset=0;
        for(int i=0;i<times;i++){
            byte[] timebyte=new byte[lengh];
            System.arraycopy(bytes,offset,timebyte,0,timebyte.length);
            list.add(timebyte);
        }
        return list;
    }

    /****************************************/
    public int[] Bytes2Ints(byte[] buf) {
        byte bLength = 4;
        int[] s = new int[buf.length / bLength];
        for (int iLoop = 0; iLoop < s.length; iLoop++) {
            byte[] temp = new byte[bLength];
            for (int jLoop = 0; jLoop < bLength; jLoop++) {
                temp[jLoop] = buf[iLoop * bLength + jLoop];
            }
            s[iLoop] = getInt(temp);
            System.out.println("2out->"+s[iLoop]);
        }
        return s;
    }

    public byte[] Ints2Bytes(int[] s) {
        byte bLength = 4;
        byte[] buf = new byte[s.length * bLength];
        for (int iLoop = 0; iLoop < s.length; iLoop++) {
            byte[] temp = getBytes(s[iLoop]);
            System.out.println("1out->"+s[iLoop]);
            for (int jLoop = 0; jLoop < bLength; jLoop++) {
                buf[iLoop * bLength + jLoop] = temp[jLoop];
            }
        }
        return buf;
    }

    /****************************************/
    public long[] Bytes2Longs(byte[] buf) {
        byte bLength = 8;
        long[] s = new long[buf.length / bLength];
        for (int iLoop = 0; iLoop < s.length; iLoop++) {
            byte[] temp = new byte[bLength];
            for (int jLoop = 0; jLoop < bLength; jLoop++) {
                temp[jLoop] = buf[iLoop * bLength + jLoop];
            }
            s[iLoop] = getLong(temp);
        }
        return s;
    }

    public byte[] Longs2Bytes(long[] s) {
        byte bLength = 8;
        byte[] buf = new byte[s.length * bLength];
        for (int iLoop = 0; iLoop < s.length; iLoop++) {
            byte[] temp = getBytes(s[iLoop]);
            for (int jLoop = 0; jLoop < bLength; jLoop++) {
                buf[iLoop * bLength + jLoop] = temp[jLoop];
            }
        }
        return buf;
    }


    public static byte[] convertShortDataByte(List<ShortData> list) {
        if(list==null){
            return  null;
        }
        int lengh=0;
        for(ShortData data:list){
            lengh+=data.getmSize();
        }
        byte[] result=new byte[lengh];
        int offset=0;
        for (ShortData data:list) {
            System.arraycopy(data.getByteBuffer(), 0, result, offset, data.getmSize());
            offset += data.getmSize();
        }
        return  result;
    }
}
