package cn.carbs.wricheditor.library.models;

public class SpanPart {

    private int start;
    private int end;

    public SpanPart(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public boolean isValid() {
        return start < end;
    }

}
