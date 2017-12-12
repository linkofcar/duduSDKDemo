package com.czl.chatClient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Administrator on 2017/9/21.
 */

public class NetBroadCastReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();

            if (netInfo != null && netInfo.isAvailable()) {
                DuduClient.getInstance().reconnect();
            } else {
                DuduClient.getInstance().closeConnect(false);
            }
        } else if ("android.intent.action.BOOT_COMPLETED".equals(intent
                .getAction())) {
            DuduClient.getInstance().reconnect();
        } else if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
            DuduClient.getInstance().reconnect();
        }
    }
}
