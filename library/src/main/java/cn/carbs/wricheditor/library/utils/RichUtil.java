package cn.carbs.wricheditor.library.utils;

import android.text.Editable;

import java.util.ArrayList;

import cn.carbs.wricheditor.library.models.RichAtomicData;
import cn.carbs.wricheditor.library.models.RichCellData;

public class RichUtil {

    public static RichAtomicData getCursorLocationData(RichCellData richCellData, Editable editable, int location) {

        if (richCellData == null || richCellData.atomicDataList == null) {
            return null;
        }

        ArrayList<RichAtomicData> atomicDataList = richCellData.atomicDataList;

        RichAtomicData retAtomicData = null;

        int size = atomicDataList.size();
        // TODO 需要优化
        int sum = 0;
        for(int i = 0; i < size; i++) {
            RichAtomicData currRichAtomicData = atomicDataList.get(i);
            sum = sum + currRichAtomicData.getTextLength();
            if (sum > location) {
                retAtomicData = currRichAtomicData;
                break;
            } else {
                continue;
            }
        }
        return retAtomicData;
    }
}
