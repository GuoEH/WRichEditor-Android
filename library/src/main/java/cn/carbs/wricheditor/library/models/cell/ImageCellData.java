package cn.carbs.wricheditor.library.models.cell;

import org.json.JSONObject;

import cn.carbs.wricheditor.library.interfaces.BaseCellData;
import cn.carbs.wricheditor.library.interfaces.IRichCellData;
import cn.carbs.wricheditor.library.types.RichType;

public class ImageCellData extends BaseCellData {

    public String imageLocalUrl;

    public String imageNetUrl;

    @Override
    public RichType getType() {
        return RichType.IMAGE;
    }

    @Override
    public String toHtml() {
        return "<div richType=\"" + getType().name() + "\">" +
                    "<picture><img src=\"" + imageNetUrl + "\"></picture>" +
                "</div>";
    }

    @Override
    public String toJson() {
        return getJson(getType().name(), imageNetUrl);
    }

    @Override
    public IRichCellData fromJson(JSONObject json) {
        if (json == null) {
            return this;
        }
        try {
            JSONObject data = json.getJSONObject(JSON_KEY_DATA);
            imageNetUrl = data.getString("url");
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