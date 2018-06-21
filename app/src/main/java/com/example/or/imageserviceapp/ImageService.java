package com.example.or.imageserviceapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

/**
 * the class for the ImageService
 */
public class ImageService extends Service {

    private BroadcastReceiver broadcastReceiver;
    private TcpClient tcpClient;

    /**
     * the constructor of the class
     */
    public ImageService() { }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * the method responsible to create the service
     * when the user click start
     */
    public void onCreate() {
        super.onCreate();

        tcpClient = new TcpClient();
    }

    /**
     * the method responsible to start the command
     * @param intent - an obj
     * @param flag - an Integer that represent a flag
     * @param startId - the start position
     * @return - an Integer
     */
    public int onStartCommand(Intent intent, int flag, int startId) {

        final IntentFilter theFilter = new IntentFilter();
        theFilter.addAction("android.net.wifi.supplicant.CONNECTION_CHANGE");
        theFilter.addAction("android.net.wifi.STATE_CHANGE");
        this.broadcastReceiver = new BroadcastReceiver() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onReceive(Context context, Intent intent) {
                WifiManager wifiManager = (WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

                final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationChannel channel = new NotificationChannel("default",
                        "Channel name",
                        NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription("Channel description");
                notificationManager.createNotificationChannel(channel);
                final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "default");
                builder.setSmallIcon(R.drawable.ic_launcher_background);
                builder.setContentTitle("Transferring Images status");
                builder.setContentText("In progress");


                if (networkInfo != null) {
                    if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                        if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                            tcpClient.startConnection(notificationManager, builder);
                        }
                    }
                }
            }
        };
        this.registerReceiver(this.broadcastReceiver, theFilter);
        Toast.makeText(this, "Service starting...", Toast.LENGTH_SHORT).show();
        return START_STICKY;
    }

    /**
     * the method responsible to destroy the service
     * when the user click stop
     */
    public void onDestroy() {
        Toast.makeText(this, "Service ending...", Toast.LENGTH_SHORT).show();
        tcpClient.closeConnection();
    }

}
