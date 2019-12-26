package com.example.lab4;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.Toast;

public class RssWebViewActivity extends Activity {

    private RssFeedModel rssFeedModel;
    private DatabaseQueries databaseQueries;
    private WebView webView;
    private int _id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rss_activity);

        if (getActionBar() != null)
            getActionBar().hide();


        databaseQueries = new DatabaseQueries(getApplicationContext());
        databaseQueries.open();

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            _id = extras.getInt("_id");
            rssFeedModel = databaseQueries.getRSSModels(_id);

            if (rssFeedModel != null) {
                webView = findViewById(R.id.webView);
                webView.getSettings().setJavaScriptEnabled(true);
                webView.setWebViewClient(new CustomWebViewClient());
                webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
                webView.loadUrl(rssFeedModel.link);
            }

            else
            {
                Toast.makeText(this, "Cant find rssFeedModel by given id: " + _id, Toast.LENGTH_LONG).show();
            }

        }
    }




    @Override
    public void onDestroy()
    {
        super.onDestroy();
        databaseQueries.close();
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
