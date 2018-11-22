package com.jsync.fileshare.utils;

/**
 * Created by jaseem on 7/11/18.
 */

public class SSocket {
    private static final SSocket ourInstance = new SSocket();

    public static SSocket getInstance() {
        return ourInstance;
    }

    private SSocket() {
    }
}
