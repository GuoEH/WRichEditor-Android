package cn.carbs.wricheditor.library.utils;

import android.text.TextUtils;

import cn.carbs.wricheditor.library.models.ContentStyleWrapper;
import cn.carbs.wricheditor.library.models.cell.RichCellData;

public class JsonUtil {

    // 为了使用 mask 做比较，没有使用 IRichSpan 对象，降低了抽象性
    public static String getJsonStringByContentStyleWrapper(ContentStyleWrapper wrapper) {

        if (wrapper == null || wrapper.getContentLength() == 0) {
            return "";
        }
        String string = wrapper.contentBuilder.toString();
        string = string.replaceAll("\"", "\\\\\"");

        if (TextUtils.isEmpty(wrapper.extra)) {
            return "{" +
                    "\"" + RichCellData.JSON_KEY_MASK + "\": " + wrapper.mask + "," +
                    "\"" + RichCellData.JSON_KEY_TEXT + "\": " + "\"" + string + "\"" +
                    "}";
        } else {
            return "{" +
                    "\"" + RichCellData.JSON_KEY_MASK + "\": " + wrapper.mask + "," +
                    "\"" + RichCellData.JSON_KEY_EXTRA + "\": " + "\"" + wrapper.extra + "\"," +
                    "\"" + RichCellData.JSON_KEY_TEXT + "\": " + "\"" + string + "\"" +
                    "}";
        }
    }

}