package cn.carbs.wricheditor.library.models;

public class SplitPart {

    private String text;
    // 在整个字符串中的起始位置，included
    private int start;
    // 在整个字符串中的终止位置，excluded
    private int end;
    // 字符串长度
    private int length;

    public SplitPart(String text, int start) {
        this.start = start;
        this.text = text;
        this.length = text.length();
        this.end = start + this.length;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

}