package cn.carbs.wricheditor;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import cn.carbs.wricheditor.library.models.cell.PanCellData;
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

    @Override
    public void setCellData(PanCellData cellData) {
        super.setCellData(cellData);
        if (cellData != null) {
            setData(cellData.fileTypeImageRes,
                    cellData.fileName,
                    convertFileSizeToDescription(cellData.fileSize));
        }
    }

    // TODO
    private String convertFileSizeToDescription(long fileSize) {
        if (fileSize > 1024 * 1000) {
            return "1M 来自:我的FTP";
        }
        return "1M 来自:我的FTP";
    }

    private void setData(int panFileTypeImageRes, String fileName, String fileDescription) {

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
