package cn.carbs.wricheditor.library.models.cell;

import org.json.JSONObject;

import cn.carbs.wricheditor.library.interfaces.BaseCellData;
import cn.carbs.wricheditor.library.interfaces.IRichCellData;
import cn.carbs.wricheditor.library.types.RichType;

public class LineCellData extends BaseCellData {

    @Override
    public RichType getRichType() {
        return RichType.LINE;
    }

    @Override
    public String toHtml() {
        return "<div richType=\"" + getRichType().name() + "\">" +
                    "<br/><hr/><br/>" +
                "</div>";
    }

    @Override
    public String toJson() {
        return getJson(getRichType().name());
    }

    @Override
    public IRichCellData fromJson(JSONObject json) {
        return this;
    }

    public String getJson(String type) {
        return "{" +
                "\"" + JSON_KEY_TYPE + "\": " + "\"" + type + "\"" +
                "}";
    }

}