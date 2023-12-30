package com.bezkoder.spring.login.payload.response;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;



@Data
public class FileResponse{
    private String fileName;
    private String fileDownloadUri;
    private String fileType;
    private long size;
    private String DownloadCode;
    private String QRCodeImg;

    public FileResponse(String fileName, String fileDownloadUri, String fileType, long size, String DownloadCode, String QRCodeImg) {
        this.fileName = fileName;
        this.fileDownloadUri = fileDownloadUri;
        this.fileType = fileType;
        this.size = size;
        this.DownloadCode = DownloadCode;
        this.QRCodeImg = QRCodeImg;

    }


}
