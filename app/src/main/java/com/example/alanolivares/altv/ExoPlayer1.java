package com.example.alanolivares.altv;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Environment;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class ExoPlayer1 extends AppCompatActivity implements View.OnTouchListener{
    PlayerView playerView;

    private long playbackPosition;
    private static ProgressDialog progressDialog;
    private ProgressBar progressBar;
    ArrayList<TiempoOb> lista;
    private int currentWindow;
    String nombre,link,capitulo,mostrar;
    int tiempo,total;
    ImageButton stop,play,atras,adelante;
    ArrayList<CanalOb> lista_capitulos;
    TextView nom;
    boolean durationSet=false;
    private boolean playWhenReady = true;
    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    boolean check=true;
    private SimpleExoPlayer player;
    private static final String TAG = "OnSwipeTouchListener";

    private GestureDetectorCompat mDetector=null;

    public Handler handler = new Handler();
    private ComponentListener componentListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exo_player);
        playerView = findViewById(R.id.video_view);
        componentListener = new ComponentListener();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        getSupportActionBar().hide();
        progressBar = findViewById(R.id.progressBar2);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        link =getIntent().getStringExtra("link");
        nombre =getIntent().getStringExtra("nombre");
        capitulo=getIntent().getStringExtra("capitulo");
        nombre=nombre.replace(".mp4","");
        atras=(ImageButton) findViewById(R.id.exo_prev);
        adelante=(ImageButton)findViewById(R.id.exo_next);
        if(capitulo!=null) {
            mostrar = nombre + "-" + capitulo;
        }else {
            mostrar = nombre;
            atras.setVisibility(View.INVISIBLE);
            adelante.setVisibility(View.INVISIBLE);
            check=false;
        }

        lista_capitulos=new ArrayList<>();
        lista_capitulos = getIntent().getParcelableArrayListExtra("capitulos");
        ImageButton bt = (ImageButton) findViewById(R.id.exo_close);
        stop = (ImageButton) findViewById(R.id.exo_pause);
        play = (ImageButton) findViewById(R.id.exo_play);
        /*atras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar
                        .make(v, "Este boton funcionará en la siguiente actualización, esperala ;)", Snackbar.LENGTH_LONG)
                        .show();
            }
        });
        adelante.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar
                        .make(v, "Este boton funcionará en la siguiente actualización, esperala ;)", Snackbar.LENGTH_LONG)
                        .show();
            }
        });*/
        nom = findViewById(R.id.nombreExo);
        nom.setText(mostrar);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }
    public void OnSwipeTouchListener(Context context) {
        mDetector = new GestureDetectorCompat(context, new GestureListener());
    }
    public void ver(){
        tiempo=0;
        SharedPreferences preferences = this.getSharedPreferences("Usuarios",Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String savedList = preferences.getString("listaTiempo","No existe");
        boolean c=true;
        if(!savedList.equals("No existe")){
            Type type = new TypeToken<ArrayList<TiempoOb>>(){}.getType();
            ArrayList<TiempoOb> listacaheTiempo = gson.fromJson(savedList, type);

            for(int x=0;x<listacaheTiempo.size();x++){
                if(listacaheTiempo.get(x).getNombre().equals(mostrar)){
                    tiempo=listacaheTiempo.get(x).getTiempo();
                    c=false;
                }
            }
            if(c){
                listacaheTiempo.add(new TiempoOb(mostrar,0,0));
                String jsonList = gson.toJson(listacaheTiempo);
                SharedPreferences.Editor editor = this.getSharedPreferences("Usuarios",Context.MODE_PRIVATE).edit();
                editor.putString("listaTiempo",jsonList);
                editor.commit();
            }
            lista=listacaheTiempo;

        }else{
            lista=new ArrayList<>();
            System.out.println();
            lista.add(new TiempoOb(mostrar,0, total));
            String jsonList = gson.toJson(lista);
            SharedPreferences.Editor editor = this.getSharedPreferences("Usuarios",Context.MODE_PRIVATE).edit();
            editor.putString("listaTiempo",jsonList);
            editor.commit();
        }
    }
    private void initializePlayer() {
        if (player == null) {
            player = ExoPlayerFactory.newSimpleInstance(
                    new DefaultRenderersFactory(this),
                    new DefaultTrackSelector(),
                    new DefaultLoadControl());

            if(!link.contains("http"))
                link=Environment.getExternalStorageDirectory()+"/Android/data/com.example.alanolivares.altv/files/Movies/"+link;
            if(lista_capitulos!=null){
                MediaSource[] mediaSources = new MediaSource[lista_capitulos.size()];
                for(int x=0;x<lista_capitulos.size();x++){
                    if(lista_capitulos.get(x).capitulo.equals(capitulo))
                        currentWindow=x;
                    mediaSources[x] = buildMediaSource(Uri.parse(lista_capitulos.get(x).link));
                }
                MediaSource mediaSource = mediaSources.length == 1 ? mediaSources[0]
                        : new ConcatenatingMediaSource(mediaSources);
                ver();
                player.prepare(mediaSource);
                playerView.setPlayer(player);

                player.seekTo(currentWindow,tiempo);
                player.setPlayWhenReady(playWhenReady);
                player.addListener(componentListener);
            }else{
                MediaSource mediaSource = buildMediaSource(Uri.parse(link));
                player.prepare(mediaSource);
                playerView.setPlayer(player);
                player.setPlayWhenReady(playWhenReady);
                player.addListener(componentListener);
            }
        }



    }

    private MediaSource buildMediaSource(Uri uri) {
        if(uri.toString().contains("http")){
            return new ExtractorMediaSource.Factory(
                    new DefaultHttpDataSourceFactory("exoplayer-codelab")).
                    createMediaSource(uri);
        }else{
            return new ExtractorMediaSource.Factory(
                    new DefaultDataSourceFactory(this,"Exoplayer-local")).
                    createMediaSource(uri);
        }

    }
    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializePlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if ((Util.SDK_INT <= 23 || player == null)) {
            initializePlayer();
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        //if (Util.SDK_INT <= 23) {
            releasePlayer();
        //}
    }

    @Override
    public void onStop() {
        super.onStop();
        //if (Util.SDK_INT > 23) {
            //releasePlayer();
        //}
    }
    private void releasePlayer() {
        if (player != null) {
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            playWhenReady = player.getPlayWhenReady();
            player.release();
            player = null;
            System.out.println("-------------------------"+playbackPosition);
            if(playbackPosition!=0) {
                Gson gson = new Gson();
                for (int x = 0; x < lista.size(); x++) {
                    if (lista.get(x).getNombre().equals(mostrar)) {
                        lista.set(x, new TiempoOb(mostrar, (int)playbackPosition, total));
                        String jsonList = gson.toJson(lista);
                        SharedPreferences.Editor editor = this.getSharedPreferences("Usuarios", Context.MODE_PRIVATE).edit();
                        editor.putString("listaTiempo", jsonList);
                        editor.commit();
                    }
                    System.out.println(lista.get(x).getNombre());
                    System.out.println(lista.get(x).getTiempo());
                }
            }
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }



    int lastWindowIndex = 0;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        System.out.println("Tocado");
        return super.onTouchEvent(event);
    }

    private class ComponentListener extends Player.DefaultEventListener {

        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
            if(!durationSet){
                stop.setVisibility(View.INVISIBLE);
            }
            //stop.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady,
                                         int playbackState) {
            String stateString;
            System.out.println("Estadooo:  "+playbackState);
            if (playbackState == PlaybackStateCompat.STATE_FAST_FORWARDING) {
                //do something
            }
            if (playbackState == PlaybackStateCompat.ACTION_SKIP_TO_NEXT) {
                System.out.println("Estadooo:  "+playbackState);
            }
            switch (playbackState) {
                case ExoPlayer.STATE_IDLE:
                    stateString = "ExoPlayer.STATE_IDLE      -";
                    System.out.println("555555555555");
                    break;
                case ExoPlayer.STATE_BUFFERING:
                    stop.setVisibility(View.INVISIBLE);
                    play.setVisibility(View.INVISIBLE);
                    stateString = "ExoPlayer.STATE_BUFFERING -";
                    System.out.println("bufferin");
                    progressBar.setVisibility(View.VISIBLE);
                    break;
                case ExoPlayer.STATE_READY:
                    if(!durationSet){
                        total = (int)player.getDuration();
                        durationSet = true;
                        if(check){
                            mostrar=lista_capitulos.get(player.getCurrentWindowIndex()).nombre+"-"+lista_capitulos.get(player.getCurrentWindowIndex()).capitulo;
                        }
                        ver();
                        System.out.println(mostrar);
                        player.seekTo(tiempo);
                        System.out.println("STATE_READY");
                    }
                    stateString = "ExoPlayer.STATE_READY     -";
                    progressBar.setVisibility(View.INVISIBLE);


                    break;
                case ExoPlayer.STATE_ENDED:
                    System.out.println("Cambiaa");
                    stateString = "ExoPlayer.STATE_ENDED     -";
                    break;
                default:
                    stateString = "UNKNOWN_STATE             -";
                    break;
            }
            Log.d("", "changed state to " + stateString
                    + " playWhenReady: " + playWhenReady);
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {

        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
                progressBar.setVisibility(View.INVISIBLE);
                AlertDialog.Builder alert=new AlertDialog.Builder(ExoPlayer1.this);
                alert.setMessage("Hubo un problema al abrir el link, intentalo con otra definicón o vuelve más tarde")
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


        @Override
        public void onPositionDiscontinuity(int reason) {
            int latestWindowIndex = player.getCurrentWindowIndex();
            if (latestWindowIndex != lastWindowIndex) {
                // item selected in playlist has changed, handle here
                lastWindowIndex = latestWindowIndex;
                // ...
            }
            if(check) {
                if(player.getPlaybackState()!=ExoPlayer.STATE_READY){
                    durationSet = false;
                }
                mostrar = lista_capitulos.get(latestWindowIndex).nombre + "-" + lista_capitulos.get(latestWindowIndex).capitulo;
                ver();
                playbackPosition = player.getCurrentPosition();
                if (playbackPosition != 0) {
                    Gson gson = new Gson();
                    for (int x = 0; x < lista.size(); x++) {
                        if (lista.get(x).getNombre().equals(mostrar)) {
                            lista.set(x, new TiempoOb(mostrar, (int) playbackPosition, total));
                            String jsonList = gson.toJson(lista);
                            SharedPreferences.Editor editor = getSharedPreferences("Usuarios", Context.MODE_PRIVATE).edit();
                            editor.putString("listaTiempo", jsonList);
                            editor.commit();
                        }
                        System.out.println(lista.get(x).getNombre());
                        System.out.println(lista.get(x).getTiempo());
                    }
                }
                //currentWindow=latestWindowIndex;
                nom.setText(lista_capitulos.get(latestWindowIndex).nombre + "-" + lista_capitulos.get(latestWindowIndex).capitulo);
                System.out.println("onPositionDiscontinuity");

            }

        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
            durationSet=false;
            System.out.println("onPlaybackParametersChanged");
        }

        @Override
        public void onSeekProcessed() {

        }
    }
    public void onSwipeRight() {
        Log.i(TAG, "onSwipeRight: Swiped to the RIGHT");
    }

    public void onSwipeLeft() {
        Log.i(TAG, "onSwipeLeft: Swiped to the LEFT");
    }

    public void onSwipeTop() {
        Log.i(TAG, "onSwipeTop: Swiped to the TOP");
    }

    public void onSwipeBottom() {
        Log.i(TAG, "onSwipeBottom: Swiped to the BOTTOM");
    }

    public void onClick() {
        Log.i(TAG, "onClick: Clicking in the screen");
    }

    public void onDoubleClick() {
        Log.i(TAG, "onClick: Clicking TWO TIMES in the screen");
    }

    public void onLongClick() {
        Log.i(TAG, "onLongClick: LONG click in the screen");
    }
    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            onClick();
            return super.onSingleTapUp(e);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            onDoubleClick();
            return super.onDoubleTap(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            onLongClick();
            super.onLongPress(e);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                        result = true;
                    }
                } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY) {
                    if (diffY > 0) {
                        onSwipeBottom();
                    } else {
                        onSwipeTop();
                    }
                    result = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }
    }
}