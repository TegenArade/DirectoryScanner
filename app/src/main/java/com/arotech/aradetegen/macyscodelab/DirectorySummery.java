package com.arotech.aradetegen.macyscodelab;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by arade.tegen on 6/21/16.
 */
public class DirectorySummery implements Serializable {

    private final static int TOP_BIGGEST_FILES_LIST_LENGTH = 10;
    private final static int TOP_FREQUENT_EXTENSIONS_LIST_LENGTH = 5;

    private List<FileInfo> topBiggestFiles = new ArrayList<>();
    private List<FrequencyInfo> topFrequentFileExtensions = new ArrayList<>();
    private long averageFileSize;

    public DirectorySummery(List<FileInfo> fileInfos, Map<String, Integer> fileTypes) {

        if (fileInfos != null) {
            Collections.sort(fileInfos, new FileInfoSizeComparator());
            this.topBiggestFiles.addAll(fileInfos.subList(0, Math.min(TOP_BIGGEST_FILES_LIST_LENGTH, fileInfos.size())));
            averageFileSize = calculateAverage(fileInfos);
        }

        if (fileTypes != null) {
            List<FrequencyInfo> topFileExtensions = new ArrayList<>();
            for (Map.Entry<String, Integer> fileExtension : fileTypes.entrySet()) {
                topFileExtensions.add(new FrequencyInfo(fileExtension));
            }
            Collections.sort(topFileExtensions, new FileInfoTypeComparator());
            this.topFrequentFileExtensions.addAll(topFileExtensions.subList(0, Math.min(TOP_FREQUENT_EXTENSIONS_LIST_LENGTH, topFileExtensions.size())));
        }
    }

    private long calculateAverage(List<FileInfo> fileInfos) {
        long sum = 0;
        if (!fileInfos.isEmpty()) {
            for (FileInfo fileInfo : fileInfos) {
                sum += fileInfo.getFileSize();
            }
            return sum / fileInfos.size();
        }
        return sum;
    }

    public String getTopBiggestFilesString() {
        String result = "";
        if (topBiggestFiles != null) {
            for (FileInfo fileInfo : topBiggestFiles) {
                result = result.concat(fileInfo.getFileNameAndSize() + "\n\n");
            }
        } else {
            result = "No file is scanned";
        }
        return result;
    }

    public String getTopFrequentFileExtensionsString() {
        String result = "";
        if (topFrequentFileExtensions != null) {
            for (FrequencyInfo frequencyInfo : topFrequentFileExtensions) {
                result = result.concat(frequencyInfo.getFileTypeAndFrequency() + "\n\n");
            }
        } else {
            result = "No file is scanned";
        }
        return result;
    }

    public long getAverageFileSize() {
        return averageFileSize;
    }

    public String getAverageFileSizeString() {
        return averageFileSize + "byte";

    }
}
