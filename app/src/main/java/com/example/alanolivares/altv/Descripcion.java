package com.example.alanolivares.altv;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Descripcion extends AppCompatActivity implements  SwipeRefreshLayout.OnRefreshListener {
    TextView call,dess,nomb;
    ImageView imageView;
    ImageButton desca,play;
    DownloadManager downloadManager;
    ProgressBar progressBar;
    ArrayList<String> listaopc;
    ArrayAdapter<String> adapter;
    ArrayList<CanalOb> lista_actualizar,lista_favoritos;
    String nombre="";
    boolean fav;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_descripcion);
        final String link =getIntent().getStringExtra("link");
        String imagen =getIntent().getStringExtra("ima");
        String cal =getIntent().getStringExtra("cal");
        String des =getIntent().getStringExtra("des");
        fav =getIntent().getBooleanExtra("fav",false);
        nombre=getIntent().getStringExtra("nom");
        getSupportActionBar().setTitle("Descripción");
        call=(TextView)findViewById(R.id.txtCalif);
        dess=(TextView)findViewById(R.id.txtdes);
        imageView=(ImageView)findViewById(R.id.imagenPe);
        desca=(ImageButton)findViewById(R.id.descarBoton);
        play=(ImageButton)findViewById(R.id.playBoton);
        nomb=(TextView)findViewById(R.id.txtNombre);
        progressBar=(ProgressBar)findViewById(R.id.progressPeli);
        Aviso();
        Picasso.get()
                .load(imagen)
                .error(R.mipmap.altvlog)
                .into(imageView);
        dess.setText(des);
        call.setText(cal);
        nomb.setText(nombre);
        progress(nombre);
        desca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resolucion(link,nombre,1);
            }
        });
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resolucion(link,nombre,0);
            }
        });


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);

        }


    }
    public void resolucion(final String lik, final String nom, final int viewId){
        final String[] def2 = new String[1];
        listaopc=new ArrayList<>();
        final String def=lik;
        System.out.println(def);
        if(def.contains("=m37")){
            listaopc.add("Resolución 1080p");
            listaopc.add("Resolución 720p");
            listaopc.add("Resolución 480p");
        }else if(def.contains("=m22")){
            listaopc.add("Resolución 720p");
            listaopc.add("Resolución 480p");
        }else if(def.contains("=m18")){
            listaopc.add("Resolución 480p");
        }
        System.out.println(listaopc);

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(Descripcion.this);
        String opc;
        if(viewId==0)
            opc="ver";
        else
            opc="descargar";
        builderSingle.setTitle("Resolución para "+opc);
        builderSingle.setNegativeButton("Cerrar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        adapter= new ArrayAdapter<String>(this,android.R.layout.simple_list_item_single_choice,listaopc);
        builderSingle.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String strName = adapter.getItem(which);

                if(strName.endsWith("1080p")){
                    def2[0] =def;
                } else if(strName.endsWith("720p")){
                    def2[0] =def.replace("=m37","=m22");
                }else if(strName.endsWith("480p")){
                    def2[0] =def.replace("=m37","=m18");
                    def2[0] =def.replace("=m22","=m18");
                }
                switch (viewId){
                    case 1:
                        downloadManager=(DownloadManager)Descripcion.this.getSystemService(Descripcion.this.DOWNLOAD_SERVICE);
                        //downloadManager.addCompletedDownload(lista_bus.get(position).getNombre(),"",true,"")
                        Uri uri =Uri.parse(def2[0]);
                        DownloadManager.Request request =new DownloadManager.Request(uri);
                        request.setTitle(nom);
                        //request.setDestinationInExternalPublicDir(Environment.getExternalStorageDirectory()+"/ALTV",lista_bus.get(position).getNombre()+".mp4");
                        request.setDestinationInExternalFilesDir(Descripcion.this, Environment.DIRECTORY_MOVIES,nom+".mp4");
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        long reference= downloadManager.enqueue(request);

                        break;
                    case 0:
                        Intent pas = new Intent(Descripcion.this, ExoPlayer1.class);
                        pas.putExtra("nombre", nom);
                        pas.putExtra("link", def2[0]);
                        startActivity(pas);

                        break;
                    default:
                        //item de la lista
                        break;
                }
            }
        });
        builderSingle.show();

    }
    public void Aviso(){
        Date date = new Date();
        SharedPreferences preferences = getSharedPreferences("Usuarios",Context.MODE_PRIVATE);
        String fecha = getIntent().getStringExtra("fecha");
        Date fechaInicial= null;
        Date fechaFinal=null;
        Date fechaactual=null;
        long dias=0;
        long dias2=0;
        String registro = preferences.getString("register","No existe");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            fechaInicial = dateFormat.parse(registro);
            if(fecha!=null&&!fecha.equals("")) {
                fechaFinal = dateFormat.parse(fecha);
            }else{
                fechaFinal = dateFormat.parse(registro);
            }
            fechaactual=dateFormat.parse(dateFormat.format(date));
            dias=(long) ((fechaFinal.getTime()-fechaInicial.getTime())/86400000);
            dias2=(long) ((fechaactual.getTime()-fechaFinal.getTime())/86400000);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        System.out.println("Hay "+dias+" dias de diferencia en dias3");
        System.out.println("Hay "+dias2+" dias de diferencia dias4");
        String avisar="";
        if(dias!=0&&dias2<4){
            avisar="¡Nueva!";
        }
        TextView aviso=findViewById(R.id.infoPel);
        aviso.setText(avisar);
        if(avisar.equals(""))
            aviso.setVisibility(View.INVISIBLE);
    }
    public void progress(String nombre){
        SharedPreferences preferences = this.getSharedPreferences("Usuarios",Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String savedList = preferences.getString("listaTiempo","No existe");
        Type type = new TypeToken<ArrayList<TiempoOb>>(){}.getType();
        int tiempo=0,tiempoFinal=0,tiempo2=0;
        if(!savedList.equals("No existe")){
            ArrayList<TiempoOb> listacaheTiempo = gson.fromJson(savedList, type);

            for(int x=0;x<listacaheTiempo.size();x++){
                if(listacaheTiempo.get(x).getNombre().equals(nombre)){
                    tiempo=listacaheTiempo.get(x).getTiempo();
                    tiempoFinal=listacaheTiempo.get(x).tiempoFinal;
                    //System.out.println(tiempo);
                    //System.out.println(tiempoFinal);
                }
            }
        }

        if(tiempoFinal!=0){
            tiempoFinal=tiempoFinal/100;
            tiempo2=tiempo/tiempoFinal;
        }
        progressBar.setMax(100);

        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(tiempo2);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflar el menú; Esto agrega elementos a la barra de acción si está presente.
        getMenuInflater().inflate(R.menu.menu_fav, menu);
        MenuItem item = menu.getItem(0);
        if(fav){
            item.setIcon(R.drawable.ic_favorite_black_24dp);
        }else{
            item.setIcon(R.drawable.ic_favorite_border_black_24dp);
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.favorito_menu:
                System.out.println("Entraaaaaa");
                int z=0;
                boolean ver=true;
                Gson gson = new Gson();
                SharedPreferences preferences = getSharedPreferences("Usuarios",Context.MODE_PRIVATE);
                String savedList2 = preferences.getString("listaPeliculas","No existe");
                Type type = new TypeToken<ArrayList<CanalOb>>(){}.getType();
                ArrayList<CanalOb> listacaheSeries = gson.fromJson(savedList2, type);
                lista_actualizar=listacaheSeries;
                for(int x=0;x<lista_actualizar.size();x++){
                    if(lista_actualizar.get(x).nombre.equals(nombre)){
                        if(lista_actualizar.get(x).favo){
                            lista_actualizar.get(x).setFavo(false);
                            fav=false;
                            item.setIcon(R.drawable.ic_favorite_border_black_24dp);
                        }else{
                            z=x;
                            lista_actualizar.get(x).setFavo(true);
                            fav=true;
                            item.setIcon(R.drawable.ic_favorite_black_24dp);
                        }
                    }
                }
                String savedList = preferences.getString("listaFavoritos","No existe");
                if(!savedList.equals("No existe")){
                    ArrayList<CanalOb> listacaheFavoritos = gson.fromJson(savedList, type);
                    lista_favoritos=new ArrayList<CanalOb>();
                    lista_favoritos=listacaheFavoritos;
                }else{
                    lista_favoritos=new ArrayList<CanalOb>();
                }

                for (int x=0;x<lista_favoritos.size();x++){
                    if(lista_favoritos.get(x).nombre.equals(nombre)){
                        if(lista_favoritos.get(x).favo){
                            lista_favoritos.remove(x);
                            ver=false;
                        }
                    }
                }
                if(ver){
                    System.out.println(lista_actualizar.get(z));
                    lista_favoritos.add(lista_actualizar.get(z));
                }
                String jsonList = gson.toJson(lista_favoritos);
                SharedPreferences.Editor editor = getSharedPreferences("Usuarios",Context.MODE_PRIVATE).edit();
                editor.putString("listaFavoritos",jsonList);
                editor.commit();
                String jsonList2 = gson.toJson(lista_actualizar);
                editor.putString("listaPeliculas",jsonList2);
                editor.commit();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        progress((String) nomb.getText());
        System.out.println((String) nomb.getText());
    }

    @Override
    public void onRefresh() {

    }
}
