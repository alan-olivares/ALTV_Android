package com.example.alanolivares.altv;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class AdaptadorCapitulos extends ArrayAdapter<CanalOb> {
    private ArrayList<CanalOb> lista_canales;
    private Context context;

    public AdaptadorCapitulos(Context context, ArrayList<CanalOb> lista){
        super(context,R.layout.vista_celda,lista);
        this.lista_canales = lista;
        this.context = context;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        if (convertView == null){
            LayoutInflater vi = LayoutInflater.from(context);
            convertView =vi.inflate(R.layout.vista_celda,null);
        }
        SharedPreferences preferences = getContext().getSharedPreferences("Usuarios",Context.MODE_PRIVATE);
        Gson gson = new Gson();
        CanalOb contacto=lista_canales.get(position);
        if(contacto!=null){
            TextView txtNombre = (TextView) convertView.findViewById(R.id.canal);
            if(contacto.capitulo!=""){
                txtNombre.setText(contacto.capitulo);
            }else{
                txtNombre.setText(contacto.getNombre());
            }
            String savedList = preferences.getString("listaTiempo","No existe");
            Type type = new TypeToken<ArrayList<TiempoOb>>(){}.getType();
            int tiempo=0,tiempoFinal=0,tiempo2=0;
            if(!savedList.equals("No existe")){
                ArrayList<TiempoOb> listacaheTiempo = gson.fromJson(savedList, type);
                for(int x=0;x<listacaheTiempo.size();x++){
                    if(listacaheTiempo.get(x).getNombre().equals(lista_canales.get(position).getNombre()+"-"+contacto.capitulo)){
                        tiempo=listacaheTiempo.get(x).getTiempo();
                        tiempoFinal=listacaheTiempo.get(x).tiempoFinal;
                        //System.out.println(tiempo);
                        //System.out.println(tiempoFinal);
                    }
                }
            }
            if(tiempoFinal!=0){
                tiempoFinal=tiempoFinal/100;
                tiempo2=tiempo/tiempoFinal;
            }
            final View finalConvertView = convertView;
            ImageButton play = convertView.findViewById(R.id.verserie);
            ProgressBar progressBar = (ProgressBar) convertView.findViewById(R.id.progressBarSer);
            progressBar.setMax(100);
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(tiempo2);

            play.setTag(position);
            play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ListView) parent).performItemClick(finalConvertView, position, 0);
                }
            });
            ImageButton descargar = convertView.findViewById(R.id.descaserie);
            descargar.setTag(position);
            descargar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ListView) parent).performItemClick(finalConvertView, position, 1);
                }
            });



        }
        return  convertView;
    }


}

