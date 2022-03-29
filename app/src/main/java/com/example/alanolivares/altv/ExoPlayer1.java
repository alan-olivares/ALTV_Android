package com.example.alanolivares.altv;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.media.session.PlaybackStateCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.alanolivares.altv.Funciones.CanalOb;
import com.example.alanolivares.altv.Funciones.Funciones;
import com.example.alanolivares.altv.Funciones.TiempoOb;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;
import java.util.HashMap;

public class ExoPlayer1 extends AppCompatActivity implements Runnable{
    PlayerView playerView;

    private long playbackPosition;
    private ProgressBar progressBar;
    private HashMap<String,TiempoOb> lista;
    private Funciones func;
    private String nombre,link,capitulo,mostrar;
    private int tiempo,total,currentWindow;
    private ImageButton stop,play,atras,adelante;
    private ArrayList<CanalOb> lista_capitulos;
    private TextView nom;
    boolean durationSet=false;
    private boolean playWhenReady = true,check=true;
    private SimpleExoPlayer player;
    private ComponentListener componentListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exo_player);
        func=new Funciones(this);
        lista=func.getTiempoSaved("listaTiempo_v2");
        playerView = findViewById(R.id.video_view);
        componentListener = new ComponentListener();
        progressBar = findViewById(R.id.progressBar2);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
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
        nom = findViewById(R.id.nombreExo);
        nom.setText(mostrar);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }
    @Override
    public boolean onTouchEvent(MotionEvent event){
        int action = event.getActionMasked();
        savePosition();
        switch(action) {
            case (MotionEvent.ACTION_DOWN) :
                return true;
            case (MotionEvent.ACTION_MOVE) :
                return true;
            case (MotionEvent.ACTION_UP) :
                return true;
            case (MotionEvent.ACTION_CANCEL) :
                return true;
            case (MotionEvent.ACTION_OUTSIDE) :
                return true;
            default :
                return super.onTouchEvent(event);
        }
    }
    public void ver(){
        tiempo=0;
        TiempoOb tiempoOb=lista.get(mostrar);
        if(tiempoOb!=null)
            tiempo=tiempoOb.getTiempo();
        else{
            lista.put(mostrar,new TiempoOb(mostrar,0, total));
        }
        func.saveHash(lista,"listaTiempo_v2");
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
                    if(lista_capitulos.get(x).getCapitulo().equals(capitulo))
                        currentWindow=x;
                    mediaSources[x] = buildMediaSource(Uri.parse(lista_capitulos.get(x).getLink()));
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
            currentWindow = player.getCurrentWindowIndex();
            playWhenReady = player.getPlayWhenReady();
            savePosition();
            player.release();
            player = null;
        }
    }

    private void savePosition(){
        Thread t = new Thread(this, "gfg");
        t.start();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }

    private void saveData(){
        lista.put(mostrar,new TiempoOb(mostrar, (int)playbackPosition, total));
        func.saveHash(lista,"listaTiempo_v2");
    }

    int lastWindowIndex = 0;

    @Override
    public void run() {
        if (player != null && lista!=null) {
            playbackPosition = player.getCurrentPosition();
            saveData();
        }

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
            if (playbackState == PlaybackStateCompat.STATE_FAST_FORWARDING) {
                //do something
            }
            if (playbackState == PlaybackStateCompat.ACTION_SKIP_TO_NEXT) {
                System.out.println("Estadooo:  "+playbackState);
            }
            switch (playbackState) {
                case ExoPlayer.STATE_IDLE:
                    stateString = "ExoPlayer.STATE_IDLE      -";
                    break;
                case ExoPlayer.STATE_BUFFERING:
                    stop.setVisibility(View.INVISIBLE);
                    play.setVisibility(View.INVISIBLE);
                    stateString = "ExoPlayer.STATE_BUFFERING -";
                    progressBar.setVisibility(View.VISIBLE);
                    break;
                case ExoPlayer.STATE_READY:
                    if(!durationSet){
                        total = (int)player.getDuration();
                        durationSet = true;
                        if(check){
                            mostrar=lista_capitulos.get(player.getCurrentWindowIndex()).getNombre()+"-"+lista_capitulos.get(player.getCurrentWindowIndex()).getCapitulo();
                        }
                        ver();
                        player.seekTo(tiempo);
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
        private void reintentarCalidad(){
            progressBar.setVisibility(View.VISIBLE);
            if(link.contains("=m37"))
                link=link.replace("=m37","=m22");
            else if(link.contains("=m22")){
                link=link.replace("=m22","=m18");
            }
            player=null;
            initializePlayer();
            Toast.makeText(getApplicationContext(),"Hubo un problema al reproducir en esta calida, reintentanto en una calidad más baja",Toast.LENGTH_LONG).show();
        }
        @Override
        public void onPlayerError(ExoPlaybackException error) {
            progressBar.setVisibility(View.INVISIBLE);
                if(!link.contains("=m18") && lista_capitulos==null && (link.contains("=m37") || link.contains("=m22")))
                    reintentarCalidad();
                else{
                    AlertDialog.Builder alert=new AlertDialog.Builder(ExoPlayer1.this);
                    alert.setMessage("Hubo un problema al abrir el link, intentalo de nuevo más tarde")
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
                mostrar = lista_capitulos.get(latestWindowIndex).getNombre() + "-" + lista_capitulos.get(latestWindowIndex).getCapitulo();
                ver();
                playbackPosition = player.getCurrentPosition();
                if (playbackPosition != 0) {
                    saveData();
                }
                //currentWindow=latestWindowIndex;
                nom.setText(lista_capitulos.get(latestWindowIndex).getNombre() + "-" + lista_capitulos.get(latestWindowIndex).getCapitulo());

            }

        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
            durationSet=false;
        }

        @Override
        public void onSeekProcessed() {

        }
    }

}