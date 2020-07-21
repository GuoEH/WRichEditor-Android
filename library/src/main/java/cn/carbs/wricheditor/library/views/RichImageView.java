package cn.carbs.wricheditor.library.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import cn.carbs.wricheditor.library.R;
import cn.carbs.wricheditor.library.WRichEditor;
import cn.carbs.wricheditor.library.callbacks.OnEditorFocusChangedListener;
import cn.carbs.wricheditor.library.interfaces.IRichCellView;
import cn.carbs.wricheditor.library.models.cell.ImageCellData;
import cn.carbs.wricheditor.library.types.RichType;
import cn.carbs.wricheditor.library.utils.CommonUtil;
import cn.carbs.wricheditor.library.utils.TypeUtil;

// 不抽象，如果需要自定义，直接在外部自定义
public class RichImageView extends RelativeLayout implements IRichCellView<ImageCellData>, View.OnClickListener {

    protected boolean mSelectMode;

    protected WRichEditor mWRichEditor;

    protected View mVContainer;

    protected ImageView mImageView;

    protected ImageView mImageViewCover;

    protected View mVDelete;

    protected ImageCellData mCellData;

    protected OnEditorFocusChangedListener mOnEditorFocusChangedListener;

    protected int mImageWidth;
    protected int mImageHeight;

    protected int mImageViewWidth;
    protected int mImageViewHeight;

    protected boolean mDataLoaded;

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
        mImageViewCover = findViewById(R.id.image_view_cover);
        mVDelete = findViewById(R.id.iv_delete);
        mVDelete.setOnClickListener(this);
        setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.wricheditor_rich_image_view_container) {
            TypeUtil.selectOnlyOneResourceType(mWRichEditor, this);
            CommonUtil.removeAllEditorFocus(getContext(), mWRichEditor);
        } else if (id == R.id.iv_delete) {
            if (mWRichEditor != null) {
                ViewParent parent = getParent();
                if (parent != null && parent instanceof ViewGroup) {
                    Log.d("clearfocus", "clearFocus() 6");
                    clearFocus();
                    ((ViewGroup) parent).removeView(this);
                }
            }
        } else if (v == this) {
            TypeUtil.selectOnlyOneResourceType(mWRichEditor, this);
            CommonUtil.removeAllEditorFocus(getContext(), mWRichEditor);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mImageViewWidth = getMeasuredWidth();
        mImageViewHeight = 0;
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
    public void setCellData(ImageCellData cellData) {
        mCellData = cellData;
        if (!mDataLoaded && getCellData() != null && getImageView() != null) {
            loadImageUrl(getCellData().imageNetUrl);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!mDataLoaded && getCellData() != null && getImageView() != null) {
            loadImageUrl(getCellData().imageNetUrl);
        }
    }

    // sdk中为了避免引入第三方图片加载框架,因此采用了空实现
    protected void loadImageUrl(String imageUrl) {

    }

    @Override
    public ImageCellData getCellData() {
        return mCellData;
    }

    @Override
    public RichType getRichType() {
        return RichType.IMAGE;
    }

    /**
     * 不要在外部主动调用此函数
     *
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

    public ImageView getImageView() {
        return mImageView;
    }

    public void setImageWidthAndHeight(int imageWidth, int imageHeight) {
        int measuredWidth = getMeasuredWidth();
        if (measuredWidth > 0) {
            mImageViewWidth = measuredWidth;
        }

        int imageViewWidth = mImageViewWidth
                - getResources().getDimensionPixelSize(R.dimen.wrich_editor_image_view_padding) * 2
                - getResources().getDimensionPixelSize(R.dimen.wrich_editor_image_view_margin_h) * 2;
        int imageViewHeight = imageViewWidth * imageHeight / imageWidth;
        setLayoutParamsForImageView(mImageView, imageViewWidth, imageViewHeight);
        setLayoutParamsForImageView(mImageViewCover, imageViewWidth, imageViewHeight);
    }

    private void setLayoutParamsForImageView(View view, int viewWidth, int viewHeight) {
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) view.getLayoutParams();
        if (lp == null) {
            lp = new RelativeLayout.LayoutParams(viewWidth, viewHeight);
            view.setLayoutParams(lp);
        } else {
            if (lp.height != viewHeight) {
                lp.height = viewHeight;
            }
            view.setLayoutParams(lp);
        }
    }

}