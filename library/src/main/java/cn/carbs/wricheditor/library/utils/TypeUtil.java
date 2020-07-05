package cn.carbs.wricheditor.library.utils;

import cn.carbs.wricheditor.library.types.SpanType;

public class TypeUtil {

    /**
     * 判断一种类型是否为富文本类型（粗体、斜体）
     * @param spanType
     * @return
     */
    public static boolean isSpanTypeRichText(SpanType spanType) {
        if (spanType == SpanType.BOLD
                || spanType == SpanType.ITALIC) {
            return true;
        }
        return false;
    }

    /**
     * 判断一种类型是否为多媒体资源类型（图片、视频、网盘）
     * @param spanType
     * @return
     */
    public static boolean isSpanTypeRichResource(SpanType spanType) {
        if (spanType == SpanType.IMAGE
                || spanType == SpanType.VIDEO
                || spanType == SpanType.MUSIC
                || spanType == SpanType.NETDISK) {
            return true;
        }
        return false;
    }
}
