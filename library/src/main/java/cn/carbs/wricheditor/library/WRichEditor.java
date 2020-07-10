package cn.carbs.wricheditor.library;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.text.Editable;
import android.text.Spanned;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import cn.carbs.wricheditor.library.callbacks.OnEditorFocusChangedListener;
import cn.carbs.wricheditor.library.callbacks.OnRichTypeChangedListener;
import cn.carbs.wricheditor.library.interfaces.IRichCellView;
import cn.carbs.wricheditor.library.interfaces.IRichSpan;
import cn.carbs.wricheditor.library.models.RichAtomicData;
import cn.carbs.wricheditor.library.models.SpanPart;
import cn.carbs.wricheditor.library.types.RichType;
import cn.carbs.wricheditor.library.utils.CursorUtil;
import cn.carbs.wricheditor.library.utils.SpanUtil;
import cn.carbs.wricheditor.library.utils.TypeUtil;

// 注意，此方法是不会合并的
// getEditableText().getSpans(0, getEditableText().toString().length(), richSpan.getClass());
// 因此最后导出的时候，是否需要合并？如何转换数据是个问题

// TODO wangwang 去掉 IRichCellView
@SuppressLint("AppCompatCustomView")
public class WRichEditor extends EditText {

    private WRichEditorScrollView mWRichEditorScrollView;

    private OnEditorFocusChangedListener mOnEditorFocusChangedListener;

    // todo
    // 1. 如果不使用数据呢？
    // 2. 这种思路是以文字推进的角度来进行的，另一种思路是按照指定的span类型，按照整行的方式获取所有的span，参考knife 标记wangwang
    private ArrayList<RichAtomicData> mRichAtomicDataList = new ArrayList<>();

    private WRichEditorWrapperView mWrapperView;

    private boolean mTextChangeValid = true;

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


    // 注意，setSpan不会响应 onTextChanged
    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);

        Log.d("textchange", "onTextChanged hint : " + getHint() + "  mTextChangeValid : " + mTextChangeValid);
        if (!mTextChangeValid) {
            return;
        }

        if (mWRichEditorScrollView == null) {
            return;
        }
        Log.d("textchange", "hint : " + getHint()
                + " , has parent : " + (getParent() != null)
                + " , editor onTextChanged text : " + text
                + " , editor getEditableText() : " + getEditableText().toString()
                + " , start : " + start
                + " , lengthBefore : " + lengthBefore
                + " , lengthAfter : " + lengthAfter);
        // 回调返回的 text 和 getEditableText() 不一定一致，SpannableStringBuilder中的textWatcher的内置问题
        if (getParent() != null && getEditableText().toString().equals(text.toString())) {
            SpanUtil.setSpan(mWRichEditorScrollView.mRichTypes, text, getEditableText(), start, start + lengthAfter);
        }
    }


    // TODO
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
            // TODO
            mOnEditorFocusChangedListener.onEditorFocusChanged(mWrapperView, focused);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.d("key", "00 keyCode : " + keyCode);
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            // 响应 keycode，如果有 headline 类型的 RichType
            Log.d("key", "keyCode : " + keyCode);
            if (mWRichEditorScrollView != null) {
                Set<RichType> richTypes = mWRichEditorScrollView.getRichTypes();
                if (richTypes != null) {
                    // TODO
                    boolean changed = TypeUtil.removeCertainRichType(richTypes, RichType.HEADLINE);
                    if (changed) {
                        OnRichTypeChangedListener typeChangedListener = mWRichEditorScrollView.getOnRichTypeChangedListener();
                        if (typeChangedListener != null) {
                            // TODO api设计是否需要优化？考虑到光标移动的情况，应该不需要
                            typeChangedListener.onRichTypeChanged(TypeUtil.assembleRichTypes(richTypes, RichType.HEADLINE), richTypes);
                        }
                    }
                }
            }
            if (mWRichEditorScrollView != null && mWrapperView != null) {
                RichType richType = mWrapperView.getRichType();
                if (richType == RichType.LIST_UNORDERED) {
                    // 如果是无序列表
                    mWRichEditorScrollView.insertAWRichEditorWrapperWithRichType(mWrapperView, RichType.LIST_UNORDERED, true);
                    return true;
                } else if (richType == RichType.LIST_ORDERED) {
                    // TODO
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
                if (mWRichEditorScrollView != null && mWRichEditorScrollView.mRichCellViewList != null && mWrapperView != null) {
                    int index = mWRichEditorScrollView.mRichCellViewList.indexOf(mWrapperView);
                    Log.d("iii", "WRichEditor index in mWRichEditorView is : " + index);
                    if (index > 0) {
                        // 查看上一个是不是WRichEditor，
                        IRichCellView iRichCellView = mWRichEditorScrollView.mRichCellViewList.get(index - 1);

                        if (iRichCellView != null && iRichCellView.getRichType() == RichType.NONE) {
                            // 1. 如果是，则将光标调整到上一个WRichEditor的最后，同时将此WRichEditor删除
                            // 获取editable，然后将此WRichEditor remove
                            Editable editable = getEditableText();

                            ((WRichEditorWrapperView) iRichCellView).addExtraEditable(editable);
                            ((WRichEditorWrapperView) iRichCellView).requestFocusAndPutCursorToTail();

                            // TODO
                            ViewParent parent = mWrapperView.getParent();
                            if (parent != null && parent instanceof ViewGroup) {
                                setText("");
                                clearFocus();
                                ((ViewGroup) parent).removeView(mWrapperView);
                                Log.d("nnn", "removeView");
                                mWRichEditorScrollView.mRichCellViewList.remove(mWrapperView);
                            }

                        } else {

                        }

                        // 2. 如果不是，则将焦点至于image、video、等资源上

                    }
                }
            }
        } else if (keyCode == KeyEvent.KEYCODE_ENTER) {
            if (mWRichEditorScrollView != null && mWrapperView != null) {
                RichType richType = mWrapperView.getRichType();
                if (richType == RichType.LIST_UNORDERED) {
                    // 如果是无序列表，KeyDown的时候不响应enter
                    return true;
                } else if (richType == RichType.LIST_ORDERED) {
                    // 如果是有序列表，KeyDown的时候不响应enter
                    return true;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public void setWRichEditorWrapperView(WRichEditorWrapperView wrapperView) {
        mWrapperView = wrapperView;
    }


    public void setWRichEditorScrollView(WRichEditorScrollView wRichEditorScrollView) {
        mWRichEditorScrollView = wRichEditorScrollView;
        Log.d("ppp", "editor setWRichEditorView mWRichEditorView == null ? " + (mWRichEditorScrollView == null));
    }

    public void setEditorFocusChangedListener(OnEditorFocusChangedListener listener) {
        mOnEditorFocusChangedListener = listener;
    }

    public boolean getSelectMode() {
        return false;
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
                updateSpanUI(richType, open, object, selectionStart, selectionEnd, mWRichEditorScrollView.getRichTypes());
            }
            return;
        } else {
            if (mWRichEditorScrollView == null) {
                return;
            }

            // 将选中的部分进行更新，同时更新此View对应的
            updateSpanUI(richType, open, object, selectionStart, selectionEnd, mWRichEditorScrollView.getRichTypes());
            // 是不是想的太复杂了？

            // TODO [难点] 切割data
            updateSpanData(selectionStart, selectionEnd, mWRichEditorScrollView.getRichTypes());
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

    // SpannableStringBuilder
    public void addExtraEditable(Editable extraEditable) {
        Log.d("textchange", "hint : " + getHint() + " , addExtraEditable 0 " + extraEditable.getClass().getName());
        if (extraEditable != null) {

//            SpannableStringBuilder spannableStringBuilder = (SpannableStringBuilder) extraEditable;
//            TextWatcher[] watchers = spannableStringBuilder.getSpans(0, extraEditable.length(),TextWatcher.class);
            // android.text.DynamicLayout$ChangeWatcher
            // android.widget.TextView$ChangeWatcher

            Editable originalEditable = getEditableText();

            // 当append是有格式的String时，即，Editable，已经remove的EditText会继续响应onTextChanged函数
            // originalEditable.append(extraEditable);

            // 当append是没有格式的String时，已经remove的EditText不会继续响应onTextChanged函数
            // originalEditable.append(extraEditable.toString());

            IRichSpan[] spans = extraEditable.getSpans(0, extraEditable.length(), IRichSpan.class);

            List<SpanPart> list = new ArrayList<>();
            for (IRichSpan span : spans) {
                list.add(new SpanPart(extraEditable.getSpanStart(span), extraEditable.getSpanEnd(span), span));
                extraEditable.removeSpan(span);
            }
            int originalLength = originalEditable.length();
            // 先添加无格式的文字
            originalEditable.append(extraEditable.toString());
            // 循环将格式赋给添加的这一段
            for (SpanPart part : list) {
                if (part.isValid()) {
//                    if (part.getStart() < spanStart) {
//                        editable.setSpan(TypeUtil.getSpanByType(richType, object), part.getStart(), spanStart, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
//                    }

//                    if (part.getEnd() > spanEnd) {
//                        editable.setSpan(TypeUtil.getSpanByType(richType, object), spanEnd, part.getEnd(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
//                    }
                    originalEditable.setSpan(part.getRichSpan(), originalLength + part.getStart(), originalLength + part.getEnd(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                }
            }

        }
    }

    // TODO 可以删除这个方法，用SpanUtil中的方法代替
    // 将某个区间的富文本取出，然后只保留这部分富文本
    public void subSpannableStringInclusiveExclusive(int start, int end) {

        Editable editable = getText();
        IRichSpan[] spans = editable.getSpans(start, end, IRichSpan.class);

        List<SpanPart> list = new ArrayList<>();
        for (IRichSpan span : spans) {
            list.add(new SpanPart(editable.getSpanStart(span), editable.getSpanEnd(span), span));
            Log.d("qwer", "subSpannableStringInclusiveExclusive editable.removeSpan(span)");
            editable.removeSpan(span);
        }
        // TODO
        Log.d("qwer", "subSpannableStringInclusiveExclusive setText ");
        mTextChangeValid = false;
        setText(editable.subSequence(start, end).toString());
        mTextChangeValid = true;
        // 循环将格式赋给添加的这一段
        for (SpanPart part : list) {
            if (part.isValid()) {
                Log.d("qwer", "subSpannableStringInclusiveExclusive setSpan  ");
                getText().setSpan(part.getRichSpan(), part.getStart() - start, part.getEnd() - start, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            }
        }

    }

    public void requestFocusAndPutCursorToTail() {
        requestFocus();
        setSelection(getEditableText().toString().length());
        Log.d("lll", "hint : " + getHint() + ", requestFocusAndPutCursorToTail()");
    }

}
