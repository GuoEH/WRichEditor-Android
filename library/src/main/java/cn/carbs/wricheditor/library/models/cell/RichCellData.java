package cn.carbs.wricheditor.library.models.cell;

import android.text.Editable;
import android.util.Log;

import cn.carbs.wricheditor.library.interfaces.IRichCellData;
import cn.carbs.wricheditor.library.types.RichType;

public class RichCellData implements IRichCellData {

    // NONE, QUOTE, LIST_ORDERED, LIST_UNORDERED
    private RichType richType = RichType.NONE;

    public Editable editable;

    @Override
    public Object getData() {
        return this;
    }

    @Override
    public RichType getType() {
        return richType;
    }

    public void setRichType(RichType richType) {
        this.richType = richType;
    }

    public void setEditable(Editable editable) {
        this.editable = editable;
    }

    // TODO
    @Override
    public String toHtml() {
        String content = editable == null ? "" : editable.toString();
        String html =  "<div>" + content + "</div>";
//        String html = "fuuuuuuuuuuck";
        Log.d("wert", "html -->" + html + "<---");
        return html;
    }

}
