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

/**
 * the class to create the app
 */
public class MainActivity extends AppCompatActivity {

    /**
     * the method run when the app begin
     */
    protected void onCreate(Bundle savedInstanceState) {
        verifyPermission();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * the method starts the Service
     * when the user click the start button
     * @param view - View obj.
     */
    public void startService(View view) {
        Intent intent = new Intent(this, ImageService.class);
        startService(intent);
    }

    /**
     * the method stops the Service
     * when the user click the stop button
     * @param view - View obj.
     */
    public void stopService(View  view) {
        Intent intent = new Intent(this, ImageService.class);
        stopService(intent);
    }

    /**
     * the method responsible to verify the premission to pictures
     */
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
