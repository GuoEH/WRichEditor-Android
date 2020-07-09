package cn.carbs.wricheditor.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import cn.carbs.wricheditor.library.callbacks.OnDataTransportListener;
import cn.carbs.wricheditor.library.callbacks.OnEditorFocusChangedListener;
import cn.carbs.wricheditor.library.callbacks.OnRichTypeChangedListener;
import cn.carbs.wricheditor.library.configures.RichEditorConfig;
import cn.carbs.wricheditor.library.constants.CharConstant;
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

    // TODO 和下面的方法一同待优化
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

    // TODO
    public void addRichCell(IRichCellView richCell, LinearLayout.LayoutParams layoutParams, int index) {
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
        mLinearLayout.addView(richCell.getView(), index, lp);
        mRichCellViewList.add(index, richCell);
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
        int[] focusedRichEditorWrapperViewIndex = new int[1];
        WRichEditorWrapperView focusedWRichEditorWrapperView = findCurrentFocusedRichEditorWrapperView(focusedRichEditorWrapperViewIndex);
        if (focusedWRichEditorWrapperView == null) {
            return;
        }
        // 2. 交给某个单元Cell去更新
        if (richType == RichType.QUOTE) {
            // 如果是引用类型，则：
            // 1. 判断此EditText中是否
            if (open) {
                // 如果是开启 QUOTE 动作
                WRichEditor wRichEditor = focusedWRichEditorWrapperView.getWRichEditor();
                if (wRichEditor == null) {
                    return;
                }
                Editable editable = wRichEditor.getEditableText();
                String editableStr = editable.toString();
                if (!editableStr.contains(CharConstant.LINE_BREAK_STRING)) {
                    // 1. 判断当前RichEditor中有几个换行符，如果有0个，则不"分裂"，直接将这个Editor的左侧drawable做变换
                    focusedWRichEditorWrapperView.toggleQuoteMode(open, false);
                } else {
                    // 2. 如果当前RichEditor中有一个或者多个换行符，则截取从换行符后的text，添加到新生成的Editor中
                    int lastIndexOfLineBreaker = editableStr.lastIndexOf(CharConstant.LINE_BREAK_CHAR);
                    if (lastIndexOfLineBreaker == editableStr.length() - 1) {
                        // 当前最后一个字符为换行符，分裂时，需要把这个换行符去掉
                        Log.d("qwe", "lastIndexOfLineBreaker == editableStr.length() - 1");
                        // 最后一个，不带入SpanString
                        // 在后面新增一个 WRichEditorWrapperView，并将焦点至于其中
                        // 获取应该插入CellView的位置
                        Log.d("qwe", "focusedRichEditorWrapperViewIndex[0] : " + focusedRichEditorWrapperViewIndex[0]
                                + " mLinearLayout.getChildCount() : " + mLinearLayout.getChildCount());
                        // TODO    dddddddd
//                        wRichEditor.getEditableText().delete(editableStr.length() - 1, 1);
                        //

                        wRichEditor.subSpannableStringInclusiveExclusive(0, editableStr.length() - 1);
//                        wRichEditor.setText(editableStr.substring(0, editableStr.length() - 1));

                        if (focusedRichEditorWrapperViewIndex[0] == mLinearLayout.getChildCount() - 1) {
                            Log.d("qwe", "cell last one");
                            // 在队尾append
                            // TODO 这里要注意，队尾append后，还要再append一个normal的

                            // todo
                            // editableStr.substring(0, editableStr.length() - 1);

                            insertAWRichEditorWrapperWithRichType(-1, RichType.QUOTE, true);
                            // 在最后加一个无格式的
                            insertAWRichEditorWrapperWithRichType(-1, RichType.NONE, false);
                        } else {
                            // todo 待验证是否有越界问题
                            insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 1, RichType.QUOTE, true);
                        }
                    } else {
                        // 换行符后面还有SpanString
                        // 将最后这部分SpanString带入新插入的Editor中

                    }

                }
            } else {

            }
            focusedWRichEditorWrapperView.updateTextByRichTypeChanged(richType, open, object);
        } else {
            focusedWRichEditorWrapperView.updateTextByRichTypeChanged(richType, open, object);
        }

    }

    public WRichEditorWrapperView findCurrentFocusedRichEditorWrapperView(int[] indexInCellViewList) {
        indexInCellViewList[0] = -1;
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
                            indexInCellViewList[0] = i;
                            break;
                        }
                    }
                } else {

                }
            }
        }
        return focusedWRichEditorWrapperView;
    }

    public WRichEditorWrapperView findCurrentOrRecentFocusedRichEditorWrapperView(int[] focusedIndex) {
        int[] focusedRichEditorWrapperView = new int[1];
        focusedRichEditorWrapperView[0] = -1;
        WRichEditorWrapperView retWRichEditorWrapperView = findCurrentFocusedRichEditorWrapperView(focusedRichEditorWrapperView);
        Log.d("xxx", "findCurrentFocusedRichEditorWrapperView() null ? : " + (retWRichEditorWrapperView == null));
        if (retWRichEditorWrapperView != null) {
            focusedIndex[0] = focusedRichEditorWrapperView[0];
            return retWRichEditorWrapperView;
        }
        // TODO focusedIndex 没有返回值
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

    public void insertAWRichEditorWrapperWithRichType(int index, RichType richType, boolean needRequestFocusWhenAdded) {
        WRichEditorWrapperView editTextWrapperView = new WRichEditorWrapperView(getContext(), richType, needRequestFocusWhenAdded);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        editTextWrapperView.getWRichEditor().setHint("CELL new ");
        editTextWrapperView.setBackgroundColor(0x10222222);
        if (index == -1) {
            // 插入到队尾
            addRichCell(editTextWrapperView, lp);
        } else {
            // 插入到中间某个位置
            addRichCell(editTextWrapperView, lp, index);
        }
    }
}
