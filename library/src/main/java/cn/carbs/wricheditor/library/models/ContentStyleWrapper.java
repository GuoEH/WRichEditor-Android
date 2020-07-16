package cn.carbs.wricheditor.library.models;

import cn.carbs.wricheditor.library.utils.HtmlUtil;

public class ContentStyleWrapper {

    public StringBuilder contentBuilder = new StringBuilder();

    public int mask;

    public ContentStyleWrapper(int mask) {
        this.mask = mask;
    }

    public ContentStyleWrapper(int mask, String str) {
        this.mask = mask;
        contentBuilder.append(str);
    }

    public void append(String text) {
        contentBuilder.append(text);
    }

    @Override
    public String toString() {
        return toHtmlString();
    }

    public String toHtmlString() {
        return HtmlUtil.getHtmlByContentStyleWrapper(this);
    }

    public int getContentLength() {
        return contentBuilder.length();
    }
}
