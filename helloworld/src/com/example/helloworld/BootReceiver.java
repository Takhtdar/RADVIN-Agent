package com.example.helloworld;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import android.content.SharedPreferences;


public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "RADVIN";

    @Override
    public void onReceive(Context context, Intent intent) {
          
      SharedPreferences prefs = context.getSharedPreferences("RADVINPrefs", Context.MODE_PRIVATE);
      boolean startOnBoot = prefs.getBoolean("boot_enabled", true);

      if (startOnBoot) {
        Intent serviceIntent = new Intent(context, ClipboardLoggerService.class);
        context.startService(serviceIntent);
        Log.d(TAG, "Boot completed, starting clipboard service");
      }
    }
}

