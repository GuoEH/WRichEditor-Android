package cn.carbs.wricheditor.library.utils;

import android.text.Editable;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import cn.carbs.wricheditor.library.interfaces.IRichSpan;
import cn.carbs.wricheditor.library.models.RichAtomicData;
import cn.carbs.wricheditor.library.models.SpanPart;
import cn.carbs.wricheditor.library.types.RichType;

public class SpanUtil {

    // SPAN_INCLUSIVE_EXCLUSIVE
    public static void setSpan(RichType richType, boolean open, ArrayList<RichAtomicData> richAtomicDataList, Editable editable, Set<RichType> richTypes, int spanStart, int spanEnd) {
        if (editable == null || spanStart < 0 || spanEnd < 0 || spanStart >= spanEnd) {
            return;
        }

        if (open) {
            editable.setSpan(TypeUtil.getSpanByType(richType), spanStart, spanEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        } else {
            IRichSpan richSpan = TypeUtil.getSpanByType(richType);
            IRichSpan[] spans = editable.getSpans(spanStart, spanEnd, richSpan.getClass());

            List<SpanPart> list = new ArrayList<>();
            for (IRichSpan span : spans) {
                list.add(new SpanPart(editable.getSpanStart(span), editable.getSpanEnd(span)));
                editable.removeSpan(span);
            }

            for (SpanPart part : list) {
                if (part.isValid()) {
                    if (part.getStart() < spanStart) {
                        editable.setSpan(TypeUtil.getSpanByType(richType), part.getStart(), spanStart, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    }

                    if (part.getEnd() > spanEnd) {
                        editable.setSpan(TypeUtil.getSpanByType(richType), spanEnd, part.getEnd(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    }
                }
            }
        }

    }

}
