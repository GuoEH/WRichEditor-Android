package cn.carbs.wricheditor.library.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import cn.carbs.wricheditor.library.R;
import cn.carbs.wricheditor.library.WRichEditorView;
import cn.carbs.wricheditor.library.callbacks.OnEditorFocusChangedListener;
import cn.carbs.wricheditor.library.interfaces.IRichCellData;
import cn.carbs.wricheditor.library.interfaces.IRichCellView;
import cn.carbs.wricheditor.library.types.RichType;

// TODO 抽象出来 provider 针对glide等
public class RichLineView extends RelativeLayout implements IRichCellView, View.OnClickListener {

    private boolean mSelectMode;

    private WRichEditorView mWRichEditorView;

    private IRichCellData mRichCellData;


    private OnEditorFocusChangedListener mOnEditorFocusChangedListener;

    private View mVLine;

    private View mVDelete;

    public RichLineView(Context context) {
        super(context);
        init(context);
    }

    public RichLineView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RichLineView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.wricheditor_layout_rich_line_view, this);
        mVLine = findViewById(R.id.v_line);
        mVDelete = findViewById(R.id.iv_delete);
        mVDelete.setOnClickListener(this);
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void setWRichEditorView(WRichEditorView wRichEditorView) {
        mWRichEditorView = wRichEditorView;
    }

    @Override
    public void setCellData(IRichCellData cellData) {
        mRichCellData = cellData;
    }

    @Override
    public IRichCellData getCellData() {
        return mRichCellData;
    }

    @Override
    public RichType getRichType() {
        return RichType.IMAGE;
    }

    @Override
    public void setSelectMode(boolean selectMode) {
        mSelectMode = selectMode;
    }

    @Override
    public boolean getSelectMode() {
        return mSelectMode;
    }

    @Override
    public void setEditorFocusChangedListener(OnEditorFocusChangedListener listener) {
        mOnEditorFocusChangedListener = listener;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_delete) {
            // TODO merge
            if (mWRichEditorView != null && mWRichEditorView.mRichCellViewList != null) {
                ViewParent parent = getParent();
                if (parent != null && parent instanceof ViewGroup) {
                    clearFocus();
                    ((ViewGroup) parent).removeView(this);
                    mWRichEditorView.mRichCellViewList.remove(this);
                }
            }
        }
    }
}
