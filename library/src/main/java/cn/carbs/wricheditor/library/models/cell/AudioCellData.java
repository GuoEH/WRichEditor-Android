package cn.carbs.wricheditor.library.models.cell;

import cn.carbs.wricheditor.library.interfaces.IRichCellData;
import cn.carbs.wricheditor.library.types.RichType;

public class AudioCellData implements IRichCellData {

    public String audioLocalUrl;

    public String audioNetUrl;

    @Override
    public Object getData() {
        return this;
    }

    @Override
    public RichType getType() {
        return RichType.AUDIO;
    }

    @Override
    public String toHtml() {
//        <div richType="AUDIO" url="xxx"></div>
        return "<div richType=\"" + getType().name()
                + "\" url=\"" + audioNetUrl
                + "\"></div>";
    }
}
