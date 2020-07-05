package cn.carbs.wricheditor.library.models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import cn.carbs.wricheditor.library.interfaces.IRichCellData;
import cn.carbs.wricheditor.library.types.RichType;

// 包含 粗体、斜体、列等等格式
public class RichCellData implements IRichCellData {

    private static Set<RichType> sRichTypes = new HashSet<>();

    static {
        sRichTypes.add(RichType.NONE);
    }

    public ArrayList<RichAtomicData> atomicDataList = new ArrayList<>();

    @Override
    public Object getData() {
        return this;
    }

    @Override
    public Set<RichType> getType() {
        // 需要更细粒度的区分
        return sRichTypes;
    }


}
