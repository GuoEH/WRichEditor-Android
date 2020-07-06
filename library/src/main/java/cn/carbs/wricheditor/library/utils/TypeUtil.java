package cn.carbs.wricheditor.library.utils;

import java.util.Set;

import cn.carbs.wricheditor.library.interfaces.IRichSpan;
import cn.carbs.wricheditor.library.spannables.BoldStyleSpan;
import cn.carbs.wricheditor.library.spannables.ItalicStyleSpan;
import cn.carbs.wricheditor.library.spannables.StrikeThroughStyleSpan;
import cn.carbs.wricheditor.library.types.RichType;

public class TypeUtil {

    /**
     * 判断一种类型是否为富文本类型（粗体、斜体）
     * @param richType
     * @return
     */
    public static boolean isSpanTypeRichText(RichType richType) {
        if (richType == RichType.BOLD
                || richType == RichType.ITALIC) {
            return true;
        }
        return false;
    }

    /**
     * 判断一种类型是否为多媒体资源类型（图片、视频、网盘）
     * @param richType
     * @return
     */
    public static boolean isSpanTypeRichResource(RichType richType) {
        if (richType == RichType.IMAGE
                || richType == RichType.VIDEO
                || richType == RichType.MUSIC
                || richType == RichType.NETDISK) {
            return true;
        }
        return false;
    }

    /**
     * 判断集合中是否有对应的富文本类型，如果没有则添加，如果有则删除
     * @param richTypes
     * @param richType
     * @return 经过此函数调用后，此中富文本类型是否开启
     */
    public static boolean toggleCertainRichType(Set<RichType> richTypes, RichType richType) {
        if (richTypes.contains(richType)) {
            richTypes.remove(richType);
            return false;
        } else {
            richTypes.add(richType);
            return true;
        }
    }

    public static boolean checkIfRichTypesSetSame(Set<RichType> richTypesA, Set<RichType> richTypesB) {
        int sizeA = richTypesA == null ? 0 : richTypesA.size();
        int sizeB = richTypesB == null ? 0 : richTypesB.size();

        if (sizeA != sizeB) {
            return false;
        }

        // 两者数量相同，则检查A集合中的每个元素 B 集合都有
        for (RichType itemInA : richTypesA) {
            if (richTypesB.contains(itemInA)) {
                continue;
            }
            return false;
        }
        return true;
    }

    public static IRichSpan getSpanByType(RichType richType) {
        switch (richType) {
            case BOLD:
                return new BoldStyleSpan();
            case ITALIC:
                return new ItalicStyleSpan();
            case STRIKE_THROUGH:
                return new StrikeThroughStyleSpan();
        }
        return null;
    }


}
