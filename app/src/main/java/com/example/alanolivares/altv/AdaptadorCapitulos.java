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

import com.example.alanolivares.altv.Funciones.CanalOb;
import com.example.alanolivares.altv.Funciones.Funciones;
import com.example.alanolivares.altv.Funciones.TiempoOb;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

public class AdaptadorCapitulos extends ArrayAdapter<CanalOb> {
    private ArrayList<CanalOb> lista_canales;
    private Context context;
    private Funciones fun;
    private HashMap<String,TiempoOb> listacaheTiempo;
    public AdaptadorCapitulos(Context context, ArrayList<CanalOb> lista){
        super(context,R.layout.vista_celda,lista);
        this.lista_canales = lista;
        this.context = context;
        fun=new Funciones(context);
        listacaheTiempo=fun.getTiempoSaved("listaTiempo_v2");
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        if (convertView == null){
            LayoutInflater vi = LayoutInflater.from(context);
            convertView =vi.inflate(R.layout.vista_celda,null);
        }
        CanalOb contacto=lista_canales.get(position);
        if(contacto!=null){
            TextView txtNombre = (TextView) convertView.findViewById(R.id.canal);
            if(contacto.getCapitulo()!=""){
                txtNombre.setText(contacto.getCapitulo());
            }else{
                txtNombre.setText(contacto.getNombre());
            }
            final View finalConvertView = convertView;
            ImageButton play = convertView.findViewById(R.id.verserie);
            ProgressBar progressBar = (ProgressBar) convertView.findViewById(R.id.progressBarSer);
            TiempoOb tiempoOb=listacaheTiempo.get(contacto.getNombre()+"-"+contacto.getCapitulo());
            fun.progress(tiempoOb,progressBar);
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

