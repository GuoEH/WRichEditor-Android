package cn.carbs.wricheditor.library.utils;

import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import cn.carbs.wricheditor.library.WRichEditor;
import cn.carbs.wricheditor.library.WEditTextWrapperView;
import cn.carbs.wricheditor.library.configures.RichEditorConfig;
import cn.carbs.wricheditor.library.constants.RichTypeConstants;
import cn.carbs.wricheditor.library.interfaces.IRichCellView;
import cn.carbs.wricheditor.library.interfaces.IRichSpan;
import cn.carbs.wricheditor.library.spannables.BoldStyleSpan;
import cn.carbs.wricheditor.library.spannables.HeadlineSpan;
import cn.carbs.wricheditor.library.spannables.ItalicStyleSpan;
import cn.carbs.wricheditor.library.spannables.LinkStyleSpan;
import cn.carbs.wricheditor.library.spannables.StrikeThroughStyleSpan;
import cn.carbs.wricheditor.library.spannables.UnderlineStyleSpan;
import cn.carbs.wricheditor.library.types.RichType;

public class TypeUtil {

    /**
     * 判断集合中是否有对应的富文本类型，如果没有则添加，如果有则删除
     *
     * @param richTypes
     * @param richType
     * @return 经过此函数调用后，此中富文本类型是否开启
     */
    public static boolean toggleCertainRichType(Set<RichType> richTypes, RichType richType) {
        if (richTypes.contains(richType)) {
            richTypes.remove(richType);
            return false;
        } else {
            richTypes.add(richType);
            return true;
        }
    }

    /**
     * 返回集合是否有变化
     *
     * @param richTypes
     * @param richType
     * @return 是否改变了
     */
    public static boolean removeCertainRichType(Set<RichType> richTypes, RichType richType) {
        if (richTypes.contains(richType)) {
            richTypes.remove(richType);
            return true;
        }
        return false;
    }

    public static Set<RichType> assembleRichTypes(Set<RichType> richTypeSet, RichType... richTypes) {
        Set<RichType> ret = new HashSet<>();
        if (richTypeSet != null) {
            ret.addAll(richTypeSet);
        }
        if (richTypes != null) {
            for (RichType item : richTypes) {
                if (item != null) {
                    ret.add(item);
                }
            }
        }
        return ret;
    }

    public static void correctLineFormatGroupType(Set<RichType> currRichTypes, WEditTextWrapperView wrapperView) {
        if (wrapperView != null && currRichTypes != null) {
            RichType richType = wrapperView.getRichType();
            ArrayList<RichType> toBeRemovedList = new ArrayList<>();
            for (RichType richType1 : currRichTypes) {
                if (richType1.getGroup() == RichTypeConstants.GROUP_LINE_FORMAT) {
                    toBeRemovedList.add(richType1);
                }
            }
            for (RichType toBeRemoved : toBeRemovedList) {
                currRichTypes.remove(toBeRemoved);
            }
            currRichTypes.add(richType);
        }
    }

    public static IRichSpan getSpanByType(RichType richType, Object extra) {
        switch (richType) {
            case BOLD:
                return new BoldStyleSpan();
            case ITALIC:
                return new ItalicStyleSpan();
            case STRIKE_THROUGH:
                return new StrikeThroughStyleSpan();
            case UNDER_LINE:
                return new UnderlineStyleSpan();
            case LINK:
                return new LinkStyleSpan((String) extra, RichEditorConfig.sLinkColor, RichEditorConfig.sLinkUnderline);
            case HEADLINE:
                return new HeadlineSpan(RichEditorConfig.sHeadlineTextSize);
            case QUOTE:
                return null;
        }
        return null;
    }

    public static void selectOnlyOneResourceType(WRichEditor scrollView, View currentSelectedView) {
        if (scrollView == null) {
            return;
        }
        if (scrollView.getContainerView() == null) {
            return;
        }
        ViewGroup viewGroup = scrollView.getContainerView();
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = viewGroup.getChildAt(i);
            if (childView instanceof IRichCellView) {
                IRichCellView cellView = (IRichCellView) childView;
                RichType richType = cellView.getRichType();
                if (richType != null && richType.getGroup() == RichTypeConstants.GROUP_RESOURCE) {
                    cellView.setSelectMode(cellView == currentSelectedView);
                }
            }
        }
    }

    public static void selectOnlyOneResourceType(WRichEditor scrollView, int currentSelectedIndex) {
        if (scrollView == null) {
            return;
        }
        if (scrollView.getContainerView() == null) {
            return;
        }
        ViewGroup viewGroup = scrollView.getContainerView();
        int childCount = viewGroup.getChildCount();

        if (viewGroup != null && currentSelectedIndex < childCount - 1) {
            View targetView = viewGroup.getChildAt(currentSelectedIndex);
            for (int i = 0; i < childCount; i++) {
                View childView = viewGroup.getChildAt(i);
                if (childView instanceof IRichCellView) {
                    IRichCellView cellView = (IRichCellView) childView;
                    RichType richType = cellView.getRichType();
                    if (richType != null && richType.getGroup() == RichTypeConstants.GROUP_RESOURCE) {
                        cellView.setSelectMode(cellView == targetView);
                    }
                }
            }
        }
    }

    public static void removeAllResourceTypeFocus(WRichEditor scrollView) {
        if (scrollView == null) {
            return;
        }
        if (scrollView.getContainerView() == null) {
            return;
        }
        ViewGroup viewGroup = scrollView.getContainerView();
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = viewGroup.getChildAt(i);
            if (childView instanceof IRichCellView) {
                IRichCellView cellView = (IRichCellView) childView;
                RichType richType = cellView.getRichType();
                if (richType != null && richType.getGroup() == RichTypeConstants.GROUP_RESOURCE) {
                    cellView.setSelectMode(false);
                }
            }
        }
    }

    public static boolean removeAllLineFormatType(Set<RichType> richTypes) {
        boolean changed = false;
        if (richTypes == null) {
            return changed;
        }
        ArrayList<RichType> toBeRemovedList = new ArrayList<>(richTypes.size());
        for (RichType item : richTypes) {
            if (item.getGroup() == RichTypeConstants.GROUP_LINE_FORMAT) {
                toBeRemovedList.add(item);
            }
        }
        for (RichType richType : toBeRemovedList) {
            changed = true;
            richTypes.remove(richType);
        }
        return changed;
    }

}