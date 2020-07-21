package cn.carbs.wricheditor.library.models.cell;

import android.util.Log;

import org.json.JSONObject;

import cn.carbs.wricheditor.library.interfaces.BaseCellData;
import cn.carbs.wricheditor.library.interfaces.IRichCellData;
import cn.carbs.wricheditor.library.types.RichType;

public class ImageCellData extends BaseCellData {

    public String imageNetUrl;

    @Override
    public RichType getRichType() {
        return RichType.IMAGE;
    }

    @Override
    public String toHtml() {
        return "<div richType=\"" + getRichType().name() + "\">" +
                    "<picture><img src=\"" + imageNetUrl + "\"></picture>" +
                "</div>";
    }

    @Override
    public String toJson() {
        return getJson(getRichType().name(), imageNetUrl);
    }

    @Override
    public IRichCellData fromJson(JSONObject json) {
        if (json == null) {
            return this;
        }
        try {
            JSONObject data = json.getJSONObject(JSON_KEY_DATA);
            imageNetUrl = data.getString("url");
            Log.d("ggg", "---> fromJson imageNetUrl : " + imageNetUrl);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("ggg", "---> exception : e : " + e.getMessage());
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

    @Override
    public String toString() {
        return "ImageCellData{" +
                "imageNetUrl='" + imageNetUrl + '\'' +
                '}';
    }
}