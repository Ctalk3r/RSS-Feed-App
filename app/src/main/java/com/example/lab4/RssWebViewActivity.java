package com.example.lab4;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;

public class RssWebViewActivity extends Activity {

    private RssFeedModel rssFeedModel;
    private DatabaseQueries databaseQueries;
    private WebView webView;
    private int _id;
    private int postIndex;
    private String link;
    private boolean connected;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rss_activity);

        if (getActionBar() != null)
            getActionBar().hide();

        progressBar = findViewById(R.id.postProgressBar);
        link = getIntent().getStringExtra("link");
        postIndex = getIntent().getIntExtra("pos", 0);
        connected = getIntent().getBooleanExtra("connected", false);
        webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        //webView.setWebViewClient(new CustomWebViewClient());
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
            }
        });
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        if (connected) {
            webView.loadUrl(link);
        }
        else {
            webView.loadUrl("file://" + getFilesDir().getAbsolutePath() + File.separator + postIndex + ".mht");
        }
    }




    @Override
    public void onDestroy()
    {
        super.onDestroy();
        // databaseQueries.close();
    }

    @Override
    public void onBackPressed()
    {
        if(webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
