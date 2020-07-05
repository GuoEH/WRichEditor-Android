package cn.carbs.wricheditor.library.spannables;

import android.graphics.Typeface;
import android.text.style.StyleSpan;

import cn.carbs.wricheditor.library.types.SpanType;


public class BoldStyleSpan extends StyleSpan {

    private SpanType type;

    public BoldStyleSpan() {
        super(Typeface.BOLD);
        type = SpanType.BOLD;
    }

}