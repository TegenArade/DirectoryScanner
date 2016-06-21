package com.arotech.aradetegen.macyscodelab;

import java.util.Comparator;

/**
 * Created by arade.tegen on 6/20/16.
 */
public class FileInfoSizeComparator implements Comparator<FileInfo> {
    @Override
    public int compare(FileInfo lhs, FileInfo rhs) {
        return lhs.getFileSize() > rhs.getFileSize() ? -1 : lhs.getFileSize() == rhs.getFileSize() ? 0 : 1;
    }
}
