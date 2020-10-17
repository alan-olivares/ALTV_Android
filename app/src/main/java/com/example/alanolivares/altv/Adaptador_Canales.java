package com.example.alanolivares.altv;

import android.app.AppComponentFactory;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.widget.TextViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Adaptador_Canales extends ArrayAdapter<CanalOb> {
    private ArrayList<CanalOb> lista_canales;
    private ArrayList<CanalOb> lista_fav,ListacaheFav;
    private Context context;

    public Adaptador_Canales(Context context, ArrayList<CanalOb> lista){
        super(context,R.layout.vista_celda,lista);
        this.lista_canales = lista;
        this.context = context;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        Date date = new Date();
        if (convertView == null){
            LayoutInflater vi = LayoutInflater.from(context);
            convertView =vi.inflate(R.layout.vista_cuadrada,null);
        }

        final CanalOb contacto=lista_canales.get(position);
        if(contacto!=null){
            String cal=contacto.calificacion;
            if(cal.equals("Sin calificación")){
                cal="";
            }
            SharedPreferences preferences = getContext().getSharedPreferences("Usuarios",Context.MODE_PRIVATE);
            String registro = preferences.getString("register","No existe");
            TextView txtNombre = (TextView) convertView.findViewById(R.id.descripcion);
            ImageButton imgFoto =(ImageButton) convertView.findViewById(R.id.imagen);
            TextView numero = (TextView) convertView.findViewById(R.id.numero);
            TextView aviso = (TextView) convertView.findViewById(R.id.aviso);
            final ToggleButton toggleButton=convertView.findViewById(R.id.favorito);
            txtNombre.setText(contacto.getNombre()+" "+cal.replace("Calificación ",""));
            //txtCal.setText(contacto.calificacion);
            numero.setText(String.valueOf(contacto.numero));
            if(contacto.getImagen().equals("")){
                imgFoto.setImageResource(R.mipmap.altvlog);
                if(!contacto.link.contains("googleusercontent")){
                    numero.setText(String.valueOf(position+1));
                }
            }else {
                if(contacto.link.contains("googleusercontent")){
                    imgFoto.setScaleType(ImageView.ScaleType.FIT_XY);
                }else{
                    imgFoto.setScaleType(ImageView.ScaleType.FIT_CENTER);
                }
                Picasso.get()
                        .load(contacto.getImagen()).error(R.mipmap.altvlog)
                        .into(imgFoto);
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            Date fechaInicial= null;
            Date fechaFinal=null;
            Date fechaFinalCap=null;
            Date fechaactual=null;
            long dias=0;
            long dias2=0;
            long dias3=0;
            long dias4=0;
            try {
                fechaInicial = dateFormat.parse(registro);
                if(contacto.fecha!=null&&!contacto.fecha.equals("")) {
                    fechaFinal = dateFormat.parse(contacto.fecha);
                }else{
                    fechaFinal = dateFormat.parse(registro);
                }
                if(contacto.fechaCap!=null&&!contacto.fechaCap.equals("")) {
                    fechaFinalCap = dateFormat.parse(contacto.fechaCap);
                }else{
                    fechaFinalCap = dateFormat.parse(registro);
                }
                fechaactual=dateFormat.parse(dateFormat.format(date));
                dias=(long) ((fechaFinal.getTime()-fechaInicial.getTime())/86400000);
                dias2=(long) ((fechaactual.getTime()-fechaFinal.getTime())/86400000);
                dias3=(long) ((fechaFinalCap.getTime()-fechaInicial.getTime())/86400000);
                dias4=(long) ((fechaactual.getTime()-fechaFinalCap.getTime())/86400000);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            System.out.println(fechaFinalCap);
            System.out.println("Hay "+dias+" dias de diferencia en dias3");
            System.out.println("Hay "+dias2+" dias de diferencia dias4");
            String avisar="";
            if(dias!=0&&dias2<4){
                avisar="¡Nueva!";
            }else if(dias3!=0&&dias4<4){
                avisar="¡Nuevos capitulos!";
            }
            aviso.setText(avisar);
            //System.out.println(contacto.getNombre());
            //imgFoto.setImageResource(contacto.getImagen());

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
                if(contacto.favo&&contacto.favo!=null){
                    toggleButton.setBackgroundResource(R.drawable.ic_favorite_black_24dp);
                }else{
                    toggleButton.setBackgroundResource(R.drawable.ic_favorite_border_black_24dp);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }




        }
        return  convertView;
    }



}

