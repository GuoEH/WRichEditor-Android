package cn.carbs.wricheditor;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import cn.carbs.wricheditor.library.WRichEditorScrollView;
import cn.carbs.wricheditor.library.WRichEditorWrapperView;
import cn.carbs.wricheditor.library.interfaces.IRichCellView;
import cn.carbs.wricheditor.library.providers.CustomViewProvider;
import cn.carbs.wricheditor.library.types.RichType;
import cn.carbs.wricheditor.library.utils.ParserUtil;

public class RestoreActivity extends AppCompatActivity {

    public static final String HTML = "HTML";

    public static final String JSON = "JSON";

    private WRichEditorScrollView mWRichEditorScrollView;

    private String mHTML;

    private String mJSON;

    private TextView mTVTitle;

    private TextView mTVTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restore);

        mTVTitle = findViewById(R.id.tv_title);
        mWRichEditorScrollView = findViewById(R.id.wrich_editor_view);

        mHTML = getIntent().getStringExtra(HTML);
        mJSON = getIntent().getStringExtra(JSON);

        if (!TextUtils.isEmpty(mHTML)) {
            Log.d("tttt", "RestoreActivity mHTML : " + mHTML);
            mTVTitle.setText("Inflated by HTML");
            inflateByHtml();
        } else if (!TextUtils.isEmpty(mJSON)) {
            Log.d("ggg", "RestoreActivity mJSON : " + mJSON);
            mTVTitle.setText("Inflated by JSON");
            inflateByJson();
        }

        mTVTest = findViewById(R.id.text_view_test);

        mTVTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WRichEditorWrapperView wRichEditorWrapperView = (WRichEditorWrapperView) mWRichEditorScrollView.getContainerView().getChildAt(0);
                Editable editable = wRichEditorWrapperView.getWRichEditor().getEditableText();
                mTVTest.setText(editable);
            }
        });
    }

    private void inflateByHtml() {
        ParserUtil.inflateFromHtml(this, mWRichEditorScrollView, mHTML, new CustomViewProvider() {
            @Override
            public IRichCellView getCellViewByRichType(RichType richType) {
                return null;
            }
        });
    }

    private void inflateByJson() {
        ParserUtil.inflateFromJson(this, mWRichEditorScrollView, mJSON, new CustomViewProvider() {
            @Override
            public IRichCellView getCellViewByRichType(RichType richType) {
                return null;
            }
        });
    }


}