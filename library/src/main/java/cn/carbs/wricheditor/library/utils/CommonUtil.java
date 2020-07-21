package cn.carbs.wricheditor.library.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import cn.carbs.wricheditor.library.WRichEditor;
import cn.carbs.wricheditor.library.WEditTextWrapperView;

public class CommonUtil {

    public static void hideSoftKeyboard(Context context, View view) {
        if (view == null) {
            return;
        }
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static void showSoftKeyboard(Context context, View view) {
        if (view == null) {
            return;
        }
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (context instanceof Activity) {
            ((Activity) context).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_FORCED);
    }

    public static boolean isKeyboardOpen(Context context) {
        if (context == null) {
            return false;
        }
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        return imm.isActive();//isOpen若返回true，则表示输入法打开
    }

    public static void removeAllEditorFocus(Context context, WRichEditor wRichEditor) {
        if (wRichEditor != null) {
            int[] index = new int[1];
            WEditTextWrapperView focusedWrapperView = wRichEditor.findCurrentOrRecentFocusedRichEditorWrapperView(index);
            if (focusedWrapperView != null && focusedWrapperView.getWEditText() != null) {
                CommonUtil.hideSoftKeyboard(context, focusedWrapperView.getWEditText());
                focusedWrapperView.getWEditText().clearFocus();
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

    public static int getScreenWidthDP(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        if (wm != null && wm.getDefaultDisplay() != null) {
            wm.getDefaultDisplay().getMetrics(dm);
            return px2dp(context, dm.widthPixels);
        } else {
            return 0;
        }
    }

    // 获取整个手机的屏幕高度
    public static int getFullScreenHeight(Activity activity) {
        if (activity == null
                || activity.getWindow() == null
                || activity.getWindow().getDecorView() == null
                || activity.getWindow().getDecorView().getRootView() == null) {
            return 0;
        }
        return activity.getWindow().getDecorView().getRootView().getHeight();
    }

    public static int getDecorViewHeight(Activity activity) {
        if (activity == null
                || activity.getWindow() == null
                || activity.getWindow().getDecorView() == null) {
            return 0;
        }
        return activity.getWindow().getDecorView().getHeight();
    }

    public static int getContentViewHeight(Activity activity) {
        if (activity == null
                || activity.getWindow() == null
                || activity.getWindow().getDecorView() == null) {
            return 0;
        }
        View decorView = activity.getWindow().getDecorView();
        View contentView = decorView.findViewById(android.R.id.content);
        if (contentView == null) {
            return 0;
        }
        return contentView.getHeight();
    }

    // 获取手机输入法上边缘与手机屏幕顶端的距离
    public static int getScreenHeightExcludeKeyboard(Activity activity) {
        if (activity == null
                || activity.getWindow() == null
                || activity.getWindow().getDecorView() == null) {
            return 0;
        }
        Rect r = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
        return r.bottom;
    }

    // 获取view所在屏幕的Y位置
    public static int getViewYLocationInScreen(View view) {
        if (view == null) {
            return 0;
        }
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return location[1];
    }

    public static void hideStatusBar(Activity activity) {
        if (activity == null) {
            return;
        }
        activity.getWindow().setFlags(
                View.SYSTEM_UI_FLAG_VISIBLE,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) {
            return context.getResources().getDisplayMetrics().widthPixels;
        }
        Point point = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            wm.getDefaultDisplay().getRealSize(point);
        } else {
            wm.getDefaultDisplay().getSize(point);
        }
        return point.x;
    }

    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) {
            return context.getResources().getDisplayMetrics().heightPixels;
        }
        Point point = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            wm.getDefaultDisplay().getRealSize(point);
        } else {
            wm.getDefaultDisplay().getSize(point);
        }
        return point.y;
    }

    public static boolean isNavigationBarShow(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Display display = activity.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            Point realSize = new Point();
            display.getSize(size);
            display.getRealSize(realSize);
            return realSize.y != size.y;
        } else {
            boolean menu = ViewConfiguration.get(activity).hasPermanentMenuKey();
            boolean back = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
            if (menu || back) {
                return false;
            } else {
                return true;
            }
        }
    }

    /**
     * 方法可以返回navigation的高度，但是判断是否显示并不准确
     * @param activity
     * @return
     */
    public static int getNavigationBarHeight(Activity activity) {
        if (!isNavigationBarShow(activity)) {
            return 0;
        }
        Resources resources = activity.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height",
                "dimen", "android");
        //获取NavigationBar的高度
        int height = resources.getDimensionPixelSize(resourceId);
        return height;
    }

    public static int getScreenHeight(Activity activity) {
        return activity.getWindowManager().getDefaultDisplay().getHeight() + getNavigationBarHeight(activity);
    }

    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dp(Context context, float pxValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}