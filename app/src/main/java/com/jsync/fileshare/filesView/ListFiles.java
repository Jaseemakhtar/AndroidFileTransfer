package com.jsync.fileshare.filesView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.jsync.fileshare.R;
import java.io.File;
import java.util.ArrayList;

public class ListFiles extends AppCompatActivity implements RecyclerListAdapter.OnClickItem{
    private final  int READ_WRITE_PERM = 778;
    private ArrayList<String> mList;
    private RecyclerListAdapter adapter;
    private RecyclerView recyclerView;
    private String path = "/storage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_files);
        mList = new ArrayList<>();
        adapter = new RecyclerListAdapter();
        adapter.setOnClickItem(this);
        recyclerView = findViewById(R.id.recyclerview_file);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (getIntent().hasExtra("path")){
            path = getIntent().getStringExtra("path");
        }

        if (Build.VERSION.SDK_INT <= 22 || ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){


            displayAllFiles();

        }else {

            displayToast("Without permission app won;t work");
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
            }, READ_WRITE_PERM);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case READ_WRITE_PERM:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    displayAllFiles();
                }
            break;
        }
    }

    public void displayAllFiles(){
        setTitle(path);
       boolean isExternalStorageReadOnly = false, isExternalStorageWritable = false;
       String state = Environment.getExternalStorageState();
       if (Environment.MEDIA_MOUNTED.equals(state)){
           isExternalStorageWritable = true;
       }else if(Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)){
           isExternalStorageReadOnly = true;
       }

       if (isExternalStorageReadOnly){
           Log.i("Directory","only readonly");
       }
       if (isExternalStorageWritable){
           Log.i("Directory","read write available");
           File root = new File(path);

           String[] listRoot = root.list();
           if (!root.canRead()){
               displayToast("Cannot Read");
               setTitle("Inaccessible");
           }

           if (listRoot!= null){
                for (String fileName: listRoot){
                    mList.add(fileName);
                    adapter.add(fileName);
                }
           }

       }


    }



    public void displayToast(String msg){
        Toast.makeText(this,msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClickItem(View view, int pos) {
        String fileName = mList.get(pos);
        if (path.endsWith(File.separator)){
            fileName = path + fileName;
        }else {
            fileName = path + File.separator + fileName;
        }

        if (new File(fileName).isDirectory()){
            Intent intent = new Intent(this, ListFiles.class);
            intent.putExtra("path",fileName);
            startActivity(intent);
        }else {
            displayToast("Not a directory");
        }
    }
}
