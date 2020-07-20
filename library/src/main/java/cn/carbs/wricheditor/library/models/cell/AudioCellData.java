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
        if (adapter != null) {
            return adapter.toJson(this);
        }
        return getJson(getType().name(), audioNetUrl, duration);
    }

    @Override
    public IRichCellData inflate(IRichCellData data) {
        if (data instanceof AudioCellData) {
            audioNetUrl = ((AudioCellData) data).audioNetUrl;
            duration = ((AudioCellData) data).duration;
        }
        return this;
    }

    @Override
    public IRichCellData fromJson(String json) {
        if (adapter != null) {
            return inflate(adapter.fromJson(json));
        }
        try {
            JSONObject obj = new JSONObject(json);
            JSONObject data = obj.getJSONObject("data");
            audioNetUrl = data.getString("url");
            duration = data.getLong("duration");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public String getJson(String type, String url, long duration) {
        return "{" +
                "\"type\": " + "\"" + type + "\"," +
                "\"data\": " +
                "{" +
                "\"url\": " + "\"" + url + "\"," +
                "\"duration\": " + duration +
                "}" +
                "}";
    }

}
