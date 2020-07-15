package cn.carbs.wricheditor;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import cn.carbs.wricheditor.library.views.RichPanView;

public class MyRichPanView extends RichPanView {

    public MyRichPanView(Context context) {
        super(context);
    }

    public MyRichPanView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyRichPanView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setData(int panFileTypeImageRes, String fileName, String fileDescription) {

        if (mIVFileType != null) {
            Glide
                    .with(getContext())
                    .load(panFileTypeImageRes)
                    .centerInside()
                    .into(mIVFileType);
        }

        if (mTVFileName != null) {
            mTVFileName.setText(fileName);
        }

        if (mTVFileDescription != null) {
            mTVFileDescription.setText(fileDescription);
        }
    }

    @Override
    public void onContainerViewClicked() {
        super.onContainerViewClicked();
        Toast.makeText(getContext(), "PAN clicked", Toast.LENGTH_LONG).show();
    }
}
