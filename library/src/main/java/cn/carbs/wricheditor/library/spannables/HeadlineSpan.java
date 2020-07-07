package cn.carbs.wricheditor.library.spannables;

import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.AbsoluteSizeSpan;

import cn.carbs.wricheditor.library.interfaces.IRichSpan;
import cn.carbs.wricheditor.library.types.RichType;

public class HeadlineSpan extends AbsoluteSizeSpan implements IRichSpan {

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
}
