package com.example.andrey.wayonmapweb;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.http.SslError;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
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

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    WebView webView;
    Boolean wasNoConnection=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView=(WebView) findViewById(R.id.webview);
        AppCompatButton button=(AppCompatButton) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isConnectingToInternet())
                    if (wasNoConnection){
                        webView.loadUrl("file:///android_asset/map.html");
                        webView.loadUrl("javascript:addPath()");
                        wasNoConnection=false;
                    }
                    else webView.loadUrl("javascript:addPath()");
                else Toast.makeText(MainActivity.this, "Нет подключения к интернету", Toast.LENGTH_SHORT).show();
            }
        });
        webView.setWebViewClient(new SSLWebClient());
        webView.setWebChromeClient(new WebChromeClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.addJavascriptInterface(new WebAppInterface(this), "android");
        if(isConnectingToInternet())webView.loadUrl("file:///android_asset/map.html");
        else {Toast.makeText(MainActivity.this, "Нет подключения к интернету", Toast.LENGTH_SHORT).show();
        wasNoConnection=true;}

    }



    private String getCoords() throws JSONException{
        FetchRouteTask fetchRouteTask=new FetchRouteTask();
        fetchRouteTask.execute();
        String latLngs=null;
        String jsonString= null;
        try {
            jsonString = (String) fetchRouteTask.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        if(jsonString==null){
            Toast.makeText(this, "Путь не загружен", Toast.LENGTH_SHORT).show();
            return latLngs;}

        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray jsonArray=jsonObject.getJSONArray("coords");
        latLngs=jsonArray.toString();
        return latLngs;

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
            try {
                return getCoords();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
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
}