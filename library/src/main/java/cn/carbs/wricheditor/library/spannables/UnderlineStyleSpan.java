package cn.carbs.wricheditor.library.spannables;

import android.text.style.UnderlineSpan;

import cn.carbs.wricheditor.library.interfaces.IRichSpan;
import cn.carbs.wricheditor.library.types.RichType;

public class UnderlineStyleSpan extends UnderlineSpan implements IRichSpan {

    private RichType type;

    public UnderlineStyleSpan() {
        super();
        type = RichType.UNDER_LINE;
    }

    @Override
    public RichType getRichType() {
        return type;
    }
}
