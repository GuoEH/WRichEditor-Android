package cn.carbs.wricheditor.library.interfaces;

public abstract class BaseCellData implements IRichCellData {

    public String type;

    public ICellDataJsonAdapter adapter;

    public IRichCellView cellView;

    @Override
    public void setCellDataJsonAdapter(ICellDataJsonAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public ICellDataJsonAdapter getCellDataJsonAdapter() {
        return adapter;
    }

    public void setIRichCellView(IRichCellView cellView) {
        this.cellView = cellView;
    }

    public IRichCellView getIRichCellView() {
        return cellView;
    }

}
