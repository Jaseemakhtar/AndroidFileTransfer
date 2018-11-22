package com.jsync.fileshare;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.TextView;
import com.jsync.fileshare.utils.RSocket;
import java.net.Socket;

public class ReceivingActivity extends AppCompatActivity implements ConnectService.ConnectionListener,
        View.OnClickListener {

    private TextView txtStatus, txtIp;
    private RadarScanView radarScanView;
    private ConnectService connectService;
    private boolean isBound;
    private boolean isConnectedToClient;
    private ServiceConnection connectConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            connectService = ((ConnectService.LocalBinder)service).getService();
            connectService.setConnectionListener(ReceivingActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private Socket connectedClient;
    private RSocket rSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiving);

        txtStatus = findViewById(R.id.txt_receive_status);
        txtIp = findViewById(R.id.txt_ip);

        radarScanView = findViewById(R.id.radarScanView);
        radarScanView.addFoundClickListener(this);

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
        txtIp.setText("Your Ip: " + ip);

        Intent connect = new Intent(this, ConnectService.class);
        startService(connect);
        bindService(connect, connectConnection, Context.BIND_AUTO_CREATE);
        isBound = true;
    }

    public void setTxtStatus(String msg){
        txtStatus.setText(msg);
    }

    @Override
    public void onConnected() {
        connectedClient = connectService.getClient();
        rSocket = RSocket.getInstance();
        rSocket.setSocket(connectedClient);
        String ip = connectedClient.getInetAddress().toString().replace("/","");
        setTxtStatus("");
        radarScanView.setFoundItemText(ip);
        radarScanView.setFound(true);
        isConnectedToClient = true;
    }

    @Override
    public void onError(String err) {
        setTxtStatus(err);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isBound) {
            unbindService(connectConnection);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case 111:
                if (isConnectedToClient){
                    Intent intent = new Intent(this, ConnectedActivity.class);
                    startActivity(intent);
                }
                break;
        }
    }
}
