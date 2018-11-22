package com.jsync.fileshare;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jsync.fileshare.sharedList.ShareListModel;
import com.jsync.fileshare.sharedList.SharedListAdapter;

import java.util.ArrayList;

public class SendingActivity extends AppCompatActivity implements View.OnClickListener, ClientService.ConnectionListener {
    private static final int PICK_FILE_REQUEST_CODE = 752;
    private ArrayList<Uri> fileUris;
    private Button btnConnect, btnSelect, btnDisconnect;
    private EditText edtIp;
    private RecyclerView recyclerView;
    private String ipAddress;
    private ClientService clientService;
    private SharedListAdapter adapter;
    private LinearLayoutManager layoutManager;

    private boolean isBound;
    private boolean isCompleted;
    private Intent client;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            clientService = ((ClientService.ClientBinder)service).getService();
            clientService.setConnectionListener(SendingActivity.this);
            clientService.setBound(true);

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sending);
        //uri = Uri.parse(getIntent().getStringExtra("Uri"));

        btnConnect = findViewById(R.id.btn_connect_sender);
        btnConnect.setOnClickListener(this);

        btnDisconnect = findViewById(R.id.btn_cancel_group_sender);
        btnDisconnect.setOnClickListener(this);

        btnSelect = findViewById(R.id.btn_select_file_sender);
        btnSelect.setOnClickListener(this);

        edtIp = findViewById(R.id.edt_send_ip);
        recyclerView = findViewById(R.id.recycler_view_files_process_sender);


        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }


        layoutManager = new LinearLayoutManager(this);
        fileUris = new ArrayList<>();
    }


    @Override
    public void onClientConnected() {
        edtIp.setEnabled(false);
        btnConnect.setEnabled(false);

        adapter = new SharedListAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);
    }

    @Override
    public void onClientStarted(String fileName) {
        ShareListModel model = new ShareListModel();
        model.setWho("S");
        model.setPercent("0%");
        model.setFileName(fileName);
        model.setProgress(0);

        adapter.add(model);
    }

    @Override
    public void onClientsProgress(int progress, int item) {
        adapter.updateProgress(item, progress);
    }

    @Override
    public void onClientCompleted() {
        if(!clientService.isRunning()){
            isCompleted = true;
        }
    }

    @Override
    public void onClientError(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_connect_sender:

                ipAddress = edtIp.getText().toString();
                ipAddress = ipAddress.trim();

                if(ipAddress.equals("")){
                    Toast.makeText(this, "Enter Ip", Toast.LENGTH_SHORT).show();
                    return;
                }

                client = new Intent(this, ClientService.class);
                client.putExtra("ip", ipAddress);
                startService(client);
                bindService(client, serviceConnection, Context.BIND_AUTO_CREATE);
                isBound = true;
                break;

            case R.id.btn_select_file_sender:
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.setType("*/*");
                i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(i, PICK_FILE_REQUEST_CODE);
                break;

            case R.id.btn_cancel_group_sender:
                if(client != null)
                    clientService.stopService(client);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case PICK_FILE_REQUEST_CODE:

                if(resultCode == Activity.RESULT_OK){
                    if(data != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            if (data.getClipData() != null) {
                                try {
                                    int fileUrisL = data.getClipData().getItemCount();
                                    for (int i = 0; i < fileUrisL; i++) {
                                        fileUris.add(data.getClipData().getItemAt(i).getUri());
                                    }

                                    if (clientService != null) {
                                        clientService.setWouldRun(true, fileUris);
                                    }
                                } catch (NullPointerException e) {
                                    e.printStackTrace();
                                    Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                                }

                            }else{
                                fileUris.add(data.getData());
                                if (clientService != null) {
                                    clientService.setWouldRun(true, fileUris);
                                }
                                Toast.makeText(this, "Selected Single item", Toast.LENGTH_SHORT).show();
                            }
                        }


                    }else {
                        Toast.makeText(this, "No item selected", Toast.LENGTH_SHORT).show();
                    }
                }

                break;
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if(isBound){
            unbindService(serviceConnection);
        }
    }
}
