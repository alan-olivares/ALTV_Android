package com.example.alanolivares.altv;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import android.widget.SearchView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import hb.xvideoplayer.MxVideoPlayerWidget;


public class Canales extends Fragment {
    String url1= "https://pastebin.com/raw/UE6xPb7T";
    private final String EXT_INF = "#EXTINF";
    private final String EXT_LOGO = "tvg-logo";
    private GridView listViewCanales;
    ArrayAdapter<String> adapter;
    Boolean check=true;
    private Adaptador_Canales adaptador;
    ArrayList<CanalOb> lista_canales,lista_bus,lista_favoritos;
    SearchView sear;
    Context con;
    ProgressDialog progressDialog;
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view;
        view= inflater.inflate(R.layout.fragment_canales,container,false);
        con=view.getContext();
        if (ContextCompat.checkSelfPermission(con,
                Manifest.permission.WRITE_SETTINGS)
                == PackageManager.PERMISSION_GRANTED){

        }else{
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_SETTINGS}, 2);
        }
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_MONTH);
        lista_bus=new ArrayList<>();
        listViewCanales = (GridView)view.findViewById(R.id.listviewCanales);
        SharedPreferences preferences = getActivity().getSharedPreferences("Usuarios",Context.MODE_PRIVATE);
        int dia = preferences.getInt("diaCanales",80);
        String nuevaVersiion = preferences.getString("NuevaVersionCanales","No existe");
        if(dia!=dayOfWeek||nuevaVersiion.equals("No existe")){
            if(isOnlineNet()){
                if(nuevaVersiion.equals("No existe")){
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences("Usuarios",Context.MODE_PRIVATE).edit();
                    editor.putString("NuevaVersionCanales","Si");
                    editor.commit();
                }
                new MyTask().execute();
                check=false;
            }else {
                Toast.makeText(getContext(), "Revisa tu conexión a internet", Toast.LENGTH_LONG).show();
            }
        }else{
            Gson gson = new Gson();
            String savedList = preferences.getString("listaCanales","No existe");
            Type type = new TypeToken<ArrayList<CanalOb>>(){}.getType();
            ArrayList<CanalOb> listacaheCanales = gson.fromJson(savedList, type);
            lista_canales=new ArrayList<>();
            lista_canales=listacaheCanales;
            lista_bus=listacaheCanales;
            System.out.println(lista_bus.get(12).getNombre());
            adaptador = new Adaptador_Canales(getContext(),lista_bus);
            listViewCanales.setAdapter(adaptador);
        }
        final SwipeRefreshLayout mSwipeRefreshView;
        mSwipeRefreshView = (SwipeRefreshLayout) view.findViewById(R.id.swipe_Canales);
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
        //progressBar=view.findViewById(R.id.progress_bar);
        setHasOptionsMenu(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Canales");
        listViewCanales.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int viewId = (int)id;
                switch (viewId){
                    case 0:
                        view.clearFocus();
                        if (view != null) {
                            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }
                        //MxVideoPlayerWidget.startFullscreen(getContext(), MxVideoPlayerWidget.class, lista_bus.get(position).getLink(), lista_bus.get(position).getNombre());
                        //MxVideoPlayerWidget.startFullscreen(view.getContext(), MxVideoPlayerWidget.class, lista_bus.get(position).getLink().replace("m3u8","ts"), lista_bus.get(position).getNombre());
                        Intent pas =new Intent(view.getContext(),ReproducirCanal.class);
                        System.out.println(lista_bus.get(position).getImagen());
                        pas.putExtra("link", lista_bus.get(position).getLink());
                        pas.putExtra("nombre", lista_bus.get(position).getNombre());
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
        int checkItem = preferences.getInt("ordenaCanales",-1);
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_MONTH);
        int dia = preferences.getInt("diaCanales",80);
        String nuevaVersiion = preferences.getString("NuevaVersionCanales","No existe");
        if((dia==dayOfWeek)&&!nuevaVersiion.equals("No existe")){
            select(checkItem);
        }

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
        editor.putString("listaCanales",jsonList);
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
                //lista_canales.remove(x);
                //System.out.println(lista_canales.get(x).getNombre());
            }
            else{
                lista_bus.add(lista_canales.get(x));
                System.out.println(lista_canales.get(x).getNombre());
            }
        }
        adaptador = new Adaptador_Canales(con,lista_bus);
        //new MyTask().execute();
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
        sear.setQueryHint("Buscar Canales");
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
        listaopc.add("Categorias");
        listaopc.add("Nombre");
        SharedPreferences preferences = getActivity().getSharedPreferences("Usuarios",Context.MODE_PRIVATE);
        int checkItem = preferences.getInt("ordenaCanales",-1);
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
        String savedList = preferences.getString("listaCanales","No existe");
        if(!savedList.equals("No existe")&&check) {
            Type type = new TypeToken<ArrayList<CanalOb>>() {
            }.getType();
            ArrayList<CanalOb> listacaheCanales = gson.fromJson(savedList, type);
            lista_canales = listacaheCanales;
            lista_bus.clear();
            lista_bus.addAll(lista_canales);
            adaptador.notifyDataSetChanged();
        }
        SharedPreferences.Editor editor = getActivity().getSharedPreferences("Usuarios",Context.MODE_PRIVATE).edit();
        editor.putInt("ordenaCanales",which);
        editor.commit();
        switch (which) {
            case 0:
                //((AppCompatActivity) getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.contenedor, new Canales()).commit();
                break;
            case 1:
                PorNombre(lista_canales);
                break;
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        check=true;
        /*SharedPreferences.Editor editor = getActivity().getSharedPreferences("Usuarios",Context.MODE_PRIVATE).edit();
        editor.putInt("ordenaPeliculas",0);
        editor.commit();*/
    }

    public void PorNombre(ArrayList<CanalOb> list){
        int cont=0;
        ArrayList<CanalOb> aux=new ArrayList<CanalOb>();
        for(int i=1; i<=list.size(); i++) {
            for(int j=0; j<list.size()-i; j++) {
                if( list.get(j).nombre.compareTo( list.get(j+1).nombre ) > 0 ) {
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
        //adaptador = new Adaptador_Canales(con,lista_bus);
        //new MyTask().execute();
        //listViewCanales.setAdapter(adaptador);
    }



     private class MyTask extends AsyncTask<Void, String, Canales> {
        Canales canal;
         @Override
         protected void onPreExecute() {
             progressDialog=new ProgressDialog(con);
             progressDialog.setTitle("Por favor espera");
             progressDialog.setMessage("Cargando Canales");
             progressDialog.show();
             progressDialog.setCanceledOnTouchOutside(false);
             //adapter=(ArrayAdapter<CanalOb>)listViewCanales.getAdapter();
         }

         @Override
         protected Canales doInBackground(Void... urls) {
             //progressDialog=ProgressDialog.show(con,"","Iniciando sesion..",true);
            try {
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
                //String fullString = urls[0];
                String da[]= new String[3];
                String nombre="";
                String link="";
                String logo="";
                int cont=1;
                Boolean favo=false;
                da[0]="";
                da[1]="";
                da[2]="";
                URL url3 = new URL("Direccion web de archivo m3u");
                BufferedReader reader1 = new BufferedReader(new InputStreamReader(url3.openStream()));
                String currLine;
                lista_canales=new ArrayList<>();
                String[] dataArray;
                while ((currLine = reader1.readLine()) != null) {
                    //Thread.sleep(15);
                    if(currLine.length()>5){
                        if (currLine.contains(EXT_INF)) {
                            currLine=currLine.replace("#EXTINF:-1","");
                             dataArray = currLine.split("\"");
                            if(dataArray.equals(null))
                                dataArray = currLine.split(",");
                            for (int x=0;x<=dataArray.length-1;x++){
                                if(dataArray[x].contains(EXT_LOGO)){
                                    logo=dataArray[x+1];

                                }

                            }
                            nombre=dataArray[dataArray.length-1];
                            nombre=nombre.replace(",","");
                        }
                        if (currLine.substring(0, 4).equals("http")) {
                            link = currLine;
                            //System.out.println(nombre+" "+logo+" "+link);
                            //publishProgress(da);
                            for(int x=0;x<lista_favoritos.size();x++){
                                if(nombre.equals(lista_favoritos.get(x).nombre)){
                                    favo=true;
                                }
                            }
                            lista_canales.add(new CanalOb(nombre, logo, link,"","Sin calificación","No disponible",favo,"",cont,0,""));
                            //Thread.sleep(15);
                            nombre="";
                            logo="";
                            link="";
                            cont+=1;
                            favo=false;
                        }

                    }

                }

                reader1.close();
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
             if(progressDialog.isShowing()){
                 progressDialog.dismiss();
             }
             Gson gson = new Gson();
             Calendar calendar = Calendar.getInstance();
             int dayOfWeek = calendar.get(Calendar.DAY_OF_MONTH);
             String jsonList = gson.toJson(lista_canales);
             SharedPreferences.Editor editor = getActivity().getSharedPreferences("Usuarios",Context.MODE_PRIVATE).edit();
             editor.putString("listaCanales",jsonList);
             editor.putInt("diaCanales",dayOfWeek);
             editor.putInt("ordenaCanales",0);
             editor.commit();
             lista_bus=lista_canales;
             adaptador = new Adaptador_Canales(con,lista_bus);
             //new MyTask().execute();
             listViewCanales.setAdapter(adaptador);
             check=true;
         }
     }

}



class CanalOb implements Parcelable {
    String nombre;
    String imagen;
    String link;
    String capitulo;
    String calificacion;
    String descripcion;
    Boolean favo;
    String fecha;
    int numero;
    int numeroCap;
    String fechaCap;

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public void setNumeroCap(int numeroCap) {
        this.numeroCap = numeroCap;
    }

    public CanalOb(String nombre, String imagen, String link, String capitulo, String calificacion, String descripcion, Boolean favo, String fecha, int numero, int numeroCap, String fechaCap){
        this.nombre= nombre;
        this.imagen=imagen;
        this.link=link;
        this.capitulo=capitulo;
        this.calificacion=calificacion;
        this.descripcion=descripcion;
        this.favo=favo;
        this.fecha=fecha;
        this.numero=numero;
        this.numeroCap=numeroCap;
        this.fechaCap=fechaCap;
    }

    public void setFechaCap(String fechaCap) {
        this.fechaCap = fechaCap;
    }

    public void setFavo(Boolean favo) {
        this.favo = favo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    protected CanalOb(Parcel in) {
        nombre = in.readString();
        imagen = in.readString();
        link = in.readString();
        capitulo = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(nombre);
        dest.writeString(imagen);
        dest.writeString(link);
        dest.writeString(capitulo);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<CanalOb> CREATOR = new Parcelable.Creator<CanalOb>() {
        @Override
        public CanalOb createFromParcel(Parcel in) {
            return new CanalOb(in);
        }

        @Override
        public CanalOb[] newArray(int size) {
            return new CanalOb[size];
        }
    };
}


