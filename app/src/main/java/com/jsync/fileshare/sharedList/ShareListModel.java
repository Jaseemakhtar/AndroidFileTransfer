package com.jsync.fileshare.sharedList;

/**
 * Created by jaseem on 7/11/18.
 */

public class ShareListModel {
    private String fileName;
    private String percent;
    private String who;
    private int progress;

    public ShareListModel(){

    }

    public ShareListModel(String name, String per, int prog){
        fileName = name;
        percent = per;
        progress = prog;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPercent() {
        return percent;
    }

    public void setPercent(String percent) {
        this.percent = percent;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getWho() {
        return who;
    }

    public void setWho(String who) {
        this.who = who;
    }
}
