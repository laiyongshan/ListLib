package com.bluearchitect.jackhan.sortlistviewlibrary;

import java.util.Comparator;

/**
 * @author xiaanming
 */
public class PinyinComparator implements Comparator<SortModel> {

    public int compare(SortModel o1, SortModel o2) {
        if (o1.getSortLetters().equals("@")
                || o2.getSortLetters().equals("#")) {
            return -1;
        } else if (o1.getSortLetters().equals("#")
                || o2.getSortLetters().equals("@")) {
            return 1;
        } else {
            int compareResult = o1.getSortLetters().compareTo(o2.getSortLetters());
            if (0 == compareResult)
                compareResult = o1.getSortFirstWordSpell().compareTo(o2.getSortFirstWordSpell());
            if (0 == compareResult)
                compareResult = o1.getSortName().compareTo(o2.getSortName());
            return compareResult;
        }
    }

}
