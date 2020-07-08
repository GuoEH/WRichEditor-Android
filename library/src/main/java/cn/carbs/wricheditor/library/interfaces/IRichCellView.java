package cn.carbs.wricheditor.library.interfaces;

import android.view.View;

import cn.carbs.wricheditor.library.WRichEditorScrollView;
import cn.carbs.wricheditor.library.callbacks.OnEditorFocusChangedListener;
import cn.carbs.wricheditor.library.types.RichType;

/**
 * 每一个添加至ScrollView的子View
 */
public interface IRichCellView {

    View getView();

    void setWRichEditorView(WRichEditorScrollView wRichEditorScrollView);

    void setCellData(IRichCellData cellData);

    void setEditorFocusChangedListener(OnEditorFocusChangedListener listener);

    IRichCellData getCellData();

    RichType getRichType();

    void setSelectMode(boolean selectMode);

    boolean getSelectMode();

}
