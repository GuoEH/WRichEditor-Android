package cn.carbs.wricheditor.library.models;

import java.util.HashSet;
import java.util.Set;

import cn.carbs.wricheditor.library.types.RichType;

// 映射某种格式，如粗体、斜体、
public class RichAtomicData {

    private Set<RichType> richTypes = new HashSet<>();

    // 如果是列表，同样用一个text代表，只不过每一行是由换行符相隔
    private String text;

    private int textLength;

    public Set<RichType> getType() {
        // 可能同时具有两种字体，比如同时选中了粗体+斜体
        return richTypes;
    }

    public void setText(String text) {
        this.text = text;
        if (text == null) {
            this.textLength = 0;
        } else {
            this.textLength = text.length();
        }
    }

    public String getText() {
        return text;
    }

    public int getTextLength() {
        return textLength;
    }

}