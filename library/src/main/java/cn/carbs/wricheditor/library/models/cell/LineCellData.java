package cn.carbs.wricheditor.library.models.cell;

import org.json.JSONObject;

import cn.carbs.wricheditor.library.interfaces.BaseCellData;
import cn.carbs.wricheditor.library.interfaces.ICellDataJsonAdapter;
import cn.carbs.wricheditor.library.interfaces.IRichCellData;
import cn.carbs.wricheditor.library.types.RichType;

public class LineCellData extends BaseCellData {

    @Override
    public Object getData() {
        return this;
    }

    @Override
    public RichType getType() {
        return RichType.LINE;
    }

    @Override
    public String toHtml() {
        return "<div richType=\"" + getType().name() + "\">" +
                    "<br/><hr/><br/>" +
                "</div>";
    }

    @Override
    public String toJson() {
        if (adapter != null) {
            return adapter.toJson(this);
        }
        return getJson(getType().name());
    }

    @Override
    public IRichCellData fromJson(String json) {
        if (adapter != null) {
            return inflate(adapter.fromJson(json));
        }
        return this;
    }

    @Override
    public IRichCellData inflate(IRichCellData data) {
        return this;
    }

    public String getJson(String type) {
        return "{" +
                "\"type\": " + "\"" + type + "\"," +
                "}";
    }

}