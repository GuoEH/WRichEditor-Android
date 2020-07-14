package cn.carbs.wricheditor;

import android.net.UrlQuerySanitizer;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.net.URLDecoder;

public class WebViewActivity extends AppCompatActivity {

    public static final String HTML = "HTML";

    private static final String HTML_PREV = "<html lang=\"en\">" +
            "<head><meta charset=\"utf-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0\">" +
            "</head>" +
            "<body>" +
            "<p>";
    private static final String HTML_POST ="</p></body></html>";

    private WebView mWebView;

    private String mHTML;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        mWebView = findViewById(R.id.web_view);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true); //将图片调整到适合webview的大小
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小
        webSettings.setSupportZoom(false); //支持缩放，默认为true。是下面那个的前提。
        webSettings.setBuiltInZoomControls(false); //设置内置的缩放控件。若为false，则该WebView不可缩放
        webSettings.setDisplayZoomControls(false); //隐藏原生的缩放控件
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); //关闭webview中缓存
        webSettings.setAllowFileAccess(true); //设置可以访问文件
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); //支持通过JS打开新窗口
        webSettings.setLoadsImagesAutomatically(true); //支持自动加载图片
        webSettings.setDefaultTextEncodingName("utf-8");//设置编码格式


        mHTML = getIntent().getStringExtra(HTML);


        String text = HTML_PREV + mHTML + HTML_POST;

        mWebView.loadData(text, "text/html", "UTF-8");

        Log.d("wangwang", "html : " + text);
        try {
            String urlStr = URLDecoder.decode(text, "UTF-8");
            Log.d("wangwang", "urlStr : " + urlStr);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
