package cn.carbs.wricheditor.library.interfaces;

public abstract class BaseCellData implements IRichCellData {

    public static final String JSON_KEY_TYPE = "type";

    public static final String JSON_KEY_DATA = "data";

    public static final String JSON_KEY_EXTRA = "extra";

    public IRichCellView cellView;

    public void setIRichCellView(IRichCellView cellView) {
        this.cellView = cellView;
    }

    public IRichCellView getIRichCellView() {
        return cellView;
    }

}
