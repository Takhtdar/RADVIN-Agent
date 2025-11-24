package com.example.helloworld;


import android.util.Log;
import android.os.Handler;


public class ReturnHello {
  private static final String TAG = "RADVIN";

  public static void logCounter() {
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
          public void run() {
            // 🐾 Actions to do every 10 seconds
            handler.postDelayed(this, 3000); // re-posts itself after 10 sec
            //Log.d(TAG, "trying harder...");
          }
        };
        handler.post(runnable); 
        //Log.d(TAG, "still Trying...AAAA");
    }
}
