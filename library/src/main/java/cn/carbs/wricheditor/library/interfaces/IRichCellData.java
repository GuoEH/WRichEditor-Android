package cn.carbs.wricheditor.library.interfaces;

import org.json.JSONObject;

import cn.carbs.wricheditor.library.types.RichType;

/**
 * 所有存储数据信息的数据单元的接口
 */
public interface IRichCellData {

    RichType getRichType();

    String toHtml();

    String toJson();

    IRichCellData fromJson(JSONObject jsonObject);

}
