package cn.carbs.wricheditor.library.utils;

import cn.carbs.wricheditor.library.models.ContentStyleWrapper;
import cn.carbs.wricheditor.library.spannables.BoldStyleSpan;
import cn.carbs.wricheditor.library.spannables.HeadlineSpan;
import cn.carbs.wricheditor.library.spannables.ItalicStyleSpan;
import cn.carbs.wricheditor.library.spannables.LinkStyleSpan;
import cn.carbs.wricheditor.library.spannables.StrikeThroughStyleSpan;
import cn.carbs.wricheditor.library.spannables.UnderlineStyleSpan;

public class HtmlUtil {

    // 为了使用 mask 做比较，没有使用 IRichSpan 对象，降低了抽象性
    public static String getHtmlByContentStyleWrapper(ContentStyleWrapper wrapper) {

        if (wrapper == null || wrapper.getContentLength() == 0) {
            return "";
        }
        StringBuilder sbPrev = new StringBuilder();
        StringBuilder sbPost = new StringBuilder();
        String content = wrapper.contentBuilder.toString();

        if ((wrapper.mask & BoldStyleSpan.MASK) >> BoldStyleSpan.MASK_SHIFT == 1) {
            sbPrev.append(getHtmlTagStartForBold());
            sbPost.insert(0, getHtmlTagEndForBold());
        }
        if ((wrapper.mask & HeadlineSpan.MASK) >> HeadlineSpan.MASK_SHIFT == 1) {
            sbPrev.append(getHtmlTagStartForHeadline());
            sbPost.insert(0, getHtmlTagEndForHeadline());
        }
        if ((wrapper.mask & ItalicStyleSpan.MASK) >> ItalicStyleSpan.MASK_SHIFT == 1) {
            sbPrev.append(getHtmlTagStartForItalic());
            sbPost.insert(0, getHtmlTagEndForItalic());
        }
        if ((wrapper.mask & LinkStyleSpan.MASK) >> LinkStyleSpan.MASK_SHIFT == 1) {
            sbPrev.append(getHtmlTagStartForLink(content));
            sbPost.insert(0, getHtmlTagEndForLink());
        }
        if ((wrapper.mask & StrikeThroughStyleSpan.MASK) >> StrikeThroughStyleSpan.MASK_SHIFT == 1) {
            sbPrev.append(getHtmlTagStartForStrikeThrough());
            sbPost.insert(0, getHtmlTagEndForStrikeThrough());
        }
        if ((wrapper.mask & UnderlineStyleSpan.MASK) >> UnderlineStyleSpan.MASK_SHIFT == 1) {
            sbPrev.append(getHtmlTagStartForUnderline());
            sbPost.insert(0, getHtmlTagEndForUnderline());
        }
        return sbPrev.toString() + content + sbPost.toString();
    }

    // 分别计算，暂时没想到好的优化方法
    public static String getHtmlTagStartForBold() {
        return "<b>";
    }

    public static String getHtmlTagEndForBold() {
        return "</b>";
    }

    public static String getHtmlTagStartForHeadline() {
        return "<h2>";
    }

    public static String getHtmlTagEndForHeadline() {
        return "</h2>";
    }

    public static String getHtmlTagStartForItalic() {
        return "<i>";
    }

    public static String getHtmlTagEndForItalic() {
        return "</i>";
    }

    public static String getHtmlTagStartForLink(String url) {
        return "<a href=\"" + url + "\">";
    }

    public static String getHtmlTagEndForLink() {
        return "</a>";
    }

    public static String getHtmlTagStartForStrikeThrough() {
        return "<s>";
    }

    public static String getHtmlTagEndForStrikeThrough() {
        return "</s>";
    }

    public static String getHtmlTagStartForUnderline() {
        return "<u>";
    }

    public static String getHtmlTagEndForUnderline() {
        return "</u>";
    }

}
