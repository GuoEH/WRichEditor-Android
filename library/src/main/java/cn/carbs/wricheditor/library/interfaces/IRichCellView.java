package cn.carbs.wricheditor.library.interfaces;

import android.view.View;

/**
 * 每一个添加至ScrollView的子View
 */
public interface IRichCellView {

    View getView();

    void setCellData(IRichCellData cellData);

    IRichCellData getCellData();

}
