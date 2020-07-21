package cn.carbs.wricheditor.library.providers;

import cn.carbs.wricheditor.library.interfaces.IRichCellView;
import cn.carbs.wricheditor.library.types.RichType;

public interface CustomViewProvider {

    IRichCellView getCellViewByRichType(RichType richType);

}