package cn.carbs.wricheditor.library.models.cell;

import android.text.Editable;

import java.util.LinkedList;

import cn.carbs.wricheditor.library.interfaces.IRichCellData;
import cn.carbs.wricheditor.library.interfaces.IRichSpan;
import cn.carbs.wricheditor.library.models.ContentStyleWrapper;
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

    @Override
    public String toHtml() {

        String content = getStringByEditable(editable);
        String html = "<div richType=\"" + getType().name() + "\">" +
                content +
                "</div>";
        return html;
    }

    private String getStringByEditable(Editable editable) {

        if (editable == null || editable.length() == 0) {
            return null;
        }

        // TODO 不同的格式，需要对应不同的样式
        RichType richType = getType();
        if (richType == RichType.NONE) {

        } else if (richType == RichType.QUOTE) {

        } else if (richType == RichType.LIST_UNORDERED) {

        } else if (richType == RichType.LIST_UNORDERED) {

        }

        int editableLength = editable.length();
        String editableString = editable.toString();

        LinkedList<ContentStyleWrapper> wrappersList = new LinkedList();
        for (int cursor = 0; cursor < editableLength; cursor++) {
            // 1. 首先检查cursor所在的位置的mask
            IRichSpan[] currRichSpans = editable.getSpans(cursor, cursor + 1, IRichSpan.class);
            int mask = 0;
            if (currRichSpans.length != 0) {
                mask = getSpansMask(currRichSpans);
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

        StringBuilder sb = new StringBuilder();
        for (ContentStyleWrapper wrapper : wrappersList) {
            sb.append(wrapper.toHtmlString());
        }

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

}