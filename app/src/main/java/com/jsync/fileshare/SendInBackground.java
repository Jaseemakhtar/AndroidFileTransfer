package com.jsync.fileshare;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by jaseem on 7/10/18.
 */

class SendInBackground extends AsyncTask<Uri,String,String> {

    private SendingGuide sendingGuide;
    private String ip;
    private Context context;

    public SendInBackground(String ip, Context context){
        this.ip = ip;
        this.context = context;
    }

    public void setSendingGuide(SendingGuide sendingGuide){
        this.sendingGuide = sendingGuide;
    }

    @Override
    protected String doInBackground(Uri... uris) {
        Socket clientSocket;
        InputStream inputStream;
        OutputStream outputStream;


        try {
            clientSocket = new Socket(ip,5273);
            inputStream = context.getContentResolver().openInputStream(uris[0]);

            while (true){
                if (clientSocket.isConnected()){
                    outputStream = clientSocket.getOutputStream();

                    File fileToSend = new File(Utils.getRealPathFromURI(context,uris[0]));
                    String fileName = fileToSend.getName();
                    long fileLength = fileToSend.length();

                    DataOutputStream d = new DataOutputStream(outputStream);
                    d.writeUTF(fileName);
                    d.writeLong(fileLength);

                    int read;
                    int total = 0;
                    byte[] bytes = new byte[4 * 1024];
                    while ((read = inputStream.read(bytes)) != -1){
                        outputStream.write(bytes, 0, read);
                        total += read;
                        publishProgress(String.valueOf(total / 1024));
                    }
                    inputStream.close();
                    outputStream.close();
                    clientSocket.close();
                    return "Completed";
                }else {
                    publishProgress("Waiting");
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "FileNotFound";
        } catch (IOException e) {
            e.printStackTrace();
            return "IOException";
        }

    }


    @Override
    protected void onProgressUpdate(String ... values) {
        super.onProgressUpdate(values);
        if (sendingGuide != null)
            sendingGuide.onSubmitProgress(values[0]);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (sendingGuide != null)
            sendingGuide.onComplete(s);
    }


    interface SendingGuide{
        void onComplete(String msg);
        void onSubmitProgress(String percent);
    }
}