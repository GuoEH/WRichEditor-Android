package cn.carbs.wricheditor.library.utils;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import cn.carbs.wricheditor.library.WRichEditorScrollView;
import cn.carbs.wricheditor.library.WRichEditorWrapperView;
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
     * 判断一种类型是否为富文本类型（粗体、斜体）
     * @param richType
     * @return
     */
    public static boolean isSpanTypeRichText(RichType richType) {
        if (richType == RichType.BOLD
                || richType == RichType.ITALIC) {
            return true;
        }
        return false;
    }

    /**
     * 判断一种类型是否为多媒体资源类型（图片、视频、网盘）
     * @param richType
     * @return
     */
    public static boolean isSpanTypeRichResource(RichType richType) {
        if (richType == RichType.IMAGE
                || richType == RichType.VIDEO
                || richType == RichType.AUDIO
                || richType == RichType.NETDISK) {
            return true;
        }
        return false;
    }

    /**
     * 判断集合中是否有对应的富文本类型，如果没有则添加，如果有则删除
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

    public static void correctLineFormatGroupType(Set<RichType> currRichTypes, WRichEditorWrapperView wrapperView) {
        if (wrapperView != null && currRichTypes != null) {
            RichType richType = wrapperView.getRichType();
            Log.d("xxx", "correctLineFormatGroupType 0 richType : " + richType.name());
//            if (richType != RichType.NONE) {
                ArrayList<RichType> toBeRemovedList = new ArrayList<>();
                for (RichType richType1 : currRichTypes) {
                    if (richType1.getGroup() == RichTypeConstants.GROUP_LINE_FORMAT) {
                        toBeRemovedList.add(richType1);

                        Log.d("xxx", "correctLineFormatGroupType 1 toBeRemovedList.add : " + richType1.name());
                    }
                }
                for (RichType toBeRemoved : toBeRemovedList) {
                    Log.d("xxx", "correctLineFormatGroupType 1 currRichTypes.remove : " + toBeRemoved.name());
                    currRichTypes.remove(toBeRemoved);
                }
                Log.d("xxx", "correctLineFormatGroupType 1 currRichTypes.add : " + richType.name());
                currRichTypes.add(richType);
//            }
        }
    }

    public static boolean checkIfRichTypesSetSame(Set<RichType> richTypesA, Set<RichType> richTypesB) {
        int sizeA = richTypesA == null ? 0 : richTypesA.size();
        int sizeB = richTypesB == null ? 0 : richTypesB.size();

        if (sizeA != sizeB) {
            return false;
        }

        // 两者数量相同，则检查A集合中的每个元素 B 集合都有
        for (RichType itemInA : richTypesA) {
            if (richTypesB.contains(itemInA)) {
                continue;
            }
            return false;
        }
        return true;
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
                return new LinkStyleSpan((String)extra, RichEditorConfig.sLinkColor, RichEditorConfig.sLinkUnderline);
            case HEADLINE:
                return new HeadlineSpan(RichEditorConfig.sHeadlineTextSize);
            case QUOTE:
                return null;
        }
        return null;
    }

    public static void selectOnlyOneResourceType(WRichEditorScrollView scrollView, View currentSelectedView) {

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

    public static void selectOnlyOneResourceType(WRichEditorScrollView scrollView, int currentSelectedIndex) {

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

}