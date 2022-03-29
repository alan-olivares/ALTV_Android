package com.example.alanolivares.altv;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.example.alanolivares.altv.Funciones.Funciones;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class BottomNavigation extends AppCompatActivity {

    Canales canales = new Canales();
    Peliculas peliculas = new Peliculas();
    Series series = new Series();
    Descargas descargas=new Descargas();
    Favoritos favorito=new Favoritos();
    DownloadManager downloadManager;
    FragmentManager fragmentManager=getSupportFragmentManager();
    String version1;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            SharedPreferences.Editor editor = getSharedPreferences("Usuarios",Context.MODE_PRIVATE).edit();
            switch (item.getItemId()) {
                case R.id.canales:
                    editor.putInt("ordenaCanales",0);
                    editor.commit();
                    fragmentManager.beginTransaction().replace(R.id.container,canales ).commit();
                    return true;
                case R.id.peliculas:
                    editor.putInt("ordenaPeliculas",0);
                    editor.commit();
                    fragmentManager.beginTransaction().replace(R.id.container,peliculas ).commit();
                    return true;
                case R.id.series:
                    editor.putInt("ordenaSeries",0);
                    editor.commit();
                    fragmentManager.beginTransaction().replace(R.id.container,series ).commit();
                    return true;
                case R.id.favoritos2:
                    fragmentManager.beginTransaction().replace(R.id.container,favorito ).commit();
                    return true;
                case R.id.descargas:
                    fragmentManager.beginTransaction().replace(R.id.container,descargas ).commit();
                    return true;
            }
            return false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.opciones,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){

            case R.id.compartir:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT,"Descarga ALTV para entretenimiento 24/7 https://1sgq.short.gy/ALTVAPP");
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                break;

            case R.id.contactar:
                new Funciones(this).openWhatsApp("Hola Alan, tengo problemas con: ");
                break;
            case R.id.informacion:
                SharedPreferences preferences = getSharedPreferences("Usuarios", Context.MODE_PRIVATE);
                String correo = preferences.getString("correo","No existe");
                String nombre = preferences.getString("nombre","No existe");
                String venc = preferences.getString("venc","No existe");
                showDialog(BottomNavigation.this, nombre,correo,version1,venc);
                break;
            case R.id.cerrar:
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setMessage("¿Quieres cerrar sesión?");
                alert.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor = getSharedPreferences("Usuarios", Context.MODE_PRIVATE).edit();
                        editor.putString("correo", "No existe");
                        editor.putString("contra", "No existe");
                        editor.commit();
                        Intent pass = new Intent(BottomNavigation.this, LoginActivity.class);
                        startActivity(pass);
                        BottomNavigation.this.finish();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog aler = alert.create();
                aler.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showDialog(Activity activity, String nom, String correo, String ver, String venc){
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.information_dialog);
        TextView nombre,email,version,vencimiento;
        nombre=(TextView)dialog.findViewById(R.id.nombre);
        email=(TextView)dialog.findViewById(R.id.emailShow);
        version=(TextView)dialog.findViewById(R.id.version);
        vencimiento=(TextView)dialog.findViewById(R.id.vencimiento);
        nombre.setText(nom);
        email.setText(correo);
        version.setText(ver);
        vencimiento.setText(venc);

        Button dialogButton = (Button) dialog.findViewById(R.id.btn_no);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(android.view.Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().requestFeature(android.view.Window.FEATURE_ACTIVITY_TRANSITIONS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navigation);
        final BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        SharedPreferences preferences = getSharedPreferences("Usuarios", Context.MODE_PRIVATE);
        String registro = preferences.getString("register","No existe");
        String nombre = preferences.getString("nombre","No existe");

        fragmentManager.beginTransaction().replace(R.id.container,canales ).commit();
        navView.post(new Runnable() {
            @Override
            public void run() {
                navView.setSelectedItemId(R.id.canales);
            }
        });
        if(registro.equals("No existe")){
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date date = new Date();
            SharedPreferences.Editor editor = getSharedPreferences("Usuarios",Context.MODE_PRIVATE).edit();
            editor.putString("register",formatter.format(date));
            editor.commit();
        }
        float ver=0;
        version1="";
        try {
            PackageInfo pInfo = BottomNavigation.this.getPackageManager().getPackageInfo(getPackageName(), 0);
            version1 = pInfo.versionName;
            ver = Float.parseFloat(version1);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Boolean isOnlineNet() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo actNetInfo = connectivityManager.getActiveNetworkInfo();

        return (actNetInfo != null && actNetInfo.isConnected());
    }
    @Override
    public void onResume() {
        super.onResume();
        System.out.println("on resume Menulateral");
        if(isOnlineNet()){
            downloadManager=(DownloadManager)BottomNavigation.this.getSystemService(BottomNavigation.this.DOWNLOAD_SERVICE);
            Uri uri =Uri.parse("https://www.google.com.mx");
            DownloadManager.Request request =new DownloadManager.Request(uri);
            request.setDestinationInExternalFilesDir(BottomNavigation.this, Environment.DIRECTORY_MOVIES,"");
            new BottomNavigation.MyTask().execute();
        }
        //new MyTask2().execute();

    }

    private class MyTask extends AsyncTask<Void, String, String> {
        String version="";
        String mejoras="";
        Boolean comp=false;
        String link="";
        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(Void... urls) {

            float ver=0;
            try {
                PackageInfo pInfo = BottomNavigation.this.getPackageManager().getPackageInfo(getPackageName(), 0);
                version = pInfo.versionName;
                ver = Float.parseFloat(version);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            //progressDialog=ProgressDialog.show(con,"","Iniciando sesion..",true);
            try {
                URL url3 = new URL("https://pastebin.com/raw/fW270XEh");
                BufferedReader reader1 = new BufferedReader(new InputStreamReader(url3.openStream()));
                String currLine;
                while ((currLine = reader1.readLine()) != null) {
                    String[] dataArray = currLine.split("\"");
                    if(dataArray[0].contains("Mejoras:")){
                        mejoras=dataArray[1];
                    }
                    if(dataArray[0].contains("Version:")){
                        float ver2 = Float.parseFloat(dataArray[1]);
                        if (ver2>ver){
                            comp=true;
                        }
                    }
                    if(dataArray[0].contains("Link:")){
                        link=dataArray[1];
                    }
                }
                System.out.println(mejoras);
                System.out.println(version);
                System.out.println(link);
                //adaptador = new Adaptador_Canales(getContext(),lista_canales);
                //listViewCanales.setAdapter(adaptador);
                reader1.close();
                return currLine;

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
        protected void onPostExecute(String canales) {
            if(comp){
                final String link2=link;
                final String ver2=version;
                AlertDialog.Builder alert=new AlertDialog.Builder(BottomNavigation.this);
                alert.setTitle("Actualizacion disponible");
                alert.setMessage(mejoras)
                        .setPositiveButton("Descargar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse(link2));
                                startActivity(launchBrowser);
                            }
                        });
                AlertDialog aler=alert.create();
                aler.show();
                System.out.println(link2);

            }
        }
    }
    long back_pressed;
    @Override
    public void onBackPressed() {
        if (back_pressed + 2000 > System.currentTimeMillis()){
            finishAffinity();
        }
        else{
            Toast.makeText(getBaseContext(),"Presiona de nuevo para salir", Toast.LENGTH_SHORT).show();
        }
        back_pressed = System.currentTimeMillis();

    }

}
