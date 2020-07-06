package cn.carbs.wricheditor.library.spannables;

import android.text.style.StrikethroughSpan;

import cn.carbs.wricheditor.library.interfaces.IRichSpan;
import cn.carbs.wricheditor.library.types.RichType;

public class StrikeThroughStyleSpan extends StrikethroughSpan implements IRichSpan {

    private RichType type;

    public StrikeThroughStyleSpan() {
        super();
        type = RichType.STRIKE_THROUGH;
    }

}