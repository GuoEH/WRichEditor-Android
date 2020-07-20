package cn.carbs.wricheditor.library.models.cell;

import org.json.JSONObject;

import cn.carbs.wricheditor.library.interfaces.BaseCellData;
import cn.carbs.wricheditor.library.interfaces.IRichCellData;
import cn.carbs.wricheditor.library.types.RichType;

public class AudioCellData extends BaseCellData {

    public String audioLocalUrl;

    public String audioNetUrl;

    public long duration;

    @Override
    public Object getData() {
        return this;
    }

    @Override
    public RichType getType() {
        return RichType.AUDIO;
    }

    @Override
    public String toHtml() {
        // TODO
//        <div richType="AUDIO" url="xxx"></div>
        return "<div richType=\"" + getType().name()
                + "\" url=\"" + audioNetUrl
                + "\"></div>";
    }

    @Override
    public String toJson() {
        return getJson(getType().name(), audioNetUrl, duration);
    }

    @Override
    public IRichCellData fromJson(JSONObject json) {
        if (json == null) {
            return this;
        }
        try {
            JSONObject data = json.getJSONObject(JSON_KEY_DATA);
            audioNetUrl = data.getString("url");
            duration = data.getLong("duration");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public String getJson(String type, String url, long duration) {
        return "{" +
                "\"" + JSON_KEY_TYPE + "\": " + "\"" + type + "\"," +
                "\"" + JSON_KEY_DATA + "\": " +
                "{" +
                "\"url\": " + "\"" + url + "\"," +
                "\"duration\": " + duration +
                "}" +
                "}";
    }

}
