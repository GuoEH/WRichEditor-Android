package cn.carbs.wricheditor.library.models.cell;

import org.json.JSONObject;

import cn.carbs.wricheditor.library.interfaces.BaseCellData;
import cn.carbs.wricheditor.library.interfaces.IRichCellData;
import cn.carbs.wricheditor.library.types.RichType;

// TODO 未完成，添加封面
public class VideoCellData extends BaseCellData {

    public String videoLocalUrl;

    public String videoNetUrl;

    @Override
    public RichType getRichType() {
        return RichType.VIDEO;
    }

    // TODO
    @Override
    public String toHtml() {
        return "<div richType=\"" + getRichType().name()
                + "\" url=\"" + videoLocalUrl
                + "\"></div>";
    }

    @Override
    public String toJson() {
        return getJson(getRichType().name(), videoNetUrl);
    }

    @Override
    public IRichCellData fromJson(JSONObject json) {
        if (json == null) {
            return this;
        }
        try {
            JSONObject data = json.getJSONObject(JSON_KEY_DATA);
            videoNetUrl = data.getString("url");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public String getJson(String type, String url) {
        return "{" +
                "\"" + JSON_KEY_TYPE + "\": " + "\"" + type + "\"," +
                "\"" + JSON_KEY_DATA + "\": " +
                "{" +
                "\"url\": " + "\"" + url + "\"" +
                "}" +
                "}";
    }

}