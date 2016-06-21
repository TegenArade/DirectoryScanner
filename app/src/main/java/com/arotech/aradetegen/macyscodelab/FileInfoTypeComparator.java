package com.arotech.aradetegen.macyscodelab;

import java.util.Comparator;

/**
 * Created by arade.tegen on 6/20/16.
 */
public class FileInfoTypeComparator implements Comparator<FrequencyInfo> {

    @Override
    public int compare(FrequencyInfo lhs, FrequencyInfo rhs) {
        return lhs.getFrequency() > rhs.getFrequency() ? -1 : lhs.getFrequency() == rhs.getFrequency() ? 0 : 1;
    }
}
