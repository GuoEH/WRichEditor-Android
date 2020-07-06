package cn.carbs.wricheditor;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Set;

import cn.carbs.wricheditor.library.WRichEditor;
import cn.carbs.wricheditor.library.WRichEditorView;
import cn.carbs.wricheditor.library.types.RichType;
import cn.carbs.wricheditor.library.utils.TypeUtil;
import cn.carbs.wricheditor.library.views.RichImageView;

public class TestLibActivity extends AppCompatActivity implements View.OnClickListener{

    private WRichEditorView mWRichEditorView;
    private Button mBtnBold;
    private Button mBtnItalic;
    private Button mBtnStrikeThrough;

    private Button mBtnAddImage;
    private Button mBtnAddEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_lib);

        mWRichEditorView = findViewById(R.id.wrich_editor_view);

        mBtnAddImage = findViewById(R.id.button_1);
        mBtnAddImage.setOnClickListener(this);
        mBtnAddEditor = findViewById(R.id.button_2);
        mBtnAddEditor.setOnClickListener(this);

        mBtnBold = findViewById(R.id.button_bold);
        mBtnBold.setOnClickListener(this);

        mBtnItalic = findViewById(R.id.button_italic);
        mBtnItalic.setOnClickListener(this);

        mBtnStrikeThrough = findViewById(R.id.button_strike_through);
        mBtnStrikeThrough.setOnClickListener(this);
    }

    private void onBoldClicked() {
        Set<RichType> richTypes = mWRichEditorView.getRichTypes();
        boolean open = TypeUtil.toggleCertainRichType(richTypes, RichType.BOLD);
        mWRichEditorView.updateTextByRichTypeChanged(RichType.BOLD, open);
        setButtonTextColor(mBtnBold, open);
    }

    private void onItalicClicked() {
        Set<RichType> richTypes = mWRichEditorView.getRichTypes();
        boolean open = TypeUtil.toggleCertainRichType(richTypes, RichType.ITALIC);
        mWRichEditorView.updateTextByRichTypeChanged(RichType.ITALIC, open);
        setButtonTextColor(mBtnItalic, open);
    }

    private void onStrikeThroughClicked() {
        Set<RichType> richTypes = mWRichEditorView.getRichTypes();
        boolean open = TypeUtil.toggleCertainRichType(richTypes, RichType.STRIKE_THROUGH);
        mWRichEditorView.updateTextByRichTypeChanged(RichType.STRIKE_THROUGH, open);
        setButtonTextColor(mBtnStrikeThrough, open);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        // 顶部功能按钮
        if (id == R.id.button_bold) {
            onBoldClicked();
        } else if (id == R.id.button_italic) {
            onItalicClicked();
        } else if (id == R.id.button_strike_through) {
            onStrikeThroughClicked();
        }
        // 底部测试button
        if (id == R.id.button_1) {
            button1Clicked();
        } else if (id == R.id.button_2) {
            button2Clicked();
        }
    }

    private void button1Clicked() {
        RichImageView richImageView = new RichImageView(TestLibActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        mWRichEditorView.addRichCell(richImageView, lp);
    }

    private void button2Clicked() {
        WRichEditor editText = new WRichEditor(TestLibActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        editText.setBackgroundColor(0x22222222);
        mWRichEditorView.addRichCell(editText, lp);
    }

    private void setButtonTextColor(Button button, boolean open) {
        if (open) {
            button.setTextColor(0xffff0000);
        } else {
            button.setTextColor(0xff000000);
        }
    }


}