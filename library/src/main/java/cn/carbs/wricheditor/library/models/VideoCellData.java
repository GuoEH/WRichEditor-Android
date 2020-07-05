package cn.carbs.wricheditor.library.models;

import java.util.HashSet;
import java.util.Set;

import cn.carbs.wricheditor.library.interfaces.IRichCellData;
import cn.carbs.wricheditor.library.types.RichType;

public class VideoCellData implements IRichCellData {

    private static Set<RichType> sRichTypes = new HashSet<>();

    static {
        sRichTypes.add(RichType.VIDEO);
    }

    public String videoLocalUrl;

    public String videoNetUrl;

    public String imageLocalUrl;

    public String imageNetUrl;

    @Override
    public Object getData() {
        return this;
    }

    @Override
    public Set<RichType> getType() {
        return sRichTypes;
    }


}
