package com.example.helloworld;


import android.content.SharedPreferences;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import android.os.Handler; 
import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;


public class ClipboardLoggerService extends Service {

    private static final String TAG = "RADVIN";
    private static boolean isRunning = false;



    @Override
    public void onCreate() {
        super.onCreate();

        ClipboardDatabaseHelper dbHelper = new ClipboardDatabaseHelper(this);
        dbHelper.getReadableDatabase(); // or getWritableDatabase()


    // Check if service is enabled
    SharedPreferences prefs = getSharedPreferences("RADVINPrefs", Context.MODE_PRIVATE);
    boolean enabled = prefs.getBoolean("service_enabled", true);

    if (!enabled) {
        stopSelf();
        return;
    }

        // ✅ Prevent multiple instances
    if (isRunning) {
        Log.d(TAG, "Service already running — stopping duplicate");
        stopSelf();
        return;
    }

    isRunning = true;

        startForegroundService();


        final ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        if (clipboard != null) {
            clipboard.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
                @Override
                public void onPrimaryClipChanged() {
                    if (clipboard.hasPrimaryClip()) {
                        android.content.ClipData clip = clipboard.getPrimaryClip();
                        if (clip.getItemCount() > 0) {
                            String text = clip.getItemAt(0).getText().toString();
                            Log.d(TAG, "Clipboard changed: " + text);
                            Toast.makeText(ClipboardLoggerService.this, "Copied: " + text, Toast.LENGTH_SHORT).show();
                            // String book = getCurrentBook(context); // We’ll write this
                            String book = "Test...";
                            ClipboardDatabaseHelper dbHelper = new ClipboardDatabaseHelper(ClipboardLoggerService.this);
                            dbHelper.addEntry(text, book);
                        }
                    }
                }
            });
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(ClipboardLoggerService.this, "OnStart Command Toast ....: ", Toast.LENGTH_SHORT).show();
        return START_STICKY;
    }

    private void startForegroundService() {
      // Create a notification (required for foreground service)
      Intent notificationIntent = new Intent(this, HelloActivity.class);
      PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

      Notification notification = new Notification.Builder(this)
        .setContentTitle("RADVIN is running")
        .setContentText("Learning new words from your reading")
        .setSmallIcon(android.R.drawable.ic_menu_info_details)
        .setContentIntent(pendingIntent)
        .build();

      // Start as foreground service
      startForeground(1, notification);
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }


  @Override
public void onDestroy() {
    super.onDestroy();
    isRunning = false; // ✅ Reset when service stops
}

}
