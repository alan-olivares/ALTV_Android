package com.example.alanolivares.altv;

import android.app.AlertDialog;
import android.app.DownloadManager;
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
import android.os.Environment;
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
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.core.view.MenuItemCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.alanolivares.altv.Funciones.CanalOb;
import com.example.alanolivares.altv.Funciones.ClaseFragmentPadre;
import com.example.alanolivares.altv.Funciones.Guia;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;


/**

 */
public class Peliculas extends ClaseFragmentPadre {
    private final String EXT_INF = "#EXTINF";
    private final String EXT_LOGO = "tvg-logo";
    private final String peliculasURL="https://drive.google.com/uc?id=0B4SOhnO9iLFReTZqOGdTbU56bW8&export=download";
    private GridView listViewCanales;
    private LinearLayout progressBar;
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view;
        view= inflater.inflate(R.layout.fragment_peliculas,container,false);
        setTitulo("Películas");
        listViewCanales = (GridView)view.findViewById(R.id.listViewPeliculas);
        progressBar = view.findViewById(R.id.progressBarPeli);
        SwipeRefreshLayout mSwipe=view.findViewById(R.id.swipe_refresh_layout);
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
            datos.put("dia","diaPeliculas");
            datos.put("version","NuevaVersionPeliculas");
            datos.put("lista","listaPeliculas");
            datos.put("ordena","ordenaPeliculas");
        } catch (JSONException e) {
            mensaje(e.getMessage());
        }
        return datos;
    }
    @Override
    public void obtenerDatos() {
        new MyTask2().execute();
    }

    @Override
    public void pasarIntent(CanalOb objeto,View view) {
        Intent pas = new Intent(getContext(), Descripcion.class);
        pas.putExtra("objeto",objeto);
        View imageView = view.findViewById(R.id.imagen);
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity()
                        , Pair.create(imageView, "transition-image")
                );
        ActivityCompat.startActivity(getContext(), pas, options.toBundle());
    }

    @Override
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
        ArrayAdapter<String> adapter= new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_single_choice,listaopc);

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



    private class MyTask2 extends AsyncTask<Void, String, Canales> {
        private ArrayList<CanalOb> lista_canales,lista_favoritos,lista_fecha;
        private HashMap<String,CanalOb> hash_canales,hash_favoritos,hash_fecha;
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected Canales doInBackground(Void... urls) {
            try {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                Date date = new Date();
                lista_favoritos=listaObjeto("listaFavoritos");
                lista_fecha=listaObjeto("listaPeliculas");
                hash_favoritos=listToHash(lista_favoritos);
                hash_fecha=listToHash(lista_fecha);
                String nombre="",link="",logo="";
                int cont=1;
                Boolean favo=false;
                URL url3 = new URL(peliculasURL);
                BufferedReader reader1 = new BufferedReader(new InputStreamReader(url3.openStream()));
                String currLine;
                hash_canales=new HashMap<>();
                String guia="";
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
                            }
                            nombre=dataArray[dataArray.length-1].replace(",", "");
                        }
                        if (currLine.startsWith("http")) {
                            link = currLine;
                            favo=hash_favoritos.containsKey(nombre);
                            hash_canales.put(nombre,new CanalOb(nombre, logo, link,"","Sin calificación","No disponible",favo,formatter.format(date),cont,0,""));
                            cont+=1;
                            nombre=logo=link="";
                        }

                    }
                }
                reader1.close();
                hash_canales.forEach((s, canalOb) -> {
                    if(hash_fecha.containsKey(s)){
                        hash_canales.get(s).setFecha(hash_fecha.get(s).getFecha());
                    }
                });
                String titulo="",calificacion="",descripcion="",currLine2="";
                URL url4 = new URL(guia);
                BufferedReader reader2 = new BufferedReader(new InputStreamReader(url4.openStream()));
                int con=0;
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
                        if(con%2==0) {
                            CanalOb canalOb= hash_canales.get(titulo);
                            if(canalOb!=null){
                                canalOb.setCalificacion(calificacion+"/10");
                                canalOb.setDescripcion(descripcion);
                                hash_canales.replace(titulo,canalOb);
                                titulo=calificacion=descripcion="";
                            }

                        }
                        con++;
                    }

                }

                reader2.close();

            } catch (Exception e) {
                e.printStackTrace();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }

            return null;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected void onPostExecute(Canales canales) {
            if (isVisible()) {
                progressBar.setVisibility(View.GONE);
                Gson gson = new Gson();
                Calendar calendar = Calendar.getInstance();
                int dayOfWeek = calendar.get(Calendar.DAY_OF_MONTH);
                lista_canales=hashToList(hash_canales);
                lista_canales.sort(new Comparator<CanalOb>() {
                    @Override
                    public int compare(CanalOb canalOb, CanalOb t1) {
                        return canalOb.getNumero()-t1.getNumero();
                    }
                });
                String jsonList = gson.toJson(lista_canales);
                SharedPreferences.Editor editor = getActivity().getSharedPreferences("Usuarios",Context.MODE_PRIVATE).edit();
                editor.putString("listaPeliculas",jsonList);
                editor.putInt("diaPeliculas",dayOfWeek);
                editor.putInt("ordenaPeliculas",-1);
                editor.commit();
            }
            setListas(lista_canales);
        }
    }
}
