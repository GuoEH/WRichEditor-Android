package cn.carbs.wricheditor.library.models.cell;

import org.json.JSONObject;

import cn.carbs.wricheditor.library.interfaces.BaseCellData;
import cn.carbs.wricheditor.library.interfaces.IRichCellData;
import cn.carbs.wricheditor.library.types.RichType;

// TODO 添加封面
public class VideoCellData extends BaseCellData {

    public String videoLocalUrl;

    public String videoNetUrl;

    @Override
    public Object getData() {
        return this;
    }

    @Override
    public RichType getType() {
        return RichType.VIDEO;
    }

    // TODO
    @Override
    public String toHtml() {
        return "<div richType=\"" + getType().name()
                + "\" url=\"" + videoLocalUrl
                + "\"></div>";
    }

    @Override
    public String toJson() {
        if (adapter != null) {
            return adapter.toJson(this);
        }
        return getJson(getType().name(), videoNetUrl);
    }

    @Override
    public IRichCellData fromJson(String json) {
        if (adapter != null) {
            return inflate(adapter.fromJson(json));
        }
        try {
            JSONObject obj = new JSONObject(json);
            JSONObject data = obj.getJSONObject("data");
            videoNetUrl = data.getString("url");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public IRichCellData inflate(IRichCellData data) {
        if (data instanceof VideoCellData) {
            videoNetUrl = ((VideoCellData) data).videoNetUrl;
        }
        return this;
    }

    public String getJson(String type, String url) {
        return "{" +
                "\"type\": " + "\"" + type + "\"," +
                "\"data\": " +
                "{" +
                "\"url\": " + "\"" + url + "\"" +
                "}" +
                "}";
    }

}
