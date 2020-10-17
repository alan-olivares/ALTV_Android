package com.example.alanolivares.altv;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Inicio extends AppCompatActivity {
    ProgressBar inicio;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);
        inicio=findViewById(R.id.progressBarInicio);
        inicio.setVisibility(View.VISIBLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        SharedPreferences preferences = getSharedPreferences("Usuarios",MODE_PRIVATE);
        String correo = preferences.getString("correo","No existe");
        if(!correo.equals("No existe")){
            if(isOnlineNet()){
                new JsonTask().execute("Edite con la dirección de la base de datos de usuarios");
            }else{
                new Handler().postDelayed(new Runnable(){
                    @Override
                    public void run() {
                        /* Create an Intent that will start the Menu-Activity. */
                        Intent mainIntent = new Intent(Inicio.this,MenuLateral.class);
                        Inicio.this.startActivity(mainIntent);
                        Inicio.this.finish();
                        inicio.setVisibility(View.INVISIBLE);
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
                    inicio.setVisibility(View.INVISIBLE);
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
                    System.out.println(jsonObject.getString("usuario"));
                    System.out.println(jsonObject.getString("password"));
                    if((jsonObject.getString("usuario").equals(correo)&&jsonObject.getString("password").equals(contra))){
                        SharedPreferences.Editor editor = getSharedPreferences("Usuarios",MODE_PRIVATE).edit();
                        editor.putString("correo",correo);
                        editor.putString("nombre",jsonObject.getString("name"));
                        editor.putString("contra",contra);
                        editor.commit();
                        nombre=jsonObject.getString("name");
                        System.out.println(jsonObject.getString("usuario"));
                        System.out.println(jsonObject.getString("password"));
                        System.out.println(correo);
                        System.out.println(contra);
                        a=true;
                        final String finalNombre = nombre;
                        new Handler().postDelayed(new Runnable(){
                            @Override
                            public void run() {
                                inicio.setVisibility(View.INVISIBLE);
                                /* Create an Intent that will start the Menu-Activity. */
                                Toast.makeText(getApplicationContext(), "Bienvenido de nuevo: "+ finalNombre, Toast.LENGTH_SHORT).show();
                                Intent mainIntent = new Intent(Inicio.this,MenuLateral.class);
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
