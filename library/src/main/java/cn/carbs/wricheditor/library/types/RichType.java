package cn.carbs.wricheditor.library.types;

public enum RichType {

    NONE(true),

    // 粗体
    BOLD(true),

    // 斜体
    ITALIC(true),

    // 中横线
    STRIKE_THROUGH(true),

    // 下划线
    UNDER_LINE(true),

    // 链接
    LINK(true),

    // 标题
    HEADLINE(true),

    // 引用
    QUOTE(true),

    // 有序列表
    LIST_ORDERED(true),

    // 无序列表
    LIST_UNORDERED(true),

    // 多媒体类型，采用自定义view
    IMAGE(false),

    VIDEO(false),

    AUDIO(false),

    NETDISK(false);

    private boolean hasEditor;

    RichType(boolean hasEditor) {
        this.hasEditor = hasEditor;
    }

    public boolean getHasEditor() {
        return hasEditor;
    }

}
