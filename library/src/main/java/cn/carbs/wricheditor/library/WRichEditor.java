package cn.carbs.wricheditor.library;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.text.Editable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Set;

import cn.carbs.wricheditor.library.callbacks.OnEditorFocusChangedListener;
import cn.carbs.wricheditor.library.callbacks.OnRichTypeChangedListener;
import cn.carbs.wricheditor.library.interfaces.IRichCellData;
import cn.carbs.wricheditor.library.interfaces.IRichCellView;
import cn.carbs.wricheditor.library.interfaces.IRichSpan;
import cn.carbs.wricheditor.library.models.RichAtomicData;
import cn.carbs.wricheditor.library.types.RichType;
import cn.carbs.wricheditor.library.utils.CursorUtil;
import cn.carbs.wricheditor.library.utils.SpanUtil;
import cn.carbs.wricheditor.library.utils.StrategyUtil;
import cn.carbs.wricheditor.library.utils.TypeUtil;

// 注意，此方法是不会合并的
// getEditableText().getSpans(0, getEditableText().toString().length(), richSpan.getClass());
// 因此最后导出的时候，是否需要合并？如何转换数据是个问题

@SuppressLint("AppCompatCustomView")
public class WRichEditor extends EditText implements IRichCellView {

    private WRichEditorView mWRichEditorView;

    private IRichCellData mRichCellData;

    private OnEditorFocusChangedListener mOnEditorFocusChangedListener;

    // todo
    // 1. 如果不使用数据呢？
    // 2. 这种思路是以文字推进的角度来进行的，另一种思路是按照指定的span类型，按照整行的方式获取所有的span，参考knife 标记wangwang
    private ArrayList<RichAtomicData> mRichAtomicDataList = new ArrayList<>();

    public WRichEditor(Context context) {
        super(context);
    }

    public WRichEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WRichEditor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    // TODO 关于监听文字改变，有 onTextChanged 和 onSelectionChanged 两个可选
    // 1. onSelectionChanged 应该适合选中一段文字后改变字体，(TODO 好像onTextChanged也能监听)
    // 2. 动态添加文字时（粘贴/一字一字的输入）onTextChanged 感觉更合适，此时的调用顺序为 onTextChanged onSelectionChanged，
    //    因为 onSelectionChanged 对应的是光标的位置，当一次输入多个字符时，onSelectionChanged 并不能体现出文字的更改，因为 selStart : n selEnd : n
    //    相反 onTextChanged 的回调返回的数据为 start: 起始位置，如6， lengthBefore: 0, lengthAfter: 新增字符串长度
    //    当选中其中两个文字，并将其替换为3个文字时， onTextChanged 的回调时 ： start : 2 lengthBefore : 2 lengthAfter : 3
    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        Log.d("fff", "editor onTextChanged text : " + text + " start : " + start + " lengthBefore : " + lengthBefore + " lengthAfter : " + lengthAfter);
//        getEditableText().setSpan();
        // Set<RichType> richTypes, Editable editable, int spanStart, int spanEnd
        if (mWRichEditorView == null) {
            return;
        }
        // TODO
        SpanUtil.setSpan(mWRichEditorView.mRichTypes, text, getEditableText(), start, start + lengthAfter);

    }


    // TODO 由此触发setSpan函数
    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        // 打字时，每次回调此函数都会 selStart == selEnd
        Log.d("qqq", "editor onSelectionChanged selStart : " + selStart + " selEnd : " + selEnd);
        if (selStart == selEnd) {
            // TODO test
            Editable editableText = getEditableText();
            int editableLength = editableText.length();
            SpanUtil.getSpanTypesForCursorLocation(editableText, selEnd);

            boolean isCursorAutoChange = CursorUtil.isCursorChangedAutomaticallyByTextChange(editableLength, selStart);
            CursorUtil.markLastTextLength(editableLength);
            CursorUtil.markLastCursorLocation(selStart);
            Log.d("qqq", "getEditableText().length() : " + getEditableText().length() + "  isCursorAutoChange : " + isCursorAutoChange);
            // 怎样区分是手动滑动了cursor，还是跟随输入汉字？通过比较 lastTextLength 与 lastCursorLocation，同时，应该注意不同的editor之间的判断
            if (getEditableText().length() == selStart) {
                // 光标在末尾
//                if ()
            } else {
                // 光标不在末尾，将用户强设置置为false ?
//                StrategyUtil.sStrongSet = false;
            }
        }
    }

    // 有效，在focus更改时，
    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        Log.d("fff", "editor onFocusChanged focused : " + focused + " direction : " + direction);
        if (mOnEditorFocusChangedListener != null) {
            mOnEditorFocusChangedListener.onEditorFocusChanged(this, focused);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.d("key", "00 keyCode : " + keyCode);
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            // 响应 keycode，如果有 headline 类型的 RichType
            Log.d("key", "keyCode : " + keyCode);
            if (mWRichEditorView != null) {
                Set<RichType> richTypes = mWRichEditorView.getRichTypes();
                if (richTypes != null) {
                    // TODO
                    boolean changed = TypeUtil.removeCertainRichType(richTypes, RichType.HEADLINE);
                    if (changed) {
                        OnRichTypeChangedListener typeChangedListener = mWRichEditorView.getOnRichTypeChangedListener();
                        if (typeChangedListener != null) {
                            // TODO api设计是否需要优化？考虑到光标移动的情况，应该不需要
                            typeChangedListener.onRichTypeChanged(TypeUtil.assembleRichTypes(richTypes, RichType.HEADLINE), richTypes);
                        }
                    }
                }
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_DEL) {
            // TODO 删除后
//            if (getEditableText().length() == 0) {
            int selectionStart = getSelectionStart();
            int selectionEnd = getSelectionEnd();
            if (selectionStart == selectionEnd && selectionStart == 0) {
                if (mWRichEditorView != null && mWRichEditorView.mRichCellViewList != null) {
                    int index = mWRichEditorView.mRichCellViewList.indexOf(this);
                    Log.d("iii", "WRichEditor index in mWRichEditorView is : " + index);
                    if (index > 0) {
                        // 查看上一个是不是WRichEditor，
                        IRichCellView iRichCellView = mWRichEditorView.mRichCellViewList.get(index - 1);

                        if (iRichCellView instanceof WRichEditor) {
                            // 1. 如果是，则将光标调整到上一个WRichEditor的最后，同时将此WRichEditor删除
                            ((WRichEditor)iRichCellView).requestFocusAndPutCursorToTail();
                        } else {

                        }

                        // 2. 如果不是，则将焦点至于image、video、等资源上

                    }
                }
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void setWRichEditorView(WRichEditorView wRichEditorView) {
        mWRichEditorView = wRichEditorView;
        Log.d("ppp", "editor setWRichEditorView mWRichEditorView == null ? " + (mWRichEditorView == null));
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void setCellData(IRichCellData cellData) {
        mRichCellData = cellData;
    }

    @Override
    public void setEditorFocusChangedListener(OnEditorFocusChangedListener listener) {
        mOnEditorFocusChangedListener = listener;
    }

    @Override
    public IRichCellData getCellData() {
        return mRichCellData;
    }

    @Override
    public RichType getRichType() {
        return RichType.NONE;
    }

    // TODO 外部主动更改了字体样式，不涉及数据插入
    public void updateTextByRichTypeChanged(RichType richType, boolean open, Object object) {
        int selectionStart = getSelectionStart();
        int selectionEnd = getSelectionEnd();
        if (selectionStart < 0 || selectionEnd < 0) {
            return;
        }
        if (selectionStart == selectionEnd) {
            // TODO 后面输入的字体将按照对应的设定字体进行
            // 当前没有选中字体
            if (richType == RichType.HEADLINE) {
                updateSpanUI(richType, open, object, selectionStart, selectionEnd, mWRichEditorView.getRichTypes());
            }
            return;
        } else {
            if (mWRichEditorView == null) {
                return;
            }

            // 将选中的部分进行更新，同时更新此View对应的
            updateSpanUI(richType, open, object, selectionStart, selectionEnd, mWRichEditorView.getRichTypes());
            // 是不是想的太复杂了？

            // TODO [难点] 切割data
            updateSpanData(selectionStart, selectionEnd, mWRichEditorView.getRichTypes());
        }
    }

    // TODO 插入数据时，应该修改data
    private void updateSpanUI(RichType richType, boolean open, Object object, int start, int end, Set<RichType> richTypes) {
        if (start < 0 || end < 0) {
            return;
        }
        SpanUtil.setSpan(richType, open, object, mRichAtomicDataList, getEditableText(), richTypes, start, end);
    }

    // TODO 插入数据时，应该修改data
    private void updateSpanData(int start, int end, Set<RichType> richTypes) {
        // 从 mRichAtomicDataList 找到start所在的位置

        // 从 mRichAtomicDataList 找到end所在的位置


    }

    public void requestFocusAndPutCursorToTail() {
        requestFocus();
        setSelection(getEditableText().toString().length());
        Log.d("iii", "requestFocusAndPutCursorToTail()");
    }

}
