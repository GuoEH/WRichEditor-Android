package cn.carbs.wricheditor.library.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import cn.carbs.wricheditor.library.R;
import cn.carbs.wricheditor.library.WRichEditorScrollView;
import cn.carbs.wricheditor.library.callbacks.OnEditorFocusChangedListener;
import cn.carbs.wricheditor.library.interfaces.IRichCellData;
import cn.carbs.wricheditor.library.interfaces.IRichCellView;
import cn.carbs.wricheditor.library.types.RichType;

// 不抽象，如果需要自定义，直接在外部自定义
public class RichImageView extends RelativeLayout implements IRichCellView, View.OnClickListener {

    private boolean mSelectMode;

    private WRichEditorScrollView mWRichEditorScrollView;

    private View mVContainer;

    private ImageView mImageView;

    private View mVDelete;

    private IRichCellData mRichCellData;

    private OnEditorFocusChangedListener mOnEditorFocusChangedListener;

    public RichImageView(Context context) {
        super(context);
        init(context);
    }

    public RichImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RichImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.wricheditor_layout_rich_image_view, this);
        mVContainer = findViewById(R.id.wricheditor_rich_image_view_container);
        mVContainer.setOnClickListener(this);
        mImageView = findViewById(R.id.image_view);
        mImageView.setImageResource(R.drawable.image_farmers);
        mVDelete = findViewById(R.id.iv_delete);
        mVDelete.setOnClickListener(this);
        setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.wricheditor_rich_image_view_container) {
            setSelectMode(true);
        } else if (id == R.id.iv_delete) {
            if (mWRichEditorScrollView != null && mWRichEditorScrollView.mRichCellViewList != null) {
                ViewParent parent = getParent();
                if (parent != null && parent instanceof ViewGroup) {
                    clearFocus();
                    ((ViewGroup) parent).removeView(this);
                    mWRichEditorScrollView.mRichCellViewList.remove(this);
                }
            }
        } else if (v == this) {
            setSelectMode(true);
        }
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void setWRichEditorScrollView(WRichEditorScrollView wRichEditorScrollView) {
        mWRichEditorScrollView = wRichEditorScrollView;
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
}
