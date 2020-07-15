package cn.carbs.wricheditor.library.models.cell;

import cn.carbs.wricheditor.library.interfaces.IRichCellData;
import cn.carbs.wricheditor.library.types.RichType;

public class ImageCellData implements IRichCellData {

    public String imageLocalUrl;

    public String imageNetUrl;

    @Override
    public Object getData() {
        return this;
    }

    @Override
    public RichType getType() {
        return RichType.IMAGE;
    }

    @Override
    public String toHtml() {
        return "<div richType=\"" + getType().name() + "\">" +
                    "<picture><img src=\"" + imageNetUrl + "\"></picture>" +
                "</div>";
    }

}