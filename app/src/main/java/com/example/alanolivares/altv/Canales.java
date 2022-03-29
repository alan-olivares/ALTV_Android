package com.example.alanolivares.altv;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
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
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.alanolivares.altv.Funciones.CanalOb;
import com.example.alanolivares.altv.Funciones.ClaseFragmentPadre;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;



public class Canales extends ClaseFragmentPadre {
    private final String canalesURL= "https://drive.google.com/uc?id=0B4SOhnO9iLFROEl0S3FpYlc0YXc&export=download";
    private final String EXT_INF = "#EXTINF";
    private final String EXT_LOGO = "tvg-logo";
    private LinearLayout progressBar;
    //private SessionManagerListener<CastSession> mSessionManagerListener;
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view;
        view= inflater.inflate(R.layout.fragment_canales,container,false);
        GridView listViewCanales = view.findViewById(R.id.listviewCanales);
        SwipeRefreshLayout mSwipeRefreshView = (SwipeRefreshLayout) view.findViewById(R.id.swipe_Canales);
        progressBar=view.findViewById(R.id.progressBarCana);
        setTitulo("Canales");
        try {
            mainDatos(listViewCanales,mSwipeRefreshView,datos());
        } catch (Exception e) {
            mensaje("Problema al cargar los datos, error: "+e.getMessage());
        }

        return view;
    }
    private JSONObject datos(){
        JSONObject datos=new JSONObject();
        try {
            datos.put("dia","diaCanales");
            datos.put("version","NuevaVersionCanales");
            datos.put("lista","listaCanales");
            datos.put("ordena","ordenaCanales");
        } catch (JSONException e) {
            mensaje(e.getMessage());
        }
        return datos;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //CastButtonFactory.setUpMediaRouteButton(getContext(),menu,R.id.media_route_menu_item);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void ordenar(){
        ArrayAdapter<String> adapter;
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
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    select(which);
                } catch (Exception e) {
                    mensaje("Error: "+e.getMessage());
                }
                if( adapter.getItemViewType(which) == 0 ){
                    dialog.dismiss();
                }
            }
        });
        builderSingle.show();
    }

    @Override
    public void obtenerDatos() {
        new MyTask().execute();
    }

    @Override
    public void pasarIntent(CanalOb objeto, View view) {
        Intent pas =new Intent(view.getContext(),ReproducirCanal.class);
        pas.putExtra("link", objeto.getLink());
        pas.putExtra("nombre", objeto.getNombre());
        startActivity(pas);

    }


    private class MyTask extends AsyncTask<Void, String, Canales> {
        private ArrayList<CanalOb> lista_canales,lista_favoritos;
        private HashMap<String,CanalOb> hash_favoritos;
         @Override
         protected void onPreExecute() {
             progressBar.setVisibility(View.VISIBLE);
             //adapter=(ArrayAdapter<CanalOb>)listViewCanales.getAdapter();
         }

         @RequiresApi(api = Build.VERSION_CODES.N)
         @Override
         protected Canales doInBackground(Void... urls) {
             //progressDialog=ProgressDialog.show(con,"","Iniciando sesion..",true);
            try {
                lista_favoritos=listaObjeto("listaFavoritos");
                hash_favoritos=listToHash(lista_favoritos);
                //String fullString = urls[0];
                String da[]= new String[]{"","",""};
                String nombre="",link="",logo="";
                int cont=1;
                URL url3 = new URL(canalesURL);
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
                        if (currLine.startsWith("http")) {
                            link = currLine;
                            lista_canales.add(new CanalOb(nombre, logo, link,"","Sin calificación","No disponible",hash_favoritos.containsKey(nombre),"",cont,0,""));
                            nombre="";
                            logo="";
                            link="";
                            cont+=1;
                        }
                    }
                }
                reader1.close();
                return null;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            } catch (Throwable throwable) {
                throwable.printStackTrace();

            }
             return null;
        }

         @Override
         protected void onProgressUpdate(String... values) {
             //adapter.add(new CanalOb(values[1], values[0], values[2]));
         }

         @RequiresApi(api = Build.VERSION_CODES.N)
         @Override
         protected void onPostExecute(Canales canales) {
             Gson gson = new Gson();
             Calendar calendar = Calendar.getInstance();
             int dayOfWeek = calendar.get(Calendar.DAY_OF_MONTH);
             String jsonList = gson.toJson(lista_canales);
             if (isVisible()) {
                 SharedPreferences.Editor editor = getActivity().getSharedPreferences("Usuarios",Context.MODE_PRIVATE).edit();
                 editor.putString("listaCanales",jsonList);
                 editor.putInt("diaCanales",dayOfWeek);
                 editor.putInt("ordenaCanales",0);
                 editor.commit();
                 progressBar.setVisibility(View.GONE);
             }
             setListas(lista_canales);
         }
     }

}







