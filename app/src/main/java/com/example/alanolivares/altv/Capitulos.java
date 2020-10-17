package com.example.alanolivares.altv;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

public class Capitulos extends AppCompatActivity {
    private ListView listViewCanales;
    private AdaptadorCapitulos adaptador;
    ArrayList<CanalOb> lista_capitulos,lista_capitulos2,lista_actualizar,lista_favoritos;
    ArrayList<String> listaopc;
    ArrayAdapter<String> adapter;
    ListView listView;
    ImageButton play,des;
    ImageView ima;
    Spinner temporada;
    TextView descri,cal;
    int tem;
    DownloadManager downloadManager;
    String nombre;
    boolean fav;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capitulos);
        listViewCanales = (ListView) findViewById(R.id.listViewCapitulos);
        descri=(TextView)findViewById(R.id.sinoserie);
        cal=(TextView)findViewById(R.id.caliserie);
        ima = (ImageView) findViewById(R.id.serieIma);
        temporada=(Spinner)findViewById(R.id.temporada);
        String imag = getIntent().getStringExtra("imagen");
        String cali = getIntent().getStringExtra("cal");
        String des = getIntent().getStringExtra("des");
        Aviso();
        fav =getIntent().getBooleanExtra("fav",false);
        cal.setText(cali);
        descri.setText(des);
        if(imag.equals("")){
            ima.setImageResource(R.mipmap.altvlog);
        }else {
            Picasso.get().load(imag).error(R.drawable.altvlog).into(ima);
        }
        nombre = getIntent().getStringExtra("serie");
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(nombre);
        }

        ver();

        //Cargar();
        listViewCanales.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int viewId = (int)id;
                System.out.println(id);
                System.out.println(R.id.descaserie);
                String dato;
                resolucion(position,viewId);

            }

        });
    }
    public void Aviso(){
        Date date = new Date();
        SharedPreferences preferences = getSharedPreferences("Usuarios",Context.MODE_PRIVATE);
        String fecha = getIntent().getStringExtra("fecha");
        String fechaCap = getIntent().getStringExtra("fechaCap");
        Date fechaInicial= null;
        Date fechaFinal=null;
        Date fechaFinalCap=null;
        Date fechaactual=null;
        long dias=0;
        long dias2=0;
        long dias3=0;
        long dias4=0;
        String registro = preferences.getString("register","No existe");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            fechaInicial = dateFormat.parse(registro);
            if(fecha!=null&&!fecha.equals("")) {
                fechaFinal = dateFormat.parse(fecha);
            }else{
                fechaFinal = dateFormat.parse(registro);
            }
            if(fechaCap!=null&&!fechaCap.equals("")) {
                fechaFinalCap = dateFormat.parse(fechaCap);
            }else{
                fechaFinalCap = dateFormat.parse(registro);
            }
            fechaactual=dateFormat.parse(dateFormat.format(date));
            dias=(long) ((fechaFinal.getTime()-fechaInicial.getTime())/86400000);
            dias2=(long) ((fechaactual.getTime()-fechaFinal.getTime())/86400000);
            dias3=(long) ((fechaFinalCap.getTime()-fechaInicial.getTime())/86400000);
            dias4=(long) ((fechaactual.getTime()-fechaFinalCap.getTime())/86400000);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        System.out.println(fechaFinalCap);
        System.out.println("Hay "+dias+" dias de diferencia en dias3");
        System.out.println("Hay "+dias2+" dias de diferencia dias4");
        String avisar="";
        if(dias!=0&&dias2<4){
            avisar="¡Nueva!";
        }else if(dias3!=0&&dias4<4){
            avisar="¡Nuevos capitulos!";
        }
        TextView aviso=findViewById(R.id.infoCap);
        aviso.setText(avisar);
        if(avisar.equals(""))
            aviso.setVisibility(View.INVISIBLE);
    }
    public void descargar(int position,String dato){

        downloadManager=(DownloadManager)Capitulos.this.getSystemService(Capitulos.this.DOWNLOAD_SERVICE);
        //downloadManager.addCompletedDownload(lista_bus.get(position).getNombre(),"",true,"")
        Uri uri =Uri.parse(dato);
        DownloadManager.Request request =new DownloadManager.Request(uri);
        request.setTitle(lista_capitulos2.get(position).getNombre()+"-"+lista_capitulos2.get(position).capitulo);
        //request.setDestinationInExternalPublicDir(Environment.getExternalStorageDirectory()+"/ALTV",lista_capitulos.get(position).getNombre()+".mp4");
        request.setDestinationInExternalFilesDir(Capitulos.this, Environment.DIRECTORY_MOVIES,lista_capitulos2.get(position).getNombre()+"-"+lista_capitulos2.get(position).capitulo+".mp4");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        long reference= downloadManager.enqueue(request);
    }
    public void resolucion(final int position, final int viewId){
        final String[] def2 = new String[1];
        listaopc=new ArrayList<>();
        final String def=lista_capitulos2.get(position).getLink();
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

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(Capitulos.this);
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
                    def2[0] =lista_capitulos2.get(position).getLink();
                } else if(strName.endsWith("720p")){
                    def2[0] =lista_capitulos2.get(position).getLink().replace("=m37","=m22");
                }else if(strName.endsWith("480p")){
                    def2[0] =lista_capitulos2.get(position).getLink().replace("=m37","=m18");
                    def2[0] =lista_capitulos2.get(position).getLink().replace("=m22","=m18");
                }
                switch (viewId){
                    case 1:
                        descargar(position,def2[0]);

                        break;
                    case 0:
                        Intent pas = new Intent(Capitulos.this, ExoPlayer1.class);
                        pas.putExtra("nombre", lista_capitulos2.get(position).getNombre());
                        pas.putExtra("link", def2[0]);
                        pas.putExtra("capitulo", lista_capitulos2.get(position).capitulo);
                        pas.putParcelableArrayListExtra("capitulos", lista_capitulos);
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
    public void ver(){
        String data[];
        final ArrayList<Integer> lista=new ArrayList<>();
        final ArrayList<String> lista2=new ArrayList<>();
        lista_capitulos = getIntent().getParcelableArrayListExtra("capitulos");
        for(int x=0;x<lista_capitulos.size();x++){
            data=lista_capitulos.get(x).capitulo.split("x");
            if(data[0].startsWith("0")){
                data[0]=data[0].replace("0","");
            }
            System.out.println(lista_capitulos.get(x).capitulo);
            lista.add(Integer.parseInt(data[0]));
        }
        HashSet<Integer> hashSet = new HashSet<Integer>(lista);
        lista.clear();
        lista.addAll(hashSet);
        for(int x=0;x<lista.size();x++)
            lista2.add("Temporada "+lista.get(x));
        Collections.sort(lista);
        temporada.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, lista2));
        temporada.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView adapter, View v, int i, long lng) {
                Cargar(lista.get(i));
                tem=lista.get(i);
                //or this can be also right: selecteditem = level[i];
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView)
            {

            }
        });
    }
    public void Cargar(int valor){
        String data[];
        lista_capitulos2=new ArrayList<>();
        lista_capitulos = getIntent().getParcelableArrayListExtra("capitulos");
        for(int x=0;x<lista_capitulos.size();x++){
            data=lista_capitulos.get(x).capitulo.split("x");
            if(data[0].startsWith("0")){
                data[0]=data[0].replace("0","");
                if(valor==Integer.parseInt(data[0])){
                    lista_capitulos2.add(lista_capitulos.get(x));
                }
            }else{
                if(valor==Integer.parseInt(data[0])){
                    lista_capitulos2.add(lista_capitulos.get(x));
                }
            }
        }
        adaptador = new AdaptadorCapitulos(this, lista_capitulos2);
        listViewCanales.setAdapter(adaptador);
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
                int z=0;
                boolean ver=true;
                Gson gson = new Gson();
                SharedPreferences preferences = getSharedPreferences("Usuarios", Context.MODE_PRIVATE);
                String savedList2 = preferences.getString("listaSeries","No existe");
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
                editor.putString("listaSeries",jsonList2);
                editor.commit();
            default:
                return super.onOptionsItemSelected(item);

        }
    }
    @Override
    protected void onResume(){
        super.onResume();
        Cargar(tem);

    }
}