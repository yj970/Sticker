package com.yj.sticker.event;

public class SaveBitmapEvent {
    String filePath;

    public SaveBitmapEvent(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }
}
