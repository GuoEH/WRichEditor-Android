package cn.carbs.wricheditor.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.util.AttributeSet;
import android.util.Log;
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
import cn.carbs.wricheditor.library.types.RichType;
import cn.carbs.wricheditor.library.utils.SpanUtil;
import cn.carbs.wricheditor.library.utils.StrategyUtil;
import cn.carbs.wricheditor.library.utils.ViewUtil;

/**
 * 主视图，继承自ScrollView，富文本通过向其中不断添加子View实现
 */
public class WRichEditorScrollView extends ScrollView implements OnEditorFocusChangedListener {

    // TODO
    public ArrayList<IRichCellView> mRichCellViewList = new ArrayList<>();

    public Set<RichType> mRichTypes = new HashSet<>();

    private LinearLayout mLinearLayout;

    private OnDataTransportListener mOnDataTransportListener;

    private OnRichTypeChangedListener mOnRichTypeChangedListener;

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

    // TODO 和下面的方法一同待优化
    public void addRichCell(IRichCellView richCell, LinearLayout.LayoutParams layoutParams) {
        Log.d("ppp", "scroll addRichCell");
        if (mLinearLayout == null || richCell.getView() == null) {
            return;
        }
        LinearLayout.LayoutParams lp = null;
        if (layoutParams != null) {
            lp = layoutParams;
        } else {
            lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        }
        // 子View中添加此ScrollView的引用，方便操作其它数据
        richCell.setWRichEditorScrollView(this);
        richCell.setEditorFocusChangedListener(this);
        mLinearLayout.addView(richCell.getView(), lp);
        mRichCellViewList.add(richCell);
    }

    // TODO
    public void addRichCell(IRichCellView richCell, LinearLayout.LayoutParams layoutParams, int index) {
        Log.d("ppp", "scroll addRichCell");
        if (mLinearLayout == null || richCell.getView() == null) {
            return;
        }
        LinearLayout.LayoutParams lp = null;
        if (layoutParams != null) {
            lp = layoutParams;
        } else {
            lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        }
        // 子View中添加此ScrollView的引用，方便操作其它数据
        richCell.setWRichEditorScrollView(this);
        richCell.setEditorFocusChangedListener(this);
        mLinearLayout.addView(richCell.getView(), index, lp);
        mRichCellViewList.add(index, richCell);
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

    // TODO 考虑 richType 此api的设计，是否将toggle放到此类中
    // object 中存储 link 等信息
    public void updateTextByRichTypeChanged(RichType richType, boolean open, Object object) {
        Log.d("xxx", "updateTextByRichTypeChanged richType : " + richType.name() + "  open : " + open);
        StrategyUtil.sStrongSet = true;
        // 1. 找到焦点所在的EditView
        int[] focusedRichEditorWrapperViewIndex = new int[1];
        WRichEditorWrapperView focusedWRichEditorWrapperView = findCurrentFocusedRichEditorWrapperView(focusedRichEditorWrapperViewIndex);
        if (focusedWRichEditorWrapperView == null) {
            Log.d("xxx", "updateTextByRichTypeChanged  focusedWRichEditorWrapperView == null");
            return;
        }
        // 2. 交给某个单元Cell去更新
        if (richType == RichType.QUOTE) {
            Log.d("xxx", "richType == RichType.QUOTE");
            // 如果是 "QUOTE" 类型，则：
            WRichEditor focusedWRichEditor = focusedWRichEditorWrapperView.getWRichEditor();
            if (focusedWRichEditor == null) {
                Log.d("xxx", "richType == RichType.QUOTE  1");
                return;
            }
            Editable editable = focusedWRichEditor.getEditableText();
            String editableStr = editable.toString();
            if (open) {
                if(focusedWRichEditorWrapperView.getRichType() == RichType.QUOTE) {
                    // 如果已经处于
                    return;
                }

                // 如果是 "开启" QUOTE 动作
                if (!editableStr.contains(CharConstant.LINE_BREAK_STRING)) {
                    // 1. 判断当前RichEditor中有几个换行符，如果有0个，则不"分裂"，直接将这个Editor的左侧drawable做变换
                    focusedWRichEditorWrapperView.toggleQuoteMode(true, false);
                } else {
                    // 2. 如果当前RichEditor中有一个或者多个换行符，则截取从换行符后的text，添加到新生成的Editor中
                    // TODO 没有关注cursor的位置
                    // 根据select的起点和终点，判断所处的行，并将这些行作为一个引用
                    int cursorStart = focusedWRichEditor.getSelectionStart();
                    int cursorEnd = focusedWRichEditor.getSelectionEnd();

                    // 无论光标是否选中文字，都将整体分为3部分？左光标在0，和右光标在最后的情况？TODO

                    int quoteStart = 0;
                    int quoteEnd = 0;

                    // 没有选中任何文字：
                    String cursorLeftStr = editableStr.substring(0, cursorStart);
                    String cursorRightStr = editableStr.substring(cursorEnd);
                    Log.d("yyy", "cursorLeftStr -->" + cursorLeftStr + "<--");
                    Log.d("yyy", "cursorRightStr -->" + cursorRightStr + "<--");
                    // 找到从光标位置到前面的换行符（没有换行符则到位置0）
                    int lastIndexOfEnterAmongLeftStr = cursorLeftStr.lastIndexOf(CharConstant.LINE_BREAK_CHAR);
                    // 这几个位置需要测试一下
                    if (lastIndexOfEnterAmongLeftStr == -1) {
                        quoteStart = 0;
                    } else {
                        quoteStart = lastIndexOfEnterAmongLeftStr + 1;
                    }
                    // 找到从光标位置到后面的换行符（没有换行符则到位置最后）
                    int firstIndexOfEnterAmongRightStr = cursorRightStr.indexOf(CharConstant.LINE_BREAK_CHAR);
                    if (firstIndexOfEnterAmongRightStr == -1) {
                        quoteEnd = editableStr.length();
                    } else {
                        quoteEnd = cursorEnd + firstIndexOfEnterAmongRightStr + 1;
                    }

                    int currentEditableLength = editableStr.length();

                    Log.d("yyy", "cursorStart : " + cursorStart + "  cursorEnd : " + cursorEnd);
                    Log.d("yyy", "quoteStart : " + quoteStart + "  quoteEnd : " + quoteEnd);
                    Log.d("yyy", "currentEditableLength : " + currentEditableLength);

                    if (quoteStart == currentEditableLength && quoteEnd == currentEditableLength) {
                        // 如果在最后
                        if (editableStr.endsWith(CharConstant.LINE_BREAK_STRING)) {
                            Editable editableText = focusedWRichEditor.getText();
                            List<SpanPart> spanPartsOutput0 = new ArrayList<>(32);
                            String textWithoutFormat0 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, 0, currentEditableLength - 1, spanPartsOutput0);
                            SpanUtil.setSpannableInclusiveExclusive(focusedWRichEditor, textWithoutFormat0, spanPartsOutput0, 0);
                            insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 1, RichType.QUOTE, true);
                        } else {
                            // 此种情况不存在
                        }
                    } else if (quoteStart == 0) {
                        // 四种情况
                        if (quoteEnd == currentEditableLength) {
                            Log.d("yyy", "111");
                            // 不选中时
                            // A   B   C   D   E | F   G   '/n'
                            // 选中时
                            // A   B   C   D   E | F   G   '/n'
                            // H   I   J   K   L | M   N   ('/n')
                            // 如果光标不在队尾
                            Editable editableText = focusedWRichEditor.getText();
                            List<SpanPart> spanPartsOutput0 = new ArrayList<>(32);
                            List<SpanPart> spanPartsOutput1 = new ArrayList<>(32);

                            if (editableStr.endsWith(CharConstant.LINE_BREAK_STRING)) {
                                Log.d("yyy", "1112");
                                String textWithoutFormat0 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, 0, quoteEnd - 1, spanPartsOutput0);
//                                String textWithoutFormat1 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, quoteEnd, editableText.length(), spanPartsOutput1);
//                                Log.d("yyy", "1112  textWithoutFormat1 length() : " + textWithoutFormat1.length());
                                SpanUtil.setSpannableInclusiveExclusive(focusedWRichEditor, textWithoutFormat0, spanPartsOutput0, 0);
                                focusedWRichEditorWrapperView.toggleQuoteMode(true, false);

                                // 添加一个WRichEditorWrapperView
                                insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 1, RichType.NONE, false);
                            } else {
                                Log.d("yyy", "1113");
                                focusedWRichEditorWrapperView.toggleQuoteMode(true, false);

                                if (needAddWRichEditor(focusedRichEditorWrapperViewIndex[0])) {
                                    // 添加一个WRichEditorWrapperView
                                    insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 1, RichType.NONE, false);
                                }
//                                WRichEditorWrapperView insertedWrapperView = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 1, RichType.NONE, false);
                            }
                        } else {
                            Log.d("yyy", "222");
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
                            // TODO 这里的quoteEnd应该要减一，因为末尾不应该留着 Enter
                            String textWithoutFormat0 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, 0, quoteEnd - 1, spanPartsOutput0);
                            String textWithoutFormat1 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, quoteEnd, editableText.length(), spanPartsOutput1);

                            SpanUtil.setSpannableInclusiveExclusive(focusedWRichEditor, textWithoutFormat0, spanPartsOutput0, 0);
                            focusedWRichEditorWrapperView.toggleQuoteMode(true, false);

                            // 添加一个WRichEditorWrapperView
                            WRichEditorWrapperView insertedWrapperView = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 1, RichType.NONE, false);
                            SpanUtil.setSpannableInclusiveExclusive(insertedWrapperView.getWRichEditor(), textWithoutFormat1, spanPartsOutput1, -quoteEnd);

                            // 从 quoteEnd 到 length，作为新insert一个editor
                        }
                    } else {
                        if (quoteEnd == editableStr.length()) {
                            Log.d("yyy", "333");
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
                            // TODO 这里的quoteEnd应该要减一，因为末尾不应该留着 Enter

                            String textWithoutFormat0 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, 0, quoteStart - 1, spanPartsOutput0);
                            SpanUtil.setSpannableInclusiveExclusive(focusedWRichEditor, textWithoutFormat0, spanPartsOutput0, 0);

                            if (editableStr.endsWith(CharConstant.LINE_BREAK_STRING)) {
                                Log.d("yyy", "3331");
                                String textWithoutFormat1 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, quoteStart, editableText.length() - 1, spanPartsOutput1);
                                // 添加一个WRichEditorWrapperView
                                WRichEditorWrapperView insertedWrapperView = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 1, RichType.QUOTE, true);
                                SpanUtil.setSpannableInclusiveExclusive(insertedWrapperView.getWRichEditor(), textWithoutFormat1, spanPartsOutput1, -quoteStart);
                                if (needAddWRichEditor(focusedRichEditorWrapperViewIndex[0] + 1)) {
                                    insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 2, RichType.NONE, false);
                                }
                            } else {
                                Log.d("yyy", "3332");
                                String textWithoutFormat1 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, quoteStart, editableText.length(), spanPartsOutput1);
                                // 添加一个WRichEditorWrapperView
                                WRichEditorWrapperView insertedWrapperView = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 1, RichType.QUOTE, true);
                                SpanUtil.setSpannableInclusiveExclusive(insertedWrapperView.getWRichEditor(), textWithoutFormat1, spanPartsOutput1, -quoteStart);
                                if (needAddWRichEditor(focusedRichEditorWrapperViewIndex[0] + 1)) {
                                    insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 2, RichType.NONE, false);
                                }
                            }
                        } else {
                            Log.d("yyy", "444");
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
                Log.d("xxx", "richType == RichType.QUOTE  2");
                if(focusedWRichEditorWrapperView.getRichType() != RichType.QUOTE) {
                    // 如果已经处于
                    Log.d("xxx", "richType == RichType.QUOTE  3");
                    return;
                }
                if (!editableStr.contains(CharConstant.LINE_BREAK_STRING)) {
                    Log.d("xxx", "richType == RichType.QUOTE  4");
                    // 1. 判断当前RichEditor中有几个换行符，如果有0个，则直接将当前RichEditor转换为普通类型
                    focusedWRichEditorWrapperView.toggleQuoteMode(false, false);
                } else {
                    // 2. 如果当前RichEditor中有一个或者多个换行符，则截取从换行符后的text，添加到新生成的Editor中
                    // TODO 没有关注cursor的位置
                    // 根据select的起点和终点，判断所处的行，并将这些行作为一个引用
                    int cursorStart = focusedWRichEditor.getSelectionStart();
                    int cursorEnd = focusedWRichEditor.getSelectionEnd();

                    // 无论光标是否选中文字，都将整体分为3部分？左光标在0，和右光标在最后的情况？TODO

                    int quoteStart = 0;
                    int quoteEnd = 0;

                    // 没有选中任何文字：
                    String cursorLeftStr = editableStr.substring(0, cursorStart);
                    String cursorRightStr = editableStr.substring(cursorEnd);
                    Log.d("xxx", "cursorLeftStr -->" + cursorLeftStr + "<--");
                    Log.d("xxx", "cursorRightStr -->" + cursorRightStr + "<--");
                    // 找到从光标位置到前面的换行符（没有换行符则到位置0）
                    int lastIndexOfEnterAmongLeftStr = cursorLeftStr.lastIndexOf(CharConstant.LINE_BREAK_CHAR);
                    // 这几个位置需要测试一下
                    if (lastIndexOfEnterAmongLeftStr == -1) {
                        quoteStart = 0;
                    } else {
                        quoteStart = lastIndexOfEnterAmongLeftStr + 1;
                    }
                    // 找到从光标位置到后面的换行符（没有换行符则到位置最后）
                    int firstIndexOfEnterAmongRightStr = cursorRightStr.indexOf(CharConstant.LINE_BREAK_CHAR);
                    if (firstIndexOfEnterAmongRightStr == -1) {
                        quoteEnd = editableStr.length();
                    } else {
                        quoteEnd = cursorEnd + firstIndexOfEnterAmongRightStr + 1;
                    }

                    int currentEditableLength = editableStr.length();

                    Log.d("xxx", "cursorStart : " + cursorStart + "  cursorEnd : " + cursorEnd);
                    Log.d("xxx", "quoteStart : " + quoteStart + "  quoteEnd : " + quoteEnd);
                    Log.d("xxx", "currentEditableLength : " + currentEditableLength);

                    if (quoteStart == currentEditableLength && quoteEnd == currentEditableLength) {
                        // 如果在最后
                        if (editableStr.endsWith(CharConstant.LINE_BREAK_STRING)) {
                            Editable editableText = focusedWRichEditor.getText();
                            List<SpanPart> spanPartsOutput0 = new ArrayList<>(32);
                            String textWithoutFormat0 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, 0, currentEditableLength - 1, spanPartsOutput0);
                            SpanUtil.setSpannableInclusiveExclusive(focusedWRichEditor, textWithoutFormat0, spanPartsOutput0, 0);
                            insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 1, RichType.NONE, true);
                        } else {
                            // 此种情况不存在
                        }
                    } else if (quoteStart == 0) {
                        // 四种情况
                        if (quoteEnd == currentEditableLength) {
                            Log.d("xxx", "111");
                            // 不选中时
                            // A   B   C   D   E | F   G   '/n'
                            // 选中时
                            // A   B   C   D   E | F   G   '/n'
                            // H   I   J   K   L | M   N   ('/n')
                            Editable editableText = focusedWRichEditor.getText();
                            List<SpanPart> spanPartsOutput0 = new ArrayList<>(32);
//                            List<SpanPart> spanPartsOutput1 = new ArrayList<>(32);

                            if (editableStr.endsWith(CharConstant.LINE_BREAK_STRING)) {
                                Log.d("xxx", "1112");
                                String textWithoutFormat0 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, 0, quoteEnd - 1, spanPartsOutput0);
//                                String textWithoutFormat1 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, quoteEnd, editableText.length(), spanPartsOutput1);
//                                Log.d("yyy", "1112  textWithoutFormat1 length() : " + textWithoutFormat1.length());
                                SpanUtil.setSpannableInclusiveExclusive(focusedWRichEditor, textWithoutFormat0, spanPartsOutput0, 0);
                                focusedWRichEditorWrapperView.toggleQuoteMode(false, false);

                                // 添加一个WRichEditorWrapperView
                                insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 1, RichType.NONE, false);
                            } else {
                                Log.d("yyy", "1113");
                                focusedWRichEditorWrapperView.toggleQuoteMode(false, false);

                                // TODO needMerge? 是否需要和后面一个merge，如果需要merge，则merge后，将后面一个remove掉
//                                if (needAddWRichEditor(focusedRichEditorWrapperViewIndex[0])) {
                                    // 添加一个WRichEditorWrapperView
//                                    insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 1, RichType.NONE, false);
//                                }
//                                WRichEditorWrapperView insertedWrapperView = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 1, RichType.NONE, false);
                            }
                        } else {
                            Log.d("yyy", "222");
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
                            // TODO 这里的quoteEnd应该要减一，因为末尾不应该留着 Enter
                            String textWithoutFormat0 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, 0, quoteEnd - 1, spanPartsOutput0);
                            String textWithoutFormat1 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, quoteEnd, editableText.length(), spanPartsOutput1);

                            SpanUtil.setSpannableInclusiveExclusive(focusedWRichEditor, textWithoutFormat0, spanPartsOutput0, 0);
                            focusedWRichEditorWrapperView.toggleQuoteMode(false, false);

                            // 添加一个WRichEditorWrapperView
                            WRichEditorWrapperView insertedWrapperView = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 1, RichType.NONE, false);
                            SpanUtil.setSpannableInclusiveExclusive(insertedWrapperView.getWRichEditor(), textWithoutFormat1, spanPartsOutput1, -quoteEnd);
                            insertedWrapperView.toggleQuoteMode(true, false);
                            // 从 quoteEnd 到 length，作为新insert一个editor
                        }
                    } else {
                        if (quoteEnd == editableStr.length()) {
                            Log.d("yyy", "333");
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
                            // TODO 这里的quoteEnd应该要减一，因为末尾不应该留着 Enter

                            String textWithoutFormat0 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, 0, quoteStart - 1, spanPartsOutput0);
                            SpanUtil.setSpannableInclusiveExclusive(focusedWRichEditor, textWithoutFormat0, spanPartsOutput0, 0);

                            if (editableStr.endsWith(CharConstant.LINE_BREAK_STRING)) {
                                Log.d("yyy", "3331");
                                String textWithoutFormat1 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, quoteStart, editableText.length() - 1, spanPartsOutput1);
                                // 添加一个WRichEditorWrapperView
                                WRichEditorWrapperView insertedWrapperView = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 1, RichType.NONE, true);
                                SpanUtil.setSpannableInclusiveExclusive(insertedWrapperView.getWRichEditor(), textWithoutFormat1, spanPartsOutput1, -quoteStart);
                                // todo 是否需要合并，如果需要合并，应该在上面生成 insertedWrapperView 前，判断是否动态合并，如果时，则不生成insertedWrapperView
//                                if (needAddWRichEditor(focusedRichEditorWrapperViewIndex[0] + 1)) {
//                                    insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 2, RichType.NONE, false);
//                                }
                            } else {
                                Log.d("yyy", "3332");
                                String textWithoutFormat1 = SpanUtil.getSpannableStringInclusiveExclusive(editableText, quoteStart, editableText.length(), spanPartsOutput1);
                                // 添加一个WRichEditorWrapperView
                                WRichEditorWrapperView insertedWrapperView = insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 1, RichType.NONE, true);
                                SpanUtil.setSpannableInclusiveExclusive(insertedWrapperView.getWRichEditor(), textWithoutFormat1, spanPartsOutput1, -quoteStart);
                                // todo 是否需要合并，如果需要合并，应该在上面生成 insertedWrapperView 前，判断是否动态合并，如果时，则不生成insertedWrapperView
//                                if (needAddWRichEditor(focusedRichEditorWrapperViewIndex[0] + 1)) {
//                                    insertAWRichEditorWrapperWithRichType(focusedRichEditorWrapperViewIndex[0] + 2, RichType.NONE, false);
//                                }
                            }
                        } else {
                            Log.d("yyy", "444");
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
            // 如果没有选中，只会更新headline
            // TODO 暂时先注释
//            focusedWRichEditorWrapperView.updateTextByRichTypeChanged(richType, open, object);
        } else if (richType == RichType.LIST_UNORDERED) {
            // 如果是无序列表
        } else if (richType == RichType.LIST_ORDERED) {
            // 如果是有序列表



        } else {
            focusedWRichEditorWrapperView.updateTextByRichTypeChanged(richType, open, object);
        }

    }

    private boolean needAddWRichEditor(int currentIndex) {
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
        Log.d("xxx", "findCurrentFocusedRichEditorWrapperView() null ? : " + (retWRichEditorWrapperView == null));
        if (retWRichEditorWrapperView != null) {
            focusedIndex[0] = focusedRichEditorWrapperView[0];
            return retWRichEditorWrapperView;
        }
        // TODO focusedIndex 没有返回值
        if (mLastFocusedRichCellView instanceof WRichEditorWrapperView) {
            return (WRichEditorWrapperView) mLastFocusedRichCellView;
        }
        return null;
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

    IRichCellView mLastFocusedRichCellView;

    @Override
    public void onEditorFocusChanged(IRichCellView iRichCellView, boolean focused) {
        Log.d("eee", "onEditorFocusChanged focused : " + focused);
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
        if (index == -1) {
            // 插入到队尾
            addRichCell(editTextWrapperView, lp);
        } else {
            // 插入到中间某个位置
            addRichCell(editTextWrapperView, lp, index);
        }
        return editTextWrapperView;
    }
}
