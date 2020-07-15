package cn.carbs.wricheditor.library.models.cell;

import cn.carbs.wricheditor.library.interfaces.IRichCellData;
import cn.carbs.wricheditor.library.types.RichType;

public class LineCellData implements IRichCellData {

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
        return "<div richType=\"" + getType().name()
                + "\"><hr /></div>";
    }

}