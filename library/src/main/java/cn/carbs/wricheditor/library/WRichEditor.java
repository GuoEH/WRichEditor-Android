package cn.carbs.wricheditor.library;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.carbs.wricheditor.library.callbacks.OnEditorFocusChangedListener;
import cn.carbs.wricheditor.library.callbacks.OnRichTypeChangedListener;
import cn.carbs.wricheditor.library.configures.RichEditorConfig;
import cn.carbs.wricheditor.library.constants.CharConstant;
import cn.carbs.wricheditor.library.interfaces.IRichCellView;
import cn.carbs.wricheditor.library.models.SpanPart;
import cn.carbs.wricheditor.library.models.SplitPart;
import cn.carbs.wricheditor.library.types.RichType;
import cn.carbs.wricheditor.library.utils.CommonUtil;
import cn.carbs.wricheditor.library.utils.DebugUtil;
import cn.carbs.wricheditor.library.utils.LogUtil;
import cn.carbs.wricheditor.library.utils.OrderListUtil;
import cn.carbs.wricheditor.library.utils.SpanUtil;
import cn.carbs.wricheditor.library.views.RichImageView;
import cn.carbs.wricheditor.library.views.RichLineView;
import cn.carbs.wricheditor.library.views.RichNetDiskView;

/**
 * 主视图，继承自ScrollView，富文本通过向其中不断添加子View实现
 */
public class WRichEditor extends ScrollView implements OnEditorFocusChangedListener {

    public static final String TAG = WRichEditor.class.getSimpleName();

    public Set<RichType> mRichTypes = new HashSet<>();

    private LinearLayout mLinearLayout;

    private OnRichTypeChangedListener mOnRichTypeChangedListener;

    private IRichCellView mLastFocusedRichCellView;

    private ViewTreeObserver.OnGlobalLayoutListener mOnGlobalLayoutListener;

    public WRichEditor(Context context) {
        super(context);
        init(context);
    }

    public WRichEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        initAttrs(attrs);
    }

    public WRichEditor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
        initAttrs(attrs);
    }

    private void init(Context context) {
        setFillViewport(true);
        inflate(context, R.layout.wricheditor_layout_main_view, this);
        mLinearLayout = findViewById(R.id.wricheditor_main_view_container);
        RichEditorConfig.sHeadlineTextSize = CommonUtil.dp2px(getContext(), 18);
        mLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IRichCellView cellView = addNoneTypeTailOptionally();
                if (cellView instanceof WEditTextWrapperView) {
                    WEditText wEditText = ((WEditTextWrapperView) cellView).getWEditText();
                    if (wEditText != null) {
                        wEditText.requestFocus();
                        // 调起软键盘
                        CommonUtil.showSoftKeyboard(getContext(), wEditText);
                    }
                }
            }
        });
        initInputKeyboardListener();
    }

    private void initAttrs(AttributeSet attrs) {
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.WRichEditor);
        RichEditorConfig.sLinkColor = array.getColor(R.styleable.WRichEditor_linkColor, RichEditorConfig.sLinkColor);
        RichEditorConfig.sLinkUnderline = array.getBoolean(R.styleable.WRichEditor_linkUnderline, RichEditorConfig.sLinkUnderline);
        RichEditorConfig.sHeadlineTextSize = array.getDimensionPixelSize(R.styleable.WRichEditor_headlineTextSize, RichEditorConfig.sHeadlineTextSize);
        RichEditorConfig.sEditorColor = array.getColor(R.styleable.WRichEditor_editorColor, RichEditorConfig.sEditorColor);
        RichEditorConfig.sScrollBottomDeltaY = array.getDimensionPixelSize(R.styleable.WRichEditor_scrollBottomDeltaY, RichEditorConfig.sScrollBottomDeltaY);
        array.recycle();
    }

    /**
     * 如果 index == -1，则加到队尾
     *
     * @param richCell
     * @param layoutParams
     * @param index
     */
    public void addRichCell(IRichCellView richCell, LinearLayout.LayoutParams layoutParams, int index) {
        LogUtil.d(TAG, "addRichCell");
        if (mLinearLayout == null || richCell.getView() == null) {
            return;
        }
        LinearLayout.LayoutParams lp;
        if (layoutParams != null) {
            lp = layoutParams;
        } else {
            lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        }
        // 子View中添加此ScrollView的引用，方便操作其它数据
        richCell.setWRichEditorScrollView(this);
        richCell.setEditorFocusChangedListener(this);
        if (index == -1) {
            mLinearLayout.addView(richCell.getView(), lp);
        } else {
            mLinearLayout.addView(richCell.getView(), index, lp);
        }
    }

    /**
     * @param richType
     * @param open
     * @param extra    存放 link 模式的 url
     */
    public void updateTextByRichTypeChanged(RichType richType, boolean open, Object extra) {
        LogUtil.d(TAG, "updateTextByRichTypeChanged richType : " + richType.name() + ", open : " + open + ", extra : " + extra);
        RichEditorConfig.sStrongSet = true;
        // 1. 找到焦点所在的EditView
        int[] focusedRichEditorWrapperViewIndex = new int[1];
        WEditTextWrapperView focusedWEditTextWrapperView = findCurrentFocusedRichEditorWrapperView(focusedRichEditorWrapperViewIndex);
        if (focusedWEditTextWrapperView == null) {
            return;
        }
        WEditText focusedWEditText = focusedWEditTextWrapperView.getWEditText();
        if (focusedWEditText == null) {
            return;
        }
        // 2. 交给某个单元Cell去更新
        if (richType == RichType.QUOTE) {
            LogUtil.d(TAG, "richType == RichType.QUOTE");
            // 如果是 "QUOTE" 类型，则：
            Editable editable = focusedWEditText.getEditableText();
            String editableStr = editable.toString();
            if (open) {
                LogUtil.d(TAG, "QUOTE open");
                if (focusedWEditTextWrapperView.getRichType() == RichType.QUOTE) {
                    return;
                }
                // 如果是 "开启" QUOTE 动作
                if (!editableStr.contains(CharConstant.ENTER_STR)) {
                    // 1. 判断当前RichEditor中有几个换行符，如果有0个，则不"分裂"，直接将这个Editor的左侧drawable做变换
                    focusedWEditTextWrapperView.toggleQuoteMode(true, false);
                    if (needAddWRichEditor(focusedRichEditorWrapperViewIndex[0])) {
                        insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 1, RichType.NONE, false);
                    }
                } else {
                    // 2. 如果当前RichEditor中有一个或者多个换行符，则截取从换行符后的text，添加到新生成的Editor中
                    // 根据select的起点和终点，判断所处的行，并将这些行作为一个引用
                    int cursorStart = focusedWEditText.getSelectionStart();
                    int cursorEnd = focusedWEditText.getSelectionEnd();

                    int quoteStart = 0;
                    int quoteEnd = 0;

                    // 没有选中任何文字：
                    String cursorLeftStr = editableStr.substring(0, cursorStart);
                    String cursorRightStr = editableStr.substring(cursorEnd);
                    // 找到从光标位置到前面的换行符（没有换行符则到位置0）
                    int lastIndexOfEnterAmongLeftStr = cursorLeftStr.lastIndexOf(CharConstant.ENTER_CHAR);
                    // 这几个位置需要测试一下
                    if (lastIndexOfEnterAmongLeftStr == -1) {
                        quoteStart = 0;
                    } else {
                        quoteStart = lastIndexOfEnterAmongLeftStr + 1;
                    }
                    // 找到从光标位置到后面的换行符（没有换行符则到位置最后）
                    int firstIndexOfEnterAmongRightStr = cursorRightStr.indexOf(CharConstant.ENTER_CHAR);
                    if (firstIndexOfEnterAmongRightStr == -1) {
                        quoteEnd = editableStr.length();
                    } else {
                        quoteEnd = cursorEnd + firstIndexOfEnterAmongRightStr + 1;
                    }

                    int currentEditableLength = editableStr.length();

                    if (quoteStart == currentEditableLength && quoteEnd == currentEditableLength) {
                        LogUtil.d(TAG, "QUOTE open 0");
                        // 肯定会以 Enter 键结尾
                        Editable editableText = focusedWEditText.getEditableText();
                        List<SpanPart> spanPartsOutput0 = new ArrayList<>(32);
                        String textWithoutFormat = SpanUtil.getSpannableStringInclusiveExclusive(editableText, 0, currentEditableLength - 1, spanPartsOutput0);
                        SpanUtil.setSpannableInclusiveExclusive(focusedWEditText, textWithoutFormat, spanPartsOutput0, 0);
                        insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 1, RichType.QUOTE, true);
                    } else if (quoteStart == 0) {
                        // 四种情况
                        if (quoteEnd == currentEditableLength) {
                            LogUtil.d(TAG, "QUOTE open 11");
                            // 不选中时
                            // A   B   C   D   E | F   G   '/n'
                            // 选中时
                            // A   B   C   D   E | F   G   '/n'
                            // H   I   J   K   L | M   N   ('/n')
                            // 如果光标不在队尾
                            Editable editableText = focusedWEditText.getEditableText();

                            if (editableStr.endsWith(CharConstant.ENTER_STR)) {
                                LogUtil.d(TAG, "QUOTE open 112");
                                List<SpanPart> spanPartsOutput = new ArrayList<>(32);
                                String textWithoutFormat = SpanUtil.getSpannableStringInclusiveExclusive(editableText, 0, quoteEnd - 1, spanPartsOutput);
                                SpanUtil.setSpannableInclusiveExclusive(focusedWEditText, textWithoutFormat, spanPartsOutput, 0);
                                focusedWEditTextWrapperView.toggleQuoteMode(true, false);
                                insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 1, RichType.NONE, false);
                            } else {
                                LogUtil.d(TAG, "QUOTE open 113");
                                focusedWEditTextWrapperView.toggleQuoteMode(true, false);
                                if (needAddWRichEditor(focusedRichEditorWrapperViewIndex[0])) {
                                    insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 1, RichType.NONE, false);
                                }
                            }
                        } else {
                            LogUtil.d(TAG, "QUOTE open 12");
                            // 不选中
                            // A   B   C   D   E | F   G   '/n'
                            // H   I   J   K   L   M   N   ('/n')
                            // 选中
                            // A   B   C | D   E | F   G   '/n'
                            // H   I   J   K   L   M   N   ('/n')
                            // 从起始0到quoteEnd，作为 focusedWRichEditor 的值，并置为quote状态
                            Editable editableText = focusedWEditText.getEditableText();
                            List<SpanPart> spanPartsOutput0 = new ArrayList<>(32);
                            List<SpanPart> spanPartsOutput1 = new ArrayList<>(32);
                            // quoteEnd应该要减一，因为末尾不应该留着 Enter
                            String textWithoutFormat0 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, 0, quoteEnd - 1, spanPartsOutput0);
                            String textWithoutFormat1 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, quoteEnd, editableText.length(), spanPartsOutput1);

                            SpanUtil.setSpannableInclusiveExclusive(focusedWEditText, textWithoutFormat0, spanPartsOutput0, 0);
                            focusedWEditTextWrapperView.toggleQuoteMode(true, false);

                            // 添加一个WRichEditorWrapperView
                            WEditTextWrapperView insertedWrapperView = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 1, RichType.NONE, false);
                            SpanUtil.setSpannableInclusiveExclusive(insertedWrapperView.getWEditText(), textWithoutFormat1, spanPartsOutput1, -quoteEnd);
                        }
                    } else {
                        if (quoteEnd == editableStr.length()) {
                            LogUtil.d(TAG, "QUOTE open 21");
                            // 不选中
                            // A   B   C   D   E   F   G   '/n'
                            // H   I   J | K   L   M   N   ('/n')

                            // 选中
                            // A   B   C   D   E   F   G   '/n'
                            // H   I   J | K   L   M   N   '/n'
                            // O   P   Q   R   S | T   U   ('/n')
                            // 光标前面有Enter，后面没有

                            Editable editableText = focusedWEditText.getEditableText();
                            List<SpanPart> spanPartsOutput0 = new ArrayList<>(32);
                            List<SpanPart> spanPartsOutput1 = new ArrayList<>(32);
                            String textWithoutFormat0 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, 0, quoteStart - 1, spanPartsOutput0);
                            SpanUtil.setSpannableInclusiveExclusive(focusedWEditText, textWithoutFormat0, spanPartsOutput0, 0);

                            if (editableStr.endsWith(CharConstant.ENTER_STR)) {
                                LogUtil.d(TAG, "QUOTE open 211");
                                String textWithoutFormat1 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, quoteStart, editableText.length() - 1, spanPartsOutput1);
                                // 添加一个WRichEditorWrapperView
                                WEditTextWrapperView insertedWrapperView = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 1, RichType.QUOTE, true);
                                SpanUtil.setSpannableInclusiveExclusive(insertedWrapperView.getWEditText(), textWithoutFormat1, spanPartsOutput1, -quoteStart);
                                if (needAddWRichEditor(focusedRichEditorWrapperViewIndex[0] + 1)) {
                                    insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 2, RichType.NONE, false);
                                }
                            } else {
                                LogUtil.d(TAG, "QUOTE open 212");
                                String textWithoutFormat1 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, quoteStart, editableText.length(), spanPartsOutput1);
                                // 添加一个WRichEditorWrapperView
                                WEditTextWrapperView insertedWrapperView = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 1, RichType.QUOTE, true);
                                SpanUtil.setSpannableInclusiveExclusive(insertedWrapperView.getWEditText(), textWithoutFormat1, spanPartsOutput1, -quoteStart);
                                if (needAddWRichEditor(focusedRichEditorWrapperViewIndex[0] + 1)) {
                                    insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 2, RichType.NONE, false);
                                }
                            }
                        } else {
                            LogUtil.d(TAG, "QUOTE open 22");
                            // 不选中
                            // A   B   C   D   E   F   G   '/n'
                            // H   I   J   K | L   M   N   '/n'
                            // O   P   Q   R   S   T   U   ('/n')
                            // 选中
                            // A   B   C   D   E   F   G   '/n'
                            // H   I   J   K | L   M   N   '/n'
                            // H   I   J   K   L   M | N   '/n'
                            // O   P   Q   R   S   T   U   ('/n')
                            Editable editableText = focusedWEditText.getEditableText();
                            List<SpanPart> spanPartsOutput0 = new ArrayList<>(32);
                            List<SpanPart> spanPartsOutput1 = new ArrayList<>(32);
                            List<SpanPart> spanPartsOutput2 = new ArrayList<>(32);
                            String textWithoutFormat0 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, 0, quoteStart - 1, spanPartsOutput0);
                            String textWithoutFormat1 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, quoteStart, quoteEnd - 1, spanPartsOutput1);
                            String textWithoutFormat2 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, quoteEnd, editableText.length(), spanPartsOutput2);

                            SpanUtil.setSpannableInclusiveExclusive(focusedWEditText, textWithoutFormat0, spanPartsOutput0, 0);

                            WEditTextWrapperView insertedWrapperView1 = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 1, RichType.QUOTE, true);
                            SpanUtil.setSpannableInclusiveExclusive(insertedWrapperView1.getWEditText(), textWithoutFormat1, spanPartsOutput1, -quoteStart);

                            WEditTextWrapperView insertedWrapperView2 = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 2, RichType.NONE, false);
                            SpanUtil.setSpannableInclusiveExclusive(insertedWrapperView2.getWEditText(), textWithoutFormat2, spanPartsOutput2, -quoteEnd);
                        }
                    }
                }
            } else {
                // 如果是 "关闭" QUOTE 动作
                LogUtil.d(TAG, "QUOTE close");
                if (focusedWEditTextWrapperView.getRichType() != RichType.QUOTE) {
                    return;
                }
                if (!editableStr.contains(CharConstant.ENTER_STR)) {
                    // 判断当前RichEditor中有几个换行符，如果有0个，则直接将当前RichEditor转换为普通类型
                    focusedWEditTextWrapperView.toggleQuoteMode(false, false);
                } else {
                    // 2. 如果当前RichEditor中有一个或者多个换行符，则截取从换行符后的text，添加到新生成的Editor中
                    // 根据select的起点和终点，判断所处的行，并将这些行作为一个引用
                    int cursorStart = focusedWEditText.getSelectionStart();
                    int cursorEnd = focusedWEditText.getSelectionEnd();

                    // 无论光标是否选中文字，都将整体分为3部分？左光标在0，和右光标在最后的情况？TODO

                    int quoteStart = 0;
                    int quoteEnd = 0;

                    // 没有选中任何文字：
                    String cursorLeftStr = editableStr.substring(0, cursorStart);
                    String cursorRightStr = editableStr.substring(cursorEnd);
                    // 找到从光标位置到前面的换行符（没有换行符则到位置0）
                    int lastIndexOfEnterAmongLeftStr = cursorLeftStr.lastIndexOf(CharConstant.ENTER_CHAR);
                    // 这几个位置需要测试一下
                    if (lastIndexOfEnterAmongLeftStr == -1) {
                        quoteStart = 0;
                    } else {
                        quoteStart = lastIndexOfEnterAmongLeftStr + 1;
                    }
                    // 找到从光标位置到后面的换行符（没有换行符则到位置最后）
                    int firstIndexOfEnterAmongRightStr = cursorRightStr.indexOf(CharConstant.ENTER_CHAR);
                    if (firstIndexOfEnterAmongRightStr == -1) {
                        quoteEnd = editableStr.length();
                    } else {
                        quoteEnd = cursorEnd + firstIndexOfEnterAmongRightStr + 1;
                    }

                    int currentEditableLength = editableStr.length();

                    if (quoteStart == currentEditableLength && quoteEnd == currentEditableLength) {
                        // 如果在最后
                        LogUtil.d(TAG, "QUOTE close 0");
                        Editable editableText = focusedWEditText.getEditableText();
                        List<SpanPart> spanPartsOutput0 = new ArrayList<>(32);
                        String textWithoutFormat0 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, 0, currentEditableLength - 1, spanPartsOutput0);
                        SpanUtil.setSpannableInclusiveExclusive(focusedWEditText, textWithoutFormat0, spanPartsOutput0, 0);
                        insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 1, RichType.NONE, true);
                    } else if (quoteStart == 0) {
                        // 四种情况
                        if (quoteEnd == currentEditableLength) {
                            LogUtil.d(TAG, "QUOTE close 11");
                            // 不选中时
                            // A   B   C   D   E | F   G   '/n'
                            // 选中时
                            // A   B   C   D   E | F   G   '/n'
                            // H   I   J   K   L | M   N   ('/n')
                            Editable editableText = focusedWEditText.getEditableText();
                            List<SpanPart> spanPartsOutput = new ArrayList<>(32);

                            if (editableStr.endsWith(CharConstant.ENTER_STR)) {
                                LogUtil.d(TAG, "QUOTE close 111");
                                String textWithoutFormat0 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, 0, quoteEnd - 1, spanPartsOutput);
                                SpanUtil.setSpannableInclusiveExclusive(focusedWEditText, textWithoutFormat0, spanPartsOutput, 0);
                                focusedWEditTextWrapperView.toggleQuoteMode(false, false);
                                insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 1, RichType.NONE, false);
                            } else {
                                LogUtil.d(TAG, "QUOTE close 112");
                                focusedWEditTextWrapperView.toggleQuoteMode(false, false);
                                // TODO 如果merge，则merge后，将后面一个remove掉
                            }
                        } else {
                            LogUtil.d(TAG, "QUOTE close 12");
                            // 不选中
                            // A   B   C   D   E | F   G   '/n'
                            // H   I   J   K   L   M   N   ('/n')
                            // 选中
                            // A   B   C | D   E | F   G   '/n'
                            // H   I   J   K   L   M   N   ('/n')
                            // 从起始0到quoteEnd，作为 focusedWRichEditor 的值，并置为quote状态

                            Editable editableText = focusedWEditText.getEditableText();
                            List<SpanPart> spanPartsOutput0 = new ArrayList<>(32);
                            List<SpanPart> spanPartsOutput1 = new ArrayList<>(32);
                            // 这里的quoteEnd应该要减一，因为末尾不应该留着 Enter
                            String textWithoutFormat0 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, 0, quoteEnd - 1, spanPartsOutput0);
                            String textWithoutFormat1 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, quoteEnd, editableText.length(), spanPartsOutput1);

                            SpanUtil.setSpannableInclusiveExclusive(focusedWEditText, textWithoutFormat0, spanPartsOutput0, 0);
                            focusedWEditTextWrapperView.toggleQuoteMode(false, false);
                            WEditTextWrapperView insertedWrapperView = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 1, RichType.NONE, false);
                            SpanUtil.setSpannableInclusiveExclusive(insertedWrapperView.getWEditText(), textWithoutFormat1, spanPartsOutput1, -quoteEnd);
                            insertedWrapperView.toggleQuoteMode(true, false);
                        }
                    } else {
                        if (quoteEnd == editableStr.length()) {
                            LogUtil.d(TAG, "QUOTE close 21");
                            // 不选中
                            // A   B   C   D   E   F   G   '/n'
                            // H   I   J | K   L   M   N   ('/n')

                            // 选中
                            // A   B   C   D   E   F   G   '/n'
                            // H   I   J | K   L   M   N   '/n'
                            // O   P   Q   R   S | T   U   ('/n')
                            // 光标前面有Enter，后面没有

                            Editable editableText = focusedWEditText.getEditableText();
                            List<SpanPart> spanPartsOutput0 = new ArrayList<>(32);
                            List<SpanPart> spanPartsOutput1 = new ArrayList<>(32);
                            // 这里的quoteEnd应该要减一，因为末尾不应该留着 Enter
                            String textWithoutFormat0 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, 0, quoteStart - 1, spanPartsOutput0);
                            SpanUtil.setSpannableInclusiveExclusive(focusedWEditText, textWithoutFormat0, spanPartsOutput0, 0);

                            if (editableStr.endsWith(CharConstant.ENTER_STR)) {
                                LogUtil.d(TAG, "QUOTE close 211");
                                String textWithoutFormat1 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, quoteStart, editableText.length() - 1, spanPartsOutput1);
                                // 添加一个WRichEditorWrapperView
                                WEditTextWrapperView insertedWrapperView = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 1, RichType.NONE, true);
                                SpanUtil.setSpannableInclusiveExclusive(insertedWrapperView.getWEditText(), textWithoutFormat1, spanPartsOutput1, -quoteStart);
                                // TODO 如果merge，则merge后，将后面一个remove掉
                            } else {
                                LogUtil.d(TAG, "QUOTE close 212");
                                String textWithoutFormat1 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, quoteStart, editableText.length(), spanPartsOutput1);
                                WEditTextWrapperView insertedWrapperView = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 1, RichType.NONE, true);
                                SpanUtil.setSpannableInclusiveExclusive(insertedWrapperView.getWEditText(), textWithoutFormat1, spanPartsOutput1, -quoteStart);
                                // TODO 如果merge，则merge后，将后面一个remove掉
                            }
                        } else {
                            LogUtil.d(TAG, "QUOTE close 22");
                            // 不选中
                            // A   B   C   D   E   F   G   '/n'
                            // H   I   J   K | L   M   N   '/n'
                            // O   P   Q   R   S   T   U   ('/n')
                            // 选中
                            // A   B   C   D   E   F   G   '/n'
                            // H   I   J   K | L   M   N   '/n'
                            // H   I   J   K   L   M | N   '/n'
                            // O   P   Q   R   S   T   U   ('/n')
                            Editable editableText = focusedWEditText.getEditableText();
                            List<SpanPart> spanPartsOutput0 = new ArrayList<>(32);
                            List<SpanPart> spanPartsOutput1 = new ArrayList<>(32);
                            List<SpanPart> spanPartsOutput2 = new ArrayList<>(32);
                            String textWithoutFormat0 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, 0, quoteStart - 1, spanPartsOutput0);
                            String textWithoutFormat1 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, quoteStart, quoteEnd - 1, spanPartsOutput1);
                            String textWithoutFormat2 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, quoteEnd, editableText.length(), spanPartsOutput2);

                            SpanUtil.setSpannableInclusiveExclusive(focusedWEditText, textWithoutFormat0, spanPartsOutput0, 0);

                            WEditTextWrapperView insertedWrapperView1 = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 1, RichType.NONE, true);
                            SpanUtil.setSpannableInclusiveExclusive(insertedWrapperView1.getWEditText(), textWithoutFormat1, spanPartsOutput1, -quoteStart);

                            WEditTextWrapperView insertedWrapperView2 = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 2, RichType.QUOTE, false);
                            SpanUtil.setSpannableInclusiveExclusive(insertedWrapperView2.getWEditText(), textWithoutFormat2, spanPartsOutput2, -quoteEnd);
                        }
                    }
                }
            }
        } else if (richType == RichType.LIST_UNORDERED) {
            // 如果是无序列表
            LogUtil.d(TAG, "richType == RichType.LIST_UNORDERED");
            Editable editable = focusedWEditText.getEditableText();
            String editableStr = editable.toString();
            if (open) {
                // 如果是 "开启" LIST_UNORDERED 动作
                if (focusedWEditTextWrapperView.getRichType() == RichType.LIST_UNORDERED) {
                    return;
                }

                if (!editableStr.contains(CharConstant.ENTER_STR)) {
                    // 1. 判断当前RichEditor中有几个换行符，如果有0个，则不"分裂"，直接将这个Editor的左侧drawable做变换
                    focusedWEditTextWrapperView.toggleUnOrderListMode(true, false);
                    if (needAddWRichEditor(focusedRichEditorWrapperViewIndex[0])) {
                        insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 1, RichType.NONE, false);
                    }
                } else {
                    // 2. 如果当前RichEditor中有一个或者多个换行符
                    // 根据select的起点和终点，判断所处的行，并将这N行作为N个引用
                    int cursorStart = focusedWEditText.getSelectionStart();
                    int cursorEnd = focusedWEditText.getSelectionEnd();

                    int unorderedStart = 0;
                    int unorderedEnd = 0;

                    // 没有选中任何文字：
                    String cursorLeftStr = editableStr.substring(0, cursorStart);
                    String cursorRightStr = editableStr.substring(cursorEnd);
                    // 找到从光标位置到前面的换行符（没有换行符则到位置0）
                    int lastIndexOfEnterAmongLeftStr = cursorLeftStr.lastIndexOf(CharConstant.ENTER_CHAR);
                    // 这几个位置需要测试一下
                    if (lastIndexOfEnterAmongLeftStr == -1) {
                        unorderedStart = 0;
                    } else {
                        unorderedStart = lastIndexOfEnterAmongLeftStr + 1;
                    }
                    // 找到从光标位置到后面的换行符（没有换行符则到位置最后）
                    int firstIndexOfEnterAmongRightStr = cursorRightStr.indexOf(CharConstant.ENTER_CHAR);
                    if (firstIndexOfEnterAmongRightStr == -1) {
                        unorderedEnd = editableStr.length();
                    } else {
                        unorderedEnd = cursorEnd + firstIndexOfEnterAmongRightStr + 1;
                    }

                    int currentEditableLength = editableStr.length();

                    if (unorderedStart == currentEditableLength && unorderedEnd == currentEditableLength) {
                        // 如果在最后
                        LogUtil.d(TAG, "LIST_UNORDERED open 0");
                        Editable editableText = focusedWEditText.getEditableText();
                        List<SpanPart> spanPartsOutput = new ArrayList<>(32);
                        String textWithoutFormat0 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, 0, currentEditableLength - 1, spanPartsOutput);
                        SpanUtil.setSpannableInclusiveExclusive(focusedWEditText, textWithoutFormat0, spanPartsOutput, 0);
                        insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 1, RichType.LIST_UNORDERED, true);
                    } else if (unorderedStart == 0) {
                        // 四种情况
                        if (unorderedEnd == currentEditableLength) {
                            // 不选中时
                            // A   B   C   D   E | F   G   '/n'
                            // 选中时
                            // A   B   C   D   E | F   G   '/n'
                            // H   I   J   K   L | M   N   ('/n')
                            // 如果光标不在队尾
                            LogUtil.d(TAG, "LIST_UNORDERED open 11");
                            List<SpanPart> spanPartsOutput = new ArrayList<>(32);

                            String selectedStr = null;

                            if (editableStr.endsWith(CharConstant.ENTER_STR)) {
                                selectedStr = editableStr.substring(0, unorderedEnd - 1);
                            } else {
                                selectedStr = editableStr.substring(0, unorderedEnd);
                            }
                            String[] splitStringByEnter = selectedStr.split(CharConstant.ENTER_STR);
                            int splitLength = splitStringByEnter.length;
                            ArrayList<SplitPart> splitParts = new ArrayList<>(splitLength);

                            int splitItemStart = 0;
                            for (int i = 0; i < splitLength; i++) {
                                LogUtil.d(TAG, "LIST_UNORDERED splitParts add: " + splitStringByEnter[i] + "  start : " + splitItemStart);
                                splitParts.add(new SplitPart(splitStringByEnter[i], splitItemStart));
                                splitItemStart = splitItemStart + splitStringByEnter[i].length() + 1; // split 后，去掉了
                            }

                            for (int i = 0; i < splitLength; i++) {
                                SplitPart itemSplit = splitParts.get(i);
                                spanPartsOutput.clear();
                                String textWithoutFormat = SpanUtil.getSpannableStringInclusiveExclusive(editable, itemSplit.getStart(), itemSplit.getEnd(), spanPartsOutput);

                                if (i == 0) {
                                    SpanUtil.setSpannableInclusiveExclusive(focusedWEditText, textWithoutFormat, spanPartsOutput, 0);
                                    focusedWEditTextWrapperView.toggleUnOrderListMode(true, false);
                                } else {
                                    WEditTextWrapperView wEditTextWrapperView = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + i, RichType.LIST_UNORDERED, false);
                                    SpanUtil.setSpannableInclusiveExclusive(wEditTextWrapperView.getWEditText(), textWithoutFormat, spanPartsOutput, -itemSplit.getStart());
                                }
                            }

                            if (editableStr.endsWith(CharConstant.ENTER_STR)) {
                                LogUtil.d(TAG, "LIST_UNORDERED open 111");
                                insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + splitLength, RichType.NONE, false);
                            } else {
                                LogUtil.d(TAG, "LIST_UNORDERED open 112");
                                if (needAddWRichEditor(focusedRichEditorWrapperViewIndex[0] + splitLength - 1)) {
                                    insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + splitLength, RichType.NONE, false);
                                }
                            }
                        } else {
                            // 不选中
                            // A   B   C   D   E | F   G   '/n'
                            // H   I   J   K   L   M   N   ('/n')
                            // 选中
                            // A   B   C | D   E | F   G   '/n'
                            // H   I   J   K   L   M   N   ('/n')
                            LogUtil.d(TAG, "LIST_UNORDERED open 12");

                            List<SpanPart> spanPartsOutput = new ArrayList<>(32);
                            String selectedStr = editableStr.substring(0, unorderedEnd - 1);

                            String[] splitStringByEnter = selectedStr.split(CharConstant.ENTER_STR);
                            int splitLength = splitStringByEnter.length;
                            ArrayList<SplitPart> splitParts = new ArrayList<>(splitLength);

                            int splitItemStart = 0;
                            for (int i = 0; i < splitLength; i++) {
                                splitParts.add(new SplitPart(splitStringByEnter[i], splitItemStart));
                                splitItemStart = splitItemStart + splitStringByEnter[i].length() + 1; // split 后，去掉了
                            }

                            for (int i = 0; i < splitLength; i++) {
                                SplitPart itemSplit = splitParts.get(i);
                                spanPartsOutput.clear();
                                String textWithoutFormat = SpanUtil.getSpannableStringInclusiveExclusive(editable, itemSplit.getStart(), itemSplit.getEnd(), spanPartsOutput);

                                if (i == 0) {
                                    SpanUtil.setSpannableInclusiveExclusive(focusedWEditText, textWithoutFormat, spanPartsOutput, 0);
                                    focusedWEditTextWrapperView.toggleUnOrderListMode(true, false);
                                } else {
                                    WEditTextWrapperView wEditTextWrapperView = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + i, RichType.LIST_UNORDERED, false);
                                    SpanUtil.setSpannableInclusiveExclusive(wEditTextWrapperView.getWEditText(), textWithoutFormat, spanPartsOutput, -itemSplit.getStart());
                                }
                            }

                            spanPartsOutput.clear();
                            String textWithoutFormat = SpanUtil.getSpannableStringInclusiveExclusive(editable, unorderedEnd, editable.length(), spanPartsOutput);
                            WEditTextWrapperView insertedWrapperView = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + splitLength, RichType.NONE, false);
                            SpanUtil.setSpannableInclusiveExclusive(insertedWrapperView.getWEditText(), textWithoutFormat, spanPartsOutput, -unorderedEnd);
                        }
                    } else {
                        if (unorderedEnd == editableStr.length()) {
                            // 不选中
                            // A   B   C   D   E   F   G   '/n'
                            // H   I   J | K   L   M   N   ('/n')

                            // 选中
                            // A   B   C   D   E   F   G   '/n'
                            // H   I   J | K   L   M   N   '/n'
                            // O   P   Q   R   S | T   U   ('/n')
                            // 光标前面有Enter，后面没有
                            LogUtil.d(TAG, "LIST_UNORDERED open 21");

                            List<SpanPart> spanPartsOutput = new ArrayList<>(32);
                            String selectedStr;

                            if (editableStr.endsWith(CharConstant.ENTER_STR)) {
                                selectedStr = editableStr.substring(unorderedStart, unorderedEnd - 1);
                            } else {
                                selectedStr = editableStr.substring(unorderedStart, unorderedEnd);
                            }
                            String textWithoutFormat = SpanUtil.getSpannableStringInclusiveExclusive(editable, 0, unorderedStart - 1, spanPartsOutput);

                            SpanUtil.setSpannableInclusiveExclusive(focusedWEditText, textWithoutFormat, spanPartsOutput, 0);
                            focusedWEditTextWrapperView.toggleUnOrderListMode(false, false);

                            String[] splitStringByEnter = selectedStr.split(CharConstant.ENTER_STR);
                            int splitLength = splitStringByEnter.length;
                            ArrayList<SplitPart> splitParts = new ArrayList<>(splitLength);

                            int splitItemStart = unorderedStart;
                            for (int i = 0; i < splitLength; i++) {
                                splitParts.add(new SplitPart(splitStringByEnter[i], splitItemStart));
                                splitItemStart = splitItemStart + splitStringByEnter[i].length() + 1; // split 后，去掉了
                            }

                            for (int i = 0; i < splitLength; i++) {
                                SplitPart itemSplit = splitParts.get(i);
                                spanPartsOutput.clear();
                                String textWithoutFormatItem = SpanUtil.getSpannableStringInclusiveExclusive(editable, itemSplit.getStart(), itemSplit.getEnd(), spanPartsOutput);
                                WEditTextWrapperView wEditTextWrapperView = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + i + 1, RichType.LIST_UNORDERED, false);
                                SpanUtil.setSpannableInclusiveExclusive(wEditTextWrapperView.getWEditText(), textWithoutFormatItem, spanPartsOutput, -itemSplit.getStart());
                            }

                            if (editableStr.endsWith(CharConstant.ENTER_STR)) {
                                LogUtil.d(TAG, "LIST_UNORDERED open 211");
                                insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + splitLength, RichType.NONE, false);
                            } else {
                                LogUtil.d(TAG, "LIST_UNORDERED open 212");
                                if (needAddWRichEditor(focusedRichEditorWrapperViewIndex[0] + splitLength)) {
                                    insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + splitLength + 1, RichType.NONE, false);
                                }
                            }
                        } else {
                            // 不选中
                            // A   B   C   D   E   F   G   '/n'
                            // H   I   J   K | L   M   N   '/n'
                            // O   P   Q   R   S   T   U   ('/n')
                            // 选中
                            // A   B   C   D   E   F   G   '/n'
                            // H   I   J   K | L   M   N   '/n'
                            // H   I   J   K   L   M | N   '/n'
                            // O   P   Q   R   S   T   U   ('/n')
                            LogUtil.d(TAG, "LIST_UNORDERED open 22");

                            List<SpanPart> spanPartsOutput = new ArrayList<>(32);

                            // 第一部分
                            String textWithoutFormatUnselectedStrBefore = SpanUtil.getSpannableStringInclusiveExclusive(editable, 0, unorderedStart - 1, spanPartsOutput);
                            SpanUtil.setSpannableInclusiveExclusive(focusedWEditText, textWithoutFormatUnselectedStrBefore, spanPartsOutput, 0);
                            focusedWEditTextWrapperView.toggleUnOrderListMode(false, false);

                            // 中间部分
                            String selectedStr = editableStr.substring(unorderedStart, unorderedEnd - 1);
                            String[] splitStringByEnter = selectedStr.split(CharConstant.ENTER_STR);
                            int splitLength = splitStringByEnter.length;
                            ArrayList<SplitPart> splitParts = new ArrayList<>(splitLength);
                            int splitItemStart = unorderedStart;
                            for (int i = 0; i < splitLength; i++) {
                                splitParts.add(new SplitPart(splitStringByEnter[i], splitItemStart));
                                splitItemStart = splitItemStart + splitStringByEnter[i].length() + 1; // split 后，去掉了
                            }

                            for (int i = 0; i < splitLength; i++) {
                                SplitPart itemSplit = splitParts.get(i);
                                spanPartsOutput.clear();
                                String textWithoutFormatItem = SpanUtil.getSpannableStringInclusiveExclusive(editable, itemSplit.getStart(), itemSplit.getEnd(), spanPartsOutput);
                                WEditTextWrapperView wEditTextWrapperView = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + i + 1, RichType.LIST_UNORDERED, false);
                                SpanUtil.setSpannableInclusiveExclusive(wEditTextWrapperView.getWEditText(), textWithoutFormatItem, spanPartsOutput, -itemSplit.getStart());
                            }

                            // 第三部分
                            spanPartsOutput.clear();
                            String textWithoutFormat = SpanUtil.getSpannableStringInclusiveExclusive(editable, unorderedEnd, editableStr.length(), spanPartsOutput);
                            WEditTextWrapperView insertedWrapperViewForThirdPart = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + splitLength + 1, RichType.NONE, false);
                            SpanUtil.setSpannableInclusiveExclusive(insertedWrapperViewForThirdPart.getWEditText(), textWithoutFormat, spanPartsOutput, -unorderedEnd);
                        }
                    }
                }
            } else {
                // 如果是 "关闭" LIST_UNORDERED 动作
                LogUtil.d(TAG, "LIST_UNORDERED close 0");
                if (focusedWEditTextWrapperView.getRichType() != RichType.LIST_UNORDERED) {
                    return;
                }
                // 因为一个回车一行，所以直接关掉
                focusedWEditTextWrapperView.toggleUnOrderListMode(false, false);
            }
        } else if (richType == RichType.LIST_ORDERED) {
            // 如果是有序列表
            LogUtil.d(TAG, "richType == RichType.LIST_ORDERED");
            // 如果是 "LIST_ORDERED" 类型，则：
            Editable editable = focusedWEditText.getEditableText();
            String editableStr = editable.toString();
            if (open) {
                // 如果是 "开启" LIST_ORDERED 动作
                if (focusedWEditTextWrapperView.getRichType() == RichType.LIST_ORDERED) {
                    return;
                }
                if (!editableStr.contains(CharConstant.ENTER_STR)) {
                    LogUtil.d(TAG, "richType == RichType.LIST_ORDERED 0");
                    // 1. 判断当前RichEditor中有几个换行符，如果有0个，则不"分裂"，直接将这个Editor的左侧drawable做变换
                    focusedWEditTextWrapperView.toggleOrderListMode(true, false);
                    if (needAddWRichEditor(focusedRichEditorWrapperViewIndex[0])) {
                        insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 1, RichType.NONE, false);
                    }
                    OrderListUtil.updateOrderListNumbersAfterViewsChanged(mLinearLayout);
                } else {
                    // 2. 如果当前RichEditor中有一个或者多个换行符
                    // 根据select的起点和终点，判断所处的行，并将这N行作为N个引用
                    int cursorStart = focusedWEditText.getSelectionStart();
                    int cursorEnd = focusedWEditText.getSelectionEnd();

                    int orderedStart = 0;
                    int orderedEnd = 0;

                    // 没有选中任何文字：
                    String cursorLeftStr = editableStr.substring(0, cursorStart);
                    String cursorRightStr = editableStr.substring(cursorEnd);
                    // 找到从光标位置到前面的换行符（没有换行符则到位置0）
                    int lastIndexOfEnterAmongLeftStr = cursorLeftStr.lastIndexOf(CharConstant.ENTER_CHAR);
                    // 这几个位置需要测试一下
                    if (lastIndexOfEnterAmongLeftStr == -1) {
                        orderedStart = 0;
                    } else {
                        orderedStart = lastIndexOfEnterAmongLeftStr + 1;
                    }
                    // 找到从光标位置到后面的换行符（没有换行符则到位置最后）
                    int firstIndexOfEnterAmongRightStr = cursorRightStr.indexOf(CharConstant.ENTER_CHAR);
                    if (firstIndexOfEnterAmongRightStr == -1) {
                        orderedEnd = editableStr.length();
                    } else {
                        orderedEnd = cursorEnd + firstIndexOfEnterAmongRightStr + 1;
                    }

                    int currentEditableLength = editableStr.length();

                    if (orderedStart == currentEditableLength && orderedEnd == currentEditableLength) {
                        // 如果在最后
                        Editable editableText = focusedWEditText.getEditableText();
                        List<SpanPart> spanPartsOutput = new ArrayList<>(32);
                        String textWithoutFormat = SpanUtil.getSpannableStringInclusiveExclusive(editableText, 0, currentEditableLength - 1, spanPartsOutput);
                        SpanUtil.setSpannableInclusiveExclusive(focusedWEditText, textWithoutFormat, spanPartsOutput, 0);
                        insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 1, RichType.LIST_ORDERED, true);
                        if (needAddWRichEditor(focusedRichEditorWrapperViewIndex[0] + 1)) {
                            insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 2, RichType.NONE, false);
                        }
                        OrderListUtil.updateOrderListNumbersAfterViewsChanged(mLinearLayout);
                    } else if (orderedStart == 0) {
                        // 四种情况
                        if (orderedEnd == currentEditableLength) {
                            // 不选中时
                            // A   B   C   D   E | F   G   '/n'
                            // 选中时
                            // A   B   C   D   E | F   G   '/n'
                            // H   I   J   K   L | M   N   ('/n')
                            // 如果光标不在队尾
                            LogUtil.d(TAG, "LIST_ORDERED open 11");
                            List<SpanPart> spanPartsOutput = new ArrayList<>(32);

                            String selectedStr = null;

                            if (editableStr.endsWith(CharConstant.ENTER_STR)) {
                                selectedStr = editableStr.substring(0, orderedEnd - 1);
                            } else {
                                selectedStr = editableStr.substring(0, orderedEnd);
                            }
                            String[] splitStringByEnter = selectedStr.split(CharConstant.ENTER_STR);
                            int splitLength = splitStringByEnter.length;
                            ArrayList<SplitPart> splitParts = new ArrayList<>(splitLength);
                            int splitItemStart = 0;
                            for (int i = 0; i < splitLength; i++) {
                                splitParts.add(new SplitPart(splitStringByEnter[i], splitItemStart));
                                splitItemStart = splitItemStart + splitStringByEnter[i].length() + 1; // split 后，去掉了
                            }

                            for (int i = 0; i < splitLength; i++) {
                                SplitPart itemSplit = splitParts.get(i);
                                spanPartsOutput.clear();
                                String textWithoutFormat = SpanUtil.getSpannableStringInclusiveExclusive(editable, itemSplit.getStart(), itemSplit.getEnd(), spanPartsOutput);
                                if (i == 0) {
                                    SpanUtil.setSpannableInclusiveExclusive(focusedWEditText, textWithoutFormat, spanPartsOutput, 0);
                                    focusedWEditTextWrapperView.toggleOrderListMode(true, false);
                                } else {
                                    WEditTextWrapperView wEditTextWrapperView = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + i, RichType.LIST_ORDERED, false);
                                    SpanUtil.setSpannableInclusiveExclusive(wEditTextWrapperView.getWEditText(), textWithoutFormat, spanPartsOutput, -itemSplit.getStart());
                                }
                            }

                            if (editableStr.endsWith(CharConstant.ENTER_STR)) {
                                LogUtil.d(TAG, "LIST_ORDERED open 111");
                                insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + splitLength, RichType.NONE, false);
                            } else {
                                LogUtil.d(TAG, "LIST_ORDERED open 112");
                                if (needAddWRichEditor(focusedRichEditorWrapperViewIndex[0] + splitLength - 1)) {
                                    insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + splitLength, RichType.NONE, false);
                                }
                            }
                            OrderListUtil.updateOrderListNumbersAfterViewsChanged(mLinearLayout);
                        } else {
                            // 不选中
                            // A   B   C   D   E | F   G   '/n'
                            // H   I   J   K   L   M   N   ('/n')
                            // 选中
                            // A   B   C | D   E | F   G   '/n'
                            // H   I   J   K   L   M   N   ('/n')
                            LogUtil.d(TAG, "LIST_ORDERED open 12");
                            List<SpanPart> spanPartsOutput = new ArrayList<>(32);
                            String selectedStr = editableStr.substring(0, orderedEnd - 1);

                            String[] splitStringByEnter = selectedStr.split(CharConstant.ENTER_STR);
                            int splitLength = splitStringByEnter.length;
                            ArrayList<SplitPart> splitParts = new ArrayList<>(splitLength);

                            int splitItemStart = 0;
                            for (int i = 0; i < splitLength; i++) {
                                splitParts.add(new SplitPart(splitStringByEnter[i], splitItemStart));
                                splitItemStart = splitItemStart + splitStringByEnter[i].length() + 1; // split 后，去掉了
                            }

                            for (int i = 0; i < splitLength; i++) {
                                SplitPart itemSplit = splitParts.get(i);
                                spanPartsOutput.clear();
                                String textWithoutFormat = SpanUtil.getSpannableStringInclusiveExclusive(editable, itemSplit.getStart(), itemSplit.getEnd(), spanPartsOutput);

                                if (i == 0) {
                                    SpanUtil.setSpannableInclusiveExclusive(focusedWEditText, textWithoutFormat, spanPartsOutput, 0);
                                    focusedWEditTextWrapperView.toggleOrderListMode(true, false);
                                } else {
                                    WEditTextWrapperView wEditTextWrapperView = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + i, RichType.LIST_ORDERED, false);
                                    SpanUtil.setSpannableInclusiveExclusive(wEditTextWrapperView.getWEditText(), textWithoutFormat, spanPartsOutput, -itemSplit.getStart());
                                }
                            }
                            spanPartsOutput.clear();
                            String textWithoutFormat = SpanUtil.getSpannableStringInclusiveExclusive(editable, orderedEnd, editable.length(), spanPartsOutput);
                            WEditTextWrapperView insertedWrapperView = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + splitLength, RichType.NONE, false);
                            SpanUtil.setSpannableInclusiveExclusive(insertedWrapperView.getWEditText(), textWithoutFormat, spanPartsOutput, -orderedEnd);
                            OrderListUtil.updateOrderListNumbersAfterViewsChanged(mLinearLayout);
                        }
                    } else {
                        if (orderedEnd == editableStr.length()) {
                            LogUtil.d(TAG, "LIST_ORDERED open 21");
                            // 不选中
                            // A   B   C   D   E   F   G   '/n'
                            // H   I   J | K   L   M   N   ('/n')

                            // 选中
                            // A   B   C   D   E   F   G   '/n'
                            // H   I   J | K   L   M   N   '/n'
                            // O   P   Q   R   S | T   U   ('/n')

                            List<SpanPart> spanPartsOutput = new ArrayList<>(32);
                            String selectedStr;
                            if (editableStr.endsWith(CharConstant.ENTER_STR)) {
                                selectedStr = editableStr.substring(orderedStart, orderedEnd - 1);
                            } else {
                                selectedStr = editableStr.substring(orderedStart, orderedEnd);
                            }
                            String textWithoutFormat = SpanUtil.getSpannableStringInclusiveExclusive(editable, 0, orderedStart - 1, spanPartsOutput);
                            SpanUtil.setSpannableInclusiveExclusive(focusedWEditText, textWithoutFormat, spanPartsOutput, 0);
                            focusedWEditTextWrapperView.toggleOrderListMode(false, false);

                            String[] splitStringByEnter = selectedStr.split(CharConstant.ENTER_STR);
                            int splitLength = splitStringByEnter.length;
                            ArrayList<SplitPart> splitParts = new ArrayList<>(splitLength);

                            int splitItemStart = orderedStart;
                            for (int i = 0; i < splitLength; i++) {
                                splitParts.add(new SplitPart(splitStringByEnter[i], splitItemStart));
                                splitItemStart = splitItemStart + splitStringByEnter[i].length() + 1; // split 后，去掉了
                            }

                            for (int i = 0; i < splitLength; i++) {
                                SplitPart itemSplit = splitParts.get(i);
                                spanPartsOutput.clear();
                                String textWithoutFormatItem = SpanUtil.getSpannableStringInclusiveExclusive(editable, itemSplit.getStart(), itemSplit.getEnd(), spanPartsOutput);
                                WEditTextWrapperView wEditTextWrapperView = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + i + 1, RichType.LIST_ORDERED, false);
                                SpanUtil.setSpannableInclusiveExclusive(wEditTextWrapperView.getWEditText(), textWithoutFormatItem, spanPartsOutput, -itemSplit.getStart());
                            }

                            if (editableStr.endsWith(CharConstant.ENTER_STR)) {
                                LogUtil.d(TAG, "LIST_ORDERED open 211");
                                insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + splitLength, RichType.NONE, false);
                            } else {
                                LogUtil.d(TAG, "LIST_ORDERED open 212");
                                if (needAddWRichEditor(focusedRichEditorWrapperViewIndex[0] + splitLength)) {
                                    insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + splitLength + 1, RichType.NONE, false);
                                }
                            }
                            OrderListUtil.updateOrderListNumbersAfterViewsChanged(mLinearLayout);
                        } else {
                            // 不选中
                            // A   B   C   D   E   F   G   '/n'
                            // H   I   J   K | L   M   N   '/n'
                            // O   P   Q   R   S   T   U   ('/n')
                            // 选中
                            // A   B   C   D   E   F   G   '/n'
                            // H   I   J   K | L   M   N   '/n'
                            // H   I   J   K   L   M | N   '/n'
                            // O   P   Q   R   S   T   U   ('/n')
                            LogUtil.d(TAG, "LIST_ORDERED open 22");
                            List<SpanPart> spanPartsOutput = new ArrayList<>(32);

                            // 第一部分
                            String textWithoutFormatUnselectedStrBefore = SpanUtil.getSpannableStringInclusiveExclusive(editable, 0, orderedStart - 1, spanPartsOutput);
                            SpanUtil.setSpannableInclusiveExclusive(focusedWEditText, textWithoutFormatUnselectedStrBefore, spanPartsOutput, 0);
                            focusedWEditTextWrapperView.toggleOrderListMode(false, false);

                            // 中间部分
                            String selectedStr = editableStr.substring(orderedStart, orderedEnd - 1);
                            String[] splitStringByEnter = selectedStr.split(CharConstant.ENTER_STR);
                            int splitLength = splitStringByEnter.length;
                            ArrayList<SplitPart> splitParts = new ArrayList<>(splitLength);
                            int splitItemStart = orderedStart;
                            for (int i = 0; i < splitLength; i++) {
                                splitParts.add(new SplitPart(splitStringByEnter[i], splitItemStart));
                                LogUtil.d(TAG, "LIST_ORDERED open splitParts add : " + splitStringByEnter[i] + ", splitItemStart : " + splitItemStart);
                                splitItemStart = splitItemStart + splitStringByEnter[i].length() + 1; // split 后，去掉了
                            }

                            for (int i = 0; i < splitLength; i++) {
                                SplitPart itemSplit = splitParts.get(i);
                                spanPartsOutput.clear();
                                String textWithoutFormatItem = SpanUtil.getSpannableStringInclusiveExclusive(editable, itemSplit.getStart(), itemSplit.getEnd(), spanPartsOutput);
                                WEditTextWrapperView wEditTextWrapperView = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + i + 1, RichType.LIST_ORDERED, false);
                                SpanUtil.setSpannableInclusiveExclusive(wEditTextWrapperView.getWEditText(), textWithoutFormatItem, spanPartsOutput, -itemSplit.getStart());
                            }

                            // 第三部分
                            spanPartsOutput.clear();
                            String textWithoutFormat = SpanUtil.getSpannableStringInclusiveExclusive(editable, orderedEnd, editableStr.length(), spanPartsOutput);
                            WEditTextWrapperView insertedWrapperViewForThirdPart = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + splitLength + 1, RichType.NONE, false);
                            SpanUtil.setSpannableInclusiveExclusive(insertedWrapperViewForThirdPart.getWEditText(), textWithoutFormat, spanPartsOutput, -orderedEnd);

                            OrderListUtil.updateOrderListNumbersAfterViewsChanged(mLinearLayout);
                        }
                    }
                }
            } else {
                LogUtil.d(TAG, "LIST_ORDERED close");
                if (focusedWEditTextWrapperView.getRichType() != RichType.LIST_ORDERED) {
                    return;
                }
                // 因为一个回车一行，所以直接关掉
                focusedWEditTextWrapperView.toggleOrderListMode(false, false);
                OrderListUtil.updateOrderListNumbersAfterViewsChanged(mLinearLayout);
            }
        } else {
            focusedWEditText.updateTextByRichTypeChanged(richType, open, extra);
        }
    }

    /**
     * 当前index后面是否需要加一个空的EditText
     *
     * @param currentIndex 当前 CellView 所处的 index
     * @return
     */
    public boolean needAddWRichEditor(int currentIndex) {
        if (mLinearLayout == null) {
            return false;
        }
        if (currentIndex < 0) {
            return false;
        }
        int childCount = mLinearLayout.getChildCount();
        if (currentIndex >= childCount) {
            // 无效index值
            return false;
        }
        // 当前为最后一个，或者当前的下一个的类型不为 NONE
        if (currentIndex == childCount - 1) {
            return true;
        }
        IRichCellView tailRichCellView = getTailCellView();
        if (tailRichCellView != null && tailRichCellView.getRichType() == RichType.NONE) {
            return false;
        }

        IRichCellView nextRichCellView = (IRichCellView) mLinearLayout.getChildAt(currentIndex + 1);
        if (nextRichCellView == null) {
            return false;
        }

        RichType richType = nextRichCellView.getRichType();
        if (richType == RichType.NONE) {
            return false;
        }
        return true;
    }

    /**
     * 当前index后面是否需要加一个空的EditText
     *
     * @param currentIndex 当前 CellView 所处的 index
     * @return
     */
    public boolean needAddWRichEditorForDeleteAction(int currentIndex) {
        if (mLinearLayout == null) {
            return false;
        }
        if (currentIndex < 0) {
            // 无效index值
            return false;
        }
        int childCount = mLinearLayout.getChildCount();
        if (currentIndex >= childCount) {
            // 无效index值
            return false;
        }
        // 当前为最后一个，或者当前的下一个的类型不为 NONE
        if (currentIndex == childCount - 1) {
            return true;
        }
        IRichCellView nextRichCellView = (IRichCellView) mLinearLayout.getChildAt(currentIndex + 1);
        if (nextRichCellView == null) {
            // 无效CellView
            return false;
        }

        RichType richType = nextRichCellView.getRichType();
        if (richType == RichType.NONE) {
            return false;
        }
        return true;
    }

    public WEditTextWrapperView findCurrentFocusedRichEditorWrapperView(int[] indexInCellViewList) {
        indexInCellViewList[0] = -1;
        WEditTextWrapperView focusedWEditTextWrapperView = null;
        if (mLinearLayout == null) {
            return focusedWEditTextWrapperView;
        }
        int childCount = mLinearLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            IRichCellView cellView = (IRichCellView) mLinearLayout.getChildAt(i);
            if (cellView != null && cellView.getView() != null) {
                if (cellView.getRichType().getHasEditor()) {
                    WEditTextWrapperView wEditTextWrapperView = ((WEditTextWrapperView) cellView.getView());
                    if (wEditTextWrapperView != null && wEditTextWrapperView.getWEditText() != null) {
                        if (wEditTextWrapperView.getWEditText().hasFocus()) {
                            focusedWEditTextWrapperView = wEditTextWrapperView;
                            indexInCellViewList[0] = i;
                            break;
                        }
                    }
                }
            }
        }
        return focusedWEditTextWrapperView;
    }

    public WEditTextWrapperView findCurrentOrRecentFocusedRichEditorWrapperView(int[] focusedIndex) {
        int[] focusedRichEditorWrapperView = new int[1];
        focusedRichEditorWrapperView[0] = -1;
        WEditTextWrapperView retWEditTextWrapperView = findCurrentFocusedRichEditorWrapperView(focusedRichEditorWrapperView);
        if (retWEditTextWrapperView != null) {
            focusedIndex[0] = focusedRichEditorWrapperView[0];
            return retWEditTextWrapperView;
        }
        if (mLastFocusedRichCellView instanceof WEditTextWrapperView) {
            return (WEditTextWrapperView) mLastFocusedRichCellView;
        }
        return null;
    }

    public Set<RichType> getRichTypes() {
        return mRichTypes;
    }

    @Override
    public void onEditorFocusChanged(IRichCellView iRichCellView, boolean focused) {
        if (focused) {
            mLastFocusedRichCellView = iRichCellView;
        }
    }

    public void setOnRichTypeChangedListener(OnRichTypeChangedListener listener) {
        mOnRichTypeChangedListener = listener;
    }

    public OnRichTypeChangedListener getOnRichTypeChangedListener() {
        return mOnRichTypeChangedListener;
    }

    public IRichCellView insertAWRichEditorWrapperWithCellView(int index, IRichCellView richCellView, boolean needRequestFocusWhenAdded) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        // 插入到中间某个位置或者队尾（index == -1）
        addRichCell(richCellView, lp, index);
        return richCellView;
    }

    public WEditTextWrapperView insertAWRichEditorWrapperWithRichType(int index, RichType richType, boolean needRequestFocusWhenAdded) {
        WEditTextWrapperView editTextWrapperView = new WEditTextWrapperView(getContext(), richType, needRequestFocusWhenAdded);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (DebugUtil.DEBUG) {
            editTextWrapperView.getWEditText().setHint("CELL new ");
            editTextWrapperView.setBackgroundColor(0x10222222);
        }
        // 插入到中间某个位置或者队尾（index == -1）
        addRichCell(editTextWrapperView, lp, index);
        return editTextWrapperView;
    }

    public WEditTextWrapperView insertAWRichEditorWrapperWithRichType(WEditTextWrapperView wrapperView, RichType richType, boolean needRequestFocusWhenAdded) {
        int index = -1;
        if (wrapperView != null) {
            index = getCellViewIndex(wrapperView) + 1;
        }
        return insertAWRichEditorWrapperWithRichType(index, richType, needRequestFocusWhenAdded);
    }

    public WEditTextWrapperView insertACellViewWithRichType(IRichCellView cellView, RichType richType, boolean needRequestFocusWhenAdded) {
        int index = -1;
        if (cellView != null) {
            index = getCellViewIndex(cellView) + 1;
        }
        return insertAWRichEditorWrapperWithRichType(index, richType, needRequestFocusWhenAdded);
    }

    /**
     * 为列表添加一个自定义的ICellView
     * @param wrapperView 锚点，在此view的下面添加
     * @param richCellView
     * @param needRequestFocusWhenAdded
     * @return
     */
    public IRichCellView insertACellView(WEditTextWrapperView wrapperView, IRichCellView richCellView, boolean needRequestFocusWhenAdded) {
        int index = -1;
        if (wrapperView != null) {
            index = getCellViewIndex(wrapperView) + 1;
        }
        return insertAWRichEditorWrapperWithCellView(index, richCellView, needRequestFocusWhenAdded);
    }

    public ViewGroup getContainerView() {
        return mLinearLayout;
    }

    public int getCellViewCount() {
        if (mLinearLayout == null) {
            return 0;
        }
        return mLinearLayout.getChildCount();
    }

    public int getCellViewIndex(IRichCellView cellView) {
        if (mLinearLayout == null) {
            return -1;
        }
        if (!(cellView instanceof View)) {
            return -1;
        }
        return mLinearLayout.indexOfChild((View) cellView);
    }

    public IRichCellView getCellViewByIndex(int index) {
        if (mLinearLayout == null) {
            return null;
        }
        if (index < 0) {
            return null;
        }
        if (index < 0 || mLinearLayout.getChildCount() <= index) {
            return null;
        }
        return (IRichCellView) mLinearLayout.getChildAt(index);
    }

    public IRichCellView getTailCellView() {
        if (mLinearLayout == null) {
            return null;
        }
        int cellCount = mLinearLayout.getChildCount();
        if (cellCount == 0) {
            return null;
        }
        return (IRichCellView) mLinearLayout.getChildAt(cellCount - 1);
    }

    public IRichCellView addNoneTypeTailOptionally() {
        IRichCellView tailCellView = getTailCellView();
        if (tailCellView == null || tailCellView.getRichType() != RichType.NONE) {
            tailCellView = insertAWRichEditorWrapperWithRichType(-1, RichType.NONE, false);
        }
        return tailCellView;
    }

    public void insertLine() {
        RichLineView richLineView = new RichLineView(getContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        insertResourceTypeView(richLineView, lp);
    }

    public void insertImage(RichImageView richImageView) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        insertResourceTypeView(richImageView, lp);
    }

    public void insertNetDisk(RichNetDiskView richNetDiskView) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        insertResourceTypeView(richNetDiskView, lp);
    }

    private void insertResourceTypeView(IRichCellView richCellView, LinearLayout.LayoutParams lp) {

        int[] focusedIndex = new int[1];
        WEditTextWrapperView focusedWrapperView = findCurrentOrRecentFocusedRichEditorWrapperView(focusedIndex);
        if (focusedWrapperView != null
                && focusedWrapperView.getWEditText() != null
                && focusedWrapperView.getWEditText().hasFocus()) {

            WEditText wFocusedEditText = focusedWrapperView.getWEditText();
            if (wFocusedEditText.getEditableText().length() == 0) {
                addRichCell(richCellView, lp, focusedIndex[0]);
            } else {
                RichType richType = focusedWrapperView.getRichType();
                int textLength = wFocusedEditText.getEditableText().length();
                int cursorStart = wFocusedEditText.getSelectionStart();
                int cursorEnd = wFocusedEditText.getSelectionEnd();

                if (cursorStart >= 0 && cursorEnd >= 0) {
                    Editable editable = wFocusedEditText.getEditableText();
                    ArrayList<SpanPart> spanPartsOutput = new ArrayList<>(32);

                    String textWithoutFormatItemStartLeft = SpanUtil.getSpannableStringInclusiveExclusive(editable, 0, cursorStart, spanPartsOutput);
                    SpanUtil.setSpannableInclusiveExclusive(wFocusedEditText, textWithoutFormatItemStartLeft, spanPartsOutput, 0);
                    spanPartsOutput.clear();

                    String textWithoutFormatItemEndRight = SpanUtil.getSpannableStringInclusiveExclusive(editable, cursorEnd, editable.length(), spanPartsOutput);
                    IRichCellView addedRichCellView = insertACellView(focusedWrapperView, richCellView, true);

                    if (cursorEnd < textLength) {
                        WEditTextWrapperView wEditTextWrapperView  = insertACellViewWithRichType(addedRichCellView, richType, false);
                        SpanUtil.setSpannableInclusiveExclusive(wEditTextWrapperView.getWEditText(), textWithoutFormatItemEndRight, spanPartsOutput, -cursorEnd);
                    }
                }
            }
        } else {
            addRichCell(richCellView, lp, focusedIndex[0] + 1);
            if (needAddWRichEditor(focusedIndex[0] + 1)) {
                insertAWRichEditorWrapperWithRichType(focusedIndex[0] + 2, RichType.NONE, true);
            }
        }
    }

    private void initInputKeyboardListener() {

        final Context context = getContext();
        if (context == null || !(context instanceof Activity)) {
            return;
        }

        mOnGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int fullScreenHeight = CommonUtil.getFullScreenHeight((Activity) context);
                int currScreenHeight = CommonUtil.getScreenHeightExcludeKeyboard((Activity) context);
                int naviHeight = CommonUtil.getNavigationBarHeight((Activity) context);
                if (currScreenHeight + naviHeight >= fullScreenHeight) {
                    // 全屏状态
                    onKeyboardHidden();
                } else {
                    // 软键盘弹起状态
                    onKeyboardShown(currScreenHeight);
                }
            }
        };

        this.getViewTreeObserver().addOnGlobalLayoutListener(mOnGlobalLayoutListener);
    }

    private void onKeyboardHidden() {
    }

    private void onKeyboardShown(final int currScreenHeight) {
        int[] focusedIndex = new int[1];
        final WEditTextWrapperView focusedWEditTextWrapperView = findCurrentOrRecentFocusedRichEditorWrapperView(focusedIndex);
        if (focusedWEditTextWrapperView == null) {
            return;
        }
        post(new Runnable() {
            @Override
            public void run() {
                float deltaY = focusedWEditTextWrapperView.getY() - getScrollY();
                // currScreenHeight - scrollView的top的Y值
                getScrollViewYInWindow();
                int needTansY = (int) (deltaY - (currScreenHeight - getScrollViewYInWindow() - RichEditorConfig.sScrollBottomDeltaY));
                if (needTansY > 0) {
                    // 说明被隐藏
                    WRichEditor.this.smoothScrollBy(0, needTansY);
                }
            }
        });
    }

    private int getScrollViewYInWindow() {
        int[] out = new int[2];
        getLocationInWindow(out);
        return out[1];
    }

    /**
     * 当前Editor处于none状态，同时下一个也处于none状态
     *
     * @param currentIndex
     * @return
     */
    private boolean needMergeWRichEditor(int currentIndex) {
        if (mLinearLayout == null) {
            return false;
        }
        if (currentIndex < 0) {
            return false;
        }
        int childCount = mLinearLayout.getChildCount();
        if (currentIndex >= childCount) {
            // 无效index值
            return false;
        }
        // 当前为最后一个，或者当前的下一个的类型不为 NONE
        if (currentIndex == childCount - 1) {
            return false;
        }
        IRichCellView nextRichCellView = (IRichCellView) mLinearLayout.getChildAt(currentIndex + 1);
        if (nextRichCellView == null) {
            return false;
        }

        RichType richType = nextRichCellView.getRichType();
        if (richType == RichType.NONE) {
            return true;
        }
        return false;
    }

}
