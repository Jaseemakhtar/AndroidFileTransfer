package com.jsync.fileshare;

import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ReceivingActivity extends AppCompatActivity implements ReceiveInBackground.ReceiveGuide {
    TextView txtStatus;
    TextView txtFileName;
    TextView txtProgress;
    ProgressBar progressBar;
    ReceiveInBackground receiveInBackground;
    SearchSignalView searchSignalView;
    boolean isWaiting = true;
    int fileSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiving);

        txtStatus = findViewById(R.id.txt_receive_status);
        txtFileName = findViewById(R.id.txt_receive_fileName);
        txtProgress = findViewById(R.id.txt_receive_progress);
        progressBar = findViewById(R.id.pb_receive_progress);

        searchSignalView = findViewById(R.id.surface_view);

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
        txtStatus.setText("Your Ip: " + ip);


        receiveInBackground = new ReceiveInBackground();
        receiveInBackground.setReceiveGuide(this);
        receiveInBackground.execute();
    }

    @Override
    public void progressUpdate(String per) {
        if (per.contains("File")){
            String[] arr = per.split(":");
            String fileName = arr[2];
            String fileLength = arr[4];
            txtFileName.setText(fileName + " ==> " + fileLength + "/bytes");
            fileLength = fileLength.trim();
            fileSize = (int) (Long.valueOf(fileLength) / 1024);
            progressBar.setVisibility(View.VISIBLE);
        }else if(per.contains("Connected")){
            String[] arr = per.split(":");
            String client = arr[1];
            isWaiting = false;
            txtStatus.setText("Connected: " + client);
        }else {
            long receivedBytes = Integer.valueOf(per) / 1024;
            int received = (int) ((Math.floor(receivedBytes) / fileSize) * 100);
            progressBar.setProgress(received);
            txtProgress.setText(receivedBytes+ "/kb");
        }
    }

    @Override
    public void onComplete(String msg) {
        txtStatus.setText(msg);
    }

    @Override
    protected void onResume() {
        super.onResume();
        searchSignalView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        searchSignalView.pause();
    }

    @Override
    public void onBackPressed() {
        if (receiveInBackground != null){
            if (!isWaiting && AsyncTask.Status.RUNNING == receiveInBackground.getStatus()){
                return;
            }
            receiveInBackground.cancel(true);
        }

        super.onBackPressed();
    }
}
