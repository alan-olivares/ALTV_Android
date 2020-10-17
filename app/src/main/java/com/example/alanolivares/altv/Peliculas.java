package com.example.alanolivares.altv;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


/**

 */
public class Peliculas extends Fragment{
    private final String EXT_INF = "#EXTINF";
    private final String EXT_LOGO = "tvg-logo";
    private GridView listViewCanales;
    ArrayAdapter<String> adapter;
    SearchView searchView;
    ArrayList<CanalOb> lista_canales,lista_bus,lista_des,lista_favoritos,lista_fecha,lista_porNombre;
    SearchView bus;
    SearchView sear;
    Context con;
    Adaptador_Canales adaptador;
    private ProgressBar progressBar;
    private static ProgressDialog progressDialog;
    DownloadManager downloadManager;
    private SwipeRefreshLayout swipeRefreshLayout;
    Boolean check=true;
    Fragment canales = new Canales();
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view;
        view= inflater.inflate(R.layout.fragment_peliculas,container,false);
        con=view.getContext();
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Películas");
        lista_bus=new ArrayList<>();
        listViewCanales = (GridView)view.findViewById(R.id.listViewPeliculas);
        progressBar = view.findViewById(R.id.progressBarPeli);
        setHasOptionsMenu(true);
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_MONTH);
        SharedPreferences preferences = getActivity().getSharedPreferences("Usuarios",Context.MODE_PRIVATE);
        int dia = preferences.getInt("diaPeliculas",80);
        String nuevaVersiion = preferences.getString("NuevaVersionPeliculas","No existe");
        if(dia!=dayOfWeek||nuevaVersiion.equals("No existe")){
            if(isOnlineNet()){
                if(nuevaVersiion.equals("No existe")){
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences("Usuarios",Context.MODE_PRIVATE).edit();
                    editor.putString("NuevaVersionPeliculas","Si");
                    editor.commit();
                }
                new Peliculas.MyTask().execute();
                check=false;
            }else {
                Toast.makeText(getContext(), "Revisa tu conexión a internet", Toast.LENGTH_LONG).show();
            }
        }else{
            Gson gson = new Gson();
            String savedList = preferences.getString("listaPeliculas","No existe");
            Type type = new TypeToken<ArrayList<CanalOb>>(){}.getType();
            ArrayList<CanalOb> listacaheCanales = gson.fromJson(savedList, type);
            lista_canales=new ArrayList<>();
            lista_bus=new ArrayList<>();
            lista_canales=listacaheCanales;
            lista_bus=lista_canales;
            adaptador = new Adaptador_Canales(getContext(),lista_bus);
            listViewCanales.setAdapter(adaptador);
        }
        final SwipeRefreshLayout mSwipeRefreshView;
        mSwipeRefreshView = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshView.setColorSchemeResources(R.color.naranja, R.color.verde, R.color.azul);
        mSwipeRefreshView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(isOnlineNet()){
                    new Peliculas.MyTask().execute();
                }else {
                    Toast.makeText(getContext(), "Revisa tu conexión a internet", Toast.LENGTH_LONG).show();
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshView.setRefreshing(false);
                    }
                }, 2000);
            }
        });
        listViewCanales.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
                        Intent pas =new Intent(view.getContext(),Descripcion.class);
                        pas.putExtra("nom",lista_bus.get(position).getNombre());
                        pas.putExtra("fav",lista_bus.get(position).favo);
                        pas.putExtra("link", lista_bus.get(position).getLink());
                        pas.putExtra("cal",lista_bus.get(position).calificacion);
                        pas.putExtra("des",lista_bus.get(position).descripcion);
                        pas.putExtra("ima",lista_bus.get(position).getImagen());
                        pas.putExtra("fecha",lista_bus.get(position).fecha);
                        startActivity(pas);
                        break;
                    case 1:
                        buttonfav(position);
                        break;
                }
            }
        });

        return view;
    }
    @Override
    public void onResume(){
        super.onResume();
        SharedPreferences preferences = getActivity().getSharedPreferences("Usuarios",Context.MODE_PRIVATE);
        int dia = preferences.getInt("diaPeliculas",80);
        int checkItem = preferences.getInt("ordenaPeliculas",-1);
        select(checkItem);

    }
    @Override
    public void onPause() {
        super.onPause();
        check=true;
    }

    public void buttonfav(int position){
        SharedPreferences preferences = getActivity().getSharedPreferences("Usuarios",Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String savedList = preferences.getString("listaFavoritos","No existe");
        if(!savedList.equals("No existe")){
            Type type = new TypeToken<ArrayList<CanalOb>>(){}.getType();
            ArrayList<CanalOb> listacaheFavoritos = gson.fromJson(savedList, type);
            lista_favoritos=new ArrayList<CanalOb>();
            lista_favoritos=listacaheFavoritos;
        }else{
            lista_favoritos=new ArrayList<CanalOb>();
        }
        if(lista_bus.get(position).favo){
            lista_bus.get(position).setFavo(false);
            for(int x=0;x<lista_favoritos.size();x++){
                if(lista_favoritos.get(x).nombre.equals(lista_bus.get(position).nombre)){
                    lista_favoritos.remove(x);
                }
            }
        }else {
            lista_bus.get(position).setFavo(true);
            lista_favoritos.add(lista_bus.get(position));
            Snackbar
                    .make(getActivity().findViewById(android.R.id.content), lista_bus.get(position).nombre+" agregado a favoritos",Snackbar.LENGTH_LONG)
                    .show();
        }
        String jsonList2 = gson.toJson(lista_favoritos);
        SharedPreferences.Editor editor2 = getActivity().getSharedPreferences("Usuarios",Context.MODE_PRIVATE).edit();
        editor2.putString("listaFavoritos",jsonList2);
        editor2.commit();
        adaptador.notifyDataSetChanged();
        String jsonList = gson.toJson(lista_canales);
        SharedPreferences.Editor editor = getActivity().getSharedPreferences("Usuarios",Context.MODE_PRIVATE).edit();
        editor.putString("listaPeliculas",jsonList);
        editor.commit();
    }
    public Boolean isOnlineNet() {

        ConnectivityManager connectivityManager = (ConnectivityManager)
                getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo actNetInfo = connectivityManager.getActiveNetworkInfo();

        return (actNetInfo != null && actNetInfo.isConnected());
    }
    public void buscar(String entrada){
        lista_bus=new ArrayList<>();
        for(int x=0;x<lista_canales.size();x++){
            if(!lista_canales.get(x).getNombre().toLowerCase().contains(entrada.toLowerCase())){
            }
            else{
                lista_bus.add(lista_canales.get(x));
            }
        }
        adaptador = new Adaptador_Canales(con,lista_bus);
        listViewCanales.setAdapter(adaptador);
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
        sear.setQueryHint("Buscar Peliculas");
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
    public void ordenar(){


        final android.app.AlertDialog.Builder builderSingle = new AlertDialog.Builder(getContext());
        builderSingle.setTitle("Ordenar por: ");
        builderSingle.setNegativeButton("Cerrar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        ArrayList<String> listaopc;
        listaopc=new ArrayList<>();
        listaopc.add("Recien agregadas");
        listaopc.add("Nombre");
        listaopc.add("Calificación");
        listaopc.add("Año");
        SharedPreferences preferences = getActivity().getSharedPreferences("Usuarios",Context.MODE_PRIVATE);
        int checkItem = preferences.getInt("ordenaPeliculas",-1);
        if(checkItem==-1){
            checkItem=0;
        }
        adapter= new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_single_choice,listaopc);

        builderSingle.setSingleChoiceItems(adapter, checkItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                select(which);
                if( adapter.getItemViewType(which) == 0 ){
                    dialog.dismiss();
                }
            }
        });

        builderSingle.show();
    }
    public void select(int which){
        SharedPreferences preferences = getActivity().getSharedPreferences("Usuarios",Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String savedList = preferences.getString("listaPeliculas","No existe");
        if(!savedList.equals("No existe")&&check) {
            Type type = new TypeToken<ArrayList<CanalOb>>() {
            }.getType();
            ArrayList<CanalOb> listacaheCanales = gson.fromJson(savedList, type);
            lista_canales = new ArrayList<>();
            lista_bus.clear();
            lista_bus.addAll(listacaheCanales);
            lista_canales = listacaheCanales;
            adaptador.notifyDataSetChanged();

        }
        SharedPreferences.Editor editor = getActivity().getSharedPreferences("Usuarios",Context.MODE_PRIVATE).edit();
        editor.putInt("ordenaPeliculas",which);
        editor.commit();
        switch (which) {
            case 0:
                break;
            case 1:
                PorNombre(lista_canales);
                break;
            case 2:
                PorCalificacion(lista_canales);
                break;
            case 3:
                PorAno(lista_canales);
                break;
        }
    }
    public void PorNombre(ArrayList<CanalOb> list){
        lista_porNombre=new ArrayList<>();
        int cont=0;
        ArrayList<CanalOb> aux=new ArrayList<CanalOb>();
        for(int i=1; i<=list.size(); i++) {
            for(int j=0; j<list.size()-i; j++) {
                if( list.get(j).nombre.compareTo( list.get(j+1).nombre ) > 0 ) {
                    aux.add(list.get(j));
                    list.set(j, list.get(j+1));
                    list.set(j+1, aux.get(cont));
                    cont++;
                }
            }
        }
        lista_bus.clear();
        lista_bus.addAll(list);
        adaptador.notifyDataSetChanged();
    }
    public void PorAno(ArrayList<CanalOb> list){
        lista_porNombre=new ArrayList<>();
        int cont=0;
        String cal1="";
        String cal2="";
        ArrayList<CanalOb> aux=new ArrayList<CanalOb>();
        for(int i=1; i<=list.size(); i++) {
            for(int j=0; j<list.size()-i; j++) {
                if(list.get(j).nombre.contains("(")){
                    cal1=list.get(j).nombre.substring(list.get(j).nombre.indexOf("("),list.get(j).nombre.indexOf(")"));
                }else{
                    cal1="";
                }
                if(list.get(j+1).nombre.contains("(")){
                    cal2=list.get(j+1).nombre.substring(list.get(j+1).nombre.indexOf("("),list.get(j+1).nombre.indexOf(")"));
                }else{
                    cal2="";
                }
                if( cal1.compareTo( cal2 ) < 0 ) {
                    aux.add(list.get(j));
                    list.set(j, list.get(j+1));
                    list.set(j+1, aux.get(cont));
                    cont++;
                }
            }
        }
        lista_bus.clear();
        lista_bus.addAll(list);
        adaptador.notifyDataSetChanged();
    }
    public void PorCalificacion(ArrayList<CanalOb> list){
        int cont=0;
        String cal1="";
        String cal2="";
        ArrayList<CanalOb> aux=new ArrayList<CanalOb>();
        for(int i=1; i<=list.size(); i++) {
            for(int j=0; j<list.size()-i; j++) {
                if(!list.get(j).calificacion.contains("Sin")){
                    cal1=list.get(j).calificacion.replace("Calificación ","");
                }else{
                    cal1="";
                }
                if(!list.get(j+1).calificacion.contains("Sin")){
                    cal2=list.get(j+1).calificacion.replace("Calificación ","");
                }else{
                    cal2="";
                }
                if( cal1.compareTo( cal2 ) < 0 ) {
                    aux.add(list.get(j));
                    list.set(j, list.get(j+1));
                    list.set(j+1, aux.get(cont));
                    cont++;
                }
            }
        }
        lista_bus.clear();
        lista_bus.addAll(list);
        adaptador.notifyDataSetChanged();
    }
    private class MyTask extends AsyncTask<Void, String, Canales> {
        int cont;
        Canales canal;
        @Override
        protected void onPreExecute() {
            progressDialog=new ProgressDialog(con);
            progressDialog.setTitle("Por favor espera");
            progressDialog.setMessage("Cargando Películas");
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(false);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Canales doInBackground(Void... urls) {
            try {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                Date date = new Date();
                SharedPreferences preferences = getActivity().getSharedPreferences("Usuarios",Context.MODE_PRIVATE);
                Gson gson = new Gson();
                String savedList = preferences.getString("listaFavoritos","No existe");
                String savedList2 = preferences.getString("listaPeliculas","No existe");
                Type type = new TypeToken<ArrayList<CanalOb>>(){}.getType();
                if(!savedList.equals("No existe")){
                    ArrayList<CanalOb> listacaheFavoritos = gson.fromJson(savedList, type);
                    lista_favoritos=new ArrayList<CanalOb>();
                    lista_favoritos=listacaheFavoritos;
                }else{
                    lista_favoritos=new ArrayList<CanalOb>();
                }
                if(!savedList2.equals("No existe")) {
                    ArrayList<CanalOb> listacaheFechas = gson.fromJson(savedList2, type);
                    lista_fecha=new ArrayList<CanalOb>();
                    lista_fecha=listacaheFechas;
                }else{
                    lista_fecha=new ArrayList<CanalOb>();
                }
                String nombre="";
                String link="";
                String logo="";
                int cont=1;
                Boolean favo=false;
                URL url3 = new URL("Direccion web de archivo m3u");//here edit your source with your own m3u
                BufferedReader reader1 = new BufferedReader(new InputStreamReader(url3.openStream()));
                String currLine;
                lista_canales=new ArrayList<>();
                while ((currLine = reader1.readLine()) != null) {
                    //Thread.sleep(15);
                    if(currLine.length()>5){
                        if (currLine.contains(EXT_INF)) {
                            String[] dataArray = currLine.split("\"");
                            for (int x=0;x<=dataArray.length-1;x++){
                                if(dataArray[x].contains(EXT_LOGO)){
                                    logo=dataArray[x+1];

                                }
                            }
                            nombre=dataArray[dataArray.length-1].replace(",", "");
                        }
                        if (currLine.substring(0, 4).equals("http")) {
                            link = currLine;
                            for(int x=0;x<lista_favoritos.size();x++){
                                if(nombre.equals(lista_favoritos.get(x).nombre)){
                                    favo=true;
                                }
                            }
                            lista_canales.add(new CanalOb(nombre, logo, link,"","Sin calificación","No disponible",favo,formatter.format(date),cont,0,""));
                            //Thread.sleep(15);
                            cont+=1;
                            nombre="";
                            logo="";
                            link="";
                            favo=false;
                        }

                    }
                }
                reader1.close();
                if(!savedList2.equals("No existe")) {
                    for (int x = 0; x < lista_canales.size(); x++) {
                        for (int a = 0; a < lista_fecha.size(); a++) {
                            if (lista_canales.get(x).nombre.equals(lista_fecha.get(a).nombre)) {
                                if (!lista_fecha.get(a).fecha.equals("")) {
                                    lista_canales.get(x).setFecha(lista_fecha.get(a).fecha);
                                }
                            }
                        }
                    }
                }
                String titulo="";
                String calificacion="";
                String descripcion="";
                URL url4 = new URL("La informacion de cada pelicula en formato xml debe ir aquí");
                BufferedReader reader2 = new BufferedReader(new InputStreamReader(url4.openStream()));
                String currLine2="";
                int con=0;
                boolean ch=true;
                lista_des=new ArrayList<>();
                while ((currLine2 = reader2.readLine()) != null) {
                    String[] dataArray = currLine2.split("\"");
                    if(currLine2.contains("<programme")){
                        for(int x=0;x<dataArray.length;x++){
                            if(dataArray[x].contains("channel=")){
                                titulo=dataArray[x+1];
                            }
                        }
                    }
                    if(currLine2.contains("<title lang")){
                        String[] dataArray2 = currLine2.split(">");
                        calificacion=dataArray2[1].replace("</title","");
                    }
                    if(currLine2.contains("<desc lang=")){
                        String[] dataArray2 = currLine2.split(">");
                        descripcion=dataArray2[1].replace("</desc","");
                        for(int x=0;x<lista_canales.size();x++){
                            if(con%2==0) {
                                if (titulo.replace(" ","").equals(lista_canales.get(x).nombre.replace(" ",""))) {
                                    lista_canales.set(x,new CanalOb(lista_canales.get(x).nombre, lista_canales.get(x).imagen, lista_canales.get(x).link, "", calificacion+"/10", descripcion,lista_canales.get(x).favo,lista_canales.get(x).fecha,lista_canales.get(x).numero,0,""));
                                    titulo="";
                                    calificacion="";
                                    descripcion="";
                                }
                            }
                        }
                        con++;
                    }

                }

                reader2.close();

                return canal;

            } catch (Exception e) {
                e.printStackTrace();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
        }

        @Override
        protected void onPostExecute(Canales canales) {
            if(progressDialog.isShowing()){
                progressDialog.dismiss();
            }
            progressBar.setVisibility(View.INVISIBLE);
            Gson gson = new Gson();
            Calendar calendar = Calendar.getInstance();
            int dayOfWeek = calendar.get(Calendar.DAY_OF_MONTH);
            String jsonList = gson.toJson(lista_canales);
            SharedPreferences.Editor editor = getActivity().getSharedPreferences("Usuarios",Context.MODE_PRIVATE).edit();
            editor.putString("listaPeliculas",jsonList);
            editor.putInt("diaPeliculas",dayOfWeek);
            editor.putInt("ordenaPeliculas",-1);
            editor.commit();
            lista_bus=lista_canales;
            adaptador = new Adaptador_Canales(con,lista_bus);
            listViewCanales.setAdapter(adaptador);
            check=true;
        }
    }
}

