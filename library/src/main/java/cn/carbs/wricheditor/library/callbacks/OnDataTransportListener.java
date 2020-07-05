package cn.carbs.wricheditor.library.callbacks;

public interface OnDataTransportListener {

    // TODO
    // adapter 模式，可自定义
    void onImportDataSuccess();

    void onImportDataFail(String message);

    // TODO
    // adapter 模式，可自定义
    void onExportDataSuccess();

    void onExportDataFail(String message);

}
