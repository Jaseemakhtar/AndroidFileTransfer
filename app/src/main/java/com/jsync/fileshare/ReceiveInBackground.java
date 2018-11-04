package com.jsync.fileshare;

import android.os.AsyncTask;
import android.os.Environment;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by jaseem on 8/10/18.
 */

public class ReceiveInBackground extends AsyncTask<Void,String,String> {
    private ReceiveGuide receiveGuide;
    private ServerSocket serverSocket = null;
    private Socket clientSocket = null;
    private InputStream inputStream = null;
    private OutputStream outputStream = null;

    @Override
    protected void onCancelled() {
        super.onCancelled();
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }

            if (clientSocket != null){
                clientSocket.close();
            }

            if (inputStream != null){
                inputStream.close();
            }

            if (outputStream != null){
                outputStream.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String doInBackground(Void... voids) {

        try {
            serverSocket = new ServerSocket(5273);
        } catch (IOException e) {
            e.printStackTrace();
            return "Unable to setup server on this port";
        }

        try {

            clientSocket = serverSocket.accept();
            publishProgress("Connected Client: " + clientSocket.getInetAddress());

        } catch (IOException e) {
            e.printStackTrace();
            return "Unable to accept client connection";
        }

        try {
            inputStream = clientSocket.getInputStream();
            DataInputStream dos = new DataInputStream(inputStream);
            String fileName = dos.readUTF();
            long fileLength = dos.readLong();

            publishProgress("File : Name: " + fileName + " : Length: " + fileLength);

            File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/JFileShare");
            if (!directory.exists()){
                directory.mkdirs();
            }

            outputStream = new FileOutputStream(directory + "/" + fileName);
            int read;
            int total = 0;
            byte[] bytes = new byte[4 * 1024];

            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
                total += read;
                publishProgress(total + "");
            }
            dos.close();
            outputStream.close();
            inputStream.close();
            serverSocket.close();
            return "Completed";

        } catch (IOException e) {
            e.printStackTrace();
            return "Error in the stream";
        }
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        if (receiveGuide != null)
            receiveGuide.progressUpdate(values[0]);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (receiveGuide != null)
            receiveGuide.onComplete(s);
    }

    public void setReceiveGuide(ReceiveGuide receiveGuide){
        this.receiveGuide = receiveGuide;
    }

    public interface ReceiveGuide{
        void progressUpdate(String per);
        void onComplete(String msg);
    }

}
