package com.jsync.fileshare;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.jsync.fileshare.utils.RSocket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by jaseem on 6/11/18.
 */

public class ReceivingService extends IntentService implements Handler.Callback{
    private IBinder localBinder = new ReceiverLocalBinder();
    private RConnectionListener connectionListener;
    private Socket socket;
    private Message message;
    private Handler handler;
    private final int ON_CONNECT = 510;
    private final int ON_PROGRESS_UPDATE = 848;
    private final int ON_COMPLETE = 874;
    private final String FILE_NAME = "filename";
    private final String FILE_SIZE = "filesize";
    private final String FILE_PROGRESS = "fileprogress";
    private final String COMPLETED = "completed";

    public void setConnectionListener(RConnectionListener connectionListener){
        this.connectionListener = connectionListener;
    }

    public class ReceiverLocalBinder extends Binder{
        ReceivingService getService(){
            return ReceivingService.this;
        }
    }

    public ReceivingService()
    {
        super("Receiving Service");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(this);
        message = new Message();
        message.setTarget(handler);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);
        return localBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.i("Socket","onHandleIntent");
        RSocket rSocket = RSocket.getInstance();
        socket = rSocket.getSocket();

        DataInputStream dis;
        DataOutputStream dos;
        FileOutputStream fos;
        InputStream is;
        OutputStream os;

        try{
            is = socket.getInputStream();
            os = socket.getOutputStream();
            dis = new DataInputStream(is);
            dos = new DataOutputStream(os);

            dos.writeUTF("ok");

            String fileName = dis.readUTF();
            long fileSize = dis.readLong();
            sendMessage(ON_CONNECT, fileName, fileSize, -1);

            File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/JFileShare");
            if (!directory.exists()){
                directory.mkdirs();
            }

            fos = new FileOutputStream(directory + "/" + fileName);
            int read;
            int total = 0;
            byte[] bytes = new byte[4 * 1024];
            int fileLength = (int) (fileSize / 1024);
            long receivedKB;
            int percent;

            while (fileSize > 0 && (read = is.read(bytes)) > 0) {
                fos.write(bytes, 0, read);
                total += read;
                fileSize -= read;
                receivedKB = total / 1024;
                percent = (int) ((Math.floor(receivedKB) / fileLength) * 100);
                //Log.i("Socket","fileLength: " +  fileLength +  "total: " + total + " receivedKB: " + receivedKB + " percent: " + percent);
                sendMessage(ON_PROGRESS_UPDATE, null, -1, percent);
            }
            Log.i("Socket", "Received file - out of the loop");

            fos.flush();
            dos.writeUTF("success");
            dos.flush();
            os.flush();

            sendMessage(ON_COMPLETE, "Completed", -1, -1);
            stopSelf();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what){

            case ON_CONNECT:
                Bundle bundle = msg.getData();
                connectionListener.onStart(bundle.getString(FILE_NAME), bundle.getLong(FILE_SIZE));
                return true;

            case ON_PROGRESS_UPDATE:
                Bundle bundle1 = msg.getData();
                connectionListener.progressUpdate(bundle1.getInt(FILE_PROGRESS));
                return true;

            case ON_COMPLETE:
                Bundle bundle2 = msg.getData();
                connectionListener.onComplete(bundle2.getString(COMPLETED));
                return true;

            default:
                return false;
        }
    }

    public void sendMessage(int type, String fileName, long fileSize, int progress){

        message = new Message();
        message.setTarget(handler);

        Bundle bundle = new Bundle();
        message.what = type;

        if (type == ON_CONNECT){
            bundle.putString(FILE_NAME, fileName);
            bundle.putLong(FILE_SIZE, fileSize);
        }else if(type == ON_PROGRESS_UPDATE){
            bundle.putInt(FILE_PROGRESS, progress);
        }else if(type == ON_COMPLETE){
            bundle.putString(COMPLETED, fileName);
        }

        message.setData(bundle);
        handler.sendMessage(message);
    }

    public interface RConnectionListener{
        void onStart(String fileName, long fileSize);
        void progressUpdate(int progress);
        void onComplete(String msg);
    }
}
