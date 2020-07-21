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
import cn.carbs.wricheditor.library.WRichEditor;
import cn.carbs.wricheditor.library.callbacks.OnEditorFocusChangedListener;
import cn.carbs.wricheditor.library.interfaces.IRichCellView;
import cn.carbs.wricheditor.library.models.cell.LineCellData;
import cn.carbs.wricheditor.library.types.RichType;
import cn.carbs.wricheditor.library.utils.TypeUtil;

// 不抽象，如果需要自定义，直接在外部自定义
public class RichLineView extends RelativeLayout implements IRichCellView<LineCellData>, View.OnClickListener {

    private boolean mSelectMode;

    private WRichEditor mWRichEditor;

    private LineCellData mCellData;

    private OnEditorFocusChangedListener mOnEditorFocusChangedListener;

    private View mVContainer;

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
        mVContainer = findViewById(R.id.wricheditor_rich_image_view_container);
        mVContainer.setOnClickListener(this);
        mVLine = findViewById(R.id.v_line);
        mVDelete = findViewById(R.id.iv_delete);
        mVDelete.setOnClickListener(this);
        setOnClickListener(this);
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void setWRichEditorScrollView(WRichEditor wRichEditor) {
        mWRichEditor = wRichEditor;
    }

    @Override
    public void setHtmlData(RichType richType, String htmlContent) {

    }

    @Override
    public void setCellData(LineCellData cellData) {
        mCellData = cellData;
    }

    @Override
    public LineCellData getCellData() {
        if (mCellData == null) {
            mCellData = new LineCellData();
        }
        return mCellData;
    }

    @Override
    public RichType getRichType() {
        return RichType.IMAGE;
    }

    @Override
    public void setSelectMode(boolean selectMode) {
        mSelectMode = selectMode;
        if (mVContainer == null) {
            return;
        }
        if (mSelectMode) {
            mVContainer.setBackgroundResource(R.drawable.shape_wre_bg_select_rect);
            mVDelete.setVisibility(View.VISIBLE);
        } else {
            mVContainer.setBackgroundResource(R.drawable.shape_wre_bg_null);
            mVDelete.setVisibility(View.INVISIBLE);
        }
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
        if (id == R.id.wricheditor_rich_image_view_container) {
            TypeUtil.selectOnlyOneResourceType(mWRichEditor, this);
        } else if (id == R.id.iv_delete) {
            // TODO merge
            if (mWRichEditor != null) {
                ViewParent parent = getParent();
                if (parent != null && parent instanceof ViewGroup) {
                    Log.d("clearfocus", "clearFocus() 7");
                    clearFocus();
                    ((ViewGroup) parent).removeView(this);
                }
            }
        }
    }
}
