package com.jsync.fileshare;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by jaseem on 4/11/18.
 */

public class ConnectService extends Service implements Runnable {
    public static boolean isConnected;
    private ServerSocket serverSocket = null;
    private Socket client = null;
    private Thread mThread;

    @Override
    public void onCreate() {
        super.onCreate();
        mThread = new Thread(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (client != null) {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mThread.start();
        return START_STICKY;
    }


    @Override
    public void run() {
        try{
            serverSocket = new ServerSocket(7313);
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("Background","Error creating server socket");
        }

        try{
            client = serverSocket.accept();
            isConnected = true;
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("Background", "Error accepting connection");
        }

    }
}
