package com.youngsee.mirrorplayer.receiver;

import com.youngsee.mirrorplayer.activity.MirrorActivity;
import com.youngsee.mirrorplayer.common.Actions;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Actions.BOOT_ACTION)) {
            context.startActivity(new Intent(context, MirrorActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

}
