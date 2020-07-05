package cn.carbs.wricheditor.library.interfaces;

import cn.carbs.wricheditor.library.types.SpanType;

/**
 * 所有存储数据信息的数据单元的接口
 */
public interface IRichCellData {

    Object getData();

    SpanType getType();

}
