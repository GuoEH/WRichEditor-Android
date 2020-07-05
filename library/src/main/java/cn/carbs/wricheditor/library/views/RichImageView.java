package cn.carbs.wricheditor.library.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import cn.carbs.wricheditor.library.R;
import cn.carbs.wricheditor.library.WRichEditorView;
import cn.carbs.wricheditor.library.interfaces.IRichCellData;
import cn.carbs.wricheditor.library.interfaces.IRichCellView;

// TODO 抽象出来 provider 针对glide等
public class RichImageView extends RelativeLayout implements IRichCellView {

    private WRichEditorView mWRichEditorView;

    private ImageView mImageView;

    private IRichCellData mRichCellData;

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
        mImageView = findViewById(R.id.image_view);
        // TODO
        mImageView.setImageResource(R.drawable.image_farmers);
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
}
