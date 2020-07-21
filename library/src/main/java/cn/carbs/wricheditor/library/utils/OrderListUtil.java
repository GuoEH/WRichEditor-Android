package cn.carbs.wricheditor.library.utils;

import android.view.View;
import android.view.ViewGroup;

import cn.carbs.wricheditor.library.WEditTextWrapperView;
import cn.carbs.wricheditor.library.types.RichType;

public class OrderListUtil {

    public static void updateOrderListNumbersAfterViewsChanged(ViewGroup cellViewContainer) {
        if (cellViewContainer == null) {
            return;
        }
        int childCount = cellViewContainer.getChildCount();

        int orderCurr = 0;
        for (int i = 0; i < childCount; i++) {
            View view = cellViewContainer.getChildAt(i);
            if (view instanceof WEditTextWrapperView) {
                WEditTextWrapperView wrapperView = (WEditTextWrapperView) view;
                RichType richType = wrapperView.getRichType();
                if (richType == RichType.LIST_ORDERED) {
                    orderCurr = orderCurr + 1;
                    wrapperView.setOrderedListText(getOrderedListText(orderCurr));
                } else {
                    orderCurr = 0;
                }
            } else {
                orderCurr = 0;
            }
        }
    }

    private static String getOrderedListText(int order) {
        return order + ".";
    }

}
