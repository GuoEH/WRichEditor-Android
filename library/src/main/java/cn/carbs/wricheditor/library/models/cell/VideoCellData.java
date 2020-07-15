package cn.carbs.wricheditor.library.models.cell;

import cn.carbs.wricheditor.library.interfaces.IRichCellData;
import cn.carbs.wricheditor.library.types.RichType;

public class VideoCellData implements IRichCellData {

    public String videoLocalUrl;

    public String videoNetUrl;

    @Override
    public Object getData() {
        return this;
    }

    @Override
    public RichType getType() {
        return RichType.VIDEO;
    }

    // TODO
    @Override
    public String toHtml() {
        return "<div richType=\"" + getType().name()
                + "\" url=\"" + videoLocalUrl
                + "\"></div>";
    }

}
