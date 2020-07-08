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

import cn.carbs.wricheditor.library.callbacks.OnEditorFocusChangedListener;
import cn.carbs.wricheditor.library.interfaces.IRichCellData;
import cn.carbs.wricheditor.library.interfaces.IRichCellView;
import cn.carbs.wricheditor.library.types.RichType;

// 注意，此方法是不会合并的
// getEditableText().getSpans(0, getEditableText().toString().length(), richSpan.getClass());
// 因此最后导出的时候，是否需要合并？如何转换数据是个问题
@SuppressLint("AppCompatCustomView")
public class WRichEditorWrapperView extends RelativeLayout implements IRichCellView {

    private WRichEditorScrollView mWRichEditorScrollView;

    private IRichCellData mRichCellData;

    private ImageView mIVInWrapper;

    private TextView mTVInWrapper;

    private WRichEditor mWRichEditor;

    private RichType mRichType;


    public WRichEditorWrapperView(Context context) {
        super(context);
        init(context);
    }

    public WRichEditorWrapperView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public WRichEditorWrapperView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    private void init(Context context) {
        inflate(context, R.layout.wricheditor_layout_rich_editor_wrapper_view, this);
        mIVInWrapper = findViewById(R.id.iv_in_wrapper);
        mTVInWrapper = findViewById(R.id.tv_in_wrapper);
        mWRichEditor = findViewById(R.id.w_rich_editor);
        mWRichEditor.setWRichEditorWrapperView(this);
        if (mWRichEditorScrollView != null) {
            mWRichEditor.setWRichEditorScrollView(mWRichEditorScrollView);
        }
    }

    @Override
    public void setWRichEditorScrollView(WRichEditorScrollView wRichEditorScrollView) {
        mWRichEditorScrollView = wRichEditorScrollView;
        Log.d("ppp", "editor setWRichEditorView mWRichEditorView == null ? " + (mWRichEditorScrollView == null));
        if (mWRichEditor != null) {
            mWRichEditor.setWRichEditorScrollView(mWRichEditorScrollView);
        }
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void setCellData(IRichCellData cellData) {
        mRichCellData = cellData;
    }

    @Override
    public void setEditorFocusChangedListener(OnEditorFocusChangedListener listener) {
        if (mWRichEditor != null) {
            mWRichEditor.setEditorFocusChangedListener(listener);
        }
    }

    @Override
    public IRichCellData getCellData() {
        return mRichCellData;
    }

    @Override
    public RichType getRichType() {
        return RichType.NONE;
    }

    @Override
    public void setSelectMode(boolean selectMode) {
        // TODO
    }

    @Override
    public boolean getSelectMode() {
        if (mWRichEditor != null) {
            return mWRichEditor.getSelectMode();
        }
        return false;
    }

    // TODO 外部主动更改了字体样式，不涉及数据插入
    public void updateTextByRichTypeChanged(RichType richType, boolean open, Object extra) {
        if (mWRichEditor != null) {
            mWRichEditor.updateTextByRichTypeChanged(richType, open, extra);
        }
    }

    // SpannableStringBuilder
    public void addExtraEditable(Editable extraEditable) {
        if (mWRichEditor != null) {
            mWRichEditor.addExtraEditable(extraEditable);
        }
    }

    public void requestFocusAndPutCursorToTail() {
        if (mWRichEditor != null) {
            mWRichEditor.requestFocusAndPutCursorToTail();
        }
    }

    public WRichEditor getWRichEditor() {
        return mWRichEditor;
    }

}
