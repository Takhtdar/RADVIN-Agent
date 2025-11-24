package com.example.helloworld;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.net.URL;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.io.FileReader;
import java.io.BufferedReader;
import android.util.Log;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

public class HelloActivity extends Activity {

    private CheckBox serviceToggle;
    private CheckBox bootToggle;
    private TextView statusText;
    private EditText serverHostInput;
    private EditText apiKeyInput;
    private Button syncButton;
    private Button restartButton;


    private static final String PREFS_NAME = "RADVINPrefs";
    private static final String KEY_SERVICE_ENABLED = "service_enabled";
    private static final String KEY_BOOT_ENABLED = "boot_enabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        serviceToggle = (CheckBox) findViewById(R.id.service_toggle);
        bootToggle = (CheckBox) findViewById(R.id.boot_toggle);
        statusText = (TextView) findViewById(R.id.status_text);
        restartButton = (Button) findViewById(R.id.restart_button);
        syncButton = (Button) findViewById(R.id.sync_button);
        apiKeyInput = (EditText) findViewById(R.id.api_key_input);
        serverHostInput = (EditText) findViewById(R.id.server_host_input);

        



        // Load saved preferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean serviceEnabled = prefs.getBoolean(KEY_SERVICE_ENABLED, true);
        boolean bootEnabled = prefs.getBoolean(KEY_BOOT_ENABLED, true);
        String apiKey = prefs.getString("api_key", "test"); //apiKeyInput.getText().toString();


        Toast.makeText(HelloActivity.this, apiKey, Toast.LENGTH_SHORT).show();
        serverHostInput.setText(prefs.getString("server_host", "http://192.168.1.84:54700"));
        apiKeyInput.setText(apiKey);
        //prefs.edit().putString("api_key", apiKey).apply();
        serviceToggle.setChecked(serviceEnabled);
        bootToggle.setChecked(bootEnabled);
        

        // Update status
        updateStatus();

        // Apply settings on button click
        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean enabled = serviceToggle.isChecked();
                boolean startOnBoot = bootToggle.isChecked();
                String newKey = apiKeyInput.getText().toString();

                String newHost = serverHostInput.getText().toString();

                                
                // Save settings
                final SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                final SharedPreferences.Editor editor = prefs.edit();

                editor.putString("server_host", newHost).apply();
                editor.putBoolean(KEY_SERVICE_ENABLED, enabled);
                editor.putBoolean(KEY_BOOT_ENABLED, startOnBoot);
                editor.putString("api_key", newKey).apply();

                editor.apply();

                // Stop service first
                stopService(new Intent(HelloActivity.this, ClipboardLoggerService.class));

                // Start if enabled
                if (enabled) {
                    startService(new Intent(HelloActivity.this, ClipboardLoggerService.class));
                }

                updateStatus();
                //Toast.makeText(HelloActivity.this, "Settings applied", Toast.LENGTH_SHORT).show();
        }
        });


        syncButton.setOnClickListener(new View.OnClickListener() {
        
          @Override
          public void onClick(View v) {
            
        sendUnsentEntries();
        Toast.makeText(HelloActivity.this, "Sync started...", Toast.LENGTH_SHORT).show();
        }
        });


    }


private int getClipboardCount() {
    try {
        File file = new File(getFilesDir(), "clipboard.log");
        if (!file.exists()) return 0;

        BufferedReader br = new BufferedReader(new FileReader(file));
        int count = 0;
        while (br.readLine() != null) count++;
        br.close();
        return count;
    } catch (Exception e) {
        return 0;
    }
}


    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Toast.makeText(this, "Paused...", Toast.LENGTH_SHORT).show();
    }

    private void updateStatus() {
    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    boolean enabled = prefs.getBoolean(KEY_SERVICE_ENABLED, true);
    statusText.setText("Status: " + (enabled ? "Monitoring ✅" : "Paused ❌"));

    // Simulate count (later: read from file or SQLite)
    ClipboardDatabaseHelper dbHelper = new ClipboardDatabaseHelper(this);
    int count = dbHelper.getUnsentCount();
    ((TextView) findViewById(R.id.count_text)).setText("Saved: " + count + " sentences");
    
    }



private void sendUnsentEntries() {
    new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                ClipboardDatabaseHelper dbHelper = new ClipboardDatabaseHelper(HelloActivity.this);
                android.database.Cursor cursor = dbHelper.getUnsentEntries();

                if (!cursor.moveToFirst()) {
                    Log.d("RADVIN", "No unsent entries");
                    return;
                }

                // Read all unsent entries
                JSONArray entries = new JSONArray();
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                String apiKey = prefs.getString("api_key", "NO_KEY");
                String serverHost = prefs.getString("server_host", "http://192.168.1.66:54700"); // Default host


                do {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
                    String text = cursor.getString(cursor.getColumnIndexOrThrow("text"));
                    String book = cursor.getString(cursor.getColumnIndexOrThrow("book"));

                    JSONObject obj = new JSONObject();
                    obj.put("id", id);
                    obj.put("text", text);
                    obj.put("book", book);
                    entries.put(obj);

                } while (cursor.moveToNext());

                cursor.close();

                // Send to server
                // URL url = new URL("http://192.168.1.66:5000/sync-F1");
                URL url = new URL(serverHost + "/sync"); // Changed from hardcoded URL

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + apiKey);

                OutputStream os = conn.getOutputStream();
                os.write(entries.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    br.close();

                    // Parse returned IDs and mark as sent
                    JSONArray sentIds = new JSONArray(response.toString());
                    for (int i = 0; i < sentIds.length(); i++) {
                        long id = sentIds.getLong(i);
                        dbHelper.markAsSent(id);
                    }

                    Log.d("RADVIN", "Synced " + sentIds.length() + " entries");
                } else {
                    Log.e("RADVIN", "Sync failed: " + responseCode);
                }

            } catch (Exception e) {
                Log.e("RADVIN", "Sync error", e);
            }
        }
    }).start();
}

}
