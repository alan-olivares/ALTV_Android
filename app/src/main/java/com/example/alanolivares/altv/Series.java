package com.example.alanolivares.altv;

import android.app.AlertDialog;
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
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Series extends Fragment {
    private final String EXT_INF = "#EXTINF";
    private final String EXT_LOGO = "tvg-logo";
    private final String EXT_GROUP = "group-title";
    private GridView listViewCanales;
    SearchView sear;
    private Adaptador_Canales adaptador;
    ArrayAdapter<String> adapter;
    ArrayList<CanalOb> lista_canales;
    ArrayList<CanalOb> lista_series,lista_bus,lista_des,lista_favoritos,lista_fecha;
    Boolean check=true;
    View progressBar;
    Context con;
    ArrayList<CanalOb> lista_capitulos2;
    private static ProgressDialog progressDialog;
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view;
        view= inflater.inflate(R.layout.fragment_series,container,false);
        lista_bus=new ArrayList<>();
        con=view.getContext();
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Series");
        setHasOptionsMenu(true);
        listViewCanales = (GridView)view.findViewById(R.id.listViewSeries);
        //progressBar=view.findViewById(R.id.progress_bar);

        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_MONTH);
        SharedPreferences preferences = getActivity().getSharedPreferences("Usuarios",Context.MODE_PRIVATE);
        int dia = preferences.getInt("diaSeries",80);
        String nuevaVersiion = preferences.getString("NuevaVeS","No existe");
        if(dia!=dayOfWeek||nuevaVersiion.equals("No existe")){
            if(isOnlineNet()){
                if(nuevaVersiion.equals("No existe")){
                    System.out.println(nuevaVersiion);
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences("Usuarios",Context.MODE_PRIVATE).edit();
                    editor.putString("NuevaVeS","Si");
                    editor.commit();
                }
                new MyTask().execute();
                check=false;
            }else {
                Toast.makeText(getContext(), "Revisa tu conexión a internet", Toast.LENGTH_LONG).show();
            }
        }else{
            Gson gson = new Gson();
            String savedList = preferences.getString("listaSeries","No existe");
            String savedList2 = preferences.getString("listaCapitulos","No existe");
            Type type = new TypeToken<ArrayList<CanalOb>>(){}.getType();
            ArrayList<CanalOb> listacaheSeries = gson.fromJson(savedList, type);
            lista_series=new ArrayList<>();
            lista_series=listacaheSeries;
            lista_bus=listacaheSeries;
            ArrayList<CanalOb> listacaheCapitulos = gson.fromJson(savedList2, type);
            lista_canales=listacaheCapitulos;
            //System.out.println(lista_bus.get(12).getNombre());
            adaptador = new Adaptador_Canales(getContext(),lista_bus);
            listViewCanales.setAdapter(adaptador);
        }
        final SwipeRefreshLayout mSwipeRefreshView;
        mSwipeRefreshView = (SwipeRefreshLayout) view.findViewById(R.id.swipe_Series);
        mSwipeRefreshView.setColorSchemeResources(R.color.naranja, R.color.verde, R.color.azul);
        mSwipeRefreshView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(isOnlineNet()){
                    new MyTask().execute();
                }else {
                    Toast.makeText(getContext(), "Revisa tu conexión a internet", Toast.LENGTH_LONG).show();
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

        listViewCanales.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int viewId = (int)id;
                switch (viewId) {
                    case 0:
                        lista_capitulos2= new ArrayList<>();
                        //lista_capitulos=getIntent().getParcelableArrayListExtra("capitulos");
                        for(int x=0;x<lista_canales.size();x++){
                            if(lista_canales.get(x).nombre.equals(lista_bus.get(position).nombre)){
                                lista_capitulos2.add(lista_canales.get(x));
                                System.out.println(lista_canales.get(x).getNombre());
                            }
                        }
                        view.clearFocus();
                        if (view != null) {
                            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }
                        Intent pas =new Intent(view.getContext(),Capitulos.class);
                        //System.out.println(lista_canales.get(position).getNombre());
                        pas.putParcelableArrayListExtra("capitulos", lista_capitulos2);
                        pas.putExtra("serie",lista_bus.get(position).getNombre());
                        pas.putExtra("imagen",lista_bus.get(position).getImagen());
                        pas.putExtra("cal",lista_bus.get(position).calificacion);
                        pas.putExtra("fav",lista_bus.get(position).favo);
                        pas.putExtra("des",lista_bus.get(position).descripcion);
                        pas.putExtra("fecha",lista_bus.get(position).fecha);
                        pas.putExtra("fechaCap",lista_bus.get(position).fechaCap);
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
        String jsonList = gson.toJson(lista_series);
        SharedPreferences.Editor editor = getActivity().getSharedPreferences("Usuarios",Context.MODE_PRIVATE).edit();
        editor.putString("listaSeries",jsonList);
        editor.commit();
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
        sear.setQueryHint("Buscar Series");
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
    public void buscar(String entrada){
        lista_bus=new ArrayList<>();
        for(int x=0;x<lista_series.size();x++){
            if(!lista_series.get(x).getNombre().toLowerCase().contains(entrada.toLowerCase())){
                //lista_canales.remove(x);
                //System.out.println(lista_canales.get(x).getNombre());
            }
            else{
                lista_bus.add(lista_series.get(x));
                System.out.println(lista_series.get(x).getNombre());
            }
        }
        adaptador = new Adaptador_Canales(con,lista_bus);
        //new MyTask().execute();
        listViewCanales.setAdapter(adaptador);
    }
    public Boolean isOnlineNet() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo actNetInfo = connectivityManager.getActiveNetworkInfo();

        return (actNetInfo != null && actNetInfo.isConnected());
    }
    @Override
    public void onPause() {
        super.onPause();
        check=true;
    }
    @Override
    public void onResume(){
        super.onResume();
        System.out.println("On Resume");
        SharedPreferences preferences = getActivity().getSharedPreferences("Usuarios",Context.MODE_PRIVATE);
        Calendar calendar = Calendar.getInstance();
        int checkItem = preferences.getInt("ordenaSeries",-1);
        select(checkItem);

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
        listaopc.add("Nombre");
        listaopc.add("Calificación");
        SharedPreferences preferences = getActivity().getSharedPreferences("Usuarios",Context.MODE_PRIVATE);
        int checkItem = preferences.getInt("ordenaSeries",-1);
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
        String savedList = preferences.getString("listaSeries","No existe");
        if(!savedList.equals("No existe")&&check) {
            Type type = new TypeToken<ArrayList<CanalOb>>() {
            }.getType();
            ArrayList<CanalOb> listacaheCanales = gson.fromJson(savedList, type);
            lista_series = listacaheCanales;
            lista_bus.clear();
            lista_bus.addAll(lista_series);
            adaptador.notifyDataSetChanged();
        }
        SharedPreferences.Editor editor = getActivity().getSharedPreferences("Usuarios",Context.MODE_PRIVATE).edit();
        editor.putInt("ordenaSeries",which);
        editor.commit();
        switch (which) {
            case 0:
                //((AppCompatActivity) getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.contenedor, new Canales()).commit();
                break;
            case 1:
                PorCalificacion(lista_series);
                break;
        }
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
                    //list.remove(j);
                    //list.add(j, list.get(j+1));
                    list.set(j, list.get(j+1));
                    //list.remove(j+1);
                    //list.add(aux.get(cont));
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
            progressDialog.setMessage("Cargando Series");
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(false);
        }

        @Override
        protected Canales doInBackground(Void... urls) {
            //progressDialog=ProgressDialog.show(con,"","Iniciando sesion..",true);
            try {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                Date date = new Date();
                System.out.println(formatter.format(date));
                SharedPreferences preferences = getActivity().getSharedPreferences("Usuarios",Context.MODE_PRIVATE);
                Gson gson = new Gson();
                String savedList = preferences.getString("listaFavoritos","No existe");
                String savedList2 = preferences.getString("listaSeries","No existe");
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
                Boolean favo=false;
                //String fullString = urls[0];
                String da[]= new String[3];
                String nombre="";
                String link="";
                String logo="";
                String cap="";
                da[0]="";
                da[1]="";
                da[2]="";
                URL url3 = new URL("Direccion web de archivo m3u");
                BufferedReader reader1 = new BufferedReader(new InputStreamReader(url3.openStream()));
                String currLine;
                lista_canales=new ArrayList<>();
                lista_series=new ArrayList<>();
                while ((currLine = reader1.readLine()) != null) {
                    //Thread.sleep(15);
                    if(currLine.length()>5){
                        if (currLine.contains(EXT_INF)) {
                            String[] dataArray = currLine.split("\"");
                            for (int x=0;x<=dataArray.length-1;x++){
                                if(dataArray[x].contains(EXT_LOGO)){
                                    logo=dataArray[x+1];

                                }
                                if(dataArray[x].contains(EXT_GROUP)){
                                    nombre=dataArray[x+1];
                                }
                            }
                            cap=dataArray[dataArray.length-1].replace(",", "");
                        }
                        if (currLine.substring(0, 4).equals("http")) {
                            link = currLine;
                            //System.out.println(nombre+" "+logo+" "+link);
                            //publishProgress(da);
                            lista_canales.add(new CanalOb(nombre, logo, link,cap,"","",false,"",0,0,""));
                            //Thread.sleep(15);
                            nombre="";
                            logo="";
                            link="";
                            //lista_canales.add(new CanalOb(name, icon, url1));
                            //lista_canales.add(new CanalOb(name, icon, url1));
                        }

                    }
                }
                //adaptador = new Adaptador_Canales(getContext(),lista_canales);
                //listViewCanales.setAdapter(adaptador);
                reader1.close();
                Boolean co,sar=false;
                co=false;
                String na="";
                int contar=0;
                int con=0;
                for (int a=0;a<lista_canales.size();a++){
                    na=lista_canales.get(a).nombre;
                    contar+=1;
                    if(sar==false){
                        for(int t=0;t<lista_favoritos.size();t++){
                            if(na.equals(lista_favoritos.get(t).nombre)){
                                favo=true;
                            }
                        }
                        lista_series.add(new CanalOb(lista_canales.get(a).nombre,lista_canales.get(a).getImagen(),lista_canales.get(a).getLink(),lista_canales.get(a).capitulo,lista_canales.get(a).calificacion,lista_canales.get(a).descripcion,favo,formatter.format(date),0,contar,""));
                        sar=true;
                    }else {
                        for (int z = 0; z < lista_series.size(); z++) {
                            if (na.equals(lista_series.get(z).nombre)) {
                                co=true;
                            }
                        }
                        if(co==false){
                            for(int x=0;x<lista_favoritos.size();x++){
                                if(na.equals(lista_favoritos.get(x).nombre)){
                                    favo=true;
                                }
                            }
                            System.out.println(contar);
                            lista_series.get(lista_series.size()-1).setNumeroCap(contar);
                            lista_series.add(new CanalOb(lista_canales.get(a).nombre,lista_canales.get(a).getImagen(),lista_canales.get(a).getLink(),lista_canales.get(a).capitulo,lista_canales.get(a).calificacion,lista_canales.get(a).descripcion,favo,formatter.format(date),0,contar,""));
                            System.out.println("entra");
                            contar=0;
                        }
                    }
                    favo=false;
                    co=false;
                }
                if(!savedList2.equals("No existe")) {
                    for (int x = 0; x < lista_series.size(); x++) {
                        for (int a = 0; a < lista_fecha.size(); a++) {
                            if (lista_series.get(x).nombre.equals(lista_fecha.get(a).nombre)) {
                                if (!lista_fecha.get(a).fecha.equals("")) {
                                    lista_series.get(x).setFecha(lista_fecha.get(a).fecha);
                                    lista_series.get(x).setFechaCap(lista_fecha.get(a).fechaCap);
                                }
                                if (lista_fecha.get(a).numeroCap < lista_series.get(x).numeroCap) {
                                    lista_series.get(x).setFechaCap(formatter.format(date));
                                }
                            }
                        }
                        System.out.println(lista_series.get(x).numeroCap);
                    }
                }
                String titulo="";
                String calificacion="";
                String descripcion="";
                URL url4 = new URL("Los detalles de informacion en formato xml debe de ir aquí");
                BufferedReader reader2 = new BufferedReader(new InputStreamReader(url4.openStream()));
                String currLine2="";
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
                        for(int x=0;x<lista_series.size();x++){
                            if(con%2==0) {
                                if (titulo.replace(" ","").equals(lista_series.get(x).nombre.replace(" ",""))) {
                                    lista_series.set(x,new CanalOb(lista_series.get(x).nombre, lista_series.get(x).imagen, lista_series.get(x).link, lista_series.get(x).capitulo, calificacion+"/10", descripcion,lista_series.get(x).favo,lista_series.get(x).fecha,x+1,lista_series.get(x).numeroCap,lista_series.get(x).fechaCap));
                                    //lista_des.add(new CanalOb(lista_canales.get(x).nombre, lista_canales.get(x).imagen, lista_canales.get(x).link, "", calificacion, descripcion));
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
            //adapter.add(new CanalOb(values[1], values[0], values[2]));
        }

        @Override
        protected void onPostExecute(Canales canales) {
            Gson gson = new Gson();
            Calendar calendar = Calendar.getInstance();
            int dayOfWeek = calendar.get(Calendar.DAY_OF_MONTH);
            String jsonList = gson.toJson(lista_canales);
            String jsonList2 = gson.toJson(lista_series);
            SharedPreferences.Editor editor = getActivity().getSharedPreferences("Usuarios",Context.MODE_PRIVATE).edit();
            editor.putString("listaCapitulos",jsonList);
            editor.putString("listaSeries",jsonList2);
            editor.putInt("diaSeries",dayOfWeek);
            editor.putInt("ordenaSeries",0);
            editor.commit();
            lista_bus=lista_series;
            if(progressDialog.isShowing()){
                progressDialog.dismiss();
            }
            adaptador = new Adaptador_Canales(con,lista_series);
            //new MyTask().execute();
            listViewCanales.setAdapter(adaptador);
            check=true;
        }
    }
}

