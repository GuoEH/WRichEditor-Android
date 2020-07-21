package cn.carbs.wricheditor.library.models.cell;

import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;

import cn.carbs.wricheditor.library.interfaces.BaseCellData;
import cn.carbs.wricheditor.library.interfaces.IRichCellData;
import cn.carbs.wricheditor.library.interfaces.IRichSpan;
import cn.carbs.wricheditor.library.models.ContentStyleWrapper;
import cn.carbs.wricheditor.library.spannables.LinkStyleSpan;
import cn.carbs.wricheditor.library.types.RichType;

public class RichCellData extends BaseCellData {

    public static final String JSON_KEY_LIST = "list";

    public static final String JSON_KEY_MASK = "mask";

    public static final String JSON_KEY_TEXT = "text";

    // NONE, QUOTE, LIST_ORDERED, LIST_UNORDERED
    private RichType richType = RichType.NONE;

    private LinkedList<ContentStyleWrapper> wrappersList;

    private Editable editable;

    @Override
    public RichType getRichType() {
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
        String html = "<div richType=\"" + getRichType().name() + "\">" +
                content +
                "</div>";
        return html;
    }

    @Override
    public String toJson() {
        return getJson(getRichType().name(), editable);
    }

    @Override
    public IRichCellData fromJson(JSONObject json) {
        if (json == null) {
            return this;
        }
        try {
            richType = RichType.valueOf(json.getString(BaseCellData.JSON_KEY_TYPE));
            wrappersList = getWrapperListByJson(json);
            // TODO test
            for (int i = 0; i < wrappersList.size(); i++) {
                Log.d("json", "wrappersList item i : " + i + "  mask : " + wrappersList.get(i).mask + "  text : " + wrappersList.get(i).contentBuilder);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public LinkedList<ContentStyleWrapper> getWrappersList() {
        return wrappersList;
    }

    private String getHtmlContentStringByEditable(Editable editable) {

        if (editable == null || editable.length() == 0) {
            return null;
        }

        // TODO 不同的格式，需要对应不同的样式
        RichType richType = getRichType();
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

        String[] urlForLink = new String[1];
        for (int cursor = 0; cursor < editableLength; cursor++) {

            // 1. 首先检查cursor所在的位置的mask
            IRichSpan[] currRichSpans = editable.getSpans(cursor, cursor + 1, IRichSpan.class);
            int mask = 0;
            urlForLink[0] = "";
            if (currRichSpans.length != 0) {
                mask = getSpansMask(currRichSpans, urlForLink);
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
                lastWrapper = new ContentStyleWrapper(mask, editableString.substring(cursor, cursor + 1));
                wrappersList.add(lastWrapper);
            }
            // TODO
            if (!TextUtils.isEmpty(urlForLink[0])) {
                lastWrapper.extra = urlForLink[0];
            }
        }
        return wrappersList;
    }

    public LinkedList<ContentStyleWrapper> getWrapperListByJson(JSONObject json) {
        LinkedList<ContentStyleWrapper> wrappers = new LinkedList<>();
        try {
            JSONObject data = json.getJSONObject(JSON_KEY_DATA);
            JSONArray array = data.getJSONArray(JSON_KEY_LIST);
            int length = array.length();
            for (int i = 0; i < length; i++) {
                JSONObject jsonObject = array.getJSONObject(i);
                int mask = jsonObject.getInt(JSON_KEY_MASK);
                String text = jsonObject.getString(JSON_KEY_TEXT);
                String extra = jsonObject.optString(JSON_KEY_MASK);
                wrappers.add(new ContentStyleWrapper(mask, text).setExtra(extra));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return wrappers;
    }

    public int getSpansMask(IRichSpan[] currRichSpans, String[] urlForLink) {
        int mask = 0;
        for (IRichSpan item : currRichSpans) {
            int start = editable.getSpanStart(item);
            int end = editable.getSpanEnd(item);
            if (start < end) {
                mask = item.getMask() | mask;
                if (mask == LinkStyleSpan.MASK) {
                    urlForLink[0] = ((LinkStyleSpan) item).getURL();
                }
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

        return "{" +
                "\"" + JSON_KEY_TYPE + "\": " + "\"" + type + "\"," +
                "\"" + JSON_KEY_DATA + "\": " +
                "{" +
                "\"" + JSON_KEY_LIST + "\": " +
                "[" +
                sbJsonList.toString() +
                "]" +
                "}" +
                "}";
    }

}