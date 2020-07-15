package cn.carbs.wricheditor;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cn.carbs.wricheditor.library.WRichEditor;
import cn.carbs.wricheditor.library.WRichEditorScrollView;
import cn.carbs.wricheditor.library.WRichEditorWrapperView;
import cn.carbs.wricheditor.library.callbacks.OnRichTypeChangedListener;
import cn.carbs.wricheditor.library.parser.Parser;
import cn.carbs.wricheditor.library.types.RichType;
import cn.carbs.wricheditor.library.utils.LogUtil;
import cn.carbs.wricheditor.library.utils.TypeUtil;
import cn.carbs.wricheditor.library.views.RichLineView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnRichTypeChangedListener {

    private WRichEditorScrollView mWRichEditorScrollView;
    private ImageView mBtnBold;
    private ImageView mBtnItalic;
    private ImageView mBtnStrikeThrough;
    private ImageView mBtnUnderLine;
    private ImageView mBtnHeadline;
    private ImageView mBtnQuote;
    private ImageView mBtnLink;
    private ImageView mBtnListOrdered;
    private ImageView mBtnListUnordered;
    private ImageView mBtnInsertImage;
    private ImageView mBtnInsertLine;
    private ImageView mBtnInsertAudio;
    private ImageView mBtnInsertVideo;
    private ImageView mBtnInsertPan;
    private ImageView mBtnExportToHTML;
    private ImageView mBtnImportFromHTML;

    private ImageView mBtnAddEditor;

    private HashMap<RichType, ImageView> mImageViewMap = new HashMap<>();

    private boolean mHasAddFirstEditor = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWRichEditorScrollView = findViewById(R.id.wrich_editor_view);
        mWRichEditorScrollView.setOnRichTypeChangedListener(this);

        mBtnAddEditor = findViewById(R.id.button_add_editor_text);
        mBtnAddEditor.setOnClickListener(this);
        if (!LogUtil.DEBUG) {
            mBtnAddEditor.setVisibility(View.GONE);
        }

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
        mBtnQuote = findViewById(R.id.button_quote);
        mBtnQuote.setOnClickListener(this);
        mBtnLink = findViewById(R.id.button_link);
        mBtnLink.setOnClickListener(this);
        mBtnInsertLine = findViewById(R.id.button_line);
        mBtnInsertLine.setOnClickListener(this);
        mBtnInsertImage = findViewById(R.id.button_image);
        mBtnInsertImage.setOnClickListener(this);
        mBtnListOrdered = findViewById(R.id.button_list_ordered);
        mBtnListOrdered.setOnClickListener(this);
        mBtnListUnordered = findViewById(R.id.button_list_unordered);
        mBtnListUnordered.setOnClickListener(this);
        mBtnInsertAudio = findViewById(R.id.button_audio);
        mBtnInsertAudio.setOnClickListener(this);
        mBtnInsertVideo = findViewById(R.id.button_video);
        mBtnInsertVideo.setOnClickListener(this);
        mBtnInsertPan = findViewById(R.id.button_pan);
        mBtnInsertPan.setOnClickListener(this);
        mBtnExportToHTML = findViewById(R.id.button_export);
        mBtnExportToHTML.setOnClickListener(this);
        mBtnImportFromHTML = findViewById(R.id.button_import);
        mBtnImportFromHTML.setOnClickListener(this);

        mImageViewMap.put(RichType.BOLD, mBtnBold);
        mImageViewMap.put(RichType.ITALIC, mBtnItalic);
        mImageViewMap.put(RichType.STRIKE_THROUGH, mBtnStrikeThrough);
        mImageViewMap.put(RichType.UNDER_LINE, mBtnUnderLine);
        mImageViewMap.put(RichType.HEADLINE, mBtnHeadline);
        mImageViewMap.put(RichType.LINK, mBtnLink);
        mImageViewMap.put(RichType.QUOTE, mBtnQuote);
        mImageViewMap.put(RichType.LIST_UNORDERED, mBtnListUnordered);
        mImageViewMap.put(RichType.LIST_ORDERED, mBtnListOrdered);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mHasAddFirstEditor) {
            return;
        }
        WRichEditorWrapperView wrapperView = addEditText();
        if (wrapperView != null && wrapperView.getWRichEditor() != null) {
            wrapperView.getWRichEditor().requestFocus();
        }
        mHasAddFirstEditor = true;
    }

    private void onBoldClicked() {
        Set<RichType> richTypes = mWRichEditorScrollView.getRichTypes();
        boolean open = TypeUtil.toggleCertainRichType(richTypes, RichType.BOLD);
        mWRichEditorScrollView.updateTextByRichTypeChanged(RichType.BOLD, open, null);
        setButtonTextColor(mBtnBold, open);
    }

    private void onItalicClicked() {
        Set<RichType> richTypes = mWRichEditorScrollView.getRichTypes();
        boolean open = TypeUtil.toggleCertainRichType(richTypes, RichType.ITALIC);
        mWRichEditorScrollView.updateTextByRichTypeChanged(RichType.ITALIC, open, null);
        setButtonTextColor(mBtnItalic, open);
    }

    private void onStrikeThroughClicked() {
        Set<RichType> richTypes = mWRichEditorScrollView.getRichTypes();
        boolean open = TypeUtil.toggleCertainRichType(richTypes, RichType.STRIKE_THROUGH);
        mWRichEditorScrollView.updateTextByRichTypeChanged(RichType.STRIKE_THROUGH, open, null);
        setButtonTextColor(mBtnStrikeThrough, open);
    }

    private void onUnderLineClicked() {
        Set<RichType> richTypes = mWRichEditorScrollView.getRichTypes();
        boolean open = TypeUtil.toggleCertainRichType(richTypes, RichType.UNDER_LINE);
        mWRichEditorScrollView.updateTextByRichTypeChanged(RichType.UNDER_LINE, open, null);
        setButtonTextColor(mBtnUnderLine, open);
    }

    private void onHeadLineClicked() {
        Set<RichType> richTypes = mWRichEditorScrollView.getRichTypes();
        boolean open = TypeUtil.toggleCertainRichType(richTypes, RichType.HEADLINE);
        mWRichEditorScrollView.updateTextByRichTypeChanged(RichType.HEADLINE, open, null);
        setButtonTextColor(mBtnHeadline, open);
    }

    private void onInsertQuoteClicked() {
        Set<RichType> richTypes = mWRichEditorScrollView.getRichTypes();
        boolean open = TypeUtil.toggleCertainRichType(richTypes, RichType.QUOTE);
        mWRichEditorScrollView.updateTextByRichTypeChanged(RichType.QUOTE, open, null);
        setButtonTextColor(mBtnQuote, open);
    }

    private void onInsertLinkClicked() {
        setButtonTextColor(mBtnLink, true);
        int[] focusedRichEditorWrapperView = new int[1];
        WRichEditorWrapperView wRichEditorWrapperView = mWRichEditorScrollView.findCurrentOrRecentFocusedRichEditorWrapperView(focusedRichEditorWrapperView);
        if (wRichEditorWrapperView == null) {
            return;
        }
        WRichEditor wRichEditor = wRichEditorWrapperView.getWRichEditor();
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
                String link = editText.getEditableText().toString().trim();
                if (TextUtils.isEmpty(link)) {
                    return;
                }
                setButtonTextColor(mBtnLink, false);
                Set<RichType> richTypes = mWRichEditorScrollView.getRichTypes();
                TypeUtil.removeCertainRichType(richTypes, RichType.LINK);
                mWRichEditorScrollView.updateTextByRichTypeChanged(RichType.LINK, true, link);
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

    private void onInsertLineClicked() {
        int[] focusedIndex = new int[1];
        mWRichEditorScrollView.findCurrentOrRecentFocusedRichEditorWrapperView(focusedIndex);
        RichLineView richLineView = new RichLineView(MainActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        mWRichEditorScrollView.addRichCell(richLineView, lp, focusedIndex[0] + 1);
        if (mWRichEditorScrollView.needAddWRichEditor(focusedIndex[0] + 1)) {
            mWRichEditorScrollView.insertAWRichEditorWrapperWithRichType(focusedIndex[0] + 2, RichType.NONE, true);
        }
    }

    private void onInsertImageClicked() {
        int[] focusedIndex = new int[1];
        mWRichEditorScrollView.findCurrentOrRecentFocusedRichEditorWrapperView(focusedIndex);
        MyRichImageView richImageView = new MyRichImageView(MainActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        mWRichEditorScrollView.addRichCell(richImageView, lp, focusedIndex[0] + 1);
        richImageView.setImageUrl("");
        if (mWRichEditorScrollView.needAddWRichEditor(focusedIndex[0] + 1)) {
            mWRichEditorScrollView.insertAWRichEditorWrapperWithRichType(focusedIndex[0] + 2, RichType.NONE, true);
        }
    }

    private void onInsertAudioClicked() {
        Toast.makeText(this, "I'm on it", Toast.LENGTH_SHORT).show();
    }

    private void onInsertVideoClicked() {
        Toast.makeText(this, "I'm on it", Toast.LENGTH_SHORT).show();
    }

    private void onInsertPanClicked() {
        int[] focusedIndex = new int[1];
        mWRichEditorScrollView.findCurrentOrRecentFocusedRichEditorWrapperView(focusedIndex);
        MyRichPanView richPanView = new MyRichPanView(MainActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        mWRichEditorScrollView.addRichCell(richPanView, lp, focusedIndex[0] + 1);
        richPanView.setData(R.drawable.ic_file_type_word, "三国演义", "3M  来自:我的FTP");
        if (mWRichEditorScrollView.needAddWRichEditor(focusedIndex[0] + 1)) {
            mWRichEditorScrollView.insertAWRichEditorWrapperWithRichType(focusedIndex[0] + 2, RichType.NONE, true);
        }
    }

    private void onListUnorderedClicked() {
        Set<RichType> richTypes = mWRichEditorScrollView.getRichTypes();
        boolean open = TypeUtil.toggleCertainRichType(richTypes, RichType.LIST_UNORDERED);
        mWRichEditorScrollView.updateTextByRichTypeChanged(RichType.LIST_UNORDERED, open, null);
        setButtonTextColor(mBtnListUnordered, open);
    }

    private void onListOrderedClicked() {
        Set<RichType> richTypes = mWRichEditorScrollView.getRichTypes();
        boolean open = TypeUtil.toggleCertainRichType(richTypes, RichType.LIST_ORDERED);
        mWRichEditorScrollView.updateTextByRichTypeChanged(RichType.LIST_ORDERED, open, null);
        setButtonTextColor(mBtnListOrdered, open);
    }

    private void onAddEditorTextClicked() {
        WRichEditorWrapperView editTextWrapperView = new WRichEditorWrapperView(MainActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int size = mWRichEditorScrollView.getCellViewCount();
        if (LogUtil.DEBUG) {
            editTextWrapperView.getWRichEditor().setHint("CELL : " + size);
            if (size % 2 == 0) {
                editTextWrapperView.setBackgroundColor(0x10222222);
            } else {
                editTextWrapperView.setBackgroundColor(0x18222222);
            }
        }
        mWRichEditorScrollView.addRichCell(editTextWrapperView, lp, -1);
    }

    private WRichEditorWrapperView addEditText() {
        WRichEditorWrapperView editTextWrapperView = new WRichEditorWrapperView(MainActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mWRichEditorScrollView.addRichCell(editTextWrapperView, lp, -1);
        if (LogUtil.DEBUG) {
            editTextWrapperView.setBackgroundColor(0x66660000);
        }
        return editTextWrapperView;
    }

    private void exportToHtml() {
        WRichEditorWrapperView editorWrapperView =(WRichEditorWrapperView) mWRichEditorScrollView.getContainerView().getChildAt(0);
        WRichEditor wRichEditor = editorWrapperView.getWRichEditor();
        if (wRichEditor != null) {
            Editable editable = wRichEditor.getEditableText();
            StringBuilder sb = new StringBuilder();
            Parser.withinContent(sb, editable, 0, editable.length());
            Intent intent = new Intent(this, WebViewActivity.class);
            intent.putExtra(WebViewActivity.HTML, sb.toString());
            startActivity(intent);
        }
    }

    private void importFromHtml() {

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
        } else if (id == R.id.button_line) {
            onInsertLineClicked();
        } else if (id == R.id.button_image) {
            onInsertImageClicked();
        } else if (id == R.id.button_quote) {
            onInsertQuoteClicked();
        } else if (id == R.id.button_list_ordered) {
            onListOrderedClicked();
        } else if (id == R.id.button_list_unordered) {
            onListUnorderedClicked();
        } else if (id == R.id.button_audio) {
            onInsertAudioClicked();
        } else if (id == R.id.button_video) {
            onInsertVideoClicked();
        } else if (id == R.id.button_pan) {
            onInsertPanClicked();
        } else if (id == R.id.button_export) {
            exportToHtml();
        } else if (id == R.id.button_import) {
            importFromHtml();
        } else if (id == R.id.button_add_editor_text) {
            onAddEditorTextClicked();
        }
    }

    private void setButtonTextColor(ImageView button, boolean open) {
        if (open) {
            button.setBackgroundResource(R.drawable.selector_format_buttons_select);
        } else {
            button.setBackgroundResource(R.drawable.selector_format_buttons_unselect);
        }
    }

    @Override
    public void onRichTypeChanged(Set<RichType> oldTypes, Set<RichType> newTypes) {
        if (newTypes == null) {
            newTypes = new HashSet<>();
        }
        for (Map.Entry<RichType, ImageView> entry : mImageViewMap.entrySet()) {
            if (newTypes.contains(entry.getKey())) {
                setButtonTextColor(entry.getValue(), true);
            } else {
                setButtonTextColor(entry.getValue(), false);
            }
        }
    }
}