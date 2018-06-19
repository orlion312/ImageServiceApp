package com.example.or.imageserviceapp;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.io.File;
import java.util.List;

public class ImageService extends Service {
    IntentFilter intentFilter = new IntentFilter();
    BroadcastReceiver broadcastReceiver;
    List<File> pictures;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startID) {
        Toast.makeText(this, "Image service started...", Toast.LENGTH_LONG).show();
        this.broadcastReceiver = new BroadcastReceiver() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onReceive(Context context, Intent intent) {
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (networkInfo != null) {
                    if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                        if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                            //start transfer
                            startTransfer(context);
                        }
                    }
                }
            }
        };
        this.registerReceiver(this.broadcastReceiver, intentFilter);
        return START_STICKY;
    }

    /**
     * startTransfer function.
     * starts transfer the photos.
     * @param context
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startTransfer(Context context) {
        //set notification progress bar
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "default");
        final int notifyId = 1;
        final NotificationManager NM = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        builder.setSmallIcon(R.drawable.ic_launcher_background);
        builder.setContentTitle("Passing images...");
        builder.setContentText("Passing in progress...");
        //start the transfer
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int barState = 0;
                    updatePicsFilesList();
                    for (File pic : pictures) {
                        //crete new tcp client to talk with server
                        TcpClient tcpClient = new TcpClient(pic);
                        //talk to image service and send him the photo
                        tcpClient.startCommunication();
                        //update the progress bar
                        barState = barState + 100 / pictures.size();
                        builder.setProgress(100, barState, false);
                        NM.notify(notifyId, builder.build());

                    }
                    //finish
                    builder.setProgress(0, 0, false);
                    builder.setContentTitle("Finished transfer!");
                    builder.setContentText("Finished transfer!");
                    NM.notify(notifyId, builder.build());
                } catch (Exception ex) {
                }
            }
        }).start();
    }

    /**
     * onCreate function.
     * defines what happens when service is on.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        //brodcast reciver issues ..
        this.intentFilter.addAction("android.net.wifi.supplicant.CONNECTION_CHANGE");
        this.intentFilter.addAction("android.net.wifi.STATE_CHANGE");
    }

    /**
     * onDestroy function.
     * defines what happens when service is destroyed.
     */
    @Override
    public void onDestroy() {
        Toast.makeText(this, "Image service stopped...", Toast.LENGTH_LONG).show();
        this.unregisterReceiver(this.broadcastReceiver);
    }

    /**
     * getOneFile function.
     * gets one file from dcim
     * @param dir - dir of files
     * @param picsFilesList - list of pic files
     */
    public void getOneFile(File dir, List<File> picsFilesList) {
        File[] dirFiles = dir.listFiles();
        int len = dirFiles.length;
        for (int i=0; i <len; i++) {
            if (dirFiles[i].isDirectory()) {
                getOneFile(dirFiles[i], picsFilesList);
            } else if(dirFiles[i].toString().contains(".jpg")) {
                picsFilesList.add(dirFiles[i]);
            }
        }
    }

    /**
     * updatePicsFilesList function.
     * updates files list (our member)
     */
    public void updatePicsFilesList() {
        File dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        //get the dirs
        File[] fileOrDir = dcim.listFiles();
        List<File> picsFilesList = new ArrayList<File>();
        int len =fileOrDir.length;
        if (fileOrDir != null) {
            for (int i=0; i <len; i++) {
                //check if dir
                if (fileOrDir[i].isDirectory()) {
                    getOneFile(fileOrDir[i], picsFilesList);
                } else if(fileOrDir[i].toString().contains(".jpg")) { //check if file
                    picsFilesList.add(fileOrDir[i]);
                }
            }
        }
        //update the member
        files = picsFilesList;
    }
}
