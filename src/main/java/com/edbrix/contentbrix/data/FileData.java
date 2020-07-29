package com.edbrix.contentbrix.data;

import java.io.File;
import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * Created by rajk.
 */

public class FileData implements Serializable {

    private String fileName;
    private double fileSizeByte;
    private double fileSizeKB;
    private double fileSizeMB;

    private File fileObject;

    public FileData(File fileObject) {
        this.fileObject = fileObject;
        fileName = fileObject.getName();
        fileSizeByte = fileObject.length();
        fileSizeKB = (fileSizeByte / 1024);
        fileSizeMB = (fileSizeKB / 1024);
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public double getFileSizeMB() {
        DecimalFormat df = new DecimalFormat("0.00");
        return Double.parseDouble((String) df.format(fileSizeMB));
    }

    public void setFileSizeMB(double fileSizeMB) {
        this.fileSizeMB = fileSizeMB;
    }

    public File getFileObject() {
        return fileObject;
    }

    public void setFileObject(File fileObject) {
        this.fileObject = fileObject;
    }
}
