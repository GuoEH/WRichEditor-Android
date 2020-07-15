package cn.carbs.wricheditor.library.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import cn.carbs.wricheditor.library.WRichEditorScrollView;
import cn.carbs.wricheditor.library.WRichEditorWrapperView;

public class CommonUtil {

    public static void hideSoftKeyboard(Context context, View view) {
        if (view == null) {
            return;
        }
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static void removeAllEditorFocus(Context context, WRichEditorScrollView wRichEditorScrollView) {
        if (wRichEditorScrollView != null) {
            int[] index = new int[1];
            WRichEditorWrapperView focusedWrapperView = wRichEditorScrollView.findCurrentOrRecentFocusedRichEditorWrapperView(index);
            if (focusedWrapperView != null && focusedWrapperView.getWRichEditor() != null) {
                CommonUtil.hideSoftKeyboard(context, focusedWrapperView.getWRichEditor());
                focusedWrapperView.getWRichEditor().clearFocus();
            }
        }
    }

    public static boolean stringEqual(String a, String b) {
        if (a == null) {
            if (b == null) {
                return true;
            } else {
                return false;
            }
        } else {
            return a.equals(b);
        }
    }

}