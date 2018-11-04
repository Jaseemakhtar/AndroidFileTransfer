package com.jsync.fileshare;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;



public class BackgroundService extends IntentService{

    public BackgroundService(String name) {
        super(name);
        Log.i("Background","Constructor");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.i("Background","OnHandleIntent");
    }
}
