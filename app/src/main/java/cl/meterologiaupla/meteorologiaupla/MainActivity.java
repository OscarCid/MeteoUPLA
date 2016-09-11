package cl.meterologiaupla.meteorologiaupla;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.pushbots.push.Pushbots;

import cl.meterologiaupla.meteorologiaupla.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Pushbots.sharedInstance().init(this);

        final WebView webView = new WebView(MainActivity.this);// webview in mainactivity
        webView.clearCache(true);
        setContentView(webView);// set the webview as the layout

        final Bundle extras = getIntent().getExtras();

        if (null != extras && getIntent().getExtras().containsKey("author"))
        {
            webView.getSettings().setJavaScriptEnabled(true);
            webView.loadUrl("http://sistema.meteorologiaupla.cl/Clima/Android/webview/index.php");
            webView.setWebViewClient(new WebViewClient(){
                public void onPageFinished(WebView view, String url){
                    webView.loadUrl("javascript:init('" + extras.getString("author") + "')");
                }
            });
        }
        else
        {
            webView.getSettings().setJavaScriptEnabled(true);
            webView.loadUrl("http://sistema.meteorologiaupla.cl/Clima/Android/webview/index.php");

            webView.setWebViewClient(new MyWebViewClient());
        }

    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (Uri.parse(url).getHost().equals("http://sistema.meteorologiaupla.cl")) {
                // This is my web site, so do not override; let my WebView load the page
                return false;
            }
            // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            return true;
        }
    }
}
