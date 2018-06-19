package com.example.or.imageserviceapp;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.Tag;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileWriter;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity {
    private File dcim;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        verifyPermission();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    /**
     * startService function.
     * defines what happens when user clicks the start btn.
     * @param view - View obj.
     */
    public void startService(View view) {

        Intent intent = new Intent(this, ImageService.class);
        startService(intent);

    }

    /**
     * stopService function.
     * defines what happens when user press the stop btn.
     * @param view
     */
    public void stopService(View  view) {
        Intent intent = new Intent(this, ImageService.class);
        stopService(intent);
    }

    private void verifyPermission() {
        Log.d("tag", "verifyPermission: asking user for permission");
        String permission[] = {Manifest.permission.READ_EXTERNAL_STORAGE};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), permission[0]) == PackageManager.PERMISSION_GRANTED) {
            return;
        } else {
            ActivityCompat.requestPermissions(this, permission, 1);
        }
    }
}
