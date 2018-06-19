package com.example.or.imageserviceapp;

import android.app.NotificationManager;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

class TcpClient {
    private Socket socket;
    private OutputStream outputStream;

    public TcpClient() {
    }

    public void connect() {
        try {
            InetAddress serverAddr = InetAddress.getByName("10.0.2.2");
            socket = new Socket(serverAddr, 6666);
            try {
                outputStream = socket.getOutputStream();
            } catch (Exception e) {
                Log.e("TCP", "S: Error:", e);
            }
        } catch (Exception e) {
            Log.e("TCP", "S: Error:", e);
        }

    }

    public void startConnection(final NotificationManager notificationManager, final NotificationCompat.Builder builder) {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                connect();
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream));
                File dcim = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Camera");
                if (dcim == null) {
                    return;
                }
                File[] files = dcim.listFiles();
                double numberOfPictures = files.length;
                double count = 0;

                if (files != null) {
                    for (File file : files) {
                        try {
                            FileInputStream fis = new FileInputStream(file);
                            Bitmap bm = BitmapFactory.decodeStream(fis);
                            byte[] imgbyte = getBytesFromBitmap(bm);
                            try {
                                int imageLength = imgbyte.length;

                                //sends the size of the array bytes.
                                String picSizeString = imageLength + "";
                                outputStream.write(picSizeString.getBytes(), 0, picSizeString.getBytes().length);
                                outputStream.flush();
                                Thread.sleep(100);

                                //sends the name of file.
                                String fileNameString = file.getName();
                                outputStream.write(fileNameString.getBytes(), 0, fileNameString.getBytes().length);
                                outputStream.flush();
                                Thread.sleep(100);

                                //sends the array bytes.
                                outputStream.write(imgbyte, 0, imgbyte.length);
                                outputStream.flush();
                                Thread.sleep(500);

                            } catch (Exception e1) {
                                Log.e("TCP", "S: Error:", e1);
                            }
                        } catch (Exception e2) {
                            Log.e("TCP", "S: Error:", e2);
                        }

                        count++;
                        int myProgress = (int) ((count / numberOfPictures) * 100);
                        String message = myProgress + "%";
                        builder.setProgress(100, myProgress, false);
                        builder.setContentText(message);
                        notificationManager.notify(1, builder.build());

                    }
                    try {
                        String toSend = "Stop Transfer\n";
                        outputStream.write(toSend.getBytes(), 0, toSend.getBytes().length);
                        outputStream.flush();

                        builder.setContentTitle("Finish transfer");
                        builder.setContentText("Image Service finish backing up your photos");
                        notificationManager.notify(1, builder.build());

                    } catch (Exception e3) {
                        Log.e("TCP", "S: Error:", e3);

                        builder.setContentTitle("Error");
                        builder.setContentText("Image Service could not transfer your photos");
                        notificationManager.notify(1, builder.build());
                    }
                }
            }
        });

        thread.start();
    }

    private byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 70, stream);
        return stream.toByteArray();
    }

    public void closeConnection() {
        try {
            this.socket.close();
        } catch (IOException e) {
            Log.e("TCP", "S: Error:", e);
        }
    }
}

}
