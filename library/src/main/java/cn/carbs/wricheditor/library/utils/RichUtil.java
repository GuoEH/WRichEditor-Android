package cn.carbs.wricheditor.library.utils;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;

import cn.carbs.wricheditor.library.models.RichAtomicData;
import cn.carbs.wricheditor.library.models.RichCellData;

public class RichUtil {

    public static void hideSoftKeyboard(Context context, View view) {
        if (view == null) {
            return;
        }
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }


    /**
     * 光标插入位置的定位，如果光标位于两个不同格式的数据之间，则返回前一个 RichAtomicData
     *
     * @param richCellData
     * @param editable
     * @param location
     * @param locationInAtomicDataList size应该为2，返回光标location的左右两个字对应列表所在的位置
     * @return
     */
    public static RichAtomicData getCursorLocationData(RichCellData richCellData, Editable editable, int location, int[] locationInAtomicDataList) {
        // todo 添加抛出异常
        if (richCellData == null || richCellData.atomicDataList == null) {
            return null;
        }
        if (locationInAtomicDataList == null || locationInAtomicDataList.length == 0) {
            throw new IllegalArgumentException("locationInAtomicDataList length should be two");
        }
        return getCursorLocationData(richCellData.atomicDataList, editable, location, locationInAtomicDataList);
    }

    // TODO 需要先保证 atomicDataList 中所有的item数据都不能为空
    // TODO
    // TODO 系统是向后依靠，所以需要向后依靠
    // 格式向前依靠，返回以前的data
    public static RichAtomicData getCursorLocationData(ArrayList<RichAtomicData> atomicDataList, Editable editable, int location, int[] locationInAtomicDataList) {

        if (atomicDataList == null) {
            return null;
        }
        int size = atomicDataList.size();

        // 某个 RichAtomicData [之前] 左侧所有字符串的[长度]
        int cLeft = 0;
        // 某个 RichAtomicData [以及] 左侧所有字符串的[长度]
        int cRight = 0;
        // cursor 左侧的 RichAtomicData 数据对应的 index
        int iLeft = -1;
        // cursor 右侧的 RichAtomicData 数据对应的 index
        int iRight = -1;

        for (int i = 0; i < size; i++) {
            RichAtomicData currRichAtomicData = atomicDataList.get(i);
            cLeft = cRight;
            cRight = cRight + currRichAtomicData.getTextLength();
            iRight = i;
            if (cLeft == location) {
                // 正好在中间
                iLeft = i - 1;
                iRight = i;
            } else if (cLeft < location) {
                if (location < cRight) {
                    iLeft = i;
                    iRight = i;
                    break;
                } else {
                    continue;
                }
            }
        }
        locationInAtomicDataList[0] = iLeft;
        locationInAtomicDataList[1] = iRight;
        if (iLeft == -1) {
            return null;
        }
        return atomicDataList.get(iLeft);
    }

    // TODO
    public static void mix(ArrayList<RichAtomicData> atomicDataList) {
        // 通过比较TypeUtil.checkIfRichTypesSetSame() 来确定是否合并相邻的相同格式

    }

    // TODO
    public static void filterLengthZero(ArrayList<RichAtomicData> atomicDataList) {

        if (atomicDataList == null || atomicDataList.size() == 0) {
            return;
        }

        ArrayList<RichAtomicData> ret = new ArrayList<>(atomicDataList.size());
        boolean needFilter = false;
        for (RichAtomicData item : atomicDataList) {
            if (item.getTextLength() == 0) {
                needFilter = true;
                continue;
            }
            ret.add(item);
        }
        if (needFilter) {
            atomicDataList.clear();
            atomicDataList.addAll(ret);
        }
    }

}
