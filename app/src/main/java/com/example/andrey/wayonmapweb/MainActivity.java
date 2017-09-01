package com.example.andrey.wayonmapweb;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity{

    WebView webView;
    Boolean wasNoConnection=false;
    String latLngs=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView=(WebView) findViewById(R.id.webview);
        final AppCompatButton button=(AppCompatButton) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isConnectingToInternet())
                    if (wasNoConnection){
                        webView.loadUrl("file:///android_asset/map.html");
                        button.setText(R.string.button_text);
                        wasNoConnection=false;
                    }
                    else {
                        FetchRouteTask fetchRouteTask=new FetchRouteTask();
                        fetchRouteTask.execute();}
                else Toast.makeText(MainActivity.this, "Нет подключения к интернету", Toast.LENGTH_SHORT).show();
            }
        });
        //webView.setWebViewClient(new SSLWebClient());
        webView.setWebChromeClient(new WebChromeClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.addJavascriptInterface(new WebAppInterface(this), "android");
        if(isConnectingToInternet())webView.loadUrl("file:///android_asset/map.html");
        else {Toast.makeText(MainActivity.this, "Нет подключения к интернету", Toast.LENGTH_SHORT).show();
        wasNoConnection=true;
        button.setText(R.string.button_load_map);}

    }



    protected void getCoords(Object o) throws JSONException{

        String jsonString= (String) o;
        if(jsonString==null){
            Toast.makeText(this, "Путь не загружен", Toast.LENGTH_SHORT).show();
            return;}
        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray jsonArray=jsonObject.getJSONArray("coords");
        latLngs=jsonArray.toString();
        webView.loadUrl("javascript:addPath()");

    }

    public class WebAppInterface {
        Context mContext;

        /** Instantiate the interface and set the context */
        WebAppInterface(Context c) {
            mContext = c;
        }

        /** Show a toast from the web page */
        @JavascriptInterface
        public String getData() {
            return latLngs;
        }
    }

    private class SSLWebClient extends WebViewClient{
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {

        }
    }

    public boolean isConnectingToInternet(){
        ConnectivityManager connectivity = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null)
                    if (info.getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }

        }
        return false;
    }

    public class FetchRouteTask extends AsyncTask {


        @Override
        protected Object doInBackground(Object[] params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

// Will contain the raw JSON response as a string.
            String jsonStr = null;

            try {
                URL url = new URL("https://test.www.estaxi.ru/route.txt");
                urlConnection = (HttpsURLConnection) url.openConnection();

                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    jsonStr = null;
                }
                jsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e("Route", "Error ", e);
                jsonStr = null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("Route", "Error closing stream", e);
                    }
                }
            }
            return jsonStr;
        }

        @Override
        protected void onPostExecute(Object o) {
            try {
                MainActivity.this.getCoords(o);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}