package cn.carbs.wricheditor.library.models.cell;

import android.text.Editable;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import cn.carbs.wricheditor.library.interfaces.IRichCellData;
import cn.carbs.wricheditor.library.interfaces.IRichSpan;
import cn.carbs.wricheditor.library.models.ContentStyleWrapper;
import cn.carbs.wricheditor.library.models.SpanPart;
import cn.carbs.wricheditor.library.parser.Parser;
import cn.carbs.wricheditor.library.spannables.BoldStyleSpan;
import cn.carbs.wricheditor.library.spannables.HeadlineSpan;
import cn.carbs.wricheditor.library.spannables.ItalicStyleSpan;
import cn.carbs.wricheditor.library.spannables.LinkStyleSpan;
import cn.carbs.wricheditor.library.spannables.StrikeThroughStyleSpan;
import cn.carbs.wricheditor.library.spannables.UnderlineStyleSpan;
import cn.carbs.wricheditor.library.types.RichType;

public class RichCellData implements IRichCellData {

    // NONE, QUOTE, LIST_ORDERED, LIST_UNORDERED
    private RichType richType = RichType.NONE;

    public Editable editable;

    @Override
    public Object getData() {
        return this;
    }

    @Override
    public RichType getType() {
        return richType;
    }

    public void setRichType(RichType richType) {
        this.richType = richType;
    }

    public void setEditable(Editable editable) {
        this.editable = editable;
    }

    // TODO
    @Override
    public String toHtml() {

        String content = getStringByEditable(editable);

        String html = "<div richType=\"" + getType().name() + "\">" +
                content +
                "</div>";

        Log.d("wert", "html -->" + html + "<---");

        StringBuilder sb = new StringBuilder();
        Parser.withinContent(sb, editable, 0, editable.length());
        Log.d("wert", "sb -->" + sb + "<---");
        return html;
    }

    private String getStringByEditable(Editable editable) {

        if (editable == null || editable.length() == 0) {
            Log.d("wert", "getStringByEditable 1");
            return null;
        }

        // TODO 不同的格式，需要对应不同的样式
        RichType richType = getType();
        if (richType == RichType.NONE) {

        } else if (richType == RichType.QUOTE) {

        } else if (richType == RichType.LIST_UNORDERED) {

        } else if (richType == RichType.LIST_UNORDERED) {

        }

//        1. 遍历每种合适
        /*HashMap<RichType, SpanWrapper> map = new HashMap<>();

        // TODO 是否遵循从左到右的规律
        IRichSpan[] spans = editable.getSpans(0, editable.length(), IRichSpan.class);
        Log.d("kkk", "----> spans length : " + spans.length);
        for (IRichSpan span : spans) {
            RichType type = span.getRichType();
            if (type != null) {
                if (!map.containsKey(type)) {
                    map.put(type, new SpanWrapper(type, editable));
                }
                SpanWrapper spanWrapperValue = map.get(type);
                spanWrapperValue.addSpan(span);
            }
        }

        for(Map.Entry<RichType, SpanWrapper> entry : map.entrySet()){
            // 整理其中的顺序
            entry.getValue().order();
        }*/

        // TODO
//        IRichSpan[] richSpansBold = editable.getSpans(0, editable.length(), BoldStyleSpan.class);
//        IRichSpan[] richSpansHeadLine = editable.getSpans(0, editable.length(), HeadlineSpan.class);
//        IRichSpan[] richSpansItalic = editable.getSpans(0, editable.length(), ItalicStyleSpan.class);
//        IRichSpan[] richSpansLink = editable.getSpans(0, editable.length(), LinkStyleSpan.class);
//        IRichSpan[] richSpansStrike = editable.getSpans(0, editable.length(), StrikeThroughStyleSpan.class);
//        IRichSpan[] richSpansUnderline = editable.getSpans(0, editable.length(), UnderlineStyleSpan.class);

//        2. 针对每种span加一个游标
//        指向开闭指针以及是否命中在区间中，


//        3. 建立一个指针cursor，指向内容
//        int cursor = 0;

//        4. g不断遍历内容，每个位置，找到对应的每个span的指针是否包含，如果否，则将每个span的指针往右移动一个。
        int editableLength = editable.length();
        String editableString = editable.toString();

        LinkedList<ContentStyleWrapper> wrappersList = new LinkedList();
        for (int cursor = 0; cursor < editableLength; cursor++) {
            Log.d("wert", "getStringByEditable 2 cursor : " + cursor);
            // 1. 首先检查cursor所在的位置的mask
            IRichSpan[] currRichSpans = editable.getSpans(cursor, cursor + 1, IRichSpan.class);

//            editable.getSpanStart()
//            editable.nextSpanTransition()

            int mask = 0;
            if (currRichSpans.length != 0) {
                mask = getSpansMask(currRichSpans);
                Log.d("wert", "===>getSpansMask cursor : " + cursor + " mask : " + mask + "currRichSpans.length : " + currRichSpans.length);
            }

            // 2. 如果队列最后一个的mask（即前一个字符的mask）等于当前的mask，则直接添加，避免转换成html时相同格式的字符串被分成多段
            ContentStyleWrapper lastWrapper;
            if (wrappersList.size() == 0) {
                lastWrapper = new ContentStyleWrapper(mask);
                wrappersList.add(lastWrapper);
            } else {
                lastWrapper = wrappersList.getLast();
            }

            if (lastWrapper.mask == mask) {
                // 与之前的格式相同，append 内容即可
                lastWrapper.append(editableString.substring(cursor, cursor + 1));
            } else {
                wrappersList.add(new ContentStyleWrapper(mask, editableString.substring(cursor, cursor + 1)));
            }
        }

//        5. 找到每个位置所属的span，然后对应一个二进制数，这个二进制数可以反推回去
        StringBuilder sb = new StringBuilder();
        for (ContentStyleWrapper wrapper : wrappersList) {
            sb.append(wrapper.toHtmlString());
        }


//        6. 预备好一个arraylist，存档类似part结构，当上面的二进制数和前一个二进制数相同时，


        return sb.toString();
    }

    public int getSpansMask(IRichSpan[] currRichSpans) {
        int mask = 0;
        for (IRichSpan item : currRichSpans) {
            int start = editable.getSpanStart(item);
            int end = editable.getSpanEnd(item);
            if (start < end) {
                mask = item.getMask() | mask;
            }
        }
        return mask;
    }

    public static class SpanWrapper {

        // todo 这里的 spanPart 的开始与结尾应该都没有交集
        ArrayList<SpanPart> spanParts = new ArrayList<>(32);
        int lookupLocationCache;
        boolean cursorHit;

        Editable editable;

        RichType richType;

        int partsSize;

        // TODO
        public SpanWrapper(RichType richType, Editable editable) {
            this.richType = richType;
            this.editable = editable;
//            IRichSpan[] spans = editable.getSpans(start, end, IRichSpan.class);
//            for (IRichSpan span : spans) {
//                spanPartsOutput.add(new SpanPart(editable.getSpanStart(span), editable.getSpanEnd(span), span));
//            }
        }

        public void addSpan(IRichSpan span) {
            // 传入的span类型虽然一样，但是对象不同，因此 getSpanStart() getSpanEnd() 返回的结果不同
            // 由这种方法获得的开闭区间，左侧include，右侧exclude
            int start = editable.getSpanStart(span);
            int end = editable.getSpanEnd(span);
            Log.d("kkk", "----> addSpan start : " + start + "  end : " + end);
            if (start < end) {
                spanParts.add(new SpanPart(start, end, span));
            }
        }

        public int getLookupLocation() {
            return lookupLocationCache;
        }

        public boolean getCursorHit() {
            return cursorHit;
        }

        // 按照 SpanPart 的 start 进行排序
        public void order() {
            partsSize = spanParts.size();
        }

        /**
         * 检查当前位置是否包含于 spanParts 中
         *
         * @param location
         * @return
         */
        public boolean lookup(int location) {
            // TODO
            for (int i = lookupLocationCache; i < partsSize; i++) {
                // 当找到后就跳出
                SpanPart part = spanParts.get(i);
                if (part.getEnd() - 1 < location) {
                    continue;
                }
                if (part.getStart() > location) {
                    lookupLocationCache = i;
                    return false;
                }
                if (part.hit(location)) {
                    lookupLocationCache = i;
                    return true;
                }
            }
            return false;
        }
    }

}
