package cn.carbs.wricheditor.library.spannables;

import android.os.Parcel;
import android.text.TextPaint;
import android.text.style.URLSpan;

import cn.carbs.wricheditor.library.interfaces.IRichSpan;
import cn.carbs.wricheditor.library.types.RichType;
import cn.carbs.wricheditor.library.utils.HtmlUtil;

public class LinkStyleSpan extends URLSpan implements IRichSpan {

    public static final int MASK_SHIFT = 3;

    public static final int MASK = 0x00000001 << MASK_SHIFT;

    private int linkColor;
    private boolean linkUnderline;

    private RichType type;

    private String url;

    public LinkStyleSpan(String url, int linkColor, boolean linkUnderline) {
        super(url);
        this.url = url;
        this.linkColor = linkColor;
        this.linkUnderline = linkUnderline;
        type = RichType.LINK;
    }

    public LinkStyleSpan(Parcel src) {
        super(src);
        this.linkColor = src.readInt();
        this.linkUnderline = src.readInt() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(linkColor);
        dest.writeInt(linkUnderline ? 1 : 0);
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        ds.setColor(linkColor != 0 ? linkColor : ds.linkColor);
        ds.setUnderlineText(linkUnderline);
    }

    @Override
    public RichType getRichType() {
        return type;
    }

    @Override
    public int getMask() {
        return MASK;
    }

}
