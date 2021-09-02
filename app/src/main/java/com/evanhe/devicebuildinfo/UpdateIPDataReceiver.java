package com.evanhe.devicebuildinfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class UpdateIPDataReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String data = intent.getStringExtra("proxy");
            MainActivity.updateIPData(context, data);
        } catch (Exception e) {
            Toast.makeText(context, "Error", Toast.LENGTH_LONG).show();
        }
    }
}
