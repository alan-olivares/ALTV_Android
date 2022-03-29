package com.example.alanolivares.altv;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.alanolivares.altv.Funciones.CanalOb;
import com.example.alanolivares.altv.Funciones.Funciones;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class Favoritos extends Fragment {
    private GridView listViewCanales;
    private ArrayList<CanalOb> lista_canales,lista_capi,lista_actualizar;
    private Funciones func;
    private Adaptador_Canales adaptador;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view;
        view=inflater.inflate(R.layout.fragment_favoritos, container, false);
        listViewCanales = (GridView)view.findViewById(R.id.listViewFavoritos);
        int cant=(getActivity().getResources().getConfiguration().orientation== Configuration.ORIENTATION_PORTRAIT)?3:6;
        listViewCanales.setNumColumns(cant);
        func=new Funciones(getContext());
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Favoritos");
        listViewCanales.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int viewId = (int) id;
                switch (viewId) {
                    case 0:
                        seleccionar(position);
                        break;
                    case 1:
                        quitar(position);
                        adaptador.notifyDataSetChanged();
                        break;
                }
            }
        });
        return view;
    }
    @Override
    public void onResume(){
        super.onResume();
        getFavs();
    }
    private void getFavs(){
        lista_canales=func.listaObjeto("listaFavoritos");
        if (lista_canales.isEmpty()) {
            Toast.makeText(getContext(), "Lista de favoritos vacia :c", Toast.LENGTH_SHORT).show();
        }
        adaptador = new Adaptador_Canales(getContext(),lista_canales);
        listViewCanales.setAdapter(adaptador);
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        View c = listViewCanales.getChildAt(0);
        if(c!=null){
            final int scrolly = (-c.getTop() + listViewCanales.getFirstVisiblePosition() * c.getHeight())/(180*3);
            listViewCanales.post(new Runnable() {
                @Override
                public void run() {
                    listViewCanales.setSelection(scrolly);
                }
            });
        }
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            listViewCanales.setNumColumns(6);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            listViewCanales.setNumColumns(3);
        }
    }
    public void seleccionar(int position){
        Intent pas;
        if(!lista_canales.get(position).getCapitulo().equals("")){
            lista_capi=func.listaObjeto("listaCapitulos");
            ArrayList<CanalOb> lista_envia=new ArrayList<>();
            for(int x=0;x<lista_capi.size();x++){
                if(lista_canales.get(position).getNombre().equals(lista_capi.get(x).getNombre())){
                    lista_envia.add(lista_capi.get(x));
                }
            }
            pas =new Intent(getContext(),Capitulos.class);
            pas.putParcelableArrayListExtra("capitulos", lista_envia);
            pas.putExtra("objeto",lista_canales.get(position));
        }else if(lista_canales.get(position).getCapitulo().equals("")&&lista_canales.get(position).getLink().contains("googleusercontent")){
            pas =new Intent(getContext(),Descripcion.class);
            pas.putExtra("objeto",lista_canales.get(position));
        }else{
            pas =new Intent(getContext(),ReproducirCanal.class);
            pas.putExtra("link", lista_canales.get(position).getLink());
            pas.putExtra("nombre", lista_canales.get(position).getNombre());
        }
        startActivity(pas);
    }

    private void quitaFav( int position){
        for(int x=0;x<lista_actualizar.size();x++){
            if(lista_actualizar.get(x).getNombre().equals(lista_canales.get(position).getNombre())){
                lista_actualizar.get(x).setFavo(false);
                return;
            }
        }
    }
    public void quitar(int position){
        if(!lista_canales.get(position).getCapitulo().equals("")){
            lista_actualizar=func.listaObjeto("listaSeries");
            quitaFav(position);
            func.saveLista(lista_actualizar,"listaSeries");
        }else if(lista_canales.get(position).getCapitulo().equals("")&&lista_canales.get(position).getLink().contains("googleusercontent")){
            lista_actualizar=func.listaObjeto("listaPeliculas");
            quitaFav(position);
            func.saveLista(lista_actualizar,"listaPeliculas");
        }else if(!lista_canales.get(position).getLink().contains("googleusercontent")){
            lista_actualizar=func.listaObjeto("listaCanales");
            quitaFav(position);
            func.saveLista(lista_actualizar,"listaCanales");
        }
        lista_canales.remove(position);
        func.saveLista(lista_canales,"listaFavoritos");
        if(lista_canales.isEmpty()){
            Toast.makeText(getContext(), "Lista de favoritos vacia :c", Toast.LENGTH_SHORT).show();
        }
    }

}
