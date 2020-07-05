package cn.carbs.wricheditor.library.utils;

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
}
