package com.czl.chatClient.audio;

import com.czl.chatClient.bean.DuduUser;

import java.util.List;

/**
 * Created by Administrator on 2017/12/12.
 */

public class TimesData {
    private DuduUser fromUser;
    private byte[] bytedata;
    private List<Integer> sizeList;

    public TimesData(DuduUser fromUser, byte[] bytedata, List<Integer> sizeList) {
        this.fromUser = fromUser;
        this.bytedata = bytedata;
        this.sizeList = sizeList;
    }

    public byte[] getBytedata() {
        return bytedata;
    }

    public void setBytedata(byte[] bytedata) {
        this.bytedata = bytedata;
    }

    public List<Integer> getSizeList() {
        return sizeList;
    }

    public void setSizeList(List<Integer> sizeList) {
        this.sizeList = sizeList;
    }

    public DuduUser getFromUser() {
        return fromUser;
    }

    public void setFromUser(DuduUser fromUser) {
        this.fromUser = fromUser;
    }
}
