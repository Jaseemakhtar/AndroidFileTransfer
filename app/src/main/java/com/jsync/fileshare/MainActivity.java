package com.jsync.fileshare;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jsync.fileshare.listing.ListFiles;
import com.jsync.fileshare.permissionmanager.PermissionManager;

import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btnSend, btnReceive;
    private final int PICKFILE_REQUEST_CODE = 345;
    private SearchSignalView searchSignalView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnSend = findViewById(R.id.btn_main_send);
        btnReceive = findViewById(R.id.btn_main_receive);

        btnSend.setOnClickListener(this);
        btnReceive.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_main_send:
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.setType("*/*");
                startActivityForResult(i, PICKFILE_REQUEST_CODE);
                break;

            case R.id.btn_main_receive:
                Intent r = new Intent(this, ReceivingActivity.class);
                r.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(r);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case PICKFILE_REQUEST_CODE:
                if (resultCode == RESULT_OK && data != null){
                    Intent send = new Intent(this,SendingActivity.class);
                    send.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    send.putExtra("Uri",data.getData().toString());
                    startActivity(send);
                }

            break;
        }
    }


}
