package com.example.alanolivares.altv;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SearchView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class Favoritos extends Fragment {
    private GridView listViewCanales;
    private Adaptador_Canales adaptador;
    ArrayList<CanalOb> lista_canales,lista_envia,lista_actualizar,lista_capi;
    SearchView sear;
    final Gson gson = new Gson();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view;
        view=inflater.inflate(R.layout.fragment_favoritos, container, false);
        listViewCanales = (GridView)view.findViewById(R.id.listViewFavoritos);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Favoritos");
        SharedPreferences preferences = getActivity().getSharedPreferences("Usuarios",Context.MODE_PRIVATE);
        String savedList = preferences.getString("listaFavoritos","No existe");
        lista_canales=new ArrayList<>();
        if(!savedList.equals("No existe")){
            Type type = new TypeToken<ArrayList<CanalOb>>(){}.getType();
            ArrayList<CanalOb> listacaheCanales = gson.fromJson(savedList, type);
            lista_canales=listacaheCanales;
            adaptador = new Adaptador_Canales(getContext(),lista_canales);
            listViewCanales.setAdapter(adaptador);
        }
        if(lista_canales.isEmpty()){
            Snackbar
                    .make(getActivity().findViewById(android.R.id.content), "Lista de favoritos vacia :c",Snackbar.LENGTH_LONG)
                    .show();
        }
        listViewCanales.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int viewId = (int) id;
                switch (viewId) {
                    case 0:
                        seleccionar(position,view);
                        break;
                    case 1:
                        quitar(position);
                        break;
                }
            }
        });


        return view;
    }
    @Override
    public void onResume(){
        super.onResume();
        Gson gson = new Gson();
        SharedPreferences preferences = getActivity().getSharedPreferences("Usuarios",Context.MODE_PRIVATE);
        String savedList = preferences.getString("listaFavoritos","No existe");
        Type type = new TypeToken<ArrayList<CanalOb>>(){}.getType();
        if(!savedList.equals("No existe")) {
            ArrayList<CanalOb> listacaheCanales = gson.fromJson(savedList, type);
            lista_canales.clear();
            lista_canales.addAll(listacaheCanales);
            adaptador.notifyDataSetChanged();
            if (lista_canales.isEmpty()) {
                Snackbar
                        .make(getActivity().findViewById(android.R.id.content), "Lista de favoritos vacia :c", Snackbar.LENGTH_LONG)
                        .show();
            }
        }

    }
    public void seleccionar(int position,View view){
        if(!lista_canales.get(position).capitulo.equals("")){
            SharedPreferences preferences = getActivity().getSharedPreferences("Usuarios",Context.MODE_PRIVATE);
            String savedList2 = preferences.getString("listaCapitulos","No existe");
            Type type = new TypeToken<ArrayList<CanalOb>>(){}.getType();
            ArrayList<CanalOb> listacaheCapitulos = gson.fromJson(savedList2, type);
            lista_capi=listacaheCapitulos;
            lista_envia=new ArrayList<>();
            for(int x=0;x<lista_capi.size();x++){
                if(lista_canales.get(position).nombre.equals(lista_capi.get(x).nombre)){
                    lista_envia.add(lista_capi.get(x));
                }
            }
            Intent pas =new Intent(view.getContext(),Capitulos.class);
            pas.putParcelableArrayListExtra("capitulos", lista_envia);
            pas.putExtra("serie",lista_canales.get(position).getNombre());
            pas.putExtra("imagen",lista_canales.get(position).getImagen());
            pas.putExtra("cal",lista_canales.get(position).calificacion);
            pas.putExtra("des",lista_canales.get(position).descripcion);
            pas.putExtra("fav",lista_canales.get(position).favo);
            pas.putExtra("fecha",lista_canales.get(position).fecha);
            pas.putExtra("fechaCap",lista_canales.get(position).fechaCap);
            startActivity(pas);
        }else if(lista_canales.get(position).capitulo.equals("")&&lista_canales.get(position).link.contains("googleusercontent")){
            Intent pas =new Intent(view.getContext(),Descripcion.class);
            pas.putExtra("nom",lista_canales.get(position).getNombre());
            pas.putExtra("link", lista_canales.get(position).getLink());
            pas.putExtra("cal",lista_canales.get(position).calificacion);
            pas.putExtra("des",lista_canales.get(position).descripcion);
            pas.putExtra("fav",lista_canales.get(position).favo);
            pas.putExtra("ima",lista_canales.get(position).getImagen());
            pas.putExtra("fecha",lista_canales.get(position).fecha);
            startActivity(pas);
        }else if(!lista_canales.get(position).link.contains("googleusercontent")){
            Intent pas =new Intent(view.getContext(),ReproducirCanal.class);
            pas.putExtra("link", lista_canales.get(position).getLink());
            pas.putExtra("nombre", lista_canales.get(position).getNombre());
            startActivity(pas);
        }
    }
    public void quitar(int position){
        lista_actualizar=new ArrayList<>();
        if(!lista_canales.get(position).capitulo.equals("")){
            SharedPreferences preferences = getActivity().getSharedPreferences("Usuarios",Context.MODE_PRIVATE);
            String savedList2 = preferences.getString("listaSeries","No existe");
            Type type = new TypeToken<ArrayList<CanalOb>>(){}.getType();
            ArrayList<CanalOb> listacaheSeries = gson.fromJson(savedList2, type);
            lista_actualizar=listacaheSeries;
            for(int x=0;x<lista_actualizar.size();x++){
                if(lista_actualizar.get(x).nombre.equals(lista_canales.get(position).nombre)){
                    lista_actualizar.get(x).setFavo(false);
                }
            }
            String jsonList2 = gson.toJson(lista_actualizar);
            SharedPreferences.Editor editor = getActivity().getSharedPreferences("Usuarios",Context.MODE_PRIVATE).edit();
            editor.putString("listaSeries",jsonList2);
            editor.commit();
        }else if(lista_canales.get(position).capitulo.equals("")&&lista_canales.get(position).link.contains("googleusercontent")){
            SharedPreferences preferences = getActivity().getSharedPreferences("Usuarios",Context.MODE_PRIVATE);
            String savedList2 = preferences.getString("listaPeliculas","No existe");
            Type type = new TypeToken<ArrayList<CanalOb>>(){}.getType();
            ArrayList<CanalOb> listacaheSeries = gson.fromJson(savedList2, type);
            lista_actualizar=listacaheSeries;
            for(int x=0;x<lista_actualizar.size();x++){
                if(lista_actualizar.get(x).nombre.equals(lista_canales.get(position).nombre)){
                    lista_actualizar.get(x).setFavo(false);
                }
            }
            String jsonList2 = gson.toJson(lista_actualizar);
            SharedPreferences.Editor editor = getActivity().getSharedPreferences("Usuarios",Context.MODE_PRIVATE).edit();
            editor.putString("listaPeliculas",jsonList2);
            editor.commit();
        }else if(!lista_canales.get(position).link.contains("googleusercontent")){
            SharedPreferences preferences = getActivity().getSharedPreferences("Usuarios",Context.MODE_PRIVATE);
            String savedList2 = preferences.getString("listaCanales","No existe");
            Type type = new TypeToken<ArrayList<CanalOb>>(){}.getType();
            ArrayList<CanalOb> listacaheSeries = gson.fromJson(savedList2, type);
            lista_actualizar=listacaheSeries;
            for(int x=0;x<lista_actualizar.size();x++){
                if(lista_actualizar.get(x).nombre.equals(lista_canales.get(position).nombre)){
                    lista_actualizar.get(x).setFavo(false);
                }
            }
            String jsonList2 = gson.toJson(lista_actualizar);
            SharedPreferences.Editor editor = getActivity().getSharedPreferences("Usuarios",Context.MODE_PRIVATE).edit();
            editor.putString("listaCanales",jsonList2);
            editor.commit();
        }
        lista_canales.get(position).setFavo(false);
        lista_canales.remove(position);
        String jsonList2 = gson.toJson(lista_canales);
        SharedPreferences.Editor editor2 = getActivity().getSharedPreferences("Usuarios",Context.MODE_PRIVATE).edit();
        editor2.putString("listaFavoritos",jsonList2);
        editor2.commit();
        adaptador.notifyDataSetChanged();
        if(lista_canales.isEmpty()){
            Snackbar
                    .make(getActivity().findViewById(android.R.id.content), "Lista de favoritos vacia",Snackbar.LENGTH_LONG)
                    .show();
        }
    }

}
