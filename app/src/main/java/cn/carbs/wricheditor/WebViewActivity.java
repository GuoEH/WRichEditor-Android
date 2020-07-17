package cn.carbs.wricheditor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import cn.carbs.wricheditor.library.utils.CommonUtil;

public class WebViewActivity extends AppCompatActivity {

    public static final String HTML = "HTML";

    private static final String HTML_PREV = "<html lang=\"en\">" +
            "<head><meta charset=\"utf-8\"><meta name=\"viewport\" content=\"width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0\">" +
            "<style>\n" +
            "        *{\n" +
            "            margin: 0;\n" +
            "            padding: 0;\n" +
            "        }\n" +
            "        html{\n" +
            "            height: 100%;\n" +
            "        }\n" +
            "        body{\n" +
            "            height: 100%;\n" +
            "        }\n" +
            "    </style>" +
            "</head>" +
            "<body>";
    private static final String HTML_POST = "</body></html>";

    private WebView mWebView;

    private TextView mTVInflate;

    private String mHTML;

    private String mText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        mWebView = findViewById(R.id.web_view);
        mTVInflate = findViewById(R.id.tv_inflate);
        mTVInflate.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WebViewActivity.this, RestoreActivity.class);
                intent.putExtra(RestoreActivity.HTML, mText);
                startActivity(intent);
            }
        });

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true); //将图片调整到适合webview的大小
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSettings.setDefaultTextEncodingName("utf-8");//设置编码格式
//        webSettings.setLoadWithOverviewMode(true);
//        webSettings.setDisplayZoomControls(false); //隐藏原生的缩放控件
//        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); //关闭webview中缓存
//        webSettings.setAllowFileAccess(true); //设置可以访问文件
//        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); //支持通过JS打开新窗口
//        webSettings.setLoadsImagesAutomatically(true); //支持自动加载图片
//        webSettings.setSupportZoom(true); //支持缩放，默认为true。是下面那个的前提。
//        webSettings.setBuiltInZoomControls(false); //设置内置的缩放控件。若为false，则该WebView不可缩放

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                loadJS();
            }
        });

        mHTML = getIntent().getStringExtra(HTML);
        mText = HTML_PREV + mHTML + HTML_POST;
        mWebView.loadData(mText, "text/html", "UTF-8");
        Log.d("wangwang", "WebViewActivity exported html : " + mHTML);
    }

    private void loadJS() {
        int webViewDp = CommonUtil.getScreenWidthDP(this) - 32;
        mWebView.loadUrl("javascript:(function(){"
//                + " var divs = document.getElementsByTagName(\"div\");"
//                + " for (var j = 0; j < divs.length; j++) {"
//                + "     divs[j].style.margin = \"0px\";"
//                + "     divs[j].style.padding = \"0px\";"
//                + "     divs[j].style.width = document.body.clientWidth-10;"
//                + " }"
                + " var imgs = document.getElementsByTagName(\"img\");"
                + " for (var i = 0; i < imgs.length; i++) {"
                + "     var vkeyWords = /.gif$/;"
                + "     if (!vkeyWords.test(imgs[i].src)) {"
                + "         var hRatio =" + webViewDp + " / imgs[i].width;"
                + "         imgs[i].height = imgs[i].height * hRatio;"//通过缩放比例来设置图片的高度
                + "         imgs[i].width= " + webViewDp + ";"//设置图片的宽度
                + "     }"
                + " }"
                + " })()");
    }
}