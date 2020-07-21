package cn.carbs.wricheditor.library;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.text.Editable;
import android.text.Spanned;
import android.util.AttributeSet;
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
import cn.carbs.wricheditor.library.configures.RichEditorConfig;
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
import cn.carbs.wricheditor.library.utils.TypeUtil;

@SuppressLint("AppCompatCustomView")
public class WEditText extends EditText {

    public static final String TAG = WEditText.class.getSimpleName();

    private WRichEditor mWRichEditor;

    private OnEditorFocusChangedListener mOnEditorFocusChangedListener;

    private WEditTextWrapperView mWrapperView;

    private boolean mTextChangeValid = true;

    public WEditText(Context context) {
        super(context);
    }

    public WEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // 1. onSelectionChanged 应该适合选中一段文字后改变字体
    // 2. 动态添加文字时（粘贴/一字一字的输入）onTextChanged 感觉更合适，此时的调用顺序为 onTextChanged onSelectionChanged，
    //    因为 onSelectionChanged 对应的是光标的位置，当一次输入多个字符时，onSelectionChanged 并不能体现出文字的更改，因为 selStart : n selEnd : n
    //    相反 onTextChanged 的回调返回的数据为 start: 起始位置，如6， lengthBefore: 0, lengthAfter: 新增字符串长度
    //    当选中其中两个文字，并将其替换为3个文字时， onTextChanged 的回调时 ： start : 2 lengthBefore : 2 lengthAfter : 3
    // 3. setSpan不会响应 onTextChanged
    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        LogUtil.d(TAG, "onTextChanged hint : " + getHint() + "  mTextChangeValid : " + mTextChangeValid);
        if (!mTextChangeValid) {
            return;
        }

        if (mWRichEditor == null) {
            return;
        }
        // 回调返回的 text 和 getEditableText() 不一定一致，SpannableStringBuilder中的textWatcher的内置问题
        if (getParent() != null && getEditableText().toString().equals(text.toString())) {
            SpanUtil.setSpan(mWRichEditor.mRichTypes, text, getEditableText(), start, start + lengthAfter);
        }
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        // 打字时，每次回调此函数都会 selStart == selEnd
        Editable editableText = getEditableText();
        int editableLength = editableText.length();
        if (selStart == selEnd) {
            SpanUtil.getSpanTypesForCursorLocation(editableText, selEnd);
            boolean isCursorAutoChange = CursorUtil.isCursorChangedAutomaticallyByTextChange(editableLength, selStart, (String) getContentDescription());
            if (!isCursorAutoChange) {
                // 强设置失效，格式改为顺应上下文
                RichEditorConfig.sStrongSet = false;
            }
            if (hasFocus() && mWRichEditor != null) {
                Set<RichType> currRichTypes = null;
                // 将上次的 RichTypes 存储到 ScrollView 中
                Set<RichType> prevRichTypes = mWRichEditor.getRichTypes();
                if (selEnd == 0) {
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
                OnRichTypeChangedListener typeChangedListener = mWRichEditor.getOnRichTypeChangedListener();
                if (typeChangedListener != null) {
                    typeChangedListener.onRichTypeChanged(prevRichTypes, currRichTypes);
                }
                prevRichTypes.clear();
                prevRichTypes.addAll(currRichTypes);
            }
        }

        CursorUtil.markLastTextLength(editableLength);
        CursorUtil.markLastCursorLocation(selStart);
        CursorUtil.markLastWRichEditorContentDescription((String) getContentDescription());

    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        LogUtil.d(TAG, "WRichEditor onFocusChanged focused : " + focused + ", contentDescription : " + getContentDescription());
        RichEditorConfig.sStrongSet = false;
        if (mOnEditorFocusChangedListener != null) {
            mOnEditorFocusChangedListener.onEditorFocusChanged(mWrapperView, focused);
        }
        if (focused && mWRichEditor != null) {
            Set<RichType> currRichTypes = null;
            // 将上次的 RichTypes 存储到 ScrollView 中
            Set<RichType> prevRichTypes = mWRichEditor.getRichTypes();
            int selEnd = getSelectionEnd();

            Editable editableText = getEditableText();
            int editableLength = editableText.length();

            if (selEnd == 0) {
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
            OnRichTypeChangedListener typeChangedListener = mWRichEditor.getOnRichTypeChangedListener();
            if (typeChangedListener != null) {
                if (currRichTypes == null) {
                    currRichTypes = new HashSet<>();
                }
                typeChangedListener.onRichTypeChanged(prevRichTypes, currRichTypes);
            }
            TypeUtil.removeAllResourceTypeFocus(mWRichEditor);
        }
    }

    // 响应顺序
    // onKeyDown KEYCODE_ENTER
    // onKeyUp KEYCODE_ENTER
    // WRichEditor onSelectionChanged
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            // 响应 keycode，如果有 headline 类型的 RichType
            if (mWRichEditor != null) {
                Set<RichType> richTypes = mWRichEditor.getRichTypes();
                if (richTypes != null) {
                    boolean changed = TypeUtil.removeCertainRichType(richTypes, RichType.HEADLINE);
                    if (changed) {
                        OnRichTypeChangedListener typeChangedListener = mWRichEditor.getOnRichTypeChangedListener();
                        if (typeChangedListener != null) {
                            typeChangedListener.onRichTypeChanged(TypeUtil.assembleRichTypes(richTypes, RichType.HEADLINE), richTypes);
                        }
                    }
                }
            }
            if (mWRichEditor != null && mWrapperView != null) {
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
                        WEditTextWrapperView richEditorWrapperView = mWRichEditor.insertAWRichEditorWrapperWithRichType(mWrapperView, RichType.LIST_UNORDERED, true);
                        SpanUtil.setSpannableInclusiveExclusive(richEditorWrapperView.getWEditText(), textWithoutFormatItemEndRight, spanPartsOutput, -cursorEnd);
                    } else {
                        mWRichEditor.insertAWRichEditorWrapperWithRichType(mWrapperView, RichType.LIST_UNORDERED, true);
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
                        WEditTextWrapperView richEditorWrapperView = mWRichEditor.insertAWRichEditorWrapperWithRichType(mWrapperView, RichType.LIST_ORDERED, true);
                        SpanUtil.setSpannableInclusiveExclusive(richEditorWrapperView.getWEditText(), textWithoutFormatItemEndRight, spanPartsOutput, -cursorEnd);
                    } else {
                        mWRichEditor.insertAWRichEditorWrapperWithRichType(mWrapperView, RichType.LIST_ORDERED, true);
                    }
                    OrderListUtil.updateOrderListNumbersAfterViewsChanged(mWRichEditor.getContainerView());
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
                if (mWRichEditor != null && mWrapperView != null) {
                    int index = mWRichEditor.getCellViewIndex(mWrapperView);
                    if (index > 0) {
                        // 查看上一个是不是WRichEditor，
                        IRichCellView iRichCellView = mWRichEditor.getCellViewByIndex(index - 1);

                        if (iRichCellView != null) {
                            RichType richType = iRichCellView.getRichType();
                            Editable editable = getEditableText();
                            int richTypeGroup = richType.getGroup();
                            if (richTypeGroup == RichTypeConstants.GROUP_CHAR_FORMAT) {
                                // 1. 如果是，则将光标调整到上一个WRichEditor的最后，同时将此WRichEditor删除
                                // 将此WRichEditor remove
                                ((WEditTextWrapperView) iRichCellView).addExtraEditable(editable);
                                ((WEditTextWrapperView) iRichCellView).requestFocusAndPutCursorToTail();

                                ViewParent parent = mWrapperView.getParent();
                                if (parent != null && parent instanceof ViewGroup) {
                                    setText("");
                                    clearFocus();
                                    ((ViewGroup) parent).removeView(mWrapperView);
                                }
                            } else if (richTypeGroup == RichTypeConstants.GROUP_RESOURCE) {
                                // 图片、音频、视频、横线、云盘
                                // 1. 此view中的text是否为空，
                                //  1.1 如果为空，判断此view的下一个view是否需要needAddEditor，
                                if (editable == null || editable.length() == 0) {
                                    ViewParent parent = mWrapperView.getParent();
                                    if (parent != null && parent instanceof ViewGroup) {
                                        setText("");
                                        clearFocus();
                                        ((ViewGroup) parent).removeView(mWrapperView);
                                    }
                                    // 依然删除，将焦点至于上面的resource上
                                    // 判断最后一个view是否为NONE
                                    mWRichEditor.addNoneTypeTailOptionally();
                                    removeFocusToResourceTypeAbove(index - 1);
                                } else {
                                    //  1.2 如果不为空，则将焦点至于上面的resource上
                                    removeFocusToResourceTypeAbove(index - 1);
                                }
                            } else if (richTypeGroup == RichTypeConstants.GROUP_LINE_FORMAT) {
                                if (editable == null || editable.length() == 0) {
                                    ViewParent parent = mWrapperView.getParent();
                                    if (parent != null && parent instanceof ViewGroup) {
                                        setText("");
                                        ((ViewGroup) parent).removeView(mWrapperView);
                                    }
                                    mWRichEditor.addNoneTypeTailOptionally();
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
            if (mWRichEditor != null && mWrapperView != null) {
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

    public void updateTextByRichTypeChanged(RichType richType, boolean open, Object extra) {
        int selectionStart = getSelectionStart();
        int selectionEnd = getSelectionEnd();
        if (selectionStart < 0 || selectionEnd < 0) {
            return;
        }
        if (selectionStart == selectionEnd) {
            // 当前没有选中字体
            if (richType == RichType.HEADLINE) {
                updateSpanUI(richType, open, extra, selectionStart, selectionEnd, mWRichEditor.getRichTypes());
            }
            return;
        } else {
            if (mWRichEditor == null) {
                return;
            }
            // 将选中的部分进行更新，同时更新此View对应的
            updateSpanUI(richType, open, extra, selectionStart, selectionEnd, mWRichEditor.getRichTypes());
        }
    }

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

    public void setWRichEditorWrapperView(WEditTextWrapperView wrapperView) {
        mWrapperView = wrapperView;
    }

    public void setWRichEditorScrollView(WRichEditor wRichEditor) {
        mWRichEditor = wRichEditor;
    }

    public void setEditorFocusChangedListener(OnEditorFocusChangedListener listener) {
        mOnEditorFocusChangedListener = listener;
    }

    public boolean getSelectMode() {
        return false;
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

    private Set<RichType> getRichTypesByTextContextOrStrongSet(Set<RichType> prevRichTypes, Editable editableText, int start) {
        if (prevRichTypes == null) {
            prevRichTypes = new HashSet<>();
        }
        Set<RichType> currRichTypes = new HashSet<>(4);
        if (RichEditorConfig.sStrongSet) {
            currRichTypes.addAll(prevRichTypes);
        } else {
            IRichSpan[] currRichSpans = editableText.getSpans(start, start + 1, IRichSpan.class);
            if (currRichSpans == null || currRichSpans.length == 0) {
                // 没有
            } else {
                for (IRichSpan richSpan : currRichSpans) {
                    currRichTypes.add(richSpan.getRichType());
                }
            }
        }
        TypeUtil.correctLineFormatGroupType(currRichTypes, mWrapperView);
        return currRichTypes;
    }

    private void removeFocusToResourceTypeAbove(int targetIndex) {
        if (mWRichEditor != null && mWRichEditor.getContainerView() != null) {
            CommonUtil.hideSoftKeyboard(getContext(), this);
            clearFocus();
            TypeUtil.selectOnlyOneResourceType(mWRichEditor, targetIndex);
        }
    }

    private void removeFocusToLineFormatTypeAbove(int targetIndex) {
        if (mWRichEditor != null) {
            IRichCellView cellView = mWRichEditor.getCellViewByIndex(targetIndex);
            if (cellView != null) {
                cellView.setSelectMode(true);
            }
        }
    }

    private void updateSpanUI(RichType richType, boolean open, Object extra, int start, int end, Set<RichType> richTypes) {
        if (start < 0 || end < 0) {
            return;
        }
        SpanUtil.setSpan(richType, open, extra, getEditableText(), richTypes, start, end);
    }
}