package com.example.alanolivares.altv;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.alanolivares.altv.Funciones.Funciones;
import com.example.alanolivares.altv.Funciones.TiempoOb;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class Inicio extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        SharedPreferences preferences = getSharedPreferences("Usuarios",MODE_PRIVATE);
        String correo = preferences.getString("correo","No existe");
        updateVersion(preferences,this);//Eliminar despues de la siguiente actualización
        if(!correo.equals("No existe")){
            if(isOnlineNet()){
                new JsonTask().execute("https://pastebin.com/raw/0e7W36ga");
            }else{
                new Handler().postDelayed(new Runnable(){
                    @Override
                    public void run() {
                        /* Create an Intent that will start the Menu-Activity. */
                        Intent mainIntent = new Intent(Inicio.this,BottomNavigation.class);
                        Inicio.this.startActivity(mainIntent);
                        Inicio.this.finish();
                    }
                }, 1000);
            }
        }else{
            new Handler().postDelayed(new Runnable(){
                @Override
                public void run() {
                    /* Create an Intent that will start the Menu-Activity. */
                    Intent mainIntent = new Intent(Inicio.this,LoginActivity.class);
                    Inicio.this.startActivity(mainIntent);
                    Inicio.this.finish();
                }
            }, 1000);
        }

    }
    public Boolean isOnlineNet() {

        ConnectivityManager connectivityManager = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo actNetInfo = connectivityManager.getActiveNetworkInfo();

        return (actNetInfo != null && actNetInfo.isConnected());
    }

    private void updateVersion(SharedPreferences preferences,Context context){//Veriones de los avances que tenia en cada elemento
        String listaV2 = preferences.getString("listaTiempo_v2","No existe");
        String lista = preferences.getString("listaTiempo","No existe");
        Gson gson = new Gson();
        if(listaV2.equals("No existe") && !lista.equals("No existe")){
            Funciones func=new Funciones(context);
            HashMap<String,TiempoOb> listaNueva = new HashMap<>();
            Type type = new TypeToken<ArrayList<TiempoOb>>(){}.getType();
            ArrayList<TiempoOb> listacaheTiempo = gson.fromJson(lista, type);
            for(int x=0;x<listacaheTiempo.size();x++){
                listaNueva.put(listacaheTiempo.get(x).getNombre(),listacaheTiempo.get(x));
            }
            func.saveHash(listaNueva,"listaTiempo_v2");
        }
    }


    private class JsonTask extends AsyncTask<String,String,String> {
        private ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection=null;
            BufferedReader reader = null;
            try{
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line ="";
                while ((line =reader.readLine())!= null){
                    buffer.append(line+"\n");
                    Log.e("Response",">"+line);
                }
                return buffer.toString();
            }catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if(connection != null){
                    connection.disconnect();
                }
                try{
                    if(reader!= null){
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try{
                SharedPreferences preferences = getSharedPreferences("Usuarios",MODE_PRIVATE);
                JSONArray jsonArray = new JSONArray(s);
                String correo,contra,nombre;
                correo=preferences.getString("correo","No existe");
                contra=preferences.getString("contra","No existe");
                boolean a=false;
                for (int i=0; i<jsonArray.length(); i++){
                    JSONObject jsonObject= jsonArray.getJSONObject(i);
                    if((jsonObject.getString("usuario").equals(correo)&&jsonObject.getString("password").equals(contra))){
                        SharedPreferences.Editor editor = getSharedPreferences("Usuarios",MODE_PRIVATE).edit();
                        editor.putString("correo",correo);
                        editor.putString("nombre",jsonObject.getString("name"));
                        editor.putString("contra",contra);
                        editor.putString("venc",jsonObject.getString("venc"));
                        editor.commit();
                        nombre=jsonObject.getString("name");
                        a=true;
                        final String finalNombre = nombre;
                        new Handler().postDelayed(new Runnable(){
                            @Override
                            public void run() {
                                /* Create an Intent that will start the Menu-Activity. */
                                Toast.makeText(getApplicationContext(), "Bienvenido de nuevo: "+ finalNombre, Toast.LENGTH_SHORT).show();
                                Intent mainIntent = new Intent(Inicio.this,BottomNavigation.class);
                                Inicio.this.startActivity(mainIntent);
                                Inicio.this.finish();
                            }
                        }, 1000);

                    }
                }
                if(a==false) {
                    SharedPreferences.Editor editor = getSharedPreferences("Usuarios", MODE_PRIVATE).edit();
                    editor.putString("correo", "No existe");
                    editor.putString("contra", "No existe");
                    editor.commit();
                    Toast.makeText(getApplicationContext(), "El usuario ha sido eliminado", Toast.LENGTH_SHORT).show();
                    Intent mainIntent = new Intent(Inicio.this,LoginActivity.class);
                    Inicio.this.startActivity(mainIntent);
                    Inicio.this.finish();
                }
                    /*listViewContactos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    }
                });*/
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
