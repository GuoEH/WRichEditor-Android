package cn.carbs.wricheditor.library.models.cell;

import android.text.Editable;
import android.util.Log;

import java.util.LinkedList;

import cn.carbs.wricheditor.library.interfaces.BaseCellData;
import cn.carbs.wricheditor.library.interfaces.IRichCellData;
import cn.carbs.wricheditor.library.interfaces.IRichSpan;
import cn.carbs.wricheditor.library.models.ContentStyleWrapper;
import cn.carbs.wricheditor.library.types.RichType;

public class RichCellData extends BaseCellData {

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

        String content = getHtmlContentStringByEditable(editable);
        String html = "<div richType=\"" + getType().name() + "\">" +
                content +
                "</div>";
        return html;
    }

    @Override
    public String toJson() {
        if (adapter != null) {
            return adapter.toJson(this);
        }
        return getJson(getType().name(), editable);
    }

    @Override
    public IRichCellData fromJson(String json) {
        if (adapter != null) {
            return inflate(adapter.fromJson(json));
        }

//        cellView 转换


        // TODO
        try {
//            JSONObject obj = new JSONObject(json);
//            JSONObject data = obj.getJSONObject("data");
//            fileUrl = data.getString("url");
//            fileName = data.getString("name");
//            fileSize = data.getLong("size");
//            fileType = data.getString("type");
            // TODO 根据fileType设置图标
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public IRichCellData inflate(IRichCellData data) {
        if (data instanceof RichCellData) {
            editable = ((RichCellData) data).editable;
            // TODO 根据fileType设置图标
        }
        return this;
    }

    private String getHtmlContentStringByEditable(Editable editable) {

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

        LinkedList<ContentStyleWrapper> wrappersList = getWrapperListByEditable(editable);

        StringBuilder sb = new StringBuilder();
        for (ContentStyleWrapper wrapper : wrappersList) {
            sb.append(wrapper.toHtmlString());
        }

        return sb.toString();
    }

    public LinkedList<ContentStyleWrapper> getWrapperListByEditable(Editable editable) {
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
        return wrappersList;
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

    public String getJson(String type, Editable editable) {

        LinkedList<ContentStyleWrapper> wrappersList = getWrapperListByEditable(editable);

        StringBuilder sbJsonList = new StringBuilder();

        int size = wrappersList.size();

        for (int i = 0; i < size; i++) {
            sbJsonList.append(wrappersList.get(i).toJsonString());
            if (i < size - 1) {
                sbJsonList.append(",");
            }
        }

        String x =  "{" +
                "\"type\": " + "\"" + type + "\"," +
                "\"data\": " +
                "{" +
                "\"list\": " +
                "[" +
                sbJsonList.toString() +
                "]" +
                "}" +
                "}";
        Log.d("json", "RichCellData : " + x);
        return x;
    }

}