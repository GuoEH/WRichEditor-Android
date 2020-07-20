package cn.carbs.wricheditor.library.models.cell;

import org.json.JSONObject;

import cn.carbs.wricheditor.library.interfaces.BaseCellData;
import cn.carbs.wricheditor.library.interfaces.IRichCellData;
import cn.carbs.wricheditor.library.types.RichType;

public class ImageCellData extends BaseCellData {

    public String imageLocalUrl;

    public String imageNetUrl;

    @Override
    public Object getData() {
        return this;
    }

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
    public IRichCellData inflate(IRichCellData data) {
        if (data instanceof ImageCellData) {
            imageNetUrl = ((ImageCellData) data).imageNetUrl;
        }
        return this;
    }

    @Override
    public String toJson() {
        if (adapter != null) {
            return adapter.toJson(this);
        }
        return getJson(getType().name(), imageNetUrl);
    }

    @Override
    public IRichCellData fromJson(String json) {
        if (adapter != null) {
            return inflate(adapter.fromJson(json));
        }
        try {
            JSONObject obj = new JSONObject(json);
            JSONObject data = obj.getJSONObject("data");
            imageNetUrl = data.getString("url");
        } catch (Exception e) {
            e.printStackTrace();
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