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
public class MyBoldStyleSpan extends StyleSpan {

    private String type;

    public MyBoldStyleSpan() {
        super(Typeface.BOLD);
    }

}
