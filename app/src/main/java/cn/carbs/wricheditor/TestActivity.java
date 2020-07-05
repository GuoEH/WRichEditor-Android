package cn.carbs.wricheditor;

import android.os.Bundle;
import android.text.Spanned;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class TestActivity extends AppCompatActivity {

    EditText editText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        editText = findViewById(R.id.et_test);
        findViewById(R.id.button_1).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (editText.getEditableText().toString().length() == 0) {
                    return;
                }
                int length = editText.getEditableText().toString().length();
                //
                editText.getEditableText().setSpan(new MyBoldStyleSpan(),0 , length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                editText.getEditableText().setSpan(new MyItalicStyleSpan(),0 , length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
//                editable.setSpan(getInlineStyleSpan(spanClazz), spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            }
        });


    }
}