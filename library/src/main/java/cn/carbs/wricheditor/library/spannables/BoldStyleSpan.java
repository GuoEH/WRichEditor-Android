package cn.carbs.wricheditor.library.spannables;

import android.graphics.Typeface;
import android.text.style.StyleSpan;

import cn.carbs.wricheditor.library.interfaces.IRichSpan;
import cn.carbs.wricheditor.library.types.RichType;
import cn.carbs.wricheditor.library.utils.HtmlUtil;

public class BoldStyleSpan extends StyleSpan implements IRichSpan {

    public static final int MASK_SHIFT = 0;

    public static final int MASK = 0x00000001 << MASK_SHIFT;

    private RichType type;

    public BoldStyleSpan() {
        super(Typeface.BOLD);
        type = RichType.BOLD;
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