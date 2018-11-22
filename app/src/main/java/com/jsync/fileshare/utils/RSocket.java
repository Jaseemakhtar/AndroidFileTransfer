package com.jsync.fileshare.utils;

import java.net.Socket;

/**
 * Created by jaseem on 7/11/18.
 */

public class RSocket {
    private static RSocket rSocket = null;
    private Socket socket;

    private RSocket(){}

    public static RSocket getInstance(){
        if (rSocket == null)
            rSocket = new RSocket();
        return rSocket;
    }

    public void setSocket(Socket socket){
        this.socket = socket;
    }
    public Socket getSocket(){ return this.socket; }
}
