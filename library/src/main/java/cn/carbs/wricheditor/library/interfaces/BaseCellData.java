package cn.carbs.wricheditor.library.interfaces;

import cn.carbs.wricheditor.library.types.RichType;

public abstract class BaseCellData implements IRichCellData {

    public static final String JSON_KEY_TYPE = "type";

    public static final String JSON_KEY_DATA = "data";

    public IRichCellView cellView;

    public void setIRichCellView(IRichCellView cellView) {
        this.cellView = cellView;
    }

    public IRichCellView getIRichCellView() {
        return cellView;
    }

}
