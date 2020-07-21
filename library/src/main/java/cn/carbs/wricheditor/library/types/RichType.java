package cn.carbs.wricheditor.library.types;

import cn.carbs.wricheditor.library.constants.RichTypeConstants;

public enum RichType {

    // wrapper 的默认状态,非quote orderList unorderedList
    NONE(true, RichTypeConstants.GROUP_CHAR_FORMAT),

    // 粗体
    BOLD(true, RichTypeConstants.GROUP_CHAR_FORMAT),

    // 斜体
    ITALIC(true, RichTypeConstants.GROUP_CHAR_FORMAT),

    // 中横线
    STRIKE_THROUGH(true, RichTypeConstants.GROUP_CHAR_FORMAT),

    // 下划线
    UNDER_LINE(true, RichTypeConstants.GROUP_CHAR_FORMAT),

    // 链接
    LINK(true, RichTypeConstants.GROUP_CHAR_FORMAT),

    // 标题
    HEADLINE(true, RichTypeConstants.GROUP_CHAR_FORMAT),

    // 引用
    QUOTE(true, RichTypeConstants.GROUP_LINE_FORMAT),

    // 有序列表
    LIST_ORDERED(true, RichTypeConstants.GROUP_LINE_FORMAT),

    // 无序列表
    LIST_UNORDERED(true, RichTypeConstants.GROUP_LINE_FORMAT),

    // 多媒体类型，图像
    IMAGE(false, RichTypeConstants.GROUP_RESOURCE),

    // 多媒体类型，视频
    VIDEO(false, RichTypeConstants.GROUP_RESOURCE),

    // 多媒体类型，音频
    AUDIO(false, RichTypeConstants.GROUP_RESOURCE),

    // 多媒体类型，网盘
    NETDISK(false, RichTypeConstants.GROUP_RESOURCE),

    // 横线
    LINE(false, RichTypeConstants.GROUP_RESOURCE);

    private boolean hasEditor;

    // 表明所处的组，quote、orderedList、unorderedList互斥
    private int group;

    RichType(boolean hasEditor, int group) {
        this.hasEditor = hasEditor;
        this.group = group;
    }

    public boolean getHasEditor() {
        return hasEditor;
    }

    public int getGroup() {
        return group;
    }

}
