package cn.carbs.wricheditor.library.spannables;

import android.text.style.StrikethroughSpan;

import cn.carbs.wricheditor.library.interfaces.IRichSpan;
import cn.carbs.wricheditor.library.types.RichType;
import cn.carbs.wricheditor.library.utils.HtmlUtil;

public class StrikeThroughStyleSpan extends StrikethroughSpan implements IRichSpan {

    public static final int MASK_SHIFT = 4;

    public static final int MASK = 0x00000001 << MASK_SHIFT;

    private RichType type;

    public StrikeThroughStyleSpan() {
        super();
        type = RichType.STRIKE_THROUGH;
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