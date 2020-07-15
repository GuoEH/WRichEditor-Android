package cn.carbs.wricheditor.library.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import cn.carbs.wricheditor.library.R;
import cn.carbs.wricheditor.library.WRichEditorScrollView;
import cn.carbs.wricheditor.library.callbacks.OnEditorFocusChangedListener;
import cn.carbs.wricheditor.library.interfaces.IRichCellData;
import cn.carbs.wricheditor.library.interfaces.IRichCellView;
import cn.carbs.wricheditor.library.types.RichType;
import cn.carbs.wricheditor.library.utils.CommonUtil;
import cn.carbs.wricheditor.library.utils.TypeUtil;

// 不抽象，如果需要自定义，直接在外部自定义
public class RichPanView extends RelativeLayout implements IRichCellView, View.OnClickListener {

    protected boolean mSelectMode;

    protected WRichEditorScrollView mWRichEditorScrollView;

    protected View mVContainer;

    protected ImageView mIVFileType;

    protected TextView mTVFileName;

    protected TextView mTVFileDescription;

    protected View mVDelete;

    protected IRichCellData mRichCellData;

    protected OnEditorFocusChangedListener mOnEditorFocusChangedListener;

    public RichPanView(Context context) {
        super(context);
        init(context);
    }

    public RichPanView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RichPanView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.wricheditor_layout_rich_pan_view, this);
        mVContainer = findViewById(R.id.wricheditor_rich_pan_view_container);
        mVContainer.setOnClickListener(this);
        mIVFileType = findViewById(R.id.iv_file_type);
        mTVFileName = findViewById(R.id.tv_file_name);
        mTVFileDescription = findViewById(R.id.tv_file_description);
        mVDelete = findViewById(R.id.iv_delete);
        mVDelete.setOnClickListener(this);
        setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.wricheditor_rich_pan_view_container) {
            TypeUtil.selectOnlyOneResourceType(mWRichEditorScrollView, this);
            CommonUtil.removeAllEditorFocus(getContext(), mWRichEditorScrollView);
            onContainerViewClicked();
        } else if (id == R.id.iv_delete) {
            if (mWRichEditorScrollView != null) {
                ViewParent parent = getParent();
                if (parent != null && parent instanceof ViewGroup) {
                    clearFocus();
                    ((ViewGroup) parent).removeView(this);
                }
            }
        } else if (v == this) {
            TypeUtil.selectOnlyOneResourceType(mWRichEditorScrollView, this);
            CommonUtil.removeAllEditorFocus(getContext(), mWRichEditorScrollView);
            onContainerViewClicked();
        }
    }

    public void onContainerViewClicked() {

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

    /**
     * 不要在外部主动调用此函数
     * @param selectMode
     */
    @Override
    public void setSelectMode(boolean selectMode) {
        if (mSelectMode == selectMode) {
            return;
        }
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