package cn.carbs.wricheditor.library;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import cn.carbs.wricheditor.library.callbacks.OnDataTransportListener;
import cn.carbs.wricheditor.library.callbacks.OnEditorFocusChangedListener;
import cn.carbs.wricheditor.library.interfaces.IRichCellData;
import cn.carbs.wricheditor.library.interfaces.IRichCellView;
import cn.carbs.wricheditor.library.types.RichType;

/**
 * 主视图，继承自ScrollView，富文本通过向其中不断添加子View实现
 */
public class WRichEditorView extends ScrollView implements OnEditorFocusChangedListener {

    // TODO 存储每一个item中的内容
    // TODO 是否去掉此数据结构，而采用单一的列表CellView对应的数据结构
    public ArrayList<IRichCellData> mRichCellDataList = new ArrayList<>();

    // TODO
    public ArrayList<IRichCellView> mRichCellViewList = new ArrayList<>();

    public Set<RichType> mRichTypes = new HashSet<>();

    private LinearLayout mLinearLayout;

    private OnDataTransportListener mOnDataTransportListener;

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
        Log.d("ppp", "scroll addRichCell");
        if (mLinearLayout == null || richCell.getView() == null) {
            return;
        }
        LinearLayout.LayoutParams lp = null;
        if (layoutParams != null) {
            lp = layoutParams;
        } else {
            lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        }
        // 子View中添加此ScrollView的引用，方便操作其它数据
        richCell.setWRichEditorView(this);
        richCell.setEditorFocusChangedListener(this);
        mLinearLayout.addView(richCell.getView(), lp);
        mRichCellViewList.add(richCell);
    }

    public void importData(OnDataTransportListener listener) {
        // TODO
        mOnDataTransportListener = listener;

    }

    public void exportData(OnDataTransportListener listener) {
        // TODO
        mOnDataTransportListener = listener;

    }

    public void setRichTypes(Set<RichType> currRichTypes) {
        mRichTypes.clear();
        mRichTypes.addAll(currRichTypes);
    }

    public Set<RichType> getRichTypes() {
        return mRichTypes;
    }

    // TODO 考虑 richType 此api的设计，是否将toggle放到此类中
    public void updateTextByRichTypeChanged() {

        // 1. 循环内部子View，找到焦点所在的EditView
        int cellViewSize = mRichCellViewList.size();
        for (int i = 0; i < cellViewSize; i++) {
            IRichCellView cellView = mRichCellViewList.get(i);
            if (cellView != null && cellView.getView() != null) {
                if (cellView.getRichType() == RichType.NONE) {
                    // 具有 EditText 的 cell
                    EditText editText = ((EditText) cellView.getView());
                    Log.d("uuu", "updateTextByRichTypeChanged() i : " + i + "editText.hasFocus() ？ " + editText.hasFocus());
                } else {

                }
            }
        }



        // 2. 如果此EditView的selection状态为选中一段文字，则针对此段文字进行富文本格式更改

        // 3. 如果此EditView的selection状态为没有选中，则往后的文字都使用此种格式

    }

    // TODO 考虑此API的设计
    public boolean toggleCertainRichType(RichType richType) {
        if (mRichTypes.contains(richType)) {
            mRichTypes.remove(richType);
            return false;
        } else {
            mRichTypes.add(richType);
            return true;
        }
    }

    @Override
    public void onEditorFocusChanged(IRichCellView iRichCellView, boolean focused) {
        Log.d("eee", "onEditorFocusChanged focused : " + focused);
    }
}
