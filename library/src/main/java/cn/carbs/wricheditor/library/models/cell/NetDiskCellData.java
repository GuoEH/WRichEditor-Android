package cn.carbs.wricheditor.library.models.cell;

import org.json.JSONObject;

import cn.carbs.wricheditor.library.interfaces.BaseCellData;
import cn.carbs.wricheditor.library.interfaces.IRichCellData;
import cn.carbs.wricheditor.library.types.RichType;

// TODO 未完成
public class NetDiskCellData extends BaseCellData {

    public String fileUrl;

    public String fileName;

    public long fileSize;

    public String fileType;

    public int fileTypeImageRes;

    @Override
    public RichType getRichType() {
        return RichType.NETDISK;
    }

    // TODO
    @Override
    public String toHtml() {
        return "<div richType=\"" + getRichType().name()
                + "\" url=\"" + fileUrl
                + "\"></div>";
    }

    @Override
    public String toJson() {
        return getJson(getRichType().name(), fileUrl, fileName, fileSize, fileType);
    }

    @Override
    public IRichCellData fromJson(JSONObject json) {
        if (json == null) {
            return this;
        }
        try {
            JSONObject data = json.getJSONObject(JSON_KEY_DATA);
            fileUrl = data.getString("url");
            fileName = data.getString("name");
            fileSize = data.getLong("size");
            fileType = data.getString("type");
            // TODO 根据fileType设置图标
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public String getJson(String type, String url, String name, long size, String fileType) {
        return "{" +
                "\"" + JSON_KEY_TYPE + "\": " + "\"" + type + "\"," +
                "\"" + JSON_KEY_DATA + "\": " +
                "{" +
                "\"url\": " + "\"" + url + "\"," +
                "\"name\": " + "\"" + name + "\"," +
                "\"size\": " + size + "," +
                "\"type\": " + "\"" + fileType + "\"" +
                "}" +
                "}";
    }

}