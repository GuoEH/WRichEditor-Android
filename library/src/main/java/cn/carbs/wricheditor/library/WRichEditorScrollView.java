package cn.carbs.wricheditor.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.carbs.wricheditor.library.callbacks.OnDataTransportListener;
import cn.carbs.wricheditor.library.callbacks.OnEditorFocusChangedListener;
import cn.carbs.wricheditor.library.callbacks.OnRichTypeChangedListener;
import cn.carbs.wricheditor.library.configures.RichEditorConfig;
import cn.carbs.wricheditor.library.constants.CharConstant;
import cn.carbs.wricheditor.library.interfaces.IRichCellView;
import cn.carbs.wricheditor.library.models.SpanPart;
import cn.carbs.wricheditor.library.models.SplitPart;
import cn.carbs.wricheditor.library.types.RichType;
import cn.carbs.wricheditor.library.utils.LogUtil;
import cn.carbs.wricheditor.library.utils.OrderListUtil;
import cn.carbs.wricheditor.library.utils.SpanUtil;
import cn.carbs.wricheditor.library.utils.StrategyUtil;
import cn.carbs.wricheditor.library.utils.ViewUtil;

/**
 * 主视图，继承自ScrollView，富文本通过向其中不断添加子View实现
 */
public class WRichEditorScrollView extends ScrollView implements OnEditorFocusChangedListener {

    public static final String TAG = WRichEditorScrollView.class.getSimpleName();

    public ArrayList<IRichCellView> mRichCellViewList = new ArrayList<>();

    public Set<RichType> mRichTypes = new HashSet<>();

    private LinearLayout mLinearLayout;

    private OnDataTransportListener mOnDataTransportListener;

    private OnRichTypeChangedListener mOnRichTypeChangedListener;

    private IRichCellView mLastFocusedRichCellView;

    public WRichEditorScrollView(Context context) {
        super(context);
        init(context);
    }

    public WRichEditorScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        initAttrs(attrs);
    }

    public WRichEditorScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
        initAttrs(attrs);
    }

    private void init(Context context) {
        inflate(context, R.layout.wricheditor_layout_main_view, this);
        mLinearLayout = findViewById(R.id.wricheditor_main_view_container);
        RichEditorConfig.sHeadlineTextSize = ViewUtil.dp2px(getContext(), 24);
    }

    private void initAttrs(AttributeSet attrs) {
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.WRichEditorScrollView);
        RichEditorConfig.sLinkColor = array.getColor(R.styleable.WRichEditorScrollView_linkColor, RichEditorConfig.sLinkColor);
        RichEditorConfig.sLinkUnderline = array.getBoolean(R.styleable.WRichEditorScrollView_linkUnderline, RichEditorConfig.sLinkUnderline);
        RichEditorConfig.sHeadlineTextSize = array.getDimensionPixelSize(R.styleable.WRichEditorScrollView_headlineTextSize, RichEditorConfig.sHeadlineTextSize);
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
            mRichCellViewList.add(richCell);
        } else {
            mLinearLayout.addView(richCell.getView(), index, lp);
            mRichCellViewList.add(index, richCell);
        }
    }

    /**
     *
     * @param richType
     * @param open
     * @param extra 存放 link 模式的 url
     */
    public void updateTextByRichTypeChanged(RichType richType, boolean open, Object extra) {
        LogUtil.d(TAG, "updateTextByRichTypeChanged richType : " + richType.name() + ", open : " + open + ", extra : " + extra);
        StrategyUtil.sStrongSet = true;
        // 1. 找到焦点所在的EditView
        int[] focusedRichEditorWrapperViewIndex = new int[1];
        WRichEditorWrapperView focusedWRichEditorWrapperView = findCurrentFocusedRichEditorWrapperView(focusedRichEditorWrapperViewIndex);
        if (focusedWRichEditorWrapperView == null) {
            return;
        }
        WRichEditor focusedWRichEditor = focusedWRichEditorWrapperView.getWRichEditor();
        if (focusedWRichEditor == null) {
            return;
        }
        // 2. 交给某个单元Cell去更新
        if (richType == RichType.QUOTE) {
            LogUtil.d(TAG, "richType == RichType.QUOTE");
            // 如果是 "QUOTE" 类型，则：

            Editable editable = focusedWRichEditor.getEditableText();
            String editableStr = editable.toString();
            if (open) {
                LogUtil.d(TAG, "QUOTE open");
                if (focusedWRichEditorWrapperView.getRichType() == RichType.QUOTE) {
                    // 如果已经处于
                    return;
                }

                // 如果是 "开启" QUOTE 动作
                if (!editableStr.contains(CharConstant.ENTER_STR)) {
                    // 1. 判断当前RichEditor中有几个换行符，如果有0个，则不"分裂"，直接将这个Editor的左侧drawable做变换
                    focusedWRichEditorWrapperView.toggleQuoteMode(true, false);
                } else {
                    // 2. 如果当前RichEditor中有一个或者多个换行符，则截取从换行符后的text，添加到新生成的Editor中
                    // 根据select的起点和终点，判断所处的行，并将这些行作为一个引用
                    int cursorStart = focusedWRichEditor.getSelectionStart();
                    int cursorEnd = focusedWRichEditor.getSelectionEnd();

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

                    LogUtil.d(TAG, "cursorStart : " + cursorStart + ", cursorEnd : " + cursorEnd);
                    LogUtil.d(TAG, "quoteStart : " + quoteStart + ", quoteEnd : " + quoteEnd);
                    LogUtil.d(TAG, "currentEditableLength : " + currentEditableLength);

                    if (quoteStart == currentEditableLength && quoteEnd == currentEditableLength) {
                        LogUtil.d(TAG, "QUOTE open 0");
                        // 肯定会以 Enter 键结尾
                        Editable editableText = focusedWRichEditor.getText();
                        List<SpanPart> spanPartsOutput0 = new ArrayList<>(32);
                        String textWithoutFormat0 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, 0, currentEditableLength - 1, spanPartsOutput0);
                        SpanUtil.setSpannableInclusiveExclusive(focusedWRichEditor, textWithoutFormat0, spanPartsOutput0, 0);
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
                            Editable editableText = focusedWRichEditor.getText();

                            if (editableStr.endsWith(CharConstant.ENTER_STR)) {
                                LogUtil.d(TAG, "QUOTE open 112");
                                List<SpanPart> spanPartsOutput = new ArrayList<>(32);
                                String textWithoutFormat = SpanUtil.getSpannableStringInclusiveExclusive(editableText, 0, quoteEnd - 1, spanPartsOutput);
                                SpanUtil.setSpannableInclusiveExclusive(focusedWRichEditor, textWithoutFormat, spanPartsOutput, 0);
                                focusedWRichEditorWrapperView.toggleQuoteMode(true, false);
                                insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 1, RichType.NONE, false);
                            } else {
                                LogUtil.d(TAG, "QUOTE open 113");
                                focusedWRichEditorWrapperView.toggleQuoteMode(true, false);
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
                            Editable editableText = focusedWRichEditor.getText();
                            List<SpanPart> spanPartsOutput0 = new ArrayList<>(32);
                            List<SpanPart> spanPartsOutput1 = new ArrayList<>(32);
                            // quoteEnd应该要减一，因为末尾不应该留着 Enter
                            String textWithoutFormat0 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, 0, quoteEnd - 1, spanPartsOutput0);
                            String textWithoutFormat1 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, quoteEnd, editableText.length(), spanPartsOutput1);

                            SpanUtil.setSpannableInclusiveExclusive(focusedWRichEditor, textWithoutFormat0, spanPartsOutput0, 0);
                            focusedWRichEditorWrapperView.toggleQuoteMode(true, false);

                            // 添加一个WRichEditorWrapperView
                            WRichEditorWrapperView insertedWrapperView = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 1, RichType.NONE, false);
                            SpanUtil.setSpannableInclusiveExclusive(insertedWrapperView.getWRichEditor(), textWithoutFormat1, spanPartsOutput1, -quoteEnd);
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

                            Editable editableText = focusedWRichEditor.getText();
                            List<SpanPart> spanPartsOutput0 = new ArrayList<>(32);
                            List<SpanPart> spanPartsOutput1 = new ArrayList<>(32);
                            String textWithoutFormat0 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, 0, quoteStart - 1, spanPartsOutput0);
                            SpanUtil.setSpannableInclusiveExclusive(focusedWRichEditor, textWithoutFormat0, spanPartsOutput0, 0);

                            if (editableStr.endsWith(CharConstant.ENTER_STR)) {
                                LogUtil.d(TAG, "QUOTE open 211");
                                String textWithoutFormat1 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, quoteStart, editableText.length() - 1, spanPartsOutput1);
                                // 添加一个WRichEditorWrapperView
                                WRichEditorWrapperView insertedWrapperView = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 1, RichType.QUOTE, true);
                                SpanUtil.setSpannableInclusiveExclusive(insertedWrapperView.getWRichEditor(), textWithoutFormat1, spanPartsOutput1, -quoteStart);
                                if (needAddWRichEditor(focusedRichEditorWrapperViewIndex[0] + 1)) {
                                    insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 2, RichType.NONE, false);
                                }
                            } else {
                                LogUtil.d(TAG, "QUOTE open 212");
                                String textWithoutFormat1 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, quoteStart, editableText.length(), spanPartsOutput1);
                                // 添加一个WRichEditorWrapperView
                                WRichEditorWrapperView insertedWrapperView = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 1, RichType.QUOTE, true);
                                SpanUtil.setSpannableInclusiveExclusive(insertedWrapperView.getWRichEditor(), textWithoutFormat1, spanPartsOutput1, -quoteStart);
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
                            Editable editableText = focusedWRichEditor.getText();
                            List<SpanPart> spanPartsOutput0 = new ArrayList<>(32);
                            List<SpanPart> spanPartsOutput1 = new ArrayList<>(32);
                            List<SpanPart> spanPartsOutput2 = new ArrayList<>(32);
                            String textWithoutFormat0 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, 0, quoteStart - 1, spanPartsOutput0);
                            String textWithoutFormat1 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, quoteStart, quoteEnd - 1, spanPartsOutput1);
                            String textWithoutFormat2 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, quoteEnd, editableText.length(), spanPartsOutput2);

                            SpanUtil.setSpannableInclusiveExclusive(focusedWRichEditor, textWithoutFormat0, spanPartsOutput0, 0);

                            WRichEditorWrapperView insertedWrapperView1 = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 1, RichType.QUOTE, true);
                            SpanUtil.setSpannableInclusiveExclusive(insertedWrapperView1.getWRichEditor(), textWithoutFormat1, spanPartsOutput1, -quoteStart);

                            WRichEditorWrapperView insertedWrapperView2 = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 2, RichType.NONE, false);
                            SpanUtil.setSpannableInclusiveExclusive(insertedWrapperView2.getWRichEditor(), textWithoutFormat2, spanPartsOutput2, -quoteEnd);
                        }
                    }
                }
            } else {
                // 如果是 "关闭" QUOTE 动作
                LogUtil.d(TAG, "QUOTE close");
                if (focusedWRichEditorWrapperView.getRichType() != RichType.QUOTE) {
                    return;
                }
                if (!editableStr.contains(CharConstant.ENTER_STR)) {
                    // 判断当前RichEditor中有几个换行符，如果有0个，则直接将当前RichEditor转换为普通类型
                    focusedWRichEditorWrapperView.toggleQuoteMode(false, false);
                } else {
                    // 2. 如果当前RichEditor中有一个或者多个换行符，则截取从换行符后的text，添加到新生成的Editor中
                    // 根据select的起点和终点，判断所处的行，并将这些行作为一个引用
                    int cursorStart = focusedWRichEditor.getSelectionStart();
                    int cursorEnd = focusedWRichEditor.getSelectionEnd();

                    // 无论光标是否选中文字，都将整体分为3部分？左光标在0，和右光标在最后的情况？TODO

                    int quoteStart = 0;
                    int quoteEnd = 0;

                    // 没有选中任何文字：
                    String cursorLeftStr = editableStr.substring(0, cursorStart);
                    String cursorRightStr = editableStr.substring(cursorEnd);
                    LogUtil.d(TAG, "cursorLeftStr -->" + cursorLeftStr + "<--");
                    LogUtil.d(TAG, "cursorRightStr -->" + cursorRightStr + "<--");
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

                    LogUtil.d(TAG, "cursorStart : " + cursorStart + "  cursorEnd : " + cursorEnd);
                    LogUtil.d(TAG, "quoteStart : " + quoteStart + "  quoteEnd : " + quoteEnd);
                    LogUtil.d(TAG, "currentEditableLength : " + currentEditableLength);

                    if (quoteStart == currentEditableLength && quoteEnd == currentEditableLength) {
                        // 如果在最后
                        LogUtil.d(TAG, "QUOTE close 0");
                        Editable editableText = focusedWRichEditor.getText();
                        List<SpanPart> spanPartsOutput0 = new ArrayList<>(32);
                        String textWithoutFormat0 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, 0, currentEditableLength - 1, spanPartsOutput0);
                        SpanUtil.setSpannableInclusiveExclusive(focusedWRichEditor, textWithoutFormat0, spanPartsOutput0, 0);
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
                            Editable editableText = focusedWRichEditor.getText();
                            List<SpanPart> spanPartsOutput = new ArrayList<>(32);

                            if (editableStr.endsWith(CharConstant.ENTER_STR)) {
                                LogUtil.d(TAG, "QUOTE close 111");
                                String textWithoutFormat0 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, 0, quoteEnd - 1, spanPartsOutput);
                                SpanUtil.setSpannableInclusiveExclusive(focusedWRichEditor, textWithoutFormat0, spanPartsOutput, 0);
                                focusedWRichEditorWrapperView.toggleQuoteMode(false, false);
                                insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 1, RichType.NONE, false);
                            } else {
                                LogUtil.d(TAG, "QUOTE close 112");
                                focusedWRichEditorWrapperView.toggleQuoteMode(false, false);
                                // TODO needMerge? 是否需要和后面一个merge，如果需要merge，则merge后，将后面一个remove掉
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

                            Editable editableText = focusedWRichEditor.getText();
                            List<SpanPart> spanPartsOutput0 = new ArrayList<>(32);
                            List<SpanPart> spanPartsOutput1 = new ArrayList<>(32);
                            // 这里的quoteEnd应该要减一，因为末尾不应该留着 Enter
                            String textWithoutFormat0 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, 0, quoteEnd - 1, spanPartsOutput0);
                            String textWithoutFormat1 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, quoteEnd, editableText.length(), spanPartsOutput1);

                            SpanUtil.setSpannableInclusiveExclusive(focusedWRichEditor, textWithoutFormat0, spanPartsOutput0, 0);
                            focusedWRichEditorWrapperView.toggleQuoteMode(false, false);
                            WRichEditorWrapperView insertedWrapperView = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 1, RichType.NONE, false);
                            SpanUtil.setSpannableInclusiveExclusive(insertedWrapperView.getWRichEditor(), textWithoutFormat1, spanPartsOutput1, -quoteEnd);
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

                            Editable editableText = focusedWRichEditor.getText();
                            List<SpanPart> spanPartsOutput0 = new ArrayList<>(32);
                            List<SpanPart> spanPartsOutput1 = new ArrayList<>(32);
                            // 这里的quoteEnd应该要减一，因为末尾不应该留着 Enter
                            String textWithoutFormat0 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, 0, quoteStart - 1, spanPartsOutput0);
                            SpanUtil.setSpannableInclusiveExclusive(focusedWRichEditor, textWithoutFormat0, spanPartsOutput0, 0);

                            if (editableStr.endsWith(CharConstant.ENTER_STR)) {
                                LogUtil.d(TAG, "QUOTE close 211");
                                String textWithoutFormat1 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, quoteStart, editableText.length() - 1, spanPartsOutput1);
                                // 添加一个WRichEditorWrapperView
                                WRichEditorWrapperView insertedWrapperView = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 1, RichType.NONE, true);
                                SpanUtil.setSpannableInclusiveExclusive(insertedWrapperView.getWRichEditor(), textWithoutFormat1, spanPartsOutput1, -quoteStart);
                                // todo 是否需要合并，如果需要合并，应该在上面生成 insertedWrapperView 前，判断是否动态合并，如果时，则不生成insertedWrapperView
                            } else {
                                LogUtil.d(TAG, "QUOTE close 212");
                                String textWithoutFormat1 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, quoteStart, editableText.length(), spanPartsOutput1);
                                WRichEditorWrapperView insertedWrapperView = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 1, RichType.NONE, true);
                                SpanUtil.setSpannableInclusiveExclusive(insertedWrapperView.getWRichEditor(), textWithoutFormat1, spanPartsOutput1, -quoteStart);
                                // todo 是否需要合并，如果需要合并，应该在上面生成 insertedWrapperView 前，判断是否动态合并，如果时，则不生成insertedWrapperView
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
                            Editable editableText = focusedWRichEditor.getText();
                            List<SpanPart> spanPartsOutput0 = new ArrayList<>(32);
                            List<SpanPart> spanPartsOutput1 = new ArrayList<>(32);
                            List<SpanPart> spanPartsOutput2 = new ArrayList<>(32);
                            String textWithoutFormat0 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, 0, quoteStart - 1, spanPartsOutput0);
                            String textWithoutFormat1 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, quoteStart, quoteEnd - 1, spanPartsOutput1);
                            String textWithoutFormat2 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, quoteEnd, editableText.length(), spanPartsOutput2);

                            SpanUtil.setSpannableInclusiveExclusive(focusedWRichEditor, textWithoutFormat0, spanPartsOutput0, 0);

                            WRichEditorWrapperView insertedWrapperView1 = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 1, RichType.NONE, true);
                            SpanUtil.setSpannableInclusiveExclusive(insertedWrapperView1.getWRichEditor(), textWithoutFormat1, spanPartsOutput1, -quoteStart);

                            WRichEditorWrapperView insertedWrapperView2 = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 2, RichType.QUOTE, false);
                            SpanUtil.setSpannableInclusiveExclusive(insertedWrapperView2.getWRichEditor(), textWithoutFormat2, spanPartsOutput2, -quoteEnd);
                        }
                    }
                }
            }
        } else if (richType == RichType.LIST_UNORDERED) {
            // 如果是无序列表
            LogUtil.d(TAG, "richType == RichType.LIST_UNORDERED");
            Editable editable = focusedWRichEditor.getEditableText();
            String editableStr = editable.toString();
            if (open) {
                // 如果是 "开启" LIST_UNORDERED 动作
                if (focusedWRichEditorWrapperView.getRichType() == RichType.LIST_UNORDERED) {
                    return;
                }

                if (!editableStr.contains(CharConstant.ENTER_STR)) {
                    // 1. 判断当前RichEditor中有几个换行符，如果有0个，则不"分裂"，直接将这个Editor的左侧drawable做变换
                    focusedWRichEditorWrapperView.toggleUnOrderListMode(true, false);
                    if (needAddWRichEditor(focusedRichEditorWrapperViewIndex[0])) {
                        insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 1, RichType.NONE, false);
                    }
                } else {
                    // 2. 如果当前RichEditor中有一个或者多个换行符
                    // 根据select的起点和终点，判断所处的行，并将这N行作为N个引用
                    int cursorStart = focusedWRichEditor.getSelectionStart();
                    int cursorEnd = focusedWRichEditor.getSelectionEnd();

                    int unorderedStart = 0;
                    int unorderedEnd = 0;

                    // 没有选中任何文字：
                    String cursorLeftStr = editableStr.substring(0, cursorStart);
                    String cursorRightStr = editableStr.substring(cursorEnd);
                    LogUtil.d(TAG, "cursorLeftStr -->" + cursorLeftStr + "<--");
                    LogUtil.d(TAG, "cursorRightStr -->" + cursorRightStr + "<--");
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

                    LogUtil.d(TAG, "cursorStart : " + cursorStart + "  cursorEnd : " + cursorEnd);
                    LogUtil.d(TAG, "unorderedStart : " + unorderedStart + "  unorderedEnd : " + unorderedEnd);
                    LogUtil.d(TAG, "currentEditableLength : " + currentEditableLength);

                    if (unorderedStart == currentEditableLength && unorderedEnd == currentEditableLength) {
                        // 如果在最后
                        LogUtil.d(TAG, "LIST_UNORDERED open 0");
                        Editable editableText = focusedWRichEditor.getText();
                        List<SpanPart> spanPartsOutput = new ArrayList<>(32);
                        String textWithoutFormat0 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, 0, currentEditableLength - 1, spanPartsOutput);
                        SpanUtil.setSpannableInclusiveExclusive(focusedWRichEditor, textWithoutFormat0, spanPartsOutput, 0);
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
                                    SpanUtil.setSpannableInclusiveExclusive(focusedWRichEditor, textWithoutFormat, spanPartsOutput, 0);
                                    focusedWRichEditorWrapperView.toggleUnOrderListMode(true, false);
                                } else {
                                    WRichEditorWrapperView wRichEditorWrapperView = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + i, RichType.LIST_UNORDERED, false);
                                    SpanUtil.setSpannableInclusiveExclusive(wRichEditorWrapperView.getWRichEditor(), textWithoutFormat, spanPartsOutput, -itemSplit.getStart());
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
                                    SpanUtil.setSpannableInclusiveExclusive(focusedWRichEditor, textWithoutFormat, spanPartsOutput, 0);
                                    focusedWRichEditorWrapperView.toggleUnOrderListMode(true, false);
                                } else {
                                    WRichEditorWrapperView wRichEditorWrapperView = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + i, RichType.LIST_UNORDERED, false);
                                    SpanUtil.setSpannableInclusiveExclusive(wRichEditorWrapperView.getWRichEditor(), textWithoutFormat, spanPartsOutput, -itemSplit.getStart());
                                }
                            }

                            spanPartsOutput.clear();
                            String textWithoutFormat = SpanUtil.getSpannableStringInclusiveExclusive(editable, unorderedEnd, editable.length(), spanPartsOutput);
                            WRichEditorWrapperView insertedWrapperView = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + splitLength, RichType.NONE, false);
                            SpanUtil.setSpannableInclusiveExclusive(insertedWrapperView.getWRichEditor(), textWithoutFormat, spanPartsOutput, -unorderedEnd);
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

                            SpanUtil.setSpannableInclusiveExclusive(focusedWRichEditor, textWithoutFormat, spanPartsOutput, 0);
                            // TODO 别的地方还没有添加
                            focusedWRichEditorWrapperView.toggleUnOrderListMode(false, false);

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
                                WRichEditorWrapperView wRichEditorWrapperView = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + i + 1, RichType.LIST_UNORDERED, false);
                                SpanUtil.setSpannableInclusiveExclusive(wRichEditorWrapperView.getWRichEditor(), textWithoutFormatItem, spanPartsOutput, -itemSplit.getStart());
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
                            SpanUtil.setSpannableInclusiveExclusive(focusedWRichEditor, textWithoutFormatUnselectedStrBefore, spanPartsOutput, 0);
                            focusedWRichEditorWrapperView.toggleUnOrderListMode(false, false);

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
                                LogUtil.d(TAG, "LIST_UNORDERED  i : " + i + " splitLength : " + splitLength
                                        + " itemSplit text : " + itemSplit.getText()
                                        + " itemSplit.start() : " + itemSplit.getStart()
                                        + " itemSplit.end() : " + itemSplit.getEnd());
                                String textWithoutFormatItem = SpanUtil.getSpannableStringInclusiveExclusive(editable, itemSplit.getStart(), itemSplit.getEnd(), spanPartsOutput);
                                WRichEditorWrapperView wRichEditorWrapperView = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + i + 1, RichType.LIST_UNORDERED, false);
                                SpanUtil.setSpannableInclusiveExclusive(wRichEditorWrapperView.getWRichEditor(), textWithoutFormatItem, spanPartsOutput, -itemSplit.getStart());
                            }

                            // 第三部分
                            spanPartsOutput.clear();
                            String textWithoutFormat = SpanUtil.getSpannableStringInclusiveExclusive(editable, unorderedEnd, editableStr.length(), spanPartsOutput);
                            WRichEditorWrapperView insertedWrapperViewForThirdPart = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + splitLength + 1, RichType.NONE, false);
                            SpanUtil.setSpannableInclusiveExclusive(insertedWrapperViewForThirdPart.getWRichEditor(), textWithoutFormat, spanPartsOutput, -unorderedEnd);
                        }
                    }
                }
            } else {
                // 如果是 "关闭" LIST_UNORDERED 动作
                LogUtil.d(TAG, "LIST_UNORDERED close 0");
                if (focusedWRichEditorWrapperView.getRichType() != RichType.LIST_UNORDERED) {
                    return;
                }
                // 因为一个回车一行，所以直接关掉
                focusedWRichEditorWrapperView.toggleUnOrderListMode(false, false);
            }
        } else if (richType == RichType.LIST_ORDERED) {
            // 如果是有序列表
            LogUtil.d(TAG, "richType == RichType.LIST_ORDERED");
            // 如果是 "LIST_ORDERED" 类型，则：
            Editable editable = focusedWRichEditor.getEditableText();
            String editableStr = editable.toString();
            if (open) {
                // 如果是 "开启" LIST_ORDERED 动作
                if (focusedWRichEditorWrapperView.getRichType() == RichType.LIST_ORDERED) {
                    return;
                }
                if (!editableStr.contains(CharConstant.ENTER_STR)) {
                    // 1. 判断当前RichEditor中有几个换行符，如果有0个，则不"分裂"，直接将这个Editor的左侧drawable做变换
                    focusedWRichEditorWrapperView.toggleOrderListMode(true, false);
                    if (needAddWRichEditor(focusedRichEditorWrapperViewIndex[0])) {
                        insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 1, RichType.NONE, false);
                    }
                    OrderListUtil.updateOrderListNumbersAfterViewsChanged(mLinearLayout);
                } else {
                    // 2. 如果当前RichEditor中有一个或者多个换行符
                    // 根据select的起点和终点，判断所处的行，并将这N行作为N个引用
                    int cursorStart = focusedWRichEditor.getSelectionStart();
                    int cursorEnd = focusedWRichEditor.getSelectionEnd();

                    int orderedStart = 0;
                    int orderedEnd = 0;

                    // 没有选中任何文字：
                    String cursorLeftStr = editableStr.substring(0, cursorStart);
                    String cursorRightStr = editableStr.substring(cursorEnd);
                    LogUtil.d(TAG, "cursorLeftStr -->" + cursorLeftStr + "<--");
                    LogUtil.d(TAG, "cursorRightStr -->" + cursorRightStr + "<--");
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

                    LogUtil.d(TAG, "cursorStart : " + cursorStart + "  cursorEnd : " + cursorEnd);
                    LogUtil.d(TAG, "orderedStart : " + orderedStart + "  orderedEnd : " + orderedEnd);
                    LogUtil.d(TAG, "currentEditableLength : " + currentEditableLength);

                    if (orderedStart == currentEditableLength && orderedEnd == currentEditableLength) {
                        // 如果在最后
                        Editable editableText = focusedWRichEditor.getText();
                        List<SpanPart> spanPartsOutput = new ArrayList<>(32);
                        String textWithoutFormat = SpanUtil.getSpannableStringInclusiveExclusive(editableText, 0, currentEditableLength - 1, spanPartsOutput);
                        SpanUtil.setSpannableInclusiveExclusive(focusedWRichEditor, textWithoutFormat, spanPartsOutput, 0);
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
                                    SpanUtil.setSpannableInclusiveExclusive(focusedWRichEditor, textWithoutFormat, spanPartsOutput, 0);
                                    focusedWRichEditorWrapperView.toggleOrderListMode(true, false);
                                } else {
                                    WRichEditorWrapperView wRichEditorWrapperView = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + i, RichType.LIST_ORDERED, false);
                                    SpanUtil.setSpannableInclusiveExclusive(wRichEditorWrapperView.getWRichEditor(), textWithoutFormat, spanPartsOutput, -itemSplit.getStart());
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
                                    SpanUtil.setSpannableInclusiveExclusive(focusedWRichEditor, textWithoutFormat, spanPartsOutput, 0);
                                    focusedWRichEditorWrapperView.toggleOrderListMode(true, false);
                                } else {
                                    WRichEditorWrapperView wRichEditorWrapperView = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + i, RichType.LIST_ORDERED, false);
                                    SpanUtil.setSpannableInclusiveExclusive(wRichEditorWrapperView.getWRichEditor(), textWithoutFormat, spanPartsOutput, -itemSplit.getStart());
                                }
                            }
                            spanPartsOutput.clear();
                            String textWithoutFormat = SpanUtil.getSpannableStringInclusiveExclusive(editable, orderedEnd, editable.length(), spanPartsOutput);
                            WRichEditorWrapperView insertedWrapperView = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + splitLength, RichType.NONE, false);
                            SpanUtil.setSpannableInclusiveExclusive(insertedWrapperView.getWRichEditor(), textWithoutFormat, spanPartsOutput, -orderedEnd);
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
                            SpanUtil.setSpannableInclusiveExclusive(focusedWRichEditor, textWithoutFormat, spanPartsOutput, 0);
                            focusedWRichEditorWrapperView.toggleOrderListMode(false, false);

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
                                WRichEditorWrapperView wRichEditorWrapperView = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + i + 1, RichType.LIST_ORDERED, false);
                                SpanUtil.setSpannableInclusiveExclusive(wRichEditorWrapperView.getWRichEditor(), textWithoutFormatItem, spanPartsOutput, -itemSplit.getStart());
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
                            SpanUtil.setSpannableInclusiveExclusive(focusedWRichEditor, textWithoutFormatUnselectedStrBefore, spanPartsOutput, 0);
                            focusedWRichEditorWrapperView.toggleOrderListMode(false, false);

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
                                WRichEditorWrapperView wRichEditorWrapperView = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + i + 1, RichType.LIST_ORDERED, false);
                                SpanUtil.setSpannableInclusiveExclusive(wRichEditorWrapperView.getWRichEditor(), textWithoutFormatItem, spanPartsOutput, -itemSplit.getStart());
                            }

                            // 第三部分
                            spanPartsOutput.clear();
                            String textWithoutFormat = SpanUtil.getSpannableStringInclusiveExclusive(editable, orderedEnd, editableStr.length(), spanPartsOutput);
                            WRichEditorWrapperView insertedWrapperViewForThirdPart = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + splitLength + 1, RichType.NONE, false);
                            SpanUtil.setSpannableInclusiveExclusive(insertedWrapperViewForThirdPart.getWRichEditor(), textWithoutFormat, spanPartsOutput, -orderedEnd);

                            OrderListUtil.updateOrderListNumbersAfterViewsChanged(mLinearLayout);
                        }
                    }
                }
            } else {
                LogUtil.d(TAG, "LIST_ORDERED close");
                if (focusedWRichEditorWrapperView.getRichType() != RichType.LIST_ORDERED) {
                    return;
                }
                // 因为一个回车一行，所以直接关掉
                focusedWRichEditorWrapperView.toggleOrderListMode(false, false);
                OrderListUtil.updateOrderListNumbersAfterViewsChanged(mLinearLayout);
            }
        } else {
            focusedWRichEditor.updateTextByRichTypeChanged(richType, open, extra);
        }

    }

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


    public int getRichEditorWrapperViewIndex(WRichEditorWrapperView wrapperView) {
        int cellViewSize = mRichCellViewList.size();
        for (int i = 0; i < cellViewSize; i++) {
            IRichCellView cellView = mRichCellViewList.get(i);
            if (cellView == wrapperView) {
                return i;
            }
        }
        return -1;
    }

    public WRichEditorWrapperView findCurrentFocusedRichEditorWrapperView(int[] indexInCellViewList) {
        indexInCellViewList[0] = -1;
        WRichEditorWrapperView focusedWRichEditorWrapperView = null;
        int cellViewSize = mRichCellViewList.size();
        for (int i = 0; i < cellViewSize; i++) {
            IRichCellView cellView = mRichCellViewList.get(i);
            if (cellView != null && cellView.getView() != null) {
                if (cellView.getRichType().getHasEditor()) {
                    WRichEditorWrapperView wRichEditorWrapperView = ((WRichEditorWrapperView) cellView.getView());
                    if (wRichEditorWrapperView != null && wRichEditorWrapperView.getWRichEditor() != null) {
                        if (wRichEditorWrapperView.getWRichEditor().hasFocus()) {
                            focusedWRichEditorWrapperView = wRichEditorWrapperView;
                            indexInCellViewList[0] = i;
                            break;
                        }
                    }
                }
            }
        }
        return focusedWRichEditorWrapperView;
    }

    public WRichEditorWrapperView findCurrentOrRecentFocusedRichEditorWrapperView(int[] focusedIndex) {
        int[] focusedRichEditorWrapperView = new int[1];
        focusedRichEditorWrapperView[0] = -1;
        WRichEditorWrapperView retWRichEditorWrapperView = findCurrentFocusedRichEditorWrapperView(focusedRichEditorWrapperView);
        if (retWRichEditorWrapperView != null) {
            focusedIndex[0] = focusedRichEditorWrapperView[0];
            return retWRichEditorWrapperView;
        }
        if (mLastFocusedRichCellView instanceof WRichEditorWrapperView) {
            return (WRichEditorWrapperView) mLastFocusedRichCellView;
        }
        return null;
    }

    public void importData(OnDataTransportListener listener) {
        // TODO
        mOnDataTransportListener = listener;

    }

    public void exportData(OnDataTransportListener listener) {
        // TODO
        mOnDataTransportListener = listener;

    }

    public void setRichTypes(Set<RichType> currRichTypes) {
        mRichTypes.clear();
        mRichTypes.addAll(currRichTypes);
    }

    public Set<RichType> getRichTypes() {
        return mRichTypes;
    }

    // TODO 考虑此API的设计
    public boolean toggleCertainRichType(RichType richType) {
        if (mRichTypes.contains(richType)) {
            mRichTypes.remove(richType);
            return false;
        } else {
            mRichTypes.add(richType);
            return true;
        }
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

    public WRichEditorWrapperView insertAWRichEditorWrapperWithRichType(int index, RichType richType, boolean needRequestFocusWhenAdded) {
        WRichEditorWrapperView editTextWrapperView = new WRichEditorWrapperView(getContext(), richType, needRequestFocusWhenAdded);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        editTextWrapperView.getWRichEditor().setHint("CELL new ");
        editTextWrapperView.setBackgroundColor(0x10222222);
        // 插入到中间某个位置或者队尾（index == -1）
        addRichCell(editTextWrapperView, lp, index);
        return editTextWrapperView;
    }

    public WRichEditorWrapperView insertAWRichEditorWrapperWithRichType(WRichEditorWrapperView wrapperView, RichType richType, boolean needRequestFocusWhenAdded) {
        int index = -1;
        if (wrapperView != null) {
            index = getRichEditorWrapperViewIndex(wrapperView) + 1;
        }
        return insertAWRichEditorWrapperWithRichType(index, richType, needRequestFocusWhenAdded);
    }

    public ViewGroup getContainerView() {
        return mLinearLayout;
    }

}
