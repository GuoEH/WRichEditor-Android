package cn.carbs.wricheditor.library.models;

import cn.carbs.wricheditor.library.interfaces.IRichSpan;

public class SpanPart {

    private int start;
    private int end;
    private IRichSpan richSpan;

    public SpanPart(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public SpanPart(int start, int end, IRichSpan iRichSpan) {
        this.start = start;
        this.end = end;
        this.richSpan = iRichSpan;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public IRichSpan getRichSpan() {
        return richSpan;
    }

    public boolean isValid() {
        return start < end;
    }

}