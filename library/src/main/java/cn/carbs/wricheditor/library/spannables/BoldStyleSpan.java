package cn.carbs.wricheditor.library.spannables;

import android.graphics.Typeface;
import android.text.style.StyleSpan;

import cn.carbs.wricheditor.library.interfaces.IRichSpan;
import cn.carbs.wricheditor.library.types.RichType;


public class BoldStyleSpan extends StyleSpan implements IRichSpan {

    private RichType type;

    public BoldStyleSpan() {
        super(Typeface.BOLD);
        type = RichType.BOLD;
    }

    @Override
    public RichType getRichType() {
        return type;
    }

}