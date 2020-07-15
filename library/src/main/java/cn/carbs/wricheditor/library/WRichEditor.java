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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.carbs.wricheditor.library.callbacks.OnEditorFocusChangedListener;
import cn.carbs.wricheditor.library.callbacks.OnRichTypeChangedListener;
import cn.carbs.wricheditor.library.constants.RichTypeConstants;
import cn.carbs.wricheditor.library.interfaces.IRichCellView;
import cn.carbs.wricheditor.library.interfaces.IRichSpan;
import cn.carbs.wricheditor.library.models.SpanPart;
import cn.carbs.wricheditor.library.types.RichType;
import cn.carbs.wricheditor.library.utils.CursorUtil;
import cn.carbs.wricheditor.library.utils.LogUtil;
import cn.carbs.wricheditor.library.utils.OrderListUtil;
import cn.carbs.wricheditor.library.utils.CommonUtil;
import cn.carbs.wricheditor.library.utils.SpanUtil;
import cn.carbs.wricheditor.library.utils.StrategyUtil;
import cn.carbs.wricheditor.library.utils.TypeUtil;

// 注意，此方法是不会合并的
// getEditableText().getSpans(0, getEditableText().toString().length(), richSpan.getClass());
// 因此最后导出的时候，是否需要合并？如何转换数据是个问题

@SuppressLint("AppCompatCustomView")
public class WRichEditor extends EditText {

    public static final String TAG = WRichEditor.class.getSimpleName();

    private WRichEditorScrollView mWRichEditorScrollView;

    private OnEditorFocusChangedListener mOnEditorFocusChangedListener;

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

        LogUtil.d(TAG, "onTextChanged hint : " + getHint() + "  mTextChangeValid : " + mTextChangeValid);
        if (!mTextChangeValid) {
            return;
        }

        if (mWRichEditorScrollView == null) {
            return;
        }
        LogUtil.d(TAG, "WRichEditor onTextChanged hint : " + getHint()
                + ", has parent : " + (getParent() != null)
                + ", editor onTextChanged text : " + text
                + ", editor getEditableText() : " + getEditableText().toString()
                + ", start : " + start
                + ", lengthBefore : " + lengthBefore
                + ", lengthAfter : " + lengthAfter);
        // 回调返回的 text 和 getEditableText() 不一定一致，SpannableStringBuilder中的textWatcher的内置问题
        if (getParent() != null && getEditableText().toString().equals(text.toString())) {
            SpanUtil.setSpan(mWRichEditorScrollView.mRichTypes, text, getEditableText(), start, start + lengthAfter);
        }
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        // 打字时，每次回调此函数都会 selStart == selEnd
        Editable editableText = getEditableText();
        int editableLength = editableText.length();
        LogUtil.d("www", "WRichEditor onSelectionChanged selStart : " + selStart + " selEnd : " + selEnd + " hasFocus : " + hasFocus());
        if (selStart == selEnd) {
            // TODO test
            SpanUtil.getSpanTypesForCursorLocation(editableText, selEnd);
            boolean isCursorAutoChange = CursorUtil.isCursorChangedAutomaticallyByTextChange(editableLength, selStart, (String) getContentDescription());
            Log.d("www", "onSelectionChanged isCursorAutoChange : " + isCursorAutoChange);
            if (isCursorAutoChange) {
                // Rich Type 不变？
            } else {
                // 强设置失效，格式改为顺应上下文
                StrategyUtil.sStrongSet = false;
            }
            if (hasFocus() && mWRichEditorScrollView != null) {
                Log.d("www", "111");
                Set<RichType> currRichTypes = null;
                // todo 将上次的 RichTypes 存储到 ScrollView 中
                Set<RichType> prevRichTypes = mWRichEditorScrollView.getRichTypes();
                if (selEnd == 0) {
                    Log.d("www", "112");
                    // 光标处于0位置
                    if (editableLength == 0) {
                        // 光标处于0位置，并且没有任何内容
                        currRichTypes = new HashSet<>(4);
                        currRichTypes.addAll(prevRichTypes);
                        Log.d("www", "113");
                        TypeUtil.correctLineFormatGroupType(currRichTypes, mWrapperView);
                    } else {
                        // 光标处于0位置，但是EditText中包含内容
                        currRichTypes = getRichTypesByTextContextOrStrongSet(prevRichTypes, editableText, 0);
                    }
                } else if (0 < selEnd && selEnd < editableText.length()) {
                    // 格式与后向文字相同
                    currRichTypes = getRichTypesByTextContextOrStrongSet(prevRichTypes, editableText, selEnd);
                } else if (selEnd == editableText.length()) {
                    // 处于最后的位置，格式与前向文字相同
                    currRichTypes = getRichTypesByTextContextOrStrongSet(prevRichTypes, editableText, selEnd - 1);
                }
                // TODO
                OnRichTypeChangedListener typeChangedListener = mWRichEditorScrollView.getOnRichTypeChangedListener();
                if (typeChangedListener != null) {
                    // TODO api设计是否需要优化？考虑到光标移动的情况，应该不需要
                    Log.d("www", "888");
                    for (RichType richType : currRichTypes) {
                        Log.d("www", "----> richType : " + richType.name());
                    }
                    typeChangedListener.onRichTypeChanged(prevRichTypes, currRichTypes);
                }
            }
        }

        CursorUtil.markLastTextLength(editableLength);
        CursorUtil.markLastCursorLocation(selStart);
        CursorUtil.markLastWRichEditorContentDescription((String) getContentDescription());

    }


    private Set<RichType> getRichTypesByTextContextOrStrongSet(Set<RichType> prevRichTypes, Editable editableText, int start) {
        if (prevRichTypes == null) {
            prevRichTypes = new HashSet<>();
        }
        Set<RichType> currRichTypes = new HashSet<>(4);
        if (StrategyUtil.sStrongSet) {
            currRichTypes.addAll(prevRichTypes);
        } else {
            IRichSpan[] currRichSpans = editableText.getSpans(start, start + 1, IRichSpan.class);
            Log.d("www", "getSpans start : " + start + " currRichSpans " + currRichSpans.length);
            if (currRichSpans == null || currRichSpans.length == 0) {
                // 没有
            } else {
                for (IRichSpan richSpan : currRichSpans) {
                    currRichTypes.add(richSpan.getRichType());
                    Log.d("www", "richSpan.getRichType() : " + richSpan.getRichType().name());
                }
            }
        }
        // TODO
//                        currRichTypes.addAll(prevRichTypes);
        TypeUtil.correctLineFormatGroupType(currRichTypes, mWrapperView);
        return currRichTypes;
    }

    // 有效，在focus更改时，
    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        LogUtil.d(TAG, "WRichEditor onFocusChanged focused : " + focused + ", contentDescription : " + getContentDescription());
        StrategyUtil.sStrongSet = false;
        if (mOnEditorFocusChangedListener != null) {
            mOnEditorFocusChangedListener.onEditorFocusChanged(mWrapperView, focused);
        }
        if (focused && mWRichEditorScrollView != null) {
//            CommonUtil.showSoftKeyboard(getContext(), this);
            Set<RichType> currRichTypes = null;
            // todo 将上次的 RichTypes 存储到 ScrollView 中
            Set<RichType> prevRichTypes = mWRichEditorScrollView.getRichTypes();
            int selEnd = getSelectionEnd();

            Editable editableText = getEditableText();
            int editableLength = editableText.length();

            if (selEnd == 0) {
                Log.d("xxx", "112");
                // 光标处于0位置
                if (editableLength == 0) {
                    // 光标处于0位置，并且没有任何内容
                    currRichTypes = new HashSet<>(4);
                    currRichTypes.addAll(prevRichTypes);
                    TypeUtil.correctLineFormatGroupType(currRichTypes, mWrapperView);
                } else {
                    // 光标处于0位置，但是EditText中包含内容
                    currRichTypes = getRichTypesByTextContextOrStrongSet(prevRichTypes, editableText, 0);
                }
            } else if (0 < selEnd && selEnd < editableText.length()) {
                // 格式与后向文字相同
                currRichTypes = getRichTypesByTextContextOrStrongSet(prevRichTypes, editableText, selEnd);
            } else if (selEnd == editableText.length()) {
                // 处于最后的位置，格式与前向文字相同
                currRichTypes = getRichTypesByTextContextOrStrongSet(prevRichTypes, editableText, selEnd - 1);
            }
            // TODO
            OnRichTypeChangedListener typeChangedListener = mWRichEditorScrollView.getOnRichTypeChangedListener();
            if (typeChangedListener != null) {
                // TODO api设计是否需要优化？考虑到光标移动的情况，应该不需要
                if (currRichTypes == null) {
                    currRichTypes = new HashSet<>();
                }
                for (RichType richType : currRichTypes) {
                    Log.d("mmm", "----> richType : " + richType.name());
                }
                typeChangedListener.onRichTypeChanged(prevRichTypes, currRichTypes);
            }
            TypeUtil.removeAllResourceTypeFocus(mWRichEditorScrollView);
        }
    }

    // 响应顺序
//    : onKeyDown KEYCODE_ENTER
//    : onKeyUp KEYCODE_ENTER
//    : WRichEditor onSelectionChanged selStart : 4 selEnd : 4 hasFocus : true
//    : onSelectionChanged isCursorAutoChange : true
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            Log.d("www", "onKeyUp KEYCODE_ENTER");
            // 响应 keycode，如果有 headline 类型的 RichType
            if (mWRichEditorScrollView != null) {
                Set<RichType> richTypes = mWRichEditorScrollView.getRichTypes();
                if (richTypes != null) {
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
                    // 删除从cursorStart到结尾的所有文字
                    int cursorStart = getSelectionStart();
                    int cursorEnd = getSelectionEnd();
                    if (cursorStart >= 0 && cursorEnd >= 0) {
                        Editable editable = getEditableText();
                        ArrayList<SpanPart> spanPartsOutput = new ArrayList<>(32);

                        String textWithoutFormatItemStartLeft = SpanUtil.getSpannableStringInclusiveExclusive(editable, 0, cursorStart, spanPartsOutput);
                        SpanUtil.setSpannableInclusiveExclusive(this, textWithoutFormatItemStartLeft, spanPartsOutput, 0);

                        spanPartsOutput.clear();
                        String textWithoutFormatItemEndRight = SpanUtil.getSpannableStringInclusiveExclusive(editable, cursorEnd, editable.length(), spanPartsOutput);
                        WRichEditorWrapperView richEditorWrapperView = mWRichEditorScrollView.insertAWRichEditorWrapperWithRichType(mWrapperView, RichType.LIST_UNORDERED, true);
                        SpanUtil.setSpannableInclusiveExclusive(richEditorWrapperView.getWRichEditor(), textWithoutFormatItemEndRight, spanPartsOutput, -cursorEnd);
                    } else {
                        mWRichEditorScrollView.insertAWRichEditorWrapperWithRichType(mWrapperView, RichType.LIST_UNORDERED, true);
                    }
                    return true;
                } else if (richType == RichType.LIST_ORDERED) {
                    // 如果是有序列表
                    // 删除从cursorStart到结尾的所有文字
                    int cursorStart = getSelectionStart();
                    int cursorEnd = getSelectionEnd();
                    if (cursorStart >= 0 && cursorEnd >= 0) {
                        Editable editable = getEditableText();
                        ArrayList<SpanPart> spanPartsOutput = new ArrayList<>(32);

                        String textWithoutFormatItemStartLeft = SpanUtil.getSpannableStringInclusiveExclusive(editable, 0, cursorStart, spanPartsOutput);
                        SpanUtil.setSpannableInclusiveExclusive(this, textWithoutFormatItemStartLeft, spanPartsOutput, 0);

                        spanPartsOutput.clear();
                        String textWithoutFormatItemEndRight = SpanUtil.getSpannableStringInclusiveExclusive(editable, cursorEnd, editable.length(), spanPartsOutput);
                        WRichEditorWrapperView richEditorWrapperView = mWRichEditorScrollView.insertAWRichEditorWrapperWithRichType(mWrapperView, RichType.LIST_ORDERED, true);
                        SpanUtil.setSpannableInclusiveExclusive(richEditorWrapperView.getWRichEditor(), textWithoutFormatItemEndRight, spanPartsOutput, -cursorEnd);
                    } else {
                        mWRichEditorScrollView.insertAWRichEditorWrapperWithRichType(mWrapperView, RichType.LIST_ORDERED, true);
                    }
                    OrderListUtil.updateOrderListNumbersAfterViewsChanged(mWRichEditorScrollView.getContainerView());
                    return true;
                }
            }

        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DEL) {
            int selectionStart = getSelectionStart();
            int selectionEnd = getSelectionEnd();
            if (selectionStart == selectionEnd && selectionStart == 0) {
                if (mWRichEditorScrollView != null && mWrapperView != null) {
                    int index = mWRichEditorScrollView.getCellViewIndex(mWrapperView);
                    if (index > 0) {
                        // 查看上一个是不是WRichEditor，
                        IRichCellView iRichCellView = mWRichEditorScrollView.getCellViewByIndex(index - 1);

                        if (iRichCellView != null) {
                            RichType richType = iRichCellView.getRichType();
                            Editable editable = getEditableText();
                            int richTypeGroup = richType.getGroup();
                            if (richTypeGroup == RichTypeConstants.GROUP_CHAR_FORMAT) {
                                // 1. 如果是，则将光标调整到上一个WRichEditor的最后，同时将此WRichEditor删除
                                // 将此WRichEditor remove
                                ((WRichEditorWrapperView) iRichCellView).addExtraEditable(editable);
                                ((WRichEditorWrapperView) iRichCellView).requestFocusAndPutCursorToTail();

                                ViewParent parent = mWrapperView.getParent();
                                if (parent != null && parent instanceof ViewGroup) {
                                    setText("");
                                    Log.d("clearfocus", "clearFocus() 1");
                                    clearFocus();
                                    ((ViewGroup) parent).removeView(mWrapperView);
                                }
                                Log.d("wangwang", "delete 1");
                            } else if (richTypeGroup == RichTypeConstants.GROUP_RESOURCE) {
                                // 图片、音频、视频、横线、云盘
                                // 1. 此view中的text是否为空，
                                //  1.1 如果为空，判断此view的下一个view是否需要needAddEditor，
                                if (editable == null || editable.length() == 0) {
//                                    boolean needAddWRichEditorIfDeleted = mWRichEditorScrollView.needAddWRichEditorForDeleteAction(index);
                                    ViewParent parent = mWrapperView.getParent();
                                    if (parent != null && parent instanceof ViewGroup) {
                                        setText("");
                                        Log.d("clearfocus", "clearFocus() 2");
                                        clearFocus();
                                        ((ViewGroup) parent).removeView(mWrapperView);
                                    }
//                                    if (needAddWRichEditorIfDeleted) {
                                        // 依然删除，将焦点至于上面的resource上
                                        // 判断最后一个view是否为NONE
                                        mWRichEditorScrollView.addNoneTypeTailOptionally();
//                                    } else {
                                        // 删除，并将焦点至于上面的resource上

//                                    }
                                    removeFocusToResourceTypeAbove(index - 1);
                                } else {
                                    //  1.2 如果不为空，则将焦点至于上面的resource上
                                    removeFocusToResourceTypeAbove(index - 1);
                                }
                            } else if (richTypeGroup == RichTypeConstants.GROUP_LINE_FORMAT) {
                                if (editable == null || editable.length() == 0) {
//                                    boolean needAddWRichEditorIfDeleted = mWRichEditorScrollView.needAddWRichEditorForDeleteAction(index);
                                    ViewParent parent = mWrapperView.getParent();
                                    if (parent != null && parent instanceof ViewGroup) {
                                        setText("");
//                                        clearFocus();
                                        ((ViewGroup) parent).removeView(mWrapperView);
                                    }
//                                    if (needAddWRichEditorIfDeleted) {
                                        // 不删除，只将焦点至于上面的LineFormat上
//                                    } else {
                                        // 删除，并将焦点至于上面的LineFormat上
//                                    }
                                    mWRichEditorScrollView.addNoneTypeTailOptionally();
                                    removeFocusToLineFormatTypeAbove(index - 1);
                                } else {
                                    //  1.2 如果不为空，则将焦点至于上面的LineFormat上
                                    removeFocusToLineFormatTypeAbove(index - 1);
                                }
                            }
                        }
                    }
                }
            }
        } else if (keyCode == KeyEvent.KEYCODE_ENTER) {
            Log.d("www", "onKeyDown KEYCODE_ENTER");
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

    private void removeFocusToResourceTypeAbove(int targetIndex) {
        if (mWRichEditorScrollView != null && mWRichEditorScrollView.getContainerView() != null) {
            CommonUtil.hideSoftKeyboard(getContext(), this);
            Log.d("clearfocus", "clearFocus() 4");
            clearFocus();
            TypeUtil.selectOnlyOneResourceType(mWRichEditorScrollView, targetIndex);
        }
    }

    private void removeFocusToLineFormatTypeAbove(int targetIndex) {
        if (mWRichEditorScrollView != null) {
            IRichCellView cellView = mWRichEditorScrollView.getCellViewByIndex(targetIndex);
            if (cellView != null) {
                cellView.setSelectMode(true);
            }
        }
    }

    public void setWRichEditorWrapperView(WRichEditorWrapperView wrapperView) {
        mWrapperView = wrapperView;
    }


    public void setWRichEditorScrollView(WRichEditorScrollView wRichEditorScrollView) {
        mWRichEditorScrollView = wRichEditorScrollView;
    }

    public void setEditorFocusChangedListener(OnEditorFocusChangedListener listener) {
        mOnEditorFocusChangedListener = listener;
    }

    public boolean getSelectMode() {
        return false;
    }

    // TODO 外部主动更改了字体样式，不涉及数据插入
    public void updateTextByRichTypeChanged(RichType richType, boolean open, Object extra) {
        LogUtil.d(TAG, "updateTextByRichTypeChanged richType : " + richType.name() + ", open : " + open + ", extra : " + extra);
        int selectionStart = getSelectionStart();
        int selectionEnd = getSelectionEnd();
        if (selectionStart < 0 || selectionEnd < 0) {
            return;
        }
        if (selectionStart == selectionEnd) {
            // TODO 后面输入的字体将按照对应的设定字体进行
            // 当前没有选中字体
            if (richType == RichType.HEADLINE) {
                updateSpanUI(richType, open, extra, selectionStart, selectionEnd, mWRichEditorScrollView.getRichTypes());
            }
            return;
        } else {
            if (mWRichEditorScrollView == null) {
                return;
            }
            // 将选中的部分进行更新，同时更新此View对应的
            updateSpanUI(richType, open, extra, selectionStart, selectionEnd, mWRichEditorScrollView.getRichTypes());
        }
    }

    // TODO 插入数据时，应该修改data
    private void updateSpanUI(RichType richType, boolean open, Object extra, int start, int end, Set<RichType> richTypes) {
        if (start < 0 || end < 0) {
            return;
        }
        SpanUtil.setSpan(richType, open, extra, getEditableText(), richTypes, start, end);
    }

    // SpannableStringBuilder
    public void addExtraEditable(Editable extraEditable) {
        if (extraEditable != null) {
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
                    originalEditable.setSpan(part.getRichSpan(), originalLength + part.getStart(), originalLength + part.getEnd(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                }
            }
        }
    }

    public void requestFocusAndPutCursorToTail() {
        requestFocus();
        setSelection(getEditableText().toString().length());
    }

    public void setTextChangeValid(boolean textChangeValid) {
        mTextChangeValid = textChangeValid;
    }

    public boolean getTextChangeValid() {
        return mTextChangeValid;
    }

}