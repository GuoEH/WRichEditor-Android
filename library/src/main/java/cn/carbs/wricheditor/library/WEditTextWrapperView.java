package cn.carbs.wricheditor.library;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import cn.carbs.wricheditor.library.callbacks.OnEditorFocusChangedListener;
import cn.carbs.wricheditor.library.callbacks.OnRichTypeChangedListener;
import cn.carbs.wricheditor.library.configures.RichEditorConfig;
import cn.carbs.wricheditor.library.interfaces.IRichCellView;
import cn.carbs.wricheditor.library.interfaces.IRichSpan;
import cn.carbs.wricheditor.library.models.ContentStyleWrapper;
import cn.carbs.wricheditor.library.models.cell.RichCellData;
import cn.carbs.wricheditor.library.types.RichType;
import cn.carbs.wricheditor.library.utils.CursorUtil;
import cn.carbs.wricheditor.library.utils.SpanUtil;
import cn.carbs.wricheditor.library.utils.TypeUtil;

// 注意，此方法是不会合并的
// getEditableText().getSpans(0, getEditableText().toString().length(), richSpan.getClass());
// 因此最后导出的时候，是否需要合并？如何转换数据是个问题
@SuppressLint("AppCompatCustomView")
public class WEditTextWrapperView extends RelativeLayout implements IRichCellView<RichCellData> {

    private WRichEditor mWRichEditor;

    private RichCellData mCellData;

    private ImageView mIVForQuoteOrUnorderList;

    private TextView mTVForOrderList;

    private WEditText mWEditText;

    private RichType mRichType = RichType.NONE;

    private boolean mNeedRequestFocusWhenAdded;


    public WEditTextWrapperView(Context context) {
        super(context);
        init(context);
    }

    public WEditTextWrapperView(Context context, RichType richType, boolean needRequestFocusWhenAdded) {
        super(context);
        mRichType = richType;
        mNeedRequestFocusWhenAdded = needRequestFocusWhenAdded;
        init(context);
    }

    public WEditTextWrapperView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public WEditTextWrapperView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.wricheditor_layout_rich_editor_wrapper_view, this);
        mIVForQuoteOrUnorderList = findViewById(R.id.iv_in_wrapper);
        mTVForOrderList = findViewById(R.id.tv_in_wrapper);
        mWEditText = findViewById(R.id.w_rich_editor);
        mWEditText.setWRichEditorWrapperView(this);
        mWEditText.setContentDescription(CursorUtil.getNewContentDescriptionForWRichEditor());
        if (mWRichEditor != null) {
            mWEditText.setWRichEditorScrollView(mWRichEditor);
        }
        Log.d("wangaa", "RichEditorConfig.sEditorColor : " + Integer.toHexString(RichEditorConfig.sEditorColor));
        mWEditText.setTextColor(RichEditorConfig.sEditorColor);
        if (mRichType != RichType.NONE) {
            // 如果不是无状态的，即，可能为 QUOTE ORDERED_LIST UNORDERED_LIST
            if (mRichType == RichType.QUOTE) {
                toggleQuoteMode(true, true);
            } else if (mRichType == RichType.LIST_ORDERED) {
                toggleOrderListMode(true, true);
            } else if (mRichType == RichType.LIST_UNORDERED) {
                toggleUnOrderListMode(true, true);
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mNeedRequestFocusWhenAdded && mWEditText != null) {
            mWEditText.requestFocus();
        }
    }

    @Override
    public void setWRichEditorScrollView(WRichEditor wRichEditor) {
        mWRichEditor = wRichEditor;
        Log.d("ppp", "editor setWRichEditorView mWRichEditorView == null ? " + (mWRichEditor == null));
        if (mWEditText != null) {
            mWEditText.setWRichEditorScrollView(mWRichEditor);
        }
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void setCellData(RichCellData cellData) {
        mCellData = cellData;
        if (mCellData != null) {
            mCellData.setIRichCellView(this);
        }
        if (mCellData != null && mWEditText != null) {
            setTextForEditor(mCellData.wrappersList);
        }
    }

    private void setTextForEditor(LinkedList<ContentStyleWrapper> wrappers) {
        if (wrappers == null) {
            return;
        }

        if (mWEditText == null) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (ContentStyleWrapper wrapper : wrappers) {
            sb.append(wrapper.contentBuilder.toString());
        }
        mWEditText.setText(sb.toString());

        ArrayList<IRichSpan> spans = new ArrayList<>(8);
        int start = 0;
        int end = 0;
        Editable editable = mWEditText.getEditableText();
        for (ContentStyleWrapper wrapper : wrappers) {
            end = end + wrapper.contentBuilder.length();
            SpanUtil.getSpanByMask(spans, wrapper.mask, wrapper.extra);
            SpanUtil.setSpan(spans, editable, start, end);
            start = end;
        }
    }

    @Override
    public void setHtmlData(RichType richType, String htmlContent) {

    }

    @Override
    public void setEditorFocusChangedListener(OnEditorFocusChangedListener listener) {
        if (mWEditText != null) {
            mWEditText.setEditorFocusChangedListener(listener);
        }
    }

    @Override
    public RichCellData getCellData() {
        if (mCellData == null) {
            mCellData = new RichCellData();
        }
        if (mWEditText != null) {
            mCellData.editable = mWEditText.getEditableText();
        }
        return mCellData;
    }

    @Override
    public RichType getRichType() {
        return mRichType;
    }

    @Override
    public void setSelectMode(boolean selectMode) {
        if (selectMode) {
            if (mWEditText != null) {
                mWEditText.requestFocus();
            }
        } else {
            if (mWEditText != null) {
                Log.d("clearfocus", "clearFocus() 5");
                mWEditText.clearFocus();
            }
        }
    }

    @Override
    public boolean getSelectMode() {
        if (mWEditText != null) {
            return mWEditText.getSelectMode();
        }
        return false;
    }

    // SpannableStringBuilder
    public void addExtraEditable(Editable extraEditable) {
        if (mWEditText != null) {
            mWEditText.addExtraEditable(extraEditable);
        }
    }

    public void requestFocusAndPutCursorToTail() {
        if (mWEditText != null) {
            mWEditText.requestFocusAndPutCursorToTail();
        }
    }

    public WEditText getWRichEditor() {
        return mWEditText;
    }


    public void toggleQuoteMode(boolean open, boolean mayRepeat) {
        if (open) {
            if (mRichType == RichType.QUOTE && !mayRepeat) {
                return;
            }
            mRichType = RichType.QUOTE;
            updateQuoteModeUI(true);
            updateUnOrderListModeUI(false);
            updateOrderListModeUI(false);
            onResourceRichTypeChanged(RichType.QUOTE);
        } else {
            if (mRichType != RichType.QUOTE && !mayRepeat) {
                return;
            }
            updateQuoteModeUI(false);
            updateUnOrderListModeUI(false);
            updateOrderListModeUI(false);
            mRichType = RichType.NONE;
        }
    }

    public void toggleOrderListMode(boolean open, boolean mayRepeat) {
        if (open) {
            if (mRichType == RichType.LIST_ORDERED && !mayRepeat) {
                return;
            }
            mRichType = RichType.LIST_ORDERED;
            updateOrderListModeUI(true);
            updateQuoteModeUI(false);
            updateUnOrderListModeUI(false);
            onResourceRichTypeChanged(RichType.LIST_ORDERED);
        } else {
            if (mRichType != RichType.LIST_ORDERED && !mayRepeat) {
                return;
            }
            updateOrderListModeUI(false);
            updateQuoteModeUI(false);
            updateUnOrderListModeUI(false);
            mRichType = RichType.NONE;
        }
    }

    public void toggleUnOrderListMode(boolean open, boolean myRepeat) {
        if (open) {
            if (mRichType == RichType.LIST_UNORDERED && !myRepeat) {
                return;
            }
            mRichType = RichType.LIST_UNORDERED;
            updateUnOrderListModeUI(true);
            updateOrderListModeUI(false);
            updateQuoteModeUI(false);
            onResourceRichTypeChanged(RichType.LIST_UNORDERED);
        } else {
            if (mRichType != RichType.LIST_UNORDERED && !myRepeat) {
                return;
            }
            updateUnOrderListModeUI(false);
            updateOrderListModeUI(false);
            updateQuoteModeUI(false);
            mRichType = RichType.NONE;
        }
    }

    // 由util类去更改
    public void setOrderedListText(String text) {
        if (mTVForOrderList != null) {
            mTVForOrderList.setText(text);
        }
    }

    private void updateQuoteModeUI(boolean open) {

        if (open) {
            if (mIVForQuoteOrUnorderList != null) {
                mIVForQuoteOrUnorderList.setImageResource(R.drawable.ic_quote_icon);
                mIVForQuoteOrUnorderList.setVisibility(View.VISIBLE);
            }
            // TODO 应该添加一个字体的附加，比如颜色变浅，字体变小

        } else {
            if (mRichType == RichType.LIST_UNORDERED) {
                return;
            }
            if (mIVForQuoteOrUnorderList != null) {
                mIVForQuoteOrUnorderList.setVisibility(View.GONE);
            }
            // TODO 应该去掉字体的附加
        }
    }

    private void updateUnOrderListModeUI(boolean open) {
        if (open) {
            if (mIVForQuoteOrUnorderList != null) {
                mIVForQuoteOrUnorderList.setImageResource(R.drawable.ic_list_unorder_icon);
                mIVForQuoteOrUnorderList.setVisibility(View.VISIBLE);
            }
        } else {
            if (mRichType == RichType.QUOTE) {
                return;
            }
            if (mIVForQuoteOrUnorderList != null) {
                mIVForQuoteOrUnorderList.setVisibility(View.GONE);
            }
        }
    }

    private void updateOrderListModeUI(boolean open) {
        if (open) {
            if (mTVForOrderList != null) {
                // todo 如果是有序列表，设置数字
                mTVForOrderList.setText("1.");
                mTVForOrderList.setVisibility(View.VISIBLE);
            }
        } else {
            if (mTVForOrderList != null) {
                mTVForOrderList.setText("");
                mTVForOrderList.setVisibility(View.GONE);
            }
        }
    }

    public void onResourceRichTypeChanged(RichType selectedResourceType) {
        if (mWRichEditor == null) {
            return;
        }

        Set<RichType> prevRichTypes = mWRichEditor.getRichTypes();
        OnRichTypeChangedListener typeChangedListener = mWRichEditor.getOnRichTypeChangedListener();

        Set<RichType> currRichTypes = new HashSet<>(4);

        TypeUtil.removeAllLineFormatType(prevRichTypes);

        currRichTypes.addAll(prevRichTypes);
        currRichTypes.add(selectedResourceType);

        if (typeChangedListener != null) {
            typeChangedListener.onRichTypeChanged(prevRichTypes, currRichTypes);
        }
    }


}
