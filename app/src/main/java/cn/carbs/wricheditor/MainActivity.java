package cn.carbs.wricheditor;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cn.carbs.wricheditor.library.WRichEditor;
import cn.carbs.wricheditor.library.WRichEditorView;
import cn.carbs.wricheditor.library.callbacks.OnRichTypeChangedListener;
import cn.carbs.wricheditor.library.types.RichType;
import cn.carbs.wricheditor.library.utils.TypeUtil;
import cn.carbs.wricheditor.library.views.RichImageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnRichTypeChangedListener {

    private WRichEditorView mWRichEditorView;
    private ImageButton mBtnBold;
    private ImageButton mBtnItalic;
    private ImageButton mBtnStrikeThrough;
    private ImageButton mBtnUnderLine;
    private ImageButton mBtnHeadline;
    private ImageButton mBtnLink;

    private Button mBtnAddImage;
    private Button mBtnAddEditor;

    private HashMap<RichType, ImageButton> mImageButtonMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_lib);

        mWRichEditorView = findViewById(R.id.wrich_editor_view);
        mWRichEditorView.setOnRichTypeChangedListener(this);

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
        mBtnUnderLine = findViewById(R.id.button_under_line);
        mBtnUnderLine.setOnClickListener(this);
        mBtnHeadline = findViewById(R.id.button_headline);
        mBtnHeadline.setOnClickListener(this);
        mBtnLink = findViewById(R.id.button_link);
        mBtnLink.setOnClickListener(this);

        mImageButtonMap.put(RichType.BOLD, mBtnBold);
        mImageButtonMap.put(RichType.ITALIC, mBtnItalic);
        mImageButtonMap.put(RichType.STRIKE_THROUGH, mBtnStrikeThrough);
        mImageButtonMap.put(RichType.UNDER_LINE, mBtnUnderLine);
        mImageButtonMap.put(RichType.HEADLINE, mBtnHeadline);
        mImageButtonMap.put(RichType.LINK, mBtnLink);

    }

    private void onBoldClicked() {
        Set<RichType> richTypes = mWRichEditorView.getRichTypes();
        boolean open = TypeUtil.toggleCertainRichType(richTypes, RichType.BOLD);
        mWRichEditorView.updateTextByRichTypeChanged(RichType.BOLD, open, null);
        setButtonTextColor(mBtnBold, open);
    }

    private void onItalicClicked() {
        Set<RichType> richTypes = mWRichEditorView.getRichTypes();
        boolean open = TypeUtil.toggleCertainRichType(richTypes, RichType.ITALIC);
        mWRichEditorView.updateTextByRichTypeChanged(RichType.ITALIC, open, null);
        setButtonTextColor(mBtnItalic, open);
    }

    private void onStrikeThroughClicked() {
        Set<RichType> richTypes = mWRichEditorView.getRichTypes();
        boolean open = TypeUtil.toggleCertainRichType(richTypes, RichType.STRIKE_THROUGH);
        mWRichEditorView.updateTextByRichTypeChanged(RichType.STRIKE_THROUGH, open, null);
        setButtonTextColor(mBtnStrikeThrough, open);
    }

    private void onUnderLineClicked() {
        Set<RichType> richTypes = mWRichEditorView.getRichTypes();
        boolean open = TypeUtil.toggleCertainRichType(richTypes, RichType.UNDER_LINE);
        mWRichEditorView.updateTextByRichTypeChanged(RichType.UNDER_LINE, open, null);
        setButtonTextColor(mBtnUnderLine, open);
    }

    private void onHeadLineClicked() {
        Set<RichType> richTypes = mWRichEditorView.getRichTypes();
        boolean open = TypeUtil.toggleCertainRichType(richTypes, RichType.HEADLINE);
        mWRichEditorView.updateTextByRichTypeChanged(RichType.HEADLINE, open, null);
        setButtonTextColor(mBtnHeadline, open);
    }

    private void onInsertLinkClicked() {
        setButtonTextColor(mBtnLink, true);
        WRichEditor wRichEditor = mWRichEditorView.findCurrentOrRecentFocusedRichEditor();
        if (wRichEditor == null) {
            return;
        }
        final int start = wRichEditor.getSelectionStart();
        final int end = wRichEditor.getSelectionEnd();
        if (start == end) {
            Toast.makeText(this, "请先选择要添加链接的文字", Toast.LENGTH_LONG).show();
            setButtonTextColor(mBtnLink, false);
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);

        View view = getLayoutInflater().inflate(R.layout.dialog_link, null, false);
        final EditText editText = (EditText) view.findViewById(R.id.edit);
        builder.setView(view);
        builder.setTitle("请输入链接");

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String link = editText.getText().toString().trim();
                if (TextUtils.isEmpty(link)) {
                    return;
                }
                setButtonTextColor(mBtnLink, false);
                Set<RichType> richTypes = mWRichEditorView.getRichTypes();
                TypeUtil.removeCertainRichType(richTypes, RichType.LINK);
                mWRichEditorView.updateTextByRichTypeChanged(RichType.LINK, true, null);
                setButtonTextColor(mBtnUnderLine, false);
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setButtonTextColor(mBtnLink, false);
            }
        });
        builder.create().show();
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
        } else if (id == R.id.button_under_line) {
            onUnderLineClicked();
        } else if (id == R.id.button_headline) {
            onHeadLineClicked();
        } else if (id == R.id.button_link) {
            onInsertLinkClicked();
        }
        // 底部测试button
        if (id == R.id.button_1) {
            button1Clicked();
        } else if (id == R.id.button_2) {
            button2Clicked();
        }
    }

    private void button1Clicked() {
        RichImageView richImageView = new RichImageView(MainActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        mWRichEditorView.addRichCell(richImageView, lp);
    }

    private void button2Clicked() {
        WRichEditor editText = new WRichEditor(MainActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        editText.setBackgroundColor(0x22222222);
        mWRichEditorView.addRichCell(editText, lp);
    }

    private void setButtonTextColor(ImageButton button, boolean open) {
        if (open) {
            button.setBackgroundColor(0xb06000E0);
        } else {
            button.setBackgroundColor(0xb0000000);
        }
    }

    @Override
    public void onRichTypeChanged(Set<RichType> oldTypes, Set<RichType> newTypes) {
        Log.d("wangwang", "onRichTypeChanged");

        if (newTypes == null) {
            newTypes = new HashSet<>();
        }
        for (Map.Entry<RichType, ImageButton> entry : mImageButtonMap.entrySet()) {
            if (newTypes.contains(entry.getKey())) {
                setButtonTextColor(entry.getValue(), true);
            } else {
                setButtonTextColor(entry.getValue(), false);
            }
        }
    }
}