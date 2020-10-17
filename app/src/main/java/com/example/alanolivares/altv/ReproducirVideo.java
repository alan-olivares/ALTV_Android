package com.example.alanolivares.altv;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.support.v7.widget.Toolbar;
import android.widget.VideoView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class ReproducirVideo extends AppCompatActivity  {
    VideoView videoView;
    public Handler handler = new Handler();
    private static ProgressDialog progressDialog;
    ArrayList<TiempoOb> lista;
    String nombre;
    int a,b;
    private MediaController mediaController;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reproducir_video);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarVideo);
        setSupportActionBar(toolbar);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        /*android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }*/

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Por favor espera");
        progressDialog.setMessage("Cargando buffering");
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(true);
        mediaController=new MediaController(this);
        final String link =getIntent().getStringExtra("link");
        nombre =getIntent().getStringExtra("nombre");
        videoView= (VideoView) findViewById(R.id.video2);
        mediaController.setAnchorView(videoView);
        getSupportActionBar().setTitle(nombre);
        getSupportActionBar().hide();
        if(link.contains("http")){
            Uri url =Uri.parse(link);
            videoView.setVideoURI(url);
        }else{
            videoView.setVideoPath(Environment.getExternalStorageDirectory()+"/Android/data/com.example.alanolivares.altv/files/Movies/"+link);
        }
        SharedPreferences preferences = this.getSharedPreferences("Usuarios",Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String savedList = preferences.getString("listaTiempo","No existe");
        int tiempo=0;
        boolean c=true;
        if(!savedList.equals("No existe")){
            Type type = new TypeToken<ArrayList<TiempoOb>>(){}.getType();
            ArrayList<TiempoOb> listacaheTiempo = gson.fromJson(savedList, type);

            for(int x=0;x<listacaheTiempo.size();x++){
                if(listacaheTiempo.get(x).getNombre().equals(nombre)){
                    tiempo=listacaheTiempo.get(x).getTiempo();
                    c=false;
                }
            }
            if(c){
                listacaheTiempo.add(new TiempoOb(nombre,0,0));
                String jsonList = gson.toJson(listacaheTiempo);
                SharedPreferences.Editor editor = this.getSharedPreferences("Usuarios",Context.MODE_PRIVATE).edit();
                editor.putString("listaTiempo",jsonList);
                editor.commit();
            }
            lista=listacaheTiempo;

        }else{
            lista=new ArrayList<>();
            System.out.println();
            lista.add(new TiempoOb(nombre,0,videoView.getDuration()));
            String jsonList = gson.toJson(lista);
            SharedPreferences.Editor editor = this.getSharedPreferences("Usuarios",Context.MODE_PRIVATE).edit();
            editor.putString("listaTiempo",jsonList);
            editor.commit();
        }
        videoView.setMediaController(mediaController);
        videoView.requestFocus();
        videoView.seekTo(tiempo);
        if(tiempo!=0)
            a=tiempo;
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(final MediaPlayer mp) {
                mp.setLooping(true);
                mp.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                    @Override
                    public void onVideoSizeChanged(final MediaPlayer mp, int arg1,
                                                   int arg2) {
                        // TODO Auto-generated method stub
                        if(progressDialog.isShowing()){
                            progressDialog.dismiss();
                            b=videoView.getDuration();
                        }
                    }
                });
            }
        });

        //videoView.start();
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                if(progressDialog.isShowing()){
                    progressDialog.dismiss();
                    AlertDialog.Builder alert=new AlertDialog.Builder(ReproducirVideo.this);
                    alert.setMessage("Hubo un problema al abrir el link, intenta de nuevo mas tarde")
                            .setCancelable(false)
                            .setPositiveButton("Cerrar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    onBackPressed();
                                }
                            });
                    AlertDialog aler=alert.create();
                    aler.show();
                    return  true;
                }else{

                    return true;
                }

                //onBackPressed();

            }
        });



        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                getSupportActionBar().show();
                //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                //getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                ocultar();
                return false;
            }
        });

    }
    void ocultar(){
        handler.postDelayed(
                new Runnable() {
                    public void run() {
                        /*View decorView = getWindow().getDecorView();
                        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
                        decorView.setSystemUiVisibility(uiOptions);*/
                        getSupportActionBar().hide();
                        //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    }},
                3000);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();

                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }
    @Override
    protected void onPause(){
        super.onPause();
        a=videoView.getCurrentPosition();
        System.out.println("-------------------------"+a);
        if(a!=0) {
            Gson gson = new Gson();
            for (int x = 0; x < lista.size(); x++) {
                if (lista.get(x).getNombre().equals(nombre)) {
                    lista.set(x, new TiempoOb(nombre, a, b));
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
    @Override
    protected void onResume(){
        super.onResume();
        int tim=0;
        SharedPreferences preferences = getSharedPreferences("Usuarios",Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String savedList = preferences.getString("listaTiempo","No existe");
        Type type = new TypeToken<ArrayList<TiempoOb>>(){}.getType();
        ArrayList<TiempoOb> listacaheTiempo = gson.fromJson(savedList, type);
        for(int x=0;x<listacaheTiempo.size();x++)
            if(listacaheTiempo.get(x).nombre.equals(nombre.toString()))
                tim=listacaheTiempo.get(x).tiempo;
        progressDialog.setTitle("Por favor espera");
        progressDialog.setMessage("Cargando buffering");
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(true);
        System.out.println(tim);
        videoView.seekTo(tim);
        videoView.start();
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if(progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
            }
        });

    }


}
class TiempoOb {
    String nombre;
    int tiempo;
    int tiempoFinal;

    public TiempoOb(String nombre, int tiempo,int tiempoFinal) {
        this.nombre = nombre;
        this.tiempo = tiempo;
        this.tiempoFinal = tiempoFinal;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getTiempo() {
        return tiempo;
    }

    public void setTiempo(int tiempo) {
        this.tiempo = tiempo;
    }
}

