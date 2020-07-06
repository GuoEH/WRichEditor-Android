package cn.carbs.wricheditor.library.spannables;

import android.graphics.Typeface;
import android.text.style.StyleSpan;

import cn.carbs.wricheditor.library.interfaces.IRichSpan;
import cn.carbs.wricheditor.library.types.RichType;

public class ItalicStyleSpan extends StyleSpan implements IRichSpan {

    private RichType type;

    public ItalicStyleSpan() {
        super(Typeface.ITALIC);
        type = RichType.ITALIC;
    }

}