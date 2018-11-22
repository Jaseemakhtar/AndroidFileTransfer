package com.jsync.fileshare;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;

import com.jsync.fileshare.sharedList.ShareListModel;
import com.jsync.fileshare.sharedList.SharedListAdapter;

public class ConnectedActivity extends AppCompatActivity
        implements ListeningService.LConnectionListener, ReceivingService.RConnectionListener {

    private RecyclerView rvFilesProcessing;
    private SharedListAdapter adapter;
    private LinearLayoutManager layoutManager;
    private Button btnSelectFile, btnCancel;
    private ReceivingService receivingService;
    private int i = 0;

    private  boolean isRSBound;
    private ServiceConnection receiveConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            receivingService = ((ReceivingService.ReceiverLocalBinder)service).getService();
            receivingService.setConnectionListener(ConnectedActivity.this);
            Log.i("Socket", "onServiceConnected (receiveConnection)");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private boolean isLSBound;
    private ListeningService listeningService;
    private ServiceConnection listenConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            listeningService = ((ListeningService.ListeningBinder)service).getService();
            listeningService.setConnectionListener(ConnectedActivity.this);
            Log.i("Socket", "onServiceConnected (listenConnection)");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected);
        btnCancel = findViewById(R.id.btn_cancel_group);
        btnSelectFile = findViewById(R.id.btn_select_file);

        rvFilesProcessing = findViewById(R.id.recycler_view_files_process);
        layoutManager = new LinearLayoutManager(this);
        adapter = new SharedListAdapter();
        rvFilesProcessing.setLayoutManager(layoutManager);
        rvFilesProcessing.setAdapter(adapter);

        startListening();
    }

    @Override
    public void receivedRequest(String msg) {
        if (msg.equals("receive")){
            Log.i("Socket", "receivedRequest -> receive");
            startReceiving();
        }
    }

    @Override
    public void onStart(String fileName, long fileSize) {
        Log.i("Socket", "onStart");
        ShareListModel model = new ShareListModel();
        model.setFileName(fileName);
        model.setProgress(0);
        model.setPercent("0%");
        model.setWho("R");
        adapter.add(model);
    }

    @Override
    public void progressUpdate(int progress) {
        //Log.i("Socket", "progressUpdate");
        adapter.updateProgress(i, progress);
    }

    @Override
    public void onComplete(String msg) {
        Log.i("Socket", "on Complete -> msg: " + msg);
        listeningService.setWouldListen(true);
        i++;
    }


    private void startListening(){
        Log.i("Socket", "startListening");
        Intent listen = new Intent(this, ListeningService.class);
        startService(listen);
        bindService(listen, listenConnection, BIND_AUTO_CREATE);
        isLSBound = true;
    }

    private void startReceiving() {
        Log.i("Socket", "startReceiving");
        Intent receive = new Intent(this,ReceivingService.class);
        startService(receive);
        bindService(receive, receiveConnection, BIND_AUTO_CREATE);
        isRSBound = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(isLSBound){
            unbindService(listenConnection);
            isLSBound = false;
        }

        if (isRSBound){
            unbindService(receiveConnection);
            isRSBound = false;
        }
    }


}
