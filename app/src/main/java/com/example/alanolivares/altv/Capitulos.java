package com.example.alanolivares.altv;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

import com.example.alanolivares.altv.Funciones.CanalOb;
import com.example.alanolivares.altv.Funciones.Funciones;
import com.example.alanolivares.altv.Funciones.NonScrollListView;
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
    private NonScrollListView listViewCanales;
    private AdaptadorCapitulos adaptador;
    private ArrayList<CanalOb> lista_capitulos,lista_capitulos2,lista_series,lista_favoritos;
    private ArrayAdapter<String> adapter;
    private ArrayList<Temporada> listacaheTemporada;
    private Spinner temporada;
    private int tem;
    private Funciones func;
    private CanalOb serie;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(android.view.Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().requestFeature(android.view.Window.FEATURE_ACTIVITY_TRANSITIONS);
        setContentView(R.layout.activity_capitulos);
        Inicializar();
    }

    private void Inicializar(){
        func=new Funciones(this);
        listViewCanales = findViewById(R.id.listViewCapitulos);
        TextView descri=(TextView)findViewById(R.id.sinoserie);
        TextView cal=(TextView)findViewById(R.id.caliserie);
        ImageView ima = (ImageView) findViewById(R.id.serieIma);
        temporada=(Spinner)findViewById(R.id.temporada);
        serie=getIntent().getParcelableExtra("objeto");

        Button trailer=(Button)findViewById(R.id.trailerSeries);
        trailer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pas = new Intent(Capitulos.this, VideoTrailer.class);
                pas.putExtra("nombre", serie.getNombre()+" serie");
                startActivity(pas);
            }
        });

        TextView aviso=findViewById(R.id.infoCap);
        func.aviso(aviso,serie);
        cal.setText(serie.getCalificacion());
        descri.setText(serie.getDescripcion());
        if(serie.getImagen().equals("")){
            ima.setImageResource(R.mipmap.altvlog);
        }else {
            Picasso.get().load(serie.getImagen()).error(R.drawable.altvlog).into(ima);
        }
        ViewCompat.setTransitionName(ima, "transition-image");
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(serie.getNombre());
        }
        ver();

        //Cargar();
        listViewCanales.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int viewId = (int)id;
                resolucion(position,viewId);
            }
        });
        lista_series=func.listaObjeto("listaSeries");
        lista_favoritos=func.listaObjeto("listaFavoritos");
    }

    public void resolucion(final int position, final int viewId){
        final String link=lista_capitulos2.get(position).getLink();
        ArrayList<String> listaopc=func.resoluciones(link);
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
                String newLink=func.getNewLink(strName,link);
                switch (viewId){
                    case 1:
                        func.descargar(lista_capitulos2.get(position));
                        break;
                    case 0:
                        Intent pas = new Intent(Capitulos.this, ExoPlayer1.class);
                        pas.putExtra("nombre", lista_capitulos2.get(position).getNombre());
                        pas.putExtra("link", newLink);
                        pas.putExtra("capitulo", lista_capitulos2.get(position).getCapitulo());
                        pas.putParcelableArrayListExtra("capitulos", lista_capitulos);
                        startActivity(pas);
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
            data=lista_capitulos.get(x).getCapitulo().split("x");
            if(data[0].startsWith("0")){
                data[0]=data[0].replace("0","");
            }
            lista.add(Integer.parseInt(data[0]));
        }
        HashSet<Integer> hashSet = new HashSet<Integer>(lista);
        lista.clear();
        lista.addAll(hashSet);
        for(int x=0;x<lista.size();x++)
            lista2.add("Temporada "+lista.get(x));
        Collections.sort(lista);
        temporada.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, lista2));

        SharedPreferences preferences = getSharedPreferences("Usuarios",Context.MODE_PRIVATE);
        final String savedList = preferences.getString("listaTemporada","No existe");
        listacaheTemporada=new ArrayList<Temporada>();
        int temp=0;
        if(checker(savedList)>=0)
            temp=checker(savedList);
        temporada.setSelection(temp);
        temporada.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView adapter, View v, int i, long lng) {
                int aux=checker2(savedList,listacaheTemporada);
                if(aux>=0){
                    listacaheTemporada.set(aux,new Temporada(serie.getNombre(),i));
                }else{
                    listacaheTemporada.add(new Temporada(serie.getNombre(),i));
                }
                func.saveLista(listacaheTemporada,"listaTemporada");
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
    public int checker(String savedList){
        if(!savedList.equals("No existe")){
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<Temporada>>(){}.getType();
            listacaheTemporada = gson.fromJson(savedList, type);
            for (int x=0;x<listacaheTemporada.size();x++){
                if(listacaheTemporada.get(x).getNombre().equals(serie.getNombre())){
                    return listacaheTemporada.get(x).temporada;
                }
            }
        }
        return -1;
    }
    public int checker2(String savedList, ArrayList<Temporada> listacaheTemporada){
        if(!savedList.equals("No existe")){
            for (int x=0;x<listacaheTemporada.size();x++){
                if(listacaheTemporada.get(x).getNombre().equals(serie.getNombre())){
                    return x;
                }
            }
        }
        return -1;
    }
    public void Cargar(int valor){
        String data[];
        lista_capitulos2=new ArrayList<>();
        lista_capitulos = getIntent().getParcelableArrayListExtra("capitulos");
        for(int x=0;x<lista_capitulos.size();x++){
            data=lista_capitulos.get(x).getCapitulo().split("x");
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
        item.setIcon(serie.getFavo()?R.drawable.ic_favorite_black_24dp:R.drawable.ic_favorite_border_black_24dp);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.favorito_menu:
                func.saveFavoritos(lista_series, lista_favoritos, serie.getNombre(), item,"listaSeries");
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

class Temporada {
    String nombre;
    int temporada;

    public Temporada(String nombre, int temporada) {
        this.nombre = nombre;
        this.temporada = temporada;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
