package com.jsync.fileshare.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * Created by jaseem on 8/10/18.
 */

public class Utils {
    public static String getRealPathFromURI(Context context, Uri contentUri) {
        String res = null;
        String[] projection = { MediaStore.Images.Media.DATA, MediaStore.Video.Media.DATA, MediaStore.Files.FileColumns.DATA};
        Cursor cursor = context.getContentResolver().query(contentUri, projection, null, null, null);
        if(cursor.moveToFirst()){
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }
}
