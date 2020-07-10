package cn.carbs.wricheditor.library.utils;

import android.text.Editable;
import android.text.Spanned;
import android.util.Log;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.carbs.wricheditor.library.constants.CharConstant;
import cn.carbs.wricheditor.library.interfaces.IRichCellView;
import cn.carbs.wricheditor.library.interfaces.IRichSpan;
import cn.carbs.wricheditor.library.models.RichAtomicData;
import cn.carbs.wricheditor.library.models.SpanPart;
import cn.carbs.wricheditor.library.types.RichType;

public class SpanUtil {

    // SPAN_INCLUSIVE_EXCLUSIVE
    public static void setSpan(RichType richType, boolean open, Object object, ArrayList<RichAtomicData> richAtomicDataList, Editable editable, Set<RichType> richTypes, int spanStart, int spanEnd) {
        if (editable == null || spanStart < 0 || spanEnd < 0 || spanStart > spanEnd) {
            return;
        }
        if (spanStart == spanEnd) {
            // 没有选中，分情况：
            // 如果是 HeadLine 类型，则将整行都置为大号字体
            // TODO 还有quote类型
            if (richType == RichType.HEADLINE) {
                // TODO 应该是整行都变
                if (spanEnd == 0) {
                    return;
                }
                String editableStr = editable.toString();
                int lastLineBreak = 0;
                for (int i = spanEnd - 1; i >= 0; i--) {
                    char ic = editableStr.charAt(i);
                    if (ic == CharConstant.LINE_BREAK_CHAR) {
                        lastLineBreak = i;
                        break;
                    }
                }
                editable.setSpan(TypeUtil.getSpanByType(richType, object), lastLineBreak, spanEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            }
            return;
        }

        if (open) {
            Log.d("mmm", "editable.setSpan : " + TypeUtil.getSpanByType(richType, object).getClass().getName());
            editable.setSpan(TypeUtil.getSpanByType(richType, object), spanStart, spanEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        } else {
            IRichSpan richSpan = TypeUtil.getSpanByType(richType, object);
            IRichSpan[] spans = editable.getSpans(spanStart, spanEnd, richSpan.getClass());

            List<SpanPart> list = new ArrayList<>();
            for (IRichSpan span : spans) {
                list.add(new SpanPart(editable.getSpanStart(span), editable.getSpanEnd(span)));
                editable.removeSpan(span);
            }

            for (SpanPart part : list) {
                if (part.isValid()) {
                    if (part.getStart() < spanStart) {
                        editable.setSpan(TypeUtil.getSpanByType(richType, object), part.getStart(), spanStart, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    }

                    if (part.getEnd() > spanEnd) {
                        editable.setSpan(TypeUtil.getSpanByType(richType, object), spanEnd, part.getEnd(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    }
                }
            }
        }
    }

    // 后面添加
    public static void setSpan(Set<RichType> richTypes, Object object, Editable editable, int spanStart, int spanEnd) {
        if (editable == null || spanStart < 0 || spanEnd < 0 || spanStart >= spanEnd) {
            return;
        }

        if (richTypes == null || richTypes.size() == 0) {
            return;
        }

        for (RichType richType : richTypes) {
            editable.setSpan(TypeUtil.getSpanByType(richType, object), spanStart, spanEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
    }

    // 此函数纯粹返回当前上下文的格式，在使用此函数之前，应该先判断location的位置，如果location的位置是最后一个，则按照当前的主动设置来执行
    // 此函数返回的格式，只能作为默认
    public static Set<RichType> getSpanTypesForCursorLocation(Editable editable, int cursorLocation) {
        Set<RichType> retRichTypes = new HashSet<>();
        if (editable == null || cursorLocation < 0) {
            return retRichTypes;
        }

        int editableLength = editable.toString().length();

        // location 超出范围
        if (editableLength < cursorLocation) {
            return retRichTypes;
        }
        // 光标位于最后
        if (cursorLocation == editableLength) {
            return retRichTypes;
        }

        IRichSpan[] spans = editable.getSpans(cursorLocation, cursorLocation + 1, IRichSpan.class);
        for (IRichSpan span : spans) {
            RichType richType = span.getRichType();
            if (richType != null) {
                retRichTypes.add(richType);
            }
        }
        Log.d("qqq", "richTypes size : " + retRichTypes.size());
        return retRichTypes;
    }

    // todo
    // 删除其中一个cell时，检查是否能够merge
    public static void mergeTwoRichCellView() {

    }

    public static boolean checkIfTwoRichCellViewCanMerge(IRichCellView cellViewA, IRichCellView cellViewB) {
        if (cellViewA == null || cellViewB == null) {
            return false;
        }
        return cellViewA.getClass().getName().equals(cellViewB.getClass().getName());
    }

    // 将某个区间的富文本取出，然后只保留这部分富文本
    public static String getSpannableStringInclusiveExclusive(Editable editable, int start, int end, List<SpanPart> spanPartsOutput) {

        IRichSpan[] spans = editable.getSpans(start, end, IRichSpan.class);

        for (IRichSpan span : spans) {
            spanPartsOutput.add(new SpanPart(editable.getSpanStart(span), editable.getSpanEnd(span), span));
            // TODO
//            editable.removeSpan(span);
        }

        return editable.subSequence(start, end).toString();
    }

    // TODO bug 这个方法有bug，以后弃用
    /*public static void setSpannableInclusiveExclusive(EditText editText, String textWithoutFormat, List<SpanPart> spanParts, boolean fromZero) {
        editText.setText(textWithoutFormat);
        // 循环将格式赋给添加的这一段
        for (SpanPart part : spanParts) {
            if (part.isValid()) {
                if (fromZero) {
                    editText.getText().setSpan(part.getRichSpan(), 0, part.getEnd() - part.getStart(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                } else {
                    editText.getText().setSpan(part.getRichSpan(), part.getStart(), part.getEnd(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                }
            }
        }
    }*/

    // FIXME 有可能 spanParts 中的 start 与 end 构成的范围超长，应该将超长的"剩余"的部分输出，下次使用
    public static void setSpannableInclusiveExclusive(EditText editText, String textWithoutFormat, List<SpanPart> spanParts, int offset) {
        if (editText == null) {
            return;
        }
        editText.setText(textWithoutFormat);
        // 循环将格式赋给添加的这一段
        Editable editableText = editText.getText();
        int editableLength = editableText.length();
        for (SpanPart part : spanParts) {
            if (part.isValid()) {
                int start = part.getStart() + offset;
                int end = part.getEnd() + offset;
                if (start < 0) {
                    start = 0;
                } else if (start > editableLength - 1) {
                    start = editableLength - 1;
                }
                if (end < 0) {
                    end = 0;
                } else if (end > editableLength - 1) {
                    end = editableLength - 1;
                }
                editableText.setSpan(part.getRichSpan(), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            }
        }


    }

}