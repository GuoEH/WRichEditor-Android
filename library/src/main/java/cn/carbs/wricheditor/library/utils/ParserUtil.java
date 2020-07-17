package cn.carbs.wricheditor.library.utils;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.carbs.wricheditor.library.WRichEditorScrollView;
import cn.carbs.wricheditor.library.interfaces.IRichCellData;
import cn.carbs.wricheditor.library.interfaces.IRichCellView;

public class ParserUtil {

    public static final String P_TAG_PREV = "<p>";
    public static final String P_TAG_POST = "</p>";

    public static final String PATTERN_BODY_STR = "<body>.*</body>";
    public static final Pattern PATTERN_BODY = Pattern.compile(PATTERN_BODY_STR);

    public static final String PATTERN_DIV_RICH_TYPE_STR = "<div richType=\".*?</div>";
    public static final Pattern PATTERN_DIV_RICH_TYPE = Pattern.compile(PATTERN_DIV_RICH_TYPE_STR);

    public static StringBuilder parseToHtml(WRichEditorScrollView scrollView) {

        StringBuilder out = new StringBuilder();

        if (scrollView == null || scrollView.getContainerView() == null) {
            return out;
        }

        ViewGroup containerView = scrollView.getContainerView();
        int childCount = containerView.getChildCount();
        ArrayList<IRichCellData> dataList = new ArrayList<>(childCount);
        for (int i = 0; i < childCount; i++) {
            View childView = containerView.getChildAt(i);
            if (childView instanceof IRichCellView) {
                IRichCellView richCellView = (IRichCellView) childView;
                IRichCellData iRichCellData = richCellView.getCellData();
                if (iRichCellData == null) {
                    continue;
                }
                dataList.add(iRichCellData);
            }
        }

        for (IRichCellData richCellData : dataList) {
            out.append(P_TAG_PREV + richCellData.toHtml() + P_TAG_POST);
        }

        return out;
    }

    public static void inflateFromHtml(WRichEditorScrollView scrollView, String html) {
        if (html == null || html.trim().length() == 0) {
            return;
        }

        String body;
        Matcher matcherBody = PATTERN_BODY.matcher(html);
        if (matcherBody.find()) {
            String bodyHtml = matcherBody.group(0);
            body = getBodyContent(bodyHtml);
        } else {
            body = html;
        }

        Log.d("tttt", "======>  body : " + body);
        ArrayList<String> cellStringList = new ArrayList<>();

        Matcher matcherRichType = PATTERN_DIV_RICH_TYPE.matcher(body);
        while (matcherRichType.find()) {
            for (int i = 0; i <= matcherRichType.groupCount(); i++) {
                // <div richType="NONE">文字</div>
                // <div richType="IMAGE"><picture><img src="https://xx.com/xx.jpg"></picture></div>
                String cellString = matcherRichType.group(i);
                cellStringList.add(cellString);
            }
        }

        int cellSize = cellStringList.size();
        for (int i = 0; i < cellStringList.size(); i++) {

        }

    }

    private static String getBodyContent(String bodyHtml) {
        if (bodyHtml == null) {
            return "";
        }
        try {
            return bodyHtml.substring(6, bodyHtml.length() - 7);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String getRichTypeCellContent(String richTypeHtml) {
        if (richTypeHtml == null) {
            return "";
        }
        return "";
    }

}
