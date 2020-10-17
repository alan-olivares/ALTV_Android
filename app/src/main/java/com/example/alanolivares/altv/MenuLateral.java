package com.example.alanolivares.altv;
import android.app.DownloadManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MenuLateral extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    Canales canales = new Canales();
    private final String EXT_INF = "#EXTINF";
    private final String EXT_LOGO = "tvg-logo";
    Peliculas peliculas = new Peliculas();
    Series series = new Series();
    Descargas descargas=new Descargas();
    Favoritos favorito=new Favoritos();
    ArrayList<CanalOb> lista_canales,lista_favoritos,lista_fecha;
    ProgressDialog progressDialog;
    DownloadManager downloadManager;
    FragmentManager fragmentManager=getSupportFragmentManager();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_lateral);

        SharedPreferences preferences = getSharedPreferences("Usuarios",Context.MODE_PRIVATE);
        String registro = preferences.getString("register","No existe");
        String nombre = preferences.getString("nombre","No existe");
        if(registro.equals("No existe")){
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date date = new Date();
            SharedPreferences.Editor editor = getSharedPreferences("Usuarios",Context.MODE_PRIVATE).edit();
            editor.putString("register",formatter.format(date));
            editor.commit();
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View hView = navigationView.getHeaderView(0);
        float ver=0;
        String version1="";
        try {
            PackageInfo pInfo = MenuLateral.this.getPackageManager().getPackageInfo(getPackageName(), 0);
            version1 = pInfo.versionName;
            ver = Float.parseFloat(version1);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        TextView nombres = (TextView) hView.findViewById(R.id.nombreIni);
        TextView version = (TextView) hView.findViewById(R.id.version);
        nombres.setText("Hola: "+nombre);
        version.setText("Versión: "+ver);
        navigationView.setNavigationItemSelectedListener(this);
        if(savedInstanceState==null){
            FragmentManager fM = getSupportFragmentManager();
            fM.beginTransaction().replace(R.id.contenedor,canales).commit();
        }
    }
    public Boolean isOnlineNet() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo actNetInfo = connectivityManager.getActiveNetworkInfo();

        return (actNetInfo != null && actNetInfo.isConnected());
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
                PackageInfo pInfo = MenuLateral.this.getPackageManager().getPackageInfo(getPackageName(), 0);
                version = pInfo.versionName;
                ver = Float.parseFloat(version);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            try {
                URL url3 = new URL("Edite con la dirección donde se pueda verificar si la versión esta actualizada");
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
        }

        @Override
        protected void onPostExecute(String canales) {
            if(comp){
                final String link2=link;
                final String ver2=version;
                AlertDialog.Builder alert=new AlertDialog.Builder(MenuLateral.this);
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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (back_pressed + 1000 > System.currentTimeMillis()){
            finishAffinity();
        }
        else{
            Toast.makeText(getBaseContext(),"Presiona de nuevo para salir", Toast.LENGTH_SHORT).show();
        }
        back_pressed = System.currentTimeMillis();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.app_bar_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onResume() {
        super.onResume();
        if(isOnlineNet()){
            downloadManager=(DownloadManager)MenuLateral.this.getSystemService(MenuLateral.this.DOWNLOAD_SERVICE);
            Uri uri =Uri.parse("https://www.google.com.mx");
            DownloadManager.Request request =new DownloadManager.Request(uri);
            request.setDestinationInExternalFilesDir(MenuLateral.this, Environment.DIRECTORY_MOVIES,"");
            new MyTask().execute();
        }

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        SharedPreferences.Editor editor = getSharedPreferences("Usuarios",Context.MODE_PRIVATE).edit();
        if (id == R.id.nav_camera) {
            editor.putInt("ordenaCanales",0);
            editor.commit();
            fragmentManager.beginTransaction().replace(R.id.contenedor,canales ).commit();
            item.setCheckable(true);
        } else if (id == R.id.nav_gallery) {
            editor.putInt("ordenaPeliculas",0);
            editor.commit();
            fragmentManager.beginTransaction().replace(R.id.contenedor,peliculas ).commit();
            item.setCheckable(true);
        } else if (id == R.id.nav_slideshow) {
            editor.putInt("ordenaSeries",0);
            editor.commit();
            fragmentManager.beginTransaction().replace(R.id.contenedor,series ).commit();
            item.setCheckable(true);
        }else if (id == R.id.favoritos) {
            fragmentManager.beginTransaction().replace(R.id.contenedor,favorito ).commit();
            item.setCheckable(true);
        } else if (id == R.id.nav_manage) {
            fragmentManager.beginTransaction().replace(R.id.contenedor,descargas ).commit();
            item.setCheckable(true);
        } else if (id == R.id.nav_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT,"Descarga ALTV para entretenimiento 24/7 http://hyperurl.co/ALTVAPP");
            sendIntent.setType("text/plain");
            startActivity(sendIntent);

        } else if (id == R.id.nav_send) {
            String[] to = { "your email address"};
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setData(Uri.parse("mailto:"));
            emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
            emailIntent.putExtra(Intent.EXTRA_SUBJECT,"");
            emailIntent.setType("message/rfc822");
            startActivity(Intent.createChooser(emailIntent, "Email "));
        }else if (id == R.id.cerrar) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setMessage("¿Quieres cerrar sesión?");
            alert.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SharedPreferences.Editor editor = getSharedPreferences("Usuarios", Context.MODE_PRIVATE).edit();
                    editor.putString("correo", "No existe");
                    editor.putString("contra", "No existe");
                    editor.commit();
                    Intent pass = new Intent(MenuLateral.this, LoginActivity.class);
                    startActivity(pass);
                    MenuLateral.this.finish();
                }
            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            AlertDialog aler = alert.create();
            aler.show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}
