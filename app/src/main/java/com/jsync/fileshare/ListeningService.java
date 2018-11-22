package com.jsync.fileshare;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.jsync.fileshare.utils.RSocket;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * Created by jaseem on 8/11/18.
 */

public class ListeningService extends Service implements Handler.Callback, Runnable{
    private IBinder listeningBinder = new ListeningBinder();
    private LConnectionListener connectionListener;
    private Socket socket;
    private RSocket rSocket;
    private Handler handler;
    private Message message;
    private Thread thread;
    private boolean wouldListen = true;
    private final String ON_ACK = "ack";
    private final int ON_RECEIVE = 876;

    class ListeningBinder extends Binder{
        ListeningService getService(){
            return ListeningService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        thread = new Thread(this);
        thread.start();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return listeningBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void run() {
        rSocket = RSocket.getInstance();
        InputStream inputStream;
        DataInputStream dataInputStream;

        while (true){
            if (wouldListen) {
                try {
                    socket = rSocket.getSocket();
                    inputStream = socket.getInputStream();
                    dataInputStream = new DataInputStream(inputStream);
                    String ack = dataInputStream.readUTF();
                    sendMessage(ON_RECEIVE, ack);
                    wouldListen = false;
                    socket = null;
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }

    public void setConnectionListener(LConnectionListener connectionListener){
        this.connectionListener = connectionListener;
    }

    public void setWouldListen(boolean b){
        wouldListen = b;
    }

    private void sendMessage(int type, String msg){
        Bundle bundle = new Bundle();
        message = new Message();
        message.setTarget(handler);

        if (type == ON_RECEIVE){
            bundle.putString(ON_ACK, msg);
        }

        message.setData(bundle);
        message.what = type;
        handler.sendMessage(message);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what){
            case ON_RECEIVE:
                Bundle bundle = msg.getData();
                String mssg = bundle.getString(ON_ACK);
                connectionListener.receivedRequest(mssg);
                return true;

            default:
                return false;
        }
    }

    public interface LConnectionListener{
        void receivedRequest(String msg);
    }
}
