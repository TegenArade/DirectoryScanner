package com.arotech.aradetegen.macyscodelab;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.Serializable;

/**
 * Created by arade.tegen on 6/20/16.
 */
public class FileInfo implements Serializable {

    private String fileName;
    private String fileType;
    private long fileSize;

    public FileInfo(File aListFile) {

        this.fileName = aListFile.getName();
        this.fileType = FilenameUtils.getExtension(aListFile.getName());

        this.fileSize = aListFile.length();
    }

    public String getFileType() {
        return fileType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getFileNameAndSize() {
        return "*  Name: " + fileName + "\n" + "    Size: " + fileSize + " byte";
    }
}
