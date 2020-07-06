package cn.carbs.wricheditor.library.spannables;

import android.os.Parcel;
import android.text.TextPaint;
import android.text.style.URLSpan;

import cn.carbs.wricheditor.library.interfaces.IRichSpan;
import cn.carbs.wricheditor.library.types.RichType;

public class LinkStyleSpan extends URLSpan implements IRichSpan {

    private int linkColor;
    private boolean linkUnderline;

    private RichType type;

    public LinkStyleSpan(String url, int linkColor, boolean linkUnderline) {
        super(url);
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
}
