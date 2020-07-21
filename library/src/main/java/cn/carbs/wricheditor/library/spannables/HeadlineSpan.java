package cn.carbs.wricheditor.library.spannables;

import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.AbsoluteSizeSpan;

import cn.carbs.wricheditor.library.interfaces.IRichSpan;
import cn.carbs.wricheditor.library.types.RichType;
import cn.carbs.wricheditor.library.utils.HtmlUtil;

public class HeadlineSpan extends AbsoluteSizeSpan implements IRichSpan {

    public static final int MASK_SHIFT = 1;

    public static final int MASK = 0x00000001 << MASK_SHIFT;

    private RichType type;

    public HeadlineSpan(int textSize) {
        super(textSize);
        type = RichType.HEADLINE;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
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
