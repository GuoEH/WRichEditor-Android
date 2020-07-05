package cn.carbs.wricheditor;

import android.graphics.Typeface;
import android.text.style.StyleSpan;

/**
 * Title:
 * Description:
 *
 * @author yuruiyin
 * @version 2019-04-29
 */
public class MyItalicStyleSpan extends StyleSpan {

    private String type;

    public MyItalicStyleSpan() {
        super(Typeface.ITALIC);
    }

}
