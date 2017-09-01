package com.example.andrey.wayonmapweb;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by andrey on 18.06.17.
 */

public class FetchRouteTask1 extends AsyncTask {


    @Override
    protected Object doInBackground(Object[] params) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

// Will contain the raw JSON response as a string.
        String jsonStr = null;

        try {
            URL url = new URL("http://test.www.estaxi.ru/route.txt");
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                jsonStr = null;
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
    }
}