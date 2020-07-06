package cn.carbs.wricheditor.library;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import cn.carbs.wricheditor.library.callbacks.OnEditorFocusChangedListener;
import cn.carbs.wricheditor.library.interfaces.IRichCellData;
import cn.carbs.wricheditor.library.interfaces.IRichCellView;
import cn.carbs.wricheditor.library.types.RichType;

@SuppressLint("AppCompatCustomView")
public class WRichEditor extends EditText implements IRichCellView {

    private WRichEditorView mWRichEditorView;

    private IRichCellData mRichCellData;

    private OnEditorFocusChangedListener mOnEditorFocusChangedListener;

    public WRichEditor(Context context) {
        super(context);
    }

    public WRichEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WRichEditor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // TODO 由此触发setSpan函数
    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        Log.d("fff", "editor onSelectionChanged selStart : " + selStart + " selEnd : " + selEnd);
        if (mWRichEditorView == null) {
            return;
        }
        mWRichEditorView.getRichTypes();

    }

    // 有效，在focus更改时，
    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        Log.d("fff", "editor onFocusChanged focused : " + focused + " direction : " + direction);
        if (mOnEditorFocusChangedListener != null) {
            mOnEditorFocusChangedListener.onEditorFocusChanged(this, focused);
        }
    }

    @Override
    public void setWRichEditorView(WRichEditorView wRichEditorView) {
        mWRichEditorView = wRichEditorView;
        Log.d("ppp", "editor setWRichEditorView mWRichEditorView == null ? " + (mWRichEditorView == null));
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
        mOnEditorFocusChangedListener = listener;
    }

    @Override
    public IRichCellData getCellData() {
        return mRichCellData;
    }

    @Override
    public RichType getRichType() {
        return RichType.NONE;
    }
}
