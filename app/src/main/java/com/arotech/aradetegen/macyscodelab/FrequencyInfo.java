package com.arotech.aradetegen.macyscodelab;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by arade.tegen on 6/20/16.
 */
public class FrequencyInfo implements Serializable {
    private String fileType;
    private int frequency;

    public FrequencyInfo(Map.Entry<String, Integer> value) {
        if (value != null) {
            fileType = value.getKey();
            frequency = value.getValue();
        }
    }

    public int getFrequency() {
        return frequency;
    }

    public String getFileType() {
        return fileType;
    }


    public String getFileTypeAndFrequency() {
        return "* Extension: " + fileType + "\n" +
                "   Frequency: " + frequency;
    }
}
