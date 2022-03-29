package com.example.alanolivares.altv;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.alanolivares.altv.Funciones.CanalOb;
import com.example.alanolivares.altv.Funciones.ClaseFragmentPadre;
import com.example.alanolivares.altv.Funciones.Guia;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.net.URL;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

public class Series extends ClaseFragmentPadre {
    private final String EXT_INF = "#EXTINF";
    private final String EXT_LOGO = "tvg-logo";
    private final String EXT_GROUP = "group-title";
    private final String seriesURL="https://drive.google.com/uc?id=0B4SOhnO9iLFRT0toM1g3c3VEYVE&export=download";
    private LinearLayout progressBar;
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view;
        view= inflater.inflate(R.layout.fragment_series,container,false);
        setTitulo("Series");
        GridView listViewCanales = (GridView)view.findViewById(R.id.listViewSeries);
        progressBar = view.findViewById(R.id.progressBarSer);
        SwipeRefreshLayout mSwipe=view.findViewById(R.id.swipe_Series);
        try {
            mainDatos(listViewCanales,mSwipe,datos());
        } catch (Exception e) {
            mensaje("Problema al cargar los datos, error: "+e.getMessage());
        }
        return view;
    }
    private JSONObject datos(){
        JSONObject datos=new JSONObject();
        try {
            datos.put("dia","diaSeries");
            datos.put("version","NuevaVeS");
            datos.put("lista","listaSeries");
            datos.put("ordena","ordenaSeries");
        } catch (JSONException e) {
            mensaje(e.getMessage());
        }
        return datos;
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
        listaopc.add("Nombre");
        listaopc.add("Calificación");
        SharedPreferences preferences = getActivity().getSharedPreferences("Usuarios",Context.MODE_PRIVATE);
        int checkItem = preferences.getInt("ordenaSeries",-1);
        if(checkItem==-1){
            checkItem=0;
        }
        adapter= new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_single_choice,listaopc);

        builderSingle.setSingleChoiceItems(adapter, checkItem, new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    select(which+1);
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
        ArrayList<CanalOb> lista_capitulos2= new ArrayList<>();
        ArrayList<CanalOb> lista_capitulos= new ArrayList<>();
        lista_capitulos=listaObjeto("listaCapitulos");
        for(int x=0;x<lista_capitulos.size();x++){
            if(lista_capitulos.get(x).getNombre().equals(objeto.getNombre())){
                lista_capitulos2.add(lista_capitulos.get(x));
            }
        }
        Intent pas =new Intent(getContext(),Capitulos.class);
        pas.putParcelableArrayListExtra("capitulos", lista_capitulos2);
        pas.putExtra("objeto",objeto);
        View imageView = view.findViewById(R.id.imagen);
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity()
                        , Pair.create(imageView, "transition-image")
                );
        ActivityCompat.startActivity(getContext(), pas, options.toBundle());
    }


    private class MyTask extends AsyncTask<Void, String, Canales> {
        private ArrayList<CanalOb> lista_capitulos,lista_series,lista_favoritos,lista_fecha;
        private HashMap<String,CanalOb> hash_favoritos,hash_fecha,hash_series;
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }
        private int countCapitulos(String nombre){
            int a=0;
            for (CanalOb can:lista_capitulos) {
                if(can.getNombre().equals(nombre))
                    a++;
            }
            return a;
        }
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected Canales doInBackground(Void... urls) {
            try {
                lista_favoritos=listaObjeto("listaFavoritos");
                lista_fecha=listaObjeto("listaSeries");
                hash_favoritos=listToHash(lista_favoritos);
                hash_fecha=listToHash(lista_fecha);
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                Date date = new Date();
                String da[]= new String[]{"","",""};
                String nombre="",link="",logo="",cap="",guia="";
                URL url3 = new URL(seriesURL);//here edit your source with your own m3u
                BufferedReader reader1 = new BufferedReader(new InputStreamReader(url3.openStream()));
                String currLine;
                hash_series=new HashMap<>();
                lista_capitulos=new ArrayList<>();
                lista_series=new ArrayList<>();
                int series=1;
                while ((currLine = reader1.readLine()) != null) {
                    if(currLine.length()>5){
                        if(currLine.contains("url-tvg=")){
                            guia=currLine.substring(currLine.indexOf("\"")+1,currLine.lastIndexOf("\""));
                        }
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
                        if (currLine.startsWith("http")) {
                            link = currLine;
                            CanalOb canal=new CanalOb(nombre, logo, link,cap,"","",false,formatter.format(date),series,1,formatter.format(date));
                            lista_capitulos.add(canal);
                            CanalOb aux=hash_series.put(nombre,canal);
                            if(aux==null)
                                series++;
                            else{
                                aux.setNumeroCap(aux.getNumeroCap()+1);
                                hash_series.replace(aux.getNombre(),aux);
                            }
                            nombre="";
                            logo="";
                            link="";
                        }

                    }
                }
                reader1.close();
                //Fecha de agregado
                hash_series.forEach((s, canalOb) -> {
                    int totalCap=countCapitulos(s);
                    if(hash_fecha.containsKey(s)){//Ya estaba agregada
                        canalOb.setFecha(hash_fecha.get(s).getFecha());
                        if(totalCap==hash_fecha.get(s).getNumeroCap()){//Si tiene el mismo número de capitulos, se mantiene como estaba
                            canalOb.setFechaCap(hash_fecha.get(s).getFechaCap());
                        }
                    }
                    canalOb.setNumeroCap(totalCap);
                    canalOb.setFavo(hash_favoritos.containsKey(s));//Es favorito?
                });
                int con=0;
                String titulo="",calificacion="",descripcion="",currLine2="";
                URL url4 = new URL(guia);
                BufferedReader reader2 = new BufferedReader(new InputStreamReader(url4.openStream()));
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
                        CanalOb canalOb= hash_series.get(titulo);
                        if(canalOb!=null){
                            canalOb.setDescripcion(descripcion);
                            canalOb.setCalificacion(calificacion+"/10");
                            hash_series.replace(titulo,canalOb);
                        }
                        titulo="";
                        calificacion="";
                        descripcion="";
                        con++;
                    }

                }
                reader2.close();
                return null;

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

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected void onPostExecute(Canales canales) {
            Gson gson = new Gson();
            Calendar calendar = Calendar.getInstance();
            int dayOfWeek = calendar.get(Calendar.DAY_OF_MONTH);
            String jsonList = gson.toJson(lista_capitulos);
            lista_series=hashToList(hash_series);
            lista_series.sort(new Comparator<CanalOb>() {
                @Override
                public int compare(CanalOb canalOb, CanalOb t1) {
                    return canalOb.getNumero()-t1.getNumero();
                }
            });
            if (isVisible()) {
                String jsonList2 = gson.toJson(lista_series);
                SharedPreferences.Editor editor = getActivity().getSharedPreferences("Usuarios",Context.MODE_PRIVATE).edit();
                editor.putString("listaCapitulos",jsonList);
                editor.putString("listaSeries",jsonList2);
                editor.putInt("diaSeries",dayOfWeek);
                editor.putInt("ordenaSeries",0);
                editor.commit();
                progressBar.setVisibility(View.GONE);
            }
            setListas(lista_series);
        }
    }

}


