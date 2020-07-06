package cn.carbs.wricheditor;

import android.os.Bundle;
import android.text.Spanned;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Set;

import cn.carbs.wricheditor.library.WRichEditor;
import cn.carbs.wricheditor.library.WRichEditorView;
import cn.carbs.wricheditor.library.types.RichType;
import cn.carbs.wricheditor.library.utils.TypeUtil;
import cn.carbs.wricheditor.library.views.RichImageView;

public class TestLibActivity extends AppCompatActivity {

    private WRichEditorView wrich_editor_view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_lib);

        wrich_editor_view = findViewById(R.id.wrich_editor_view);
        findViewById(R.id.button_1).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                RichImageView richImageView = new RichImageView(TestLibActivity.this);

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.gravity = Gravity.CENTER_HORIZONTAL;
                wrich_editor_view.addRichCell(richImageView, lp);

            }
        });

        findViewById(R.id.button_2).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.d("ppp", "activity before new WRichEditor");
                WRichEditor editText = new WRichEditor(TestLibActivity.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                editText.setBackgroundColor(0x22222222);
                Log.d("ppp", "activity new WRichEditor");
                wrich_editor_view.addRichCell(editText, lp);
            }
        });


        findViewById(R.id.button_bold).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                onBoldClicked();
            }
        });

        findViewById(R.id.button_italic).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                onItalicClicked();
            }
        });

    }

    private void onBoldClicked() {
        Set<RichType> richTypes = wrich_editor_view.getRichTypes();
        TypeUtil.toggleCertainRichType(richTypes, RichType.BOLD);
        wrich_editor_view.updateTextByRichTypeChanged();
    }

    private void onItalicClicked() {
        Set<RichType> richTypes = wrich_editor_view.getRichTypes();
        TypeUtil.toggleCertainRichType(richTypes, RichType.BOLD);
        wrich_editor_view.updateTextByRichTypeChanged();
    }

}