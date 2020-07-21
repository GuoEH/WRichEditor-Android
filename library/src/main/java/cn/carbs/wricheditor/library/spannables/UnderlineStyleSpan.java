package cn.carbs.wricheditor.library.spannables;

import android.text.style.UnderlineSpan;

import cn.carbs.wricheditor.library.interfaces.IRichSpan;
import cn.carbs.wricheditor.library.types.RichType;
import cn.carbs.wricheditor.library.utils.HtmlUtil;

public class UnderlineStyleSpan extends UnderlineSpan implements IRichSpan {

    public static final int MASK_SHIFT = 5;

    public static final int MASK = 0x00000001 << MASK_SHIFT;

    private RichType type;

    public UnderlineStyleSpan() {
        super();
        type = RichType.UNDER_LINE;
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
