package cn.carbs.wricheditor.library.utils;

import android.util.Log;

import cn.carbs.wricheditor.library.models.ContentStyleWrapper;
import cn.carbs.wricheditor.library.models.cell.RichCellData;
import cn.carbs.wricheditor.library.spannables.BoldStyleSpan;
import cn.carbs.wricheditor.library.spannables.HeadlineSpan;
import cn.carbs.wricheditor.library.spannables.ItalicStyleSpan;
import cn.carbs.wricheditor.library.spannables.LinkStyleSpan;
import cn.carbs.wricheditor.library.spannables.StrikeThroughStyleSpan;
import cn.carbs.wricheditor.library.spannables.UnderlineStyleSpan;

public class JsonUtil {

    // 为了使用 mask 做比较，没有使用 IRichSpan 对象，降低了抽象性
    public static String getJsonStringByContentStyleWrapper(ContentStyleWrapper wrapper) {

        if (wrapper == null || wrapper.getContentLength() == 0) {
            return "";
        }
        // TODO

        String string = wrapper.contentBuilder.toString();
        Log.d("json", "string0 : " + string);
        string = string.replaceAll("\"", "\\\\\"");
        Log.d("json", "string1 : " + string);

        return "{" +
                "\"" + RichCellData.JSON_KEY_MASK + "\": " + wrapper.mask + "," +
                "\"" + RichCellData.JSON_KEY_TEXT + "\": " + "\"" + string + "\"" +
                "}";
    }

}
