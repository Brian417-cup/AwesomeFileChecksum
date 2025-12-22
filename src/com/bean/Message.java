package com.bean;

public class Message {
    private String completeFilePath;
    private String fileName;
    private String checkSum;

    public Message(String completeFilePath, String fileName, String checkSum) {
        this.completeFilePath = completeFilePath;
        this.fileName = fileName;
        this.checkSum = checkSum;
    }

    public String getCompleteFilePath() {
        return completeFilePath;
    }

    public String getFileName() {
        return fileName;
    }

    public String getCheckSum() {
        return checkSum;
    }

    public void setCheckSum(String checkSum) {
        this.checkSum = checkSum;
    }
}
