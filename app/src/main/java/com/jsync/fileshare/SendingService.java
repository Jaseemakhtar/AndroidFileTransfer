package com.jsync.fileshare;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by jaseem on 6/11/18.
 */

public class SendingService extends IntentService {
    private IBinder localBinder = new SendingLocalBinder();

    public class SendingLocalBinder extends Binder{
        SendingService getService(){
            return SendingService.this;
        }
    }

    public SendingService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        /*Bundle received = intent.getExtras();

        Socket socket = pSocket.getSocket();
        DataInputStream dis = null;
        OutputStream os = null;
        InputStream is = null;
        try{
            is = socket.getInputStream();
            dis = new DataInputStream(is);

        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }
}
