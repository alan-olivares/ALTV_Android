package com.example.alanolivares.altv.Funciones;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.SearchView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.alanolivares.altv.Adaptador_Canales;
import com.example.alanolivares.altv.Descripcion;
import com.example.alanolivares.altv.Peliculas;
import com.example.alanolivares.altv.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public abstract class ClaseFragmentPadre extends Fragment {
    private GridView listaObjetos;
    private SearchView sear;
    private Adaptador_Canales adaptador;
    private JSONObject datos;
    private View seleccionado=null;
    private ArrayList<CanalOb> lista_objetos,lista_bus,lista_favoritos;
    private Funciones func;
    private HashMap<String,CanalOb> lista_hash;
    private int index;
    public abstract void obtenerDatos();
    public abstract void pasarIntent(CanalOb objeto,View view);
    public abstract void ordenar();

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void mainDatos(GridView listaObjetos, SwipeRefreshLayout mSwipeRefreshView, JSONObject datos) throws Exception{
        int cant=(getActivity().getResources().getConfiguration().orientation== Configuration.ORIENTATION_PORTRAIT)?3:6;
        listaObjetos.setNumColumns(cant);
        this.listaObjetos=listaObjetos;
        setHasOptionsMenu(true);
        this.datos=datos;
        func=new Funciones(getContext());
        int dayOfWeek = getCurrentDay();
        int dia = diaObjeto(datos.getString("dia"));
        lista_objetos=new ArrayList<>();
        if(dia!=dayOfWeek){
            if(isOnlineNet()){
                obtenerDatos();
            }else {
                mensaje("Revisa tu conexión a internet");
            }
        }else{
            ArrayList<CanalOb> listacaheObjetos = listaObjeto(datos.getString("lista"));
            lista_bus=listacaheObjetos;
            setListas(listacaheObjetos);
        }
        mSwipeRefreshView.setColorSchemeResources(R.color.naranja, R.color.verde, R.color.azul);
        mSwipeRefreshView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(isOnlineNet()){
                    obtenerDatos();
                }else {
                    mensaje("Revisa tu conexión a internet");
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshView.setRefreshing(false);
                    }
                }, 2000);
                // make your api request here
            }
        });
        listaObjetos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {
                int viewId = (int)id;
                switch (viewId) {
                    case 0:
                        view.clearFocus();
                        if (view != null) {
                            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }
                        index=position;
                        seleccionado=view;
                        CanalOb objeto= getObjeto(position);
                        pasarIntent(objeto,view);
                        break;
                    case 1:
                        buttonfav(position,view);
                        break;
                }
            }
        });
    }
    public Funciones getFunc(){
        return func;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        View c = listaObjetos.getChildAt(0);
        if(c!=null){
            final int scrolly = (-c.getTop() + listaObjetos.getFirstVisiblePosition() * c.getHeight())/(180*3);
            listaObjetos.post(new Runnable() {
                @Override
                public void run() {
                    listaObjetos.setSelection(scrolly);
                }
            });
        }
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            listaObjetos.setNumColumns(6);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            listaObjetos.setNumColumns(3);
        }
    }

    public void buttonfav(int position,View view){
        ToggleButton toggleButton=view.findViewById(R.id.favorito);
        lista_favoritos=listaObjeto("listaFavoritos");
        if(lista_bus.get(position).getFavo()){
            toggleButton.setBackgroundResource(R.drawable.ic_favorite_border_black_24dp);
            for(int x=0;x<lista_favoritos.size();x++){
                if(lista_favoritos.get(x).getNombre().equals(lista_bus.get(position).getNombre())){
                    lista_favoritos.remove(x);
                    break;
                }
            }
        }else {
            toggleButton.setBackgroundResource(R.drawable.ic_favorite_black_24dp);
            lista_favoritos.add(lista_bus.get(position));
            //mensaje(lista_bus.get(position).getNombre()+" agregado a favoritos");
        }
        changeFav(position,!lista_bus.get(position).getFavo());
        func.saveLista(lista_favoritos,"listaFavoritos");
        try {
            func.saveLista(lista_objetos,datos.getString("lista"));
        } catch (JSONException e) {}
    }

    public void buscar(String entrada){
        lista_bus=new ArrayList<>();
        String cadenaNormalize = Normalizer.normalize(entrada, Normalizer.Form.NFD);
        String cadenaSinAcentos = cadenaNormalize.replaceAll("[^\\p{ASCII}]", "");
        for(int x=0;x<lista_objetos.size();x++){
            String cadenaNormalize2 = Normalizer.normalize(lista_objetos.get(x).getNombre(), Normalizer.Form.NFD);
            String cadenaSinAcentos2 = cadenaNormalize2.replaceAll("[^\\p{ASCII}]", "");
            if(cadenaSinAcentos2.toLowerCase().contains(cadenaSinAcentos.toLowerCase())){
                lista_bus.add(lista_objetos.get(x));
            }
        }
        if(adaptador!=null && adaptador.getCount()!=lista_bus.size()){
            adaptador = new Adaptador_Canales(getContext(),lista_bus);
            listaObjetos.setAdapter(adaptador);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(seleccionado!=null){
            ToggleButton toggleButton=seleccionado.findViewById(R.id.favorito);
            ArrayList<CanalOb> aux=listaObjeto("listaFavoritos");
            if(aux.size()>lista_favoritos.size()){
                toggleButton.setBackgroundResource(R.drawable.ic_favorite_black_24dp);
                changeFav(index,true);
            }else if(aux.size()<lista_favoritos.size()){
                toggleButton.setBackgroundResource(R.drawable.ic_favorite_border_black_24dp);
                changeFav(index,false);
            }
        }
    }
    private void changeFav(int index,boolean caso){
        lista_bus.get(index).setFavo(caso);
        for(int x=0;x<lista_objetos.size();x++){
            if(lista_objetos.get(x).getNombre().equals(lista_bus.get(index).getNombre())){
                lista_objetos.get(x).setFavo(caso);
                return;
            }
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        lista_favoritos=listaObjeto("listaFavoritos");
    }

    public int getCurrentDay(){
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    public ArrayList<CanalOb> listaObjeto(String tipo){
        return func.listaObjeto(tipo);
    }

    public int diaObjeto(String tipo){
        SharedPreferences preferences = getActivity().getSharedPreferences("Usuarios", Context.MODE_PRIVATE);
        return preferences.getInt(tipo,80);
    }
    public void setTitulo(String titulo){
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(titulo);
    }
    public Boolean isOnlineNet() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo actNetInfo = connectivityManager.getActiveNetworkInfo();
        return (actNetInfo != null && actNetInfo.isConnected());
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_lateral, menu);
        final MenuItem ordenar=menu.findItem(R.id.ordenar);
        ordenar.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                ordenar();
                return false;
            }
        });
        MenuItem searchItem = menu.findItem(R.id.app_bar_search);
        sear=(SearchView) MenuItemCompat.getActionView(searchItem);
        sear.setQueryHint("Buscar algún titulo");
        sear.setIconifiedByDefault(true);
        sear.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                buscar(newText);
                return false;
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public ArrayList<CanalOb> hashToList(HashMap<String,CanalOb> hash){
        ArrayList<CanalOb> lista=new ArrayList<>();
        hash.forEach((s, canalOb) -> lista.add(canalOb));
        return lista;
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public HashMap<String,CanalOb> listToHash(ArrayList<CanalOb> lista){
        HashMap<String,CanalOb> hashMap= new HashMap<>();
        lista.forEach(canalOb -> hashMap.put(canalOb.getNombre(),canalOb));
        return hashMap;
    }
    public void mensaje(String mensaje){
        Toast.makeText(getContext(),mensaje,Toast.LENGTH_LONG);
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setListas(ArrayList<CanalOb> lista){
        listaObjetos.post(new Runnable() {
            @Override
            public void run() {
                if (isVisible()) {
                    lista_bus=lista;
                    lista_objetos=lista;
                    lista_hash=listToHash(lista);
                    adaptador = new Adaptador_Canales(getContext(),lista_bus);
                    listaObjetos.setAdapter(adaptador);
                }
            }
        });
    }

    public CanalOb getObjeto(int pos){
        return lista_bus.get(pos);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void select(int which) throws Exception{
        lista_objetos=listaObjeto(datos.getString("lista"));
        lista_bus.clear();
        lista_bus.addAll(lista_objetos);
        SharedPreferences.Editor editor = getActivity().getSharedPreferences("Usuarios",Context.MODE_PRIVATE).edit();
        editor.putInt(datos.getString("ordena"),which);
        editor.commit();
        switch (which) {
            case 0:
                adaptador.notifyDataSetChanged();
                break;
            case 1:
                ordenar(new Comparator<CanalOb>() {
                    @Override
                    public int compare(CanalOb canalOb, CanalOb t1) {
                        return canalOb.getNombre().compareTo(t1.getNombre());
                    }
                });
                break;
            case 2:
                ordenar(new Comparator<CanalOb>() {
                    @Override
                    public int compare(CanalOb canalOb, CanalOb t1) {
                        String cale1="",cale2="";
                        if(!canalOb.getCalificacion().contains("Sin")){
                            cale1=canalOb.getCalificacion().replace("Calificación ", "").replace("/10","");
                        }
                        if(!t1.getCalificacion().contains("Sin")){
                            cale2=t1.getCalificacion().replace("Calificación ", "").replace("/10","");
                        }
                        return cale2.compareTo(cale1);
                    }
                });
                break;
            case 3:
                ordenar(new Comparator<CanalOb>() {
                    @Override
                    public int compare(CanalOb canalOb, CanalOb t1) {
                        String cale1="",cale2="";
                        if(canalOb.getNombre().contains("(")){
                            cale1=canalOb.getNombre().substring(canalOb.getNombre().indexOf("("));
                        }
                        if(t1.getNombre().contains("(")){
                            cale2=t1.getNombre().substring(t1.getNombre().indexOf("("));
                        }
                        return cale2.compareTo(cale1);
                    }
                });
                break;
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void ordenar(Comparator<CanalOb> comparator){
        lista_bus.sort(comparator);
        adaptador.notifyDataSetChanged();
    }


}
