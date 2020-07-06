package cn.carbs.wricheditor.library.spannables;

import android.graphics.Typeface;
import android.text.style.StyleSpan;

import cn.carbs.wricheditor.library.interfaces.IRichSpan;
import cn.carbs.wricheditor.library.types.RichType;

// 这个没什么用
public class NormalStyleSpan extends StyleSpan implements IRichSpan {

    private RichType type;

    public NormalStyleSpan() {
        super(Typeface.NORMAL);
        type = RichType.BOLD;
    }

}