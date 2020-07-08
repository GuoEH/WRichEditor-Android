package cn.carbs.wricheditor.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import cn.carbs.wricheditor.library.callbacks.OnDataTransportListener;
import cn.carbs.wricheditor.library.callbacks.OnEditorFocusChangedListener;
import cn.carbs.wricheditor.library.callbacks.OnRichTypeChangedListener;
import cn.carbs.wricheditor.library.configures.RichEditorConfig;
import cn.carbs.wricheditor.library.interfaces.IRichCellView;
import cn.carbs.wricheditor.library.types.RichType;
import cn.carbs.wricheditor.library.utils.StrategyUtil;
import cn.carbs.wricheditor.library.utils.ViewUtil;

/**
 * 主视图，继承自ScrollView，富文本通过向其中不断添加子View实现
 */
public class WRichEditorScrollView extends ScrollView implements OnEditorFocusChangedListener {

    // TODO
    public ArrayList<IRichCellView> mRichCellViewList = new ArrayList<>();

    public Set<RichType> mRichTypes = new HashSet<>();

    private LinearLayout mLinearLayout;

    private OnDataTransportListener mOnDataTransportListener;

    private OnRichTypeChangedListener mOnRichTypeChangedListener;

    public WRichEditorScrollView(Context context) {
        super(context);
        init(context);
    }

    public WRichEditorScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        initAttrs(attrs);
    }

    public WRichEditorScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
        initAttrs(attrs);
    }

    private void init(Context context) {
        inflate(context, R.layout.wricheditor_layout_main_view, this);
        mLinearLayout = findViewById(R.id.wricheditor_main_view_container);
        RichEditorConfig.sHeadlineTextSize = ViewUtil.dp2px(getContext(), 24);
    }

    private void initAttrs(AttributeSet attrs) {
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.WRichEditorScrollView);
        RichEditorConfig.sLinkColor = array.getColor(R.styleable.WRichEditorScrollView_linkColor, RichEditorConfig.sLinkColor);
        RichEditorConfig.sLinkUnderline = array.getBoolean(R.styleable.WRichEditorScrollView_linkUnderline, RichEditorConfig.sLinkUnderline);
        RichEditorConfig.sHeadlineTextSize = array.getDimensionPixelSize(R.styleable.WRichEditorScrollView_headlineTextSize, RichEditorConfig.sHeadlineTextSize);
        array.recycle();
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
        richCell.setWRichEditorScrollView(this);
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
    // object 中存储 link 等信息
    public void updateTextByRichTypeChanged(RichType richType, boolean open, Object object) {
        StrategyUtil.sStrongSet = true;
        // 1. 找到焦点所在的EditView
        WRichEditorWrapperView focusedWRichEditorWrapperView = findCurrentFocusedRichEditorWrapperView();
        if (focusedWRichEditorWrapperView == null) {
            return;
        }
        // 2. 交给某个单元Cell去更新
        focusedWRichEditorWrapperView.updateTextByRichTypeChanged(richType, open, object);
    }

    public WRichEditorWrapperView findCurrentFocusedRichEditorWrapperView() {
        WRichEditorWrapperView focusedWRichEditorWrapperView = null;
        int cellViewSize = mRichCellViewList.size();
        for (int i = 0; i < cellViewSize; i++) {
            IRichCellView cellView = mRichCellViewList.get(i);
            if (cellView != null && cellView.getView() != null) {
                if (cellView.getRichType() == RichType.NONE) {
                    // 具有 EditText 的 cell
                    WRichEditorWrapperView wRichEditorWrapperView = ((WRichEditorWrapperView) cellView.getView());
//                    Log.d("uuu", "updateTextByRichTypeChanged() i : " + i + "wRichEditor.hasFocus() ？ " + wRichEditor.hasFocus());
                    if (wRichEditorWrapperView != null && wRichEditorWrapperView.getWRichEditor() != null) {
                        if (wRichEditorWrapperView.getWRichEditor().hasFocus()) {
                            focusedWRichEditorWrapperView = wRichEditorWrapperView;
                            break;
                        }
                    }
                } else {

                }
            }
        }
        return focusedWRichEditorWrapperView;
    }

    public WRichEditorWrapperView findCurrentOrRecentFocusedRichEditorWrapperView() {
        WRichEditorWrapperView retWRichEditorWrapperView = findCurrentFocusedRichEditorWrapperView();
        Log.d("xxx", "findCurrentFocusedRichEditorWrapperView() null ? : " + (retWRichEditorWrapperView == null));
        if (retWRichEditorWrapperView != null) {
            return retWRichEditorWrapperView;
        }
        if (mLastFocusedRichCellView instanceof WRichEditorWrapperView) {
            return (WRichEditorWrapperView) mLastFocusedRichCellView;
        }
        return null;
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

    IRichCellView mLastFocusedRichCellView;

    @Override
    public void onEditorFocusChanged(IRichCellView iRichCellView, boolean focused) {
        Log.d("eee", "onEditorFocusChanged focused : " + focused);
        if (focused) {
            mLastFocusedRichCellView = iRichCellView;
        }
    }


    public void setOnRichTypeChangedListener(OnRichTypeChangedListener listener) {
        mOnRichTypeChangedListener = listener;
    }

    public OnRichTypeChangedListener getOnRichTypeChangedListener() {
        return mOnRichTypeChangedListener;
    }
}
