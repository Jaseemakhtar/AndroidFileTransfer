package com.jsync.fileshare;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by jaseem on 4/11/18.
 */

public class ConnectService extends Service implements Runnable, Handler.Callback {
    private ServerSocket serverSocket = null;
    private Socket client;
    private Thread mThread;
    private IBinder binder = new LocalBinder();
    public Socket getClient() {
        return client;
    }

    public class LocalBinder extends Binder {
        ConnectService getService(){
            return ConnectService.this;
        }
    }
    private ConnectionListener connectionListener;
    private Message message;
    private Handler handler;
    private final int ON_ERROR = 11;
    private final int ON_CONNECT = 12;

    public void setConnectionListener(ConnectionListener connectionListener){
        this.connectionListener = connectionListener;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mThread = new Thread(this);
        handler = new Handler(this);
        message = new Message();
        message.setTarget(handler);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        /*if (client != null)
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        if (serverSocket != null)
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }*/


        if (mThread != null)
            try {
                mThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        mThread = null;

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        stopSelf();
        return true;
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
            sendMessage(ON_ERROR, e.getMessage());
        }
        try{
            client = serverSocket.accept();
            sendMessage(ON_CONNECT, null);
            stopSelf();
        } catch (IOException e) {
            e.printStackTrace();
            sendMessage(ON_ERROR, e.getMessage());
        }
        mThread = null;
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what){
            case ON_CONNECT:
                connectionListener.onConnected();
                break;

            case ON_ERROR:
                connectionListener.onError(msg.getData().getString("ERROR"));
                break;
        }
        return true;
    }

    public void sendMessage(int type, String msg){
        Bundle bundle = new Bundle();
        message.what = type;
        if (type == ON_CONNECT){

        }else {
            bundle.putString("ERROR",msg);
        }
        message.setData(bundle);
        handler.sendMessage(message);
    }

    public interface ConnectionListener{
        void onConnected();
        void onError(String err);
    }
}
