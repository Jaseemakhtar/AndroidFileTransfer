package com.jsync.fileshare;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

public class SendingActivity extends AppCompatActivity implements SendInBackground.SendingGuide, View.OnClickListener {
    Uri uri;
    Button btnSend;
    TextView txtProgressPer;
    EditText edtIpAddress;
    ProgressBar progressBar;
    String ipAddress;
    SendInBackground sendInBackground;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sending);
        uri = Uri.parse(getIntent().getStringExtra("Uri"));

        btnSend = findViewById(R.id.btn_send_ip);
        txtProgressPer = findViewById(R.id.txt_send_progress);
        edtIpAddress = findViewById(R.id.edt_send_ip);
        progressBar = findViewById(R.id.pb_send_progress);

        btnSend.setOnClickListener(this);
        String path = new File(Utils.getRealPathFromURI(this,uri)).getName();
        txtProgressPer.setText(path);
    }

    @Override
    public void onComplete(String msg) {
        String prev = txtProgressPer.getText().toString();
        txtProgressPer.setText(prev + " - " + msg);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onSubmitProgress(String  percent) {
        txtProgressPer.setText(percent + "/Kb");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_send_ip:
                btnSend.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
                ipAddress = edtIpAddress.getText().toString();
                sendInBackground = new SendInBackground(ipAddress, SendingActivity.this);
                sendInBackground.setSendingGuide(SendingActivity.this);
                sendInBackground.execute(uri);
                break;
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (sendInBackground != null){
            if (sendInBackground.getStatus() == AsyncTask.Status.RUNNING){
                return;
            }

            sendInBackground.cancel(true);
        }
        super.onBackPressed();
    }
}
