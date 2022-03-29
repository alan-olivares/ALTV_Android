package com.example.alanolivares.altv;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

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

public class VideoTrailer extends YouTubeBaseActivity {
    YouTubePlayerView youTubePlayerView;
    YouTubePlayer.OnInitializedListener onInitializedListener;
    ImageButton exitButton;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.videotrailer);
        getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT,800);
        String nombre =getIntent().getStringExtra("nombre");
        youTubePlayerView = findViewById(R.id.youtube);
        exitButton=findViewById(R.id.exitbutton);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });

        new JsonTask().execute("https://www.googleapis.com/youtube/v3/search?fields=items&q=" + nombre + " trailer latino o sub&type=video&key=AIzaSyBCrMyoUb9Fqpl80rQ_8IQZ8V_MXqsNC28");

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
                final String id=idcode.getString("videoId");
                onInitializedListener=new YouTubePlayer.OnInitializedListener(){

                    @Override
                    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                        youTubePlayer.loadVideo(id);
                    }

                    @Override
                    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

                    }
                };
                youTubePlayerView.initialize("AIzaSyBCrMyoUb9Fqpl80rQ_8IQZ8V_MXqsNC28",onInitializedListener);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
