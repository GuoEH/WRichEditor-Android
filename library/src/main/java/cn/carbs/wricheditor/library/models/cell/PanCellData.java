package cn.carbs.wricheditor.library.models.cell;

import org.json.JSONObject;

import cn.carbs.wricheditor.library.interfaces.BaseCellData;
import cn.carbs.wricheditor.library.interfaces.IRichCellData;
import cn.carbs.wricheditor.library.types.RichType;

public class PanCellData extends BaseCellData {

    public String fileUrl;

    public String fileName;

    public long fileSize;

    public String fileType;

    public int fileTypeImageRes;

    @Override
    public Object getData() {
        return this;
    }

    @Override
    public RichType getType() {
        return RichType.NETDISK;
    }

    // TODO
    @Override
    public String toHtml() {
        return "<div richType=\"" + getType().name()
                + "\" url=\"" + fileUrl
                + "\"></div>";
    }

    @Override
    public String toJson() {
        if (adapter != null) {
            return adapter.toJson(this);
        }
        return getJson(getType().name(), fileUrl, fileName, fileSize, fileType);
    }

    @Override
    public IRichCellData fromJson(String json) {
        if (adapter != null) {
            return inflate(adapter.fromJson(json));
        }
        try {
            JSONObject obj = new JSONObject(json);
            JSONObject data = obj.getJSONObject("data");
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

    @Override
    public IRichCellData inflate(IRichCellData data) {
        if (data instanceof PanCellData) {
            fileUrl = ((PanCellData) data).fileUrl;
            fileName = ((PanCellData) data).fileName;
            fileType = ((PanCellData) data).fileType;
            fileSize = ((PanCellData) data).fileSize;
            // TODO 根据fileType设置图标
        }
        return this;
    }

    public String getJson(String type, String url, String name, long size, String fileType) {
        return "{" +
                "\"type\": " + "\"" + type + "\"," +
                "\"data\": " +
                "{" +
                "\"url\": " + "\"" + url + "\"," +
                "\"name\": " + "\"" + name + "\"," +
                "\"size\": " + size + "," +
                "\"type\": " + "\"" + fileType + "\"" +
                "}" +
                "}";
    }

}