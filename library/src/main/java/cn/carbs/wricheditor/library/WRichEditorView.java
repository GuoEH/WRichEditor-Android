package cn.carbs.wricheditor.library;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import cn.carbs.wricheditor.library.callbacks.OnDataTransportListener;
import cn.carbs.wricheditor.library.interfaces.IRichCellData;
import cn.carbs.wricheditor.library.interfaces.IRichCellView;
import cn.carbs.wricheditor.library.types.RichType;

/**
 * 主视图，继承自ScrollView，富文本通过向其中不断添加子View实现
 */
public class WRichEditorView extends ScrollView {

    // TODO 存储每一个item中的内容
    public ArrayList<IRichCellData> mRichCellDataList = new ArrayList<>();

    public Set<RichType> mRichTypes = new HashSet<>();

    private LinearLayout mLinearLayout;

    public WRichEditorView(Context context) {
        super(context);
        init(context);
    }

    public WRichEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public WRichEditorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.wricheditor_layout_main_view, this);
        mLinearLayout = findViewById(R.id.wricheditor_main_view_container);
    }

    public void addRichCell(IRichCellView richCell, LinearLayout.LayoutParams layoutParams) {
        if (mLinearLayout == null || richCell.getView() == null) {
            return;
        }
        LinearLayout.LayoutParams lp = null;
        if (layoutParams != null) {
            lp = layoutParams;
        } else {
            lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        }
        mLinearLayout.addView(richCell.getView(), lp);
        // 子View中添加此ScrollView的引用，方便操作其它数据
        richCell.setWRichEditorView(this);
    }

    public void importData(OnDataTransportListener listener) {

    }

    public void exportData(OnDataTransportListener listener) {

    }

    public void setRichTypes(Set<RichType> currRichTypes) {
        mRichTypes.clear();
        mRichTypes.addAll(currRichTypes);
    }

    public Set<RichType> getRichTypes() {
        return mRichTypes;
    }

}
