package com.example.alanolivares.altv;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;

import hb.xvideoplayer.MxTvPlayerWidget;
import hb.xvideoplayer.MxVideoPlayer;
import hb.xvideoplayer.MxVideoPlayerWidget;

public class ReproducirCanal extends AppCompatActivity {
    MxVideoPlayerWidget videoPlayerWidget;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reproducir_canal);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        String link =getIntent().getStringExtra("link");
        final String nombre =getIntent().getStringExtra("nombre");
        if(link.contains("/live/")){
            link=link.replace("m3u8","ts");
        }
        videoPlayerWidget = (MxVideoPlayerWidget) findViewById(R.id.mpw_video_player);
        videoPlayerWidget.autoStartPlay(link,MxVideoPlayer.FIND_VIEWS_WITH_CONTENT_DESCRIPTION,nombre);
        MxVideoPlayer.FULLSCREEN_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        MxVideoPlayer.NORMAL_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        videoPlayerWidget.setAllControlsVisible(View.VISIBLE, View.INVISIBLE, View.INVISIBLE,View.VISIBLE, View.INVISIBLE, View.INVISIBLE);
        videoPlayerWidget.setAutoProcessUI(true);
        videoPlayerWidget.setBottomProgressBarVisibility(false);
        videoPlayerWidget.mFullscreenButton.setImageResource(R.drawable.live);
        videoPlayerWidget.mFullscreenButton.setClickable(false);
        videoPlayerWidget.mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        final String finalLink = link;
        videoPlayerWidget.setUIStatusListener(new MxVideoPlayerWidget.UIStatusChangeListener() {
            Boolean a=false;
            @Override
            public void onUIChange(MxVideoPlayerWidget.Mode mode) {
                System.out.println(mode);
                if(mode.equals(MxVideoPlayerWidget.Mode.MODE_ERROR)){
                    AlertDialog.Builder alert=new AlertDialog.Builder(ReproducirCanal.this);
                    alert.setMessage("Hubo un problema al abrir el link, intenta de nuevo más tarde")
                            .setCancelable(false)
                            .setPositiveButton("Cerrar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    onBackPressed();
                                }
                            });
                    AlertDialog aler=alert.create();
                    aler.show();
                }
                if(mode.equals(MxVideoPlayerWidget.Mode.MODE_BUFFERING)){
                    videoPlayerWidget.setAllControlsVisible(View.VISIBLE, View.INVISIBLE, View.INVISIBLE,View.VISIBLE, View.INVISIBLE, View.INVISIBLE);
                }
                if(mode.equals(MxVideoPlayerWidget.Mode.MODE_PLAYING)){
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    videoPlayerWidget.setAllControlsVisible(View.VISIBLE, View.VISIBLE, View.INVISIBLE,View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
                }
                if(mode.equals(MxVideoPlayerWidget.Mode.MODE_COMPLETE)){
                    a=true;

                }
                if(mode.equals(MxVideoPlayerWidget.Mode.MODE_NORMAL)){
                    videoPlayerWidget.setAllControlsVisible(View.VISIBLE, View.INVISIBLE, View.INVISIBLE,View.VISIBLE, View.INVISIBLE, View.INVISIBLE);
                    if(a){
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                videoPlayerWidget.mPlayControllerButton.setSoundEffectsEnabled(false);
                                videoPlayerWidget.mPlayControllerButton.performClick();
                                //videoPlayerWidget.startWindowFullscreen();
                            }
                        }, 10);
                        a=false;
                    }
                }
            }
        });


    }



    @Override
    protected void onPause() {
        super.onPause();
        MxVideoPlayer.releaseAllVideos();
    }

    @Override
    public void onBackPressed() {
        if (MxVideoPlayer.backPress()) {
            onBackPressed();
            return;
        }
        super.onBackPressed();
    }


}
