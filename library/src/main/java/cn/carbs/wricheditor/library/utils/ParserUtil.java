package cn.carbs.wricheditor.library.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.carbs.wricheditor.library.WRichEditor;
import cn.carbs.wricheditor.library.WEditTextWrapperView;
import cn.carbs.wricheditor.library.interfaces.BaseCellData;
import cn.carbs.wricheditor.library.interfaces.IRichCellData;
import cn.carbs.wricheditor.library.interfaces.IRichCellView;
import cn.carbs.wricheditor.library.models.cell.AudioCellData;
import cn.carbs.wricheditor.library.models.cell.ImageCellData;
import cn.carbs.wricheditor.library.models.cell.LineCellData;
import cn.carbs.wricheditor.library.models.cell.NetDiskCellData;
import cn.carbs.wricheditor.library.models.cell.RichCellData;
import cn.carbs.wricheditor.library.models.cell.VideoCellData;
import cn.carbs.wricheditor.library.providers.CustomViewProvider;
import cn.carbs.wricheditor.library.types.RichType;
import cn.carbs.wricheditor.library.views.RichAudioView;
import cn.carbs.wricheditor.library.views.RichImageView;
import cn.carbs.wricheditor.library.views.RichLineView;
import cn.carbs.wricheditor.library.views.RichNetDiskView;
import cn.carbs.wricheditor.library.views.RichVideoView;

public class ParserUtil {

    public static final String P_JSON_PREV = "[";
    public static final String P_JSON_POST = "]";


    public static final String P_TAG_PREV = "<p>";
    public static final String P_TAG_POST = "</p>";

    public static final String PATTERN_BODY_STR = "<body>.*</body>";
    public static final Pattern PATTERN_BODY = Pattern.compile(PATTERN_BODY_STR);

    public static final String PATTERN_DIV_CELL_VIEW_STR = "<div richType=\".*?</div>";
    public static final Pattern PATTERN_DIV_CELL_VIEW = Pattern.compile(PATTERN_DIV_CELL_VIEW_STR);

    public static final String PATTERN_DIV_RICH_TYPE_STR = "<div richType=\".*?\">";
    public static final Pattern PATTERN_DIV_RICH_TYPE = Pattern.compile(PATTERN_DIV_RICH_TYPE_STR);

    public static StringBuilder parseToHtml(WRichEditor scrollView) {

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

    public static StringBuilder parseToJson(WRichEditor scrollView) {

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

        int listCount = dataList.size();

        out.append(P_JSON_PREV);
        for (int j = 0; j < listCount; j++) {
            out.append(dataList.get(j).toJson());
            if (j < listCount - 1) {
                out.append(",");
            }
        }
        out.append(P_JSON_POST);
        return out;
    }

    // TODO 由html转换回富文本编辑器比较复杂，因此先使用json的方式
    public static void inflateFromHtml(Context context, WRichEditor scrollView, String html, CustomViewProvider provider) {
        if (html == null || html.trim().length() == 0 || scrollView == null) {
            return;
        }

        String bodyContent;
        Matcher matcherBody = PATTERN_BODY.matcher(html);
        if (matcherBody.find()) {
            String bodyHtml = matcherBody.group(0);
            bodyContent = getBodyContent(bodyHtml);
        } else {
            bodyContent = html;
        }

        ArrayList<String> cellStringList = new ArrayList<>();

        Matcher matcherCellView = PATTERN_DIV_CELL_VIEW.matcher(bodyContent);
        while (matcherCellView.find()) {
            for (int i = 0; i <= matcherCellView.groupCount(); i++) {
                // <div richType="NONE">文字</div>
                // <div richType="IMAGE"><picture><img src="https://xx.com/xx.jpg"></picture></div>
                String cellString = matcherCellView.group(i);
                cellStringList.add(cellString);
            }
        }

        for (String cellString : cellStringList) {
            IRichCellView cellView = inflateCellViewByCellHtml(context, cellString, provider);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            scrollView.addRichCell(cellView, lp, -1);
        }
        scrollView.addNoneTypeTailOptionally();
    }

    // TODO 由html转换回富文本编辑器比较复杂，因此先使用json的方式
    public static void inflateFromJson(Context context, WRichEditor scrollView, String json, CustomViewProvider provider) {
        if (json == null || json.trim().length() == 0 || scrollView == null) {
            return;
        }

        LinkedList<BaseCellData> cellDataList = new LinkedList<>();

        try {
            JSONArray jsonArray = new JSONArray(json);
            int length = jsonArray.length();
            for (int i = 0; i < length; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                BaseCellData cellData = getCellDataByJSONObject(jsonObject);
                if (cellData != null) {
                    cellDataList.add(cellData);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (BaseCellData cellData : cellDataList) {
            IRichCellView cellView = inflateCellViewByCellData(context, cellData, provider);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            scrollView.addRichCell(cellView, lp, -1);
        }
        scrollView.addNoneTypeTailOptionally();

    }

    private static BaseCellData getCellDataByJSONObject(JSONObject jsonObject) {
        BaseCellData cellData = null;
        try {
            String type = jsonObject.getString(BaseCellData.JSON_KEY_TYPE);
            RichType richType = RichType.valueOf(type);
            if (richType == RichType.NONE) {
                cellData = new RichCellData();
            } else if (richType == RichType.QUOTE) {
                cellData = new RichCellData();
            } else if (richType == RichType.LIST_UNORDERED) {
                cellData = new RichCellData();
            } else if (richType == RichType.LIST_ORDERED) {
                cellData = new RichCellData();
            } else if (richType == RichType.IMAGE) {
                cellData = new ImageCellData();
            } else if (richType == RichType.VIDEO) {
                cellData = new VideoCellData();
            } else if (richType == RichType.AUDIO) {
                cellData = new AudioCellData();
            } else if (richType == RichType.NETDISK) {
                cellData = new NetDiskCellData();
            } else if (richType == RichType.LINE) {
                cellData = new LineCellData();
            }
            if (cellData != null) {
                // 数据填充
                cellData.fromJson(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cellData;
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

    private static IRichCellView inflateCellViewByCellHtml(Context context, String cellHtml, CustomViewProvider provider) {
        int[] cellContentHtmlStart = new int[1];
        RichType cellRichType = getRichTypeByCellHtml(cellHtml, cellContentHtmlStart);
        return inflateCellViewByRichTypeAndHtml(context, cellRichType, cellHtml, cellContentHtmlStart[0], provider);
    }

    private static IRichCellView inflateCellViewByCellData(Context context, BaseCellData cellData, CustomViewProvider provider) {
        RichType richType = cellData.getRichType();
        if (richType == null || context == null) {
            return null;
        }
        IRichCellView iRichCellView = null;
        if (richType == RichType.NONE) {
            // 普通的富文本
            iRichCellView = new WEditTextWrapperView(context);
        } else if (richType == RichType.QUOTE) {
            iRichCellView = new WEditTextWrapperView(context);
        } else if (richType == RichType.LIST_UNORDERED) {
            iRichCellView = new WEditTextWrapperView(context);
        } else if (richType == RichType.LIST_ORDERED) {
            iRichCellView = new WEditTextWrapperView(context);
        } else if (richType == RichType.IMAGE) {
            if (provider == null || provider.getCellViewByRichType(richType) == null) {
                iRichCellView = new RichImageView(context);
            } else {
                iRichCellView = provider.getCellViewByRichType(richType);
            }
        } else if (richType == RichType.LINE) {
            if (provider == null || provider.getCellViewByRichType(richType) == null) {
                iRichCellView = new RichLineView(context);
            } else {
                iRichCellView = provider.getCellViewByRichType(richType);
            }
        } else if (richType == RichType.VIDEO) {
            if (provider == null || provider.getCellViewByRichType(richType) == null) {
                iRichCellView = new RichVideoView(context);
            } else {
                iRichCellView = provider.getCellViewByRichType(richType);
            }
        } else if (richType == RichType.AUDIO) {
            if (provider == null || provider.getCellViewByRichType(richType) == null) {
                iRichCellView = new RichAudioView(context);
            } else {
                iRichCellView = provider.getCellViewByRichType(richType);
            }
        } else if (richType == RichType.NETDISK) {
            if (provider == null || provider.getCellViewByRichType(richType) == null) {
                iRichCellView = new RichNetDiskView(context);
            } else {
                iRichCellView = provider.getCellViewByRichType(richType);
            }
        }
        // 这里为View填充数据
        iRichCellView.setCellData(cellData);
        return iRichCellView;
    }

    // 注册自定义 CellView
    private static IRichCellView inflateCellViewByRichTypeAndHtml(Context context, RichType richType, String cellHtml, int contentStart, CustomViewProvider provider) {
        if (richType == null || context == null) {
            return null;
        }
        IRichCellView iRichCellView = null;
        if (richType == RichType.NONE) {
            // 普通的富文本
            iRichCellView = new WEditTextWrapperView(context);
        } else if (richType == RichType.QUOTE) {
            iRichCellView = new WEditTextWrapperView(context);
        } else if (richType == RichType.LIST_UNORDERED) {
            iRichCellView = new WEditTextWrapperView(context);
        } else if (richType == RichType.LIST_ORDERED) {
            iRichCellView = new WEditTextWrapperView(context);
        } else if (richType == RichType.IMAGE) {
            if (provider == null || provider.getCellViewByRichType(richType) == null) {
                iRichCellView = new RichImageView(context);
            } else {
                iRichCellView = provider.getCellViewByRichType(richType);
            }
        } else if (richType == RichType.LINE) {
            if (provider == null || provider.getCellViewByRichType(richType) == null) {
                iRichCellView = new RichLineView(context);
            } else {
                iRichCellView = provider.getCellViewByRichType(richType);
            }
        } else if (richType == RichType.VIDEO) {
            if (provider == null || provider.getCellViewByRichType(richType) == null) {
                iRichCellView = new RichVideoView(context);
            } else {
                iRichCellView = provider.getCellViewByRichType(richType);
            }
        } else if (richType == RichType.AUDIO) {
            if (provider == null || provider.getCellViewByRichType(richType) == null) {
                iRichCellView = new RichAudioView(context);
            } else {
                iRichCellView = provider.getCellViewByRichType(richType);
            }
        } else if (richType == RichType.NETDISK) {
            if (provider == null || provider.getCellViewByRichType(richType) == null) {
                iRichCellView = new RichNetDiskView(context);
            } else {
                iRichCellView = provider.getCellViewByRichType(richType);
            }
        }
        // 去掉div
        iRichCellView.setHtmlData(richType, cellHtml.substring(contentStart, cellHtml.length() - 6));
        return iRichCellView;
    }

    private static RichType getRichTypeByCellHtml(String cellHtml, int[] start) {
//        <div richType="IMAGE">
        Matcher matcherRichType = PATTERN_DIV_RICH_TYPE.matcher(cellHtml);
        if (matcherRichType.find()) {
            // <div richType="NONE">文字</div>
            // <div richType="IMAGE"><picture><img src="https://xx.com/xx.jpg"></picture></div>
            String cellRichTypeDivStr = matcherRichType.group(0);
            start[0] = cellRichTypeDivStr.length();
            if (cellRichTypeDivStr != null) {
                String richTypeStr = cellRichTypeDivStr.substring(15, cellRichTypeDivStr.length() - 2);
                return RichType.valueOf(richTypeStr);
            }
        }
        return null;
    }

}
