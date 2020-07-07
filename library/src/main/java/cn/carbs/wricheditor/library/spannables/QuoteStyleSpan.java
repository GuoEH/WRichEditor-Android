package cn.carbs.wricheditor.library.spannables;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Parcel;
import android.text.Layout;
import android.text.ParcelableSpan;
import android.text.style.LeadingMarginSpan;
import android.util.Log;

import cn.carbs.wricheditor.library.interfaces.IRichSpan;
import cn.carbs.wricheditor.library.types.RichType;

public class QuoteStyleSpan implements LeadingMarginSpan, ParcelableSpan, IRichSpan {
    /**
     * Default stripe width in pixels.
     */
    public static final int STANDARD_STRIPE_WIDTH_PX = 2;

    /**
     * Default gap width in pixels.
     */
    public static final int STANDARD_GAP_WIDTH_PX = 2;

    /**
     * Default color for the quote stripe.
     */
    public static final int STANDARD_COLOR = 0xff0000ff;

    private final int mColor;
    private final int mStripeWidth;
    private final int mGapWidth;

    private RichType type;

    /**
     * Creates a {@link android.text.style.QuoteSpan} with the default values.
     */
    public QuoteStyleSpan() {
        this(STANDARD_COLOR, STANDARD_STRIPE_WIDTH_PX, STANDARD_GAP_WIDTH_PX);
    }

    /**
     * Creates a {@link android.text.style.QuoteSpan} based on a color.
     *
     * @param color the color of the quote stripe.
     */
    public QuoteStyleSpan(int color) {
        this(color, STANDARD_STRIPE_WIDTH_PX, STANDARD_GAP_WIDTH_PX);
    }

    /**
     * Creates a {@link android.text.style.QuoteSpan} based on a color, a stripe width and the width of the gap
     * between the stripe and the text.
     *
     * @param color       the color of the quote stripe.
     * @param stripeWidth the width of the stripe.
     * @param gapWidth    the width of the gap between the stripe and the text.
     */
    public QuoteStyleSpan(int color, int stripeWidth, int gapWidth) {
        mColor = color;
        mStripeWidth = stripeWidth;
        mGapWidth = gapWidth;
        type = RichType.QUOTE;
    }

    /**
     * Create a {@link android.text.style.QuoteSpan} from a parcel.
     */
    public QuoteStyleSpan(Parcel src) {
        mColor = src.readInt();
        mStripeWidth = src.readInt();
        mGapWidth = src.readInt();
        type = RichType.QUOTE;
    }

    @Override
    public int getSpanTypeId() {
        return getSpanTypeIdInternal();
    }

    public int getSpanTypeIdInternal() {
//        return TextUtils.QUOTE_SPAN;
        return 9;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        writeToParcelInternal(dest, flags);
    }

    public void writeToParcelInternal(Parcel dest, int flags) {
        dest.writeInt(mColor);
        dest.writeInt(mStripeWidth);
        dest.writeInt(mGapWidth);
    }

    /**
     * Get the color of the quote stripe.
     *
     * @return the color of the quote stripe.
     */
    public int getColor() {
        return mColor;
    }

    /**
     * Get the width of the quote stripe.
     *
     * @return the width of the quote stripe.
     */
    public int getStripeWidth() {
        return mStripeWidth;
    }

    /**
     * Get the width of the gap between the stripe and the text.
     *
     * @return the width of the gap between the stripe and the text.
     */
    public int getGapWidth() {
        return mGapWidth;
    }

    @Override
    public int getLeadingMargin(boolean first) {
        return mStripeWidth + mGapWidth;
    }

    @Override
    public void drawLeadingMargin(Canvas c, Paint p, int x, int dir,
                                  int top, int baseline, int bottom,
                                  CharSequence text, int start, int end,
                                  boolean first, Layout layout) {
        Log.d("mmm", "drawLeadingMargin");
        Paint.Style style = p.getStyle();
        int color = p.getColor();

        p.setStyle(Paint.Style.FILL);
        p.setColor(mColor);

        c.drawRect(x, top, x + dir * mStripeWidth, bottom, p);

        p.setStyle(style);
        p.setColor(color);
    }

    @Override
    public RichType getRichType() {
        return type;
    }
}
