package cn.carbs.wricheditor.library.utils;

import android.text.Editable;
import android.text.Spanned;
import android.util.Log;

import java.util.Set;

import cn.carbs.wricheditor.library.types.RichType;

public class SpanUtil {

    // SPAN_INCLUSIVE_EXCLUSIVE
    public static void setSpan(Editable editable, Set<RichType> richTypes, int spanStart, int spanEnd) {
        if (editable == null || spanStart < 0 || spanEnd < 0 || spanStart >= spanEnd) {
            return;
        }
        if (richTypes == null || richTypes.size() == 0) {
            Log.d("vvv", "setSpan null");
            // null 是无效的
//            editable.setSpan(new NormalStyleSpan(), spanStart, spanEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);


        } else {
            Log.d("vvv", "setSpan BoldStyleSpan");
            // 循环遍历
            for (RichType richType : richTypes) {
                editable.setSpan(TypeUtil.getSpanByType(richType), spanStart, spanEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            }
        }
    }

}
