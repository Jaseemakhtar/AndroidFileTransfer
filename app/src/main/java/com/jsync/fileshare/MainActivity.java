package com.jsync.fileshare;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import java.io.File;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btnSend, btnReceive;

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
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View view) {
       switch (view.getId()){
            case R.id.btn_main_send:
                Intent intent = new Intent(this, SendingActivity.class);
                startActivity(intent);
                break;

            case R.id.btn_main_receive:
                Intent r = new Intent(this, ReceivingActivity.class);
                startActivity(r);
                break;
        }

    }

}
