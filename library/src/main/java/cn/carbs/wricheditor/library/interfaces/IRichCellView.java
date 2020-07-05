package cn.carbs.wricheditor.library.interfaces;

import android.view.View;

import cn.carbs.wricheditor.library.WRichEditorView;

/**
 * 每一个添加至ScrollView的子View
 */
public interface IRichCellView {

    View getView();

    void setWRichEditorView(WRichEditorView wRichEditorView);

    void setCellData(IRichCellData cellData);

    IRichCellData getCellData();

}
