package cn.carbs.wricheditor.library.interfaces;

public interface ICellDataJsonAdapter {

    String toJson(BaseCellData cellData);

    IRichCellData fromJson(String str);

}
