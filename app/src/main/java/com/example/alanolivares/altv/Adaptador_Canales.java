package com.example.alanolivares.altv;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.alanolivares.altv.Funciones.CanalOb;
import com.example.alanolivares.altv.Funciones.Funciones;
import com.squareup.picasso.Picasso;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Adaptador_Canales extends ArrayAdapter<CanalOb> {
    private ArrayList<CanalOb> lista_canales;
    private Context context;
    private Funciones func;

    public Adaptador_Canales(Context context, ArrayList<CanalOb> lista){
        super(context,R.layout.vista_celda,lista);
        this.lista_canales = lista;
        this.context = context;
        func=new Funciones(context);
    }

    public void setLista(ArrayList<CanalOb> lista_canales){
        this.lista_canales=lista_canales;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        if (convertView == null){
            LayoutInflater vi = LayoutInflater.from(context);
            convertView =vi.inflate(R.layout.vista_cuadrada,null);
        }

        final CanalOb contacto=lista_canales.get(position);
        if(contacto!=null){
            String cal=contacto.getCalificacion();
            if(cal.equals("Sin calificación")){
                cal="";
            }
            TextView txtNombre = (TextView) convertView.findViewById(R.id.descripcion);
            ImageButton imgFoto =(ImageButton) convertView.findViewById(R.id.imagen);
            TextView numero = (TextView) convertView.findViewById(R.id.numero);
            TextView aviso = (TextView) convertView.findViewById(R.id.aviso);
            func.aviso(aviso,contacto);
            final ToggleButton toggleButton=convertView.findViewById(R.id.favorito);
            txtNombre.setText(contacto.getNombre()+" "+cal.replace("Calificación ",""));
            //txtCal.setText(contacto.calificacion);
            numero.setText(String.valueOf(contacto.getNumero()));
            if(contacto.getImagen().equals("")){
                imgFoto.setImageResource(R.mipmap.altvlog);
                if(!contacto.getLink().contains("googleusercontent")){
                    numero.setText(String.valueOf(position+1));
                }
            }else {
                if(contacto.getLink().contains("googleusercontent")){
                    imgFoto.setScaleType(ImageView.ScaleType.FIT_XY);
                }else{
                    imgFoto.setScaleType(ImageView.ScaleType.FIT_CENTER);
                }
                Picasso.get()
                        .load(contacto.getImagen()).error(R.mipmap.altvlog)
                        .into(imgFoto);
            }

            final View finalConvertView = convertView;
            imgFoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((GridView) parent).performItemClick(finalConvertView, position, 0);
                }
            });
            toggleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((GridView) parent).performItemClick(finalConvertView, position, 1);
                }
            });
            try{
                toggleButton.setBackgroundResource(contacto.getFavo()?R.drawable.ic_favorite_black_24dp:R.drawable.ic_favorite_border_black_24dp);
            } catch (Exception e) {
                e.printStackTrace();
            }




        }
        return  convertView;
    }



}

