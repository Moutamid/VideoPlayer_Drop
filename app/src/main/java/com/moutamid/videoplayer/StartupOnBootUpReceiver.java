package com.moutamid.videoplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

public class StartupOnBootUpReceiver extends BroadcastReceiver {
    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onReceive(Context context, Intent intent) {
        if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent activityIntent = new Intent(context, MainActivity.class);
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(activityIntent);
        }
    }
}
