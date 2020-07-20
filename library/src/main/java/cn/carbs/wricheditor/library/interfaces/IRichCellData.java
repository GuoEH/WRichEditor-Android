package cn.carbs.wricheditor.library.interfaces;

import cn.carbs.wricheditor.library.types.RichType;

/**
 * 所有存储数据信息的数据单元的接口
 */
public interface IRichCellData {

    Object getData();

    RichType getType();

    String toHtml();

    String toJson();

    IRichCellData fromJson(String str);

    void setCellDataJsonAdapter(ICellDataJsonAdapter adapter);

    ICellDataJsonAdapter getCellDataJsonAdapter();

    IRichCellData inflate(IRichCellData data);

}
