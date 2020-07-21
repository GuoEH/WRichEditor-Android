package cn.carbs.wricheditor.library.interfaces;

import android.view.View;

import cn.carbs.wricheditor.library.WRichEditor;
import cn.carbs.wricheditor.library.callbacks.OnEditorFocusChangedListener;
import cn.carbs.wricheditor.library.types.RichType;

/**
 * 每一个添加至ScrollView的子View
 */
public interface IRichCellView <T extends IRichCellData> {

    View getView();

    void setWRichEditorScrollView(WRichEditor wRichEditor);

    void  setCellData(T cellData);

    // <div richType="NONE">xxx</div> exclude div tag --> xxx
    void setHtmlData(RichType richType, String htmlContent);

    void setEditorFocusChangedListener(OnEditorFocusChangedListener listener);

    T getCellData();

    RichType getRichType();

    void setSelectMode(boolean selectMode);

    boolean getSelectMode();

}
