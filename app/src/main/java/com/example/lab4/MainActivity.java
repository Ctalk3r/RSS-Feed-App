package com.example.lab4;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.droidparts.widget.ClearableEditText;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static java.lang.Math.min;


public class MainActivity extends AppCompatActivity implements NetworkStateReceiver.NetworkStateReceiverListener {

    private CustomArrayAdapter customArrayAdapter;
    private AdapterView adapterView;
    private DatabaseQueries databaseQueries;
    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout mSwipeLayout;
    private ClearableEditText mEditText;
    private Button networkButton;
    private NetworkStateReceiver networkStateReceiver;
    private boolean isConnected = false;

    private ArrayList<RssFeedModel> mFeedModelList;

    private static final int requestCode = 1;

    public MainActivity() {
    }

    void showDialog(boolean cancelable) {
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(R.layout.dialog_layout)
                .setTitle("RSS feed url")
                .setPositiveButton("Set", null)
                .setCancelable(cancelable)
                .create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button button = ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
                if (pref.contains("Feed") && !TextUtils.isEmpty(pref.getString("Feed", ""))) {
                    EditText editText = (EditText)((AlertDialog)dialog).findViewById(R.id.rss_url);
                    editText.setText(pref.getString("Feed", ""));
                }
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText editText = (EditText)((AlertDialog)dialog).findViewById(R.id.rss_url);
                        if (TextUtils.isEmpty(editText.getText()))
                            return;
                        SharedPreferences pr = getApplicationContext().getSharedPreferences("MyPref", 0);
                        pr.edit().putString("Feed", editText.getText().toString()).apply();
                        getSupportActionBar().setTitle("RSS feed = " + editText.getText().toString().replace("https://", "").replace("http://", "").replace("www.", ""));
                        dialog.dismiss();
                    }
                });
            }
        });
        dialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        mEditText = findViewById(R.id.rss_url);
        networkButton = findViewById(R.id.network_button);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        if (pref.contains("Feed") && !TextUtils.isEmpty(pref.getString("Feed", ""))) {
            // getSupportActionBar().setTitle("RSS feed = " + pref.getString("Feed", ""));
        }
        else {
            getSupportActionBar().setTitle("RSS reader");
            showDialog(false);
        }
        if (pref.contains("Feed") && !TextUtils.isEmpty(pref.getString("Feed", "")))
            getSupportActionBar().setTitle("RSS feed = " + pref.getString("Feed", "").replace("https://", "").replace("http://", "").replace("www.", ""));


        databaseQueries = new DatabaseQueries(this);
        databaseQueries.open();

        // SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd 'at' hh.mm", Locale.getDefault());
        // String currentDate = sdf.format(new Date());
        ArrayList<RssFeedModel> array = databaseQueries.getRSSModels();
        customArrayAdapter = new CustomArrayAdapter(this, array, networkButton);
       // if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
       // {
            ListView listView = findViewById(R.id.listview);
            listView.setAdapter(customArrayAdapter);

            adapterView = listView;
        /*}
        else
        {
            GridView gridView = findViewById(R.id.gridview);
            gridView.setAdapter(customArrayAdapter);
            adapterView = gridView;
        }*/


        adapterView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                RssFeedModel rssFeedModel = (RssFeedModel) customArrayAdapter.getItem(position);
                if (rssFeedModel != null)
                {
                    Intent intent = new Intent(getApplicationContext(), RssWebViewActivity.class);
                    //intent.putExtra("_id", rssFeedModel.id);

                    //int pos = mFeedModelList.indexOf(rssFeedModel);
                    intent.putExtra("pos", position);
                    intent.putExtra("link", rssFeedModel.link);
                    intent.putExtra("connected", isConnected);
                    startActivityForResult(intent, requestCode);
                }
            }
        });


        mSwipeLayout = findViewById(R.id.swipeRefreshLayout);
        final Context context = this;
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new FetchFeedTask(context).execute((Void) null);
            }
        });

        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        this.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
    }


    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public  void onStart()
    {
        super.onStart();
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    public void onStop()
    {
        super.onStop();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        networkStateReceiver.removeListener(this);
        this.unregisterReceiver(networkStateReceiver);
        databaseQueries.close();
    }

    @Override
    public void networkAvailable() {
        Toast.makeText(MainActivity.this, "Internet available", Toast.LENGTH_SHORT).show();
        isConnected = true;
        networkButton.setVisibility(View.INVISIBLE);
    }

    @Override
    public void networkUnavailable() {
        isConnected = false;
        networkButton.setVisibility(View.VISIBLE);
    }

    public void onClick(View view) {
        Intent intent = new Intent(this, RssWebViewActivity.class);
        startActivityForResult(intent, requestCode);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.custom_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.edit) {
            showDialog(true);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /*@Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        switch (v.getId()) {
            case R.id.listview:
                AdapterView.AdapterContextMenuInfo info =
                        (AdapterView.AdapterContextMenuInfo) menuInfo;

                String [] actions = getResources().getStringArray(R.array.context_menu);
                for (int i=0;i<actions.length;i++){
                    menu.add(Menu.NONE, i, i, actions[i]);
                }
                break;
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int menuItemIndex = item.getItemId();
        String [] menuItems = getResources().getStringArray(R.array.context_menu);
        String menuItemName = menuItems[menuItemIndex];

        switch (menuItemName) {
            case "Delete":
                // Do something...
                break;

        }
        return true;
    }*/

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == MainActivity.requestCode){
            if(resultCode == RESULT_OK){
                DatabaseQueries databaseQueries = new DatabaseQueries(this);
                databaseQueries.open();
                ArrayList<RssFeedModel> array = databaseQueries.getRSSModels();
                customArrayAdapter = new CustomArrayAdapter(this, array, networkButton);
                adapterView.setAdapter(customArrayAdapter);
                databaseQueries.close();
            }
        }
        else{
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private class FetchFeedTask extends AsyncTask<Void, Void, Boolean> {

        private String urlLink;
        private Context context;
        private ArrayList<WebView> webViews = new ArrayList<>();

        public FetchFeedTask(Context context)
        {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            if (!isConnected) {
                Toast.makeText(MainActivity.this,
                        "No internet connection",
                        Toast.LENGTH_LONG).show();
                return;
            }
            mSwipeLayout.setRefreshing(true);
            SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
            urlLink = pref.getString("Feed", "");
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (TextUtils.isEmpty(urlLink))
                return false;
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.INTERNET)
                    == PackageManager.PERMISSION_GRANTED) {
                try {
                    if(!urlLink.startsWith("http://") && !urlLink.startsWith("https://"))
                        urlLink = "https://" + urlLink;

                    mFeedModelList = getDataFromUrl(urlLink);
                    if (mFeedModelList != null) {
                        downloadImages(mFeedModelList);
                        return true;
                    }
                } catch (IOException e) {
                    int a = 2;
                }
                catch (XmlPullParserException e) {
                    int a = 3;
                }
                catch (Exception e) {
                    int a = 4;
                }
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            mSwipeLayout.setRefreshing(false);

            if (success) {
                databaseQueries.clear();
                databaseQueries.clear();
                for (int i = 0; i < mFeedModelList.size(); i++) {
                    if (i <= 10)
                        mFeedModelList.get(i).id = databaseQueries.insert(mFeedModelList.get(i));
                    WebView view = new WebView(context);
                    final int k = i;
                    view.setWebViewClient(new WebViewClient()
                    {
                        @Override
                        public void onPageFinished(WebView view, String url)
                        {
                            view.saveWebArchive(context.getFilesDir().getAbsolutePath() + File.separator + k + ".mht");
                        }
                    });
                    view.loadUrl(mFeedModelList.get(i).link);
                    webViews.add(view);
                }
                // Fill Views
                customArrayAdapter = new CustomArrayAdapter(MainActivity.this, mFeedModelList, networkButton);
                // if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                // {
                    ListView listView = findViewById(R.id.listview);
                    listView.setAdapter(customArrayAdapter);

                    adapterView = listView;
                /* }
                else
                {
                    GridView gridView = findViewById(R.id.gridview);
                    gridView.setAdapter(customArrayAdapter);
                    adapterView = gridView;
                }*/
            } else if (isConnected){
                Toast.makeText(MainActivity.this,
                        "Enter a valid Rss feed url",
                        Toast.LENGTH_LONG).show();
            }
        }

        private ArrayList<RssFeedModel> getDataFromUrl(String urlString) throws XmlPullParserException, IOException {
            ArrayList<RssFeedModel> RssFeedModels = null;
            InputStream stream = null;
            XmlParser parser = new XmlParser();
            StringBuilder htmlString = new StringBuilder();
            try {
                stream = openConnection(urlString);
                RssFeedModels = parser.parse(stream);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
            return RssFeedModels;
        }

        private InputStream openConnection(String urlAddress) throws IOException {
            InputStream in = null;        
            int response = -1;

            URL url = new URL(urlAddress);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            try {
                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.setRequestMethod("GET");
                connection.setDoInput(true);

                connection.connect();

                response = connection.getResponseCode();

                if (response / 100 == 2) {
                    in = connection.getInputStream();
                }
            } catch (Exception ex) {
                throw new IOException("Connection error");
            }
            return in;
        }
    }

    private void downloadImages(ArrayList<RssFeedModel> result) throws IOException {
        File dir = new File(getCacheDir().toString() + File.separator + "images");
        if (dir.exists()) {
            org.apache.commons.io.FileUtils.cleanDirectory(dir);
        } else {
            dir.mkdir();
        }
        for (RssFeedModel rssFeedModel : result.subList(0, min(result.size(), 11))) {
            try {
                URL image = new URL(rssFeedModel.image);
                InputStream inputStream = (InputStream) image.getContent();
                byte[] bufferImage = new byte[1024];
                String path = getCacheDir().toString()
                        + File.separator + "images" + File.separator
                        + rssFeedModel.image.substring(rssFeedModel.image.lastIndexOf('/') + 1, rssFeedModel.image.length());
               OutputStream outputstream = new FileOutputStream(path);
                int count;
                while ((count = inputStream.read(bufferImage)) != -1) {
                    outputstream.write(bufferImage, 0, count);
                }
                inputStream.close();
                outputstream.close();
            } catch (IOException exception) {
            }
        }
    }

    /*private ArrayList<RssFeedModel> loadImages() {
        ArrayList<RssFeedModel> rssFeedModel = new ArrayList<RssFeedModel>();
        for (RssFeedModel rssFeedModel : result.subList(result.size() - 10, result.size())) {
            try {
                URL image = new URL(rssFeedModel.image);
                InputStream inputStream = (InputStream) image.getContent();
                byte[] bufferImage = new byte[1024];
                String path = getCacheDir().toString() + File.separator + "images" + File.separator
                        + rssFeedModel.image.substring(rssFeedModel.image.lastIndexOf('/') + 1, rssFeedModel.image.length());
                OutputStream outputstream = new FileOutputStream(path);
                int count;
                while ((count = inputStream.read(bufferImage)) != -1) {
                    outputstream.write(bufferImage, 0, count);
                }
                inputStream.close();
                outputstream.close();
            } catch (IOException exception) {
            }
        }
    }*/

}
