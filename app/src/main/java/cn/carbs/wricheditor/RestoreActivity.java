package cn.carbs.wricheditor;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import cn.carbs.wricheditor.library.WRichEditorScrollView;
import cn.carbs.wricheditor.library.interfaces.IRichCellView;
import cn.carbs.wricheditor.library.providers.CustomViewProvider;
import cn.carbs.wricheditor.library.types.RichType;
import cn.carbs.wricheditor.library.utils.ParserUtil;

public class RestoreActivity extends AppCompatActivity {

    public static final String HTML = "HTML";

    private WRichEditorScrollView mWRichEditorScrollView;

    private String mHTML;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restore);

        mWRichEditorScrollView = findViewById(R.id.wrich_editor_view);

        mHTML = getIntent().getStringExtra(HTML);

        Log.d("tttt", "RestoreActivity mHTML : " + mHTML);

        ParserUtil.inflateFromHtml(this, mWRichEditorScrollView, mHTML, new CustomViewProvider() {
            @Override
            public IRichCellView getCellViewByRichType(RichType richType) {
                return null;
            }
        });

    }


}