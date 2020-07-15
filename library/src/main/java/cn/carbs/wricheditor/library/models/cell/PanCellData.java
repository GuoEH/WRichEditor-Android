package cn.carbs.wricheditor.library.models.cell;

import cn.carbs.wricheditor.library.interfaces.IRichCellData;
import cn.carbs.wricheditor.library.types.RichType;

public class PanCellData implements IRichCellData {

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
}