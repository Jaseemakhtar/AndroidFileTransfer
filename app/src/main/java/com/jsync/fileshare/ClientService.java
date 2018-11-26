package com.jsync.fileshare;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import com.jsync.fileshare.utils.RSocket;
import com.jsync.fileshare.utils.Utils;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by jaseem on 21/11/18.
 */

public class ClientService extends Service implements Runnable, Handler.Callback{
    private IBinder clientBinder = new ClientBinder();
    private Socket client;
    private Thread thread;
    private boolean wouldRun;

    private final int ON_START = 44;
    private final int ON_PROGRESS = 43;
    private final int ON_ERROR = 23;
    private final int ON_CONNECTED = 32;
    private final int ON_COMPLETED = 41;
    private ConnectionListener connectionListener = null;

    private String serverIp;
    private RSocket rSocket;
    private Handler handler;
    private Message message;

    private ArrayList<Uri> fileUri = new ArrayList<>();
    private int indicator = 0;
    private boolean isBounded;

    class ClientBinder extends Binder{
        ClientService getService(){
            return ClientService.this;
        }
    }

    interface ConnectionListener{
        void onClientConnected();
        void onClientStarted(String fileName);
        void onClientsProgress(int progress, int item);
        void onClientCompleted();
        void onClientError(String msg);
    }

    public void setConnectionListener(ConnectionListener connectionListener){
        this.connectionListener = connectionListener;
    }

    public void setBound(boolean b){
        isBounded = b;
    }

    public void setWouldRun(boolean wouldRun, ArrayList<Uri> fileUri){
        this.wouldRun = wouldRun;
        this.fileUri.addAll(fileUri);
    }

    public boolean isRunning(){
        return wouldRun;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return clientBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        rSocket = RSocket.getInstance();
        handler = new Handler(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        serverIp = intent.getStringExtra("ip");
        thread = new Thread(this);
        thread.start();
        return START_STICKY;
    }

    @Override
    public void run() {
        DataOutputStream dataOutputStream = null;
        DataInputStream dataInputStream = null;
        OutputStream outputStream = null;
        InputStream inputStream = null;
        FileInputStream fileInputStream = null;
        File file;
        long fileSize;
        String fileName;
        byte[] bytes;

        try {
            client = new Socket(serverIp, 7313);
            while(!isBounded){
                Log.i("Socket", "Inside Loop");
            }
            sendMessage(ON_CONNECTED, "connected", -1, -1);
            Log.i("Socket", "Connected: " + client.getInetAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            outputStream = client.getOutputStream();
            inputStream = client.getInputStream();
            dataInputStream = new DataInputStream(inputStream);
            dataOutputStream = new DataOutputStream(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true){
            if(wouldRun){

                try{
                    Log.i("Socket", "Inside sending queue");
                    for(int i = indicator; i < fileUri.size(); i++) {

                        file = new File(Utils.getRealPathFromURI(this, fileUri.get(i)));
                        fileSize = file.length();
                        fileName = file.getName();

                        bytes = new byte[4 * 1024];
                        fileInputStream = new FileInputStream(file);
                        dataOutputStream.writeUTF("receive");

                        String ack = dataInputStream.readUTF();

                        if (!ack.equals("ok")) {
                            return;
                        }

                        dataOutputStream.writeUTF(fileName);
                        dataOutputStream.writeLong(fileSize);

                        int read;
                        int total = 0;
                        int fileLength = (int) (fileSize / 1024);
                        long receivedKB;
                        int percent;

                        sendMessage(ON_START, fileName, -1, -1);

                        while ((read = fileInputStream.read(bytes)) > 0) {
                            outputStream.write(bytes, 0, read);
                            total += read;

                            receivedKB = total / 1024;
                            percent = (int) ((Math.floor(receivedKB) / fileLength) * 100);
                            sendMessage(ON_PROGRESS, "progress", percent, i);
                        }

                        sendMessage(ON_COMPLETED, "completed", -1, -1);

                        fileInputStream.close();
                        dataOutputStream.flush();
                        outputStream.flush();

                        ack = dataInputStream.readUTF();

                        if (ack.equals("success")) {
                            Log.i("Socket", "Client received it successfully");
                        }

                        indicator = i + 1;
                        Log.i("Socket", "Sending File: " + indicator);
                    }
                    wouldRun = false;
                } catch (IOException e) {
                    e.printStackTrace();
                    wouldRun = false;
                    break;
                }
            }else{
                Log.i("Socket", "Outside sending queue");
            }
        }
    }

    private void sendMessage(int type, String msg, int progress, int item){
        message = new Message();
        message.setTarget(handler);
        message.what = type;

        Bundle bundle =new Bundle();

        switch (type){
            case ON_CONNECTED:
                bundle.putString("connected",msg);
                break;

            case ON_PROGRESS:
                bundle.putInt("progress", progress);
                bundle.putInt("item", item);
                break;

            case ON_COMPLETED:
                bundle.putString("completed", msg);
                break;

            case ON_START:
                bundle.putString("start", msg);
                break;

            case ON_ERROR:
                bundle.putString("error", msg);
                break;
        }

        message.setData(bundle);
        handler.sendMessage(message);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what){
            case ON_CONNECTED:
                if(connectionListener != null)
                    connectionListener.onClientConnected();
                return true;

            case ON_PROGRESS:
                Bundle bundle = msg.getData();
                if(connectionListener != null)
                    connectionListener.onClientsProgress(bundle.getInt("progress"), bundle.getInt("item"));
                return true;

            case ON_ERROR:
                if(connectionListener != null)
                    connectionListener.onClientError(msg.getData().getString("error"));
                return true;

            case ON_COMPLETED:
                if(connectionListener != null)
                    connectionListener.onClientCompleted();
                return true;

            case ON_START:
                if(connectionListener != null)
                    connectionListener.onClientStarted(msg.getData().getString("start"));
                return true;

            default:
                return false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
