package com.example.alanolivares.altv;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeIntents;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

public class TrailerActivity extends AppCompatActivity {

    WebView myWebView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trailer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
         myWebView = (WebView) findViewById(R.id.webinfo);
        String nombre =getIntent().getStringExtra("nombre");
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_close_black_24dp));
        getSupportActionBar().setTitle("ALTV Trailers");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });
        new JsonTask().execute("https://www.googleapis.com/youtube/v3/search?fields=items&q="+nombre+" trailer español o sub&type=video&key=AIzaSyBCrMyoUb9Fqpl80rQ_8IQZ8V_MXqsNC28");
    }


    private class JsonTask extends AsyncTask<String,String,String> {
        private ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection=null;
            BufferedReader reader = null;
            try{
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line ="";
                while ((line =reader.readLine())!= null){
                    buffer.append(line+"\n");
                    Log.e("Response",">"+line);
                }
                return buffer.toString();
            }catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if(connection != null){
                    connection.disconnect();
                }
                try{
                    if(reader!= null){
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try{
                JSONObject jsonArray = new JSONObject(s);
                JSONArray items=jsonArray.getJSONArray("items");
                JSONObject firstJSON=items.getJSONObject(0);
                JSONObject idcode=firstJSON.getJSONObject("id");
                String id=idcode.getString("videoId");
                myWebView.loadUrl("https://www.youtube.com/watch?v="+id);
                myWebView.setBackgroundColor(Color.TRANSPARENT);
                myWebView.getSettings().setJavaScriptEnabled(true);
                myWebView.setWebViewClient(new WebViewClient() {
                    public boolean shouldOverrideUrlLoading(WebView viewx, String urlx) {
                        viewx.loadUrl(urlx);
                        return false;
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


}


