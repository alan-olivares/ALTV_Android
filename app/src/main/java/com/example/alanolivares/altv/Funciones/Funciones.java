package com.example.alanolivares.altv.Funciones;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alanolivares.altv.Capitulos;
import com.example.alanolivares.altv.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Funciones <T>{
    private static Context context;
    /**
     * Constructor de la clase que inicializa el Context
     * @param context - Contexto de la vista en la que es llamada la clase
     */
    public Funciones(Context context){
        this.context=context;
    }

    public void openWhatsApp(String mensaje){

        try{
            PackageManager packageManager = context.getPackageManager();
            Intent i = new Intent(Intent.ACTION_VIEW);
            String url = "https://api.whatsapp.com/send?phone=523310954449&text=" + URLEncoder.encode(mensaje, "UTF-8");
            i.setPackage("com.whatsapp");
            i.setData(Uri.parse(url));
            if (i.resolveActivity(packageManager) != null) {
                context.startActivity(i);
            }else {
                Toast.makeText(context, "Error al abrir la aplicación de WhatsApp", Toast.LENGTH_SHORT).show();
            }
        } catch(Exception e) {
            Toast.makeText(context, "No tienes WhatsApp instalado", Toast.LENGTH_SHORT).show();
        }
    }
    public void aviso(TextView textView,CanalOb canalOb){
        String registro = getSaved("register");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        Date fechaInicial= null,fechaFinal=null,fechaFinalCap=null,fechaactual=null;
        long dias=0,dias2=0,dias3=0,dias4=0;
        try {
            fechaInicial = dateFormat.parse(registro);
            if(canalOb.getFecha()!=null&&!canalOb.getFecha().equals("")) {
                fechaFinal = dateFormat.parse(canalOb.getFecha());
            }else{
                fechaFinal = dateFormat.parse(registro);
            }
            if(canalOb.getFechaCap()!=null&&!canalOb.getFechaCap().equals("")) {
                fechaFinalCap = dateFormat.parse(canalOb.getFechaCap());
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

        String avisar="";
        if(dias!=0&&dias2<4){
            avisar="¡Nueva!";
        }else if(dias3!=0&&dias4<4){
            avisar="¡Nuevos capitulos!";
        }

        textView.setText(avisar);
    }
    public ArrayList<String> resoluciones(String link){
        ArrayList<String> listaopc=new ArrayList<>();
        if(link.contains("=m37")){
            listaopc.add("Resolución 1080p");
            listaopc.add("Resolución 720p");
            listaopc.add("Resolución 480p");
        }else if(link.contains("=m22")){
            listaopc.add("Resolución 720p");
            listaopc.add("Resolución 480p");
        }else if(link.contains("=m18")){
            listaopc.add("Resolución 480p");
        }else{
            listaopc.add("Resolución 4K");
        }
        return listaopc;
    }
    public String getSaved(String key){
        SharedPreferences preferences = context.getSharedPreferences("Usuarios",Context.MODE_PRIVATE);
        return preferences.getString(key,"No existe");
    }
    public void progress(String nombre, ProgressBar progressBar){
        HashMap<String,TiempoOb> listacaheTiempo = getTiempoSaved("listaTiempo_v2");
        progress(listacaheTiempo.get(nombre),progressBar);
    }
    public void progress(TiempoOb tiempoOb, ProgressBar progressBar){
        int tiempo=0,tiempoFinal=0,tiempo2=0;
        if(tiempoOb!=null){
            tiempo=tiempoOb.getTiempo();
            tiempoFinal=tiempoOb.getTiempoFinal();
        }
        if(tiempoFinal!=0){
            tiempoFinal/=100;
            tiempo2=tiempo/tiempoFinal;
        }
        progressBar.setMax(100);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(tiempo2);
    }
    public HashMap<String,TiempoOb> getTiempoSaved(String key){
        Gson gson = new Gson();
        String savedList = getSaved(key);
        HashMap<String,TiempoOb> listacaheTiempo=new HashMap<>();
        Type type = new TypeToken<HashMap<String,TiempoOb>>(){}.getType();
        if(!savedList.equals("No existe")){
            listacaheTiempo = gson.fromJson(savedList, type);
        }
        return listacaheTiempo;
    }
    public ArrayList<CanalOb> listaObjeto(String tipo){
        SharedPreferences preferences = context.getSharedPreferences("Usuarios", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String savedList = preferences.getString(tipo,"No existe");
        if(!savedList.equals("No existe")) {
            Type type = new TypeToken<ArrayList<CanalOb>>(){}.getType();
            return gson.fromJson(savedList, type);
        }
        return new ArrayList<CanalOb>();
    }
    public void saveLista(ArrayList<T> lista,String key){
        Gson gson = new Gson();
        String jsonList = gson.toJson(lista);
        SharedPreferences.Editor editor = context.getSharedPreferences("Usuarios",Context.MODE_PRIVATE).edit();
        editor.putString(key,jsonList);
        editor.commit();
    }
    public void saveHash(HashMap<String,T> lista,String key){
        Gson gson = new Gson();
        String jsonList = gson.toJson(lista);
        SharedPreferences.Editor editor = context.getSharedPreferences("Usuarios",Context.MODE_PRIVATE).edit();
        editor.putString(key,jsonList);
        editor.commit();
    }
    public void saveFavoritos(ArrayList<CanalOb> objetos, ArrayList<CanalOb> favoritos, String nombre, MenuItem item,String caso){
        CanalOb pelicula=null;
        for(int x=0;x<objetos.size();x++){
            if(objetos.get(x).getNombre().equals(nombre)){
                item.setIcon(objetos.get(x).getFavo()? R.drawable.ic_favorite_border_black_24dp:R.drawable.ic_favorite_black_24dp);
                objetos.get(x).setFavo(!objetos.get(x).getFavo());
                pelicula=objetos.get(x);
                break;
            }
        }
        boolean aux=true;
        for (int x=0;x<favoritos.size();x++){
            if(favoritos.get(x).getNombre().equals(nombre)){
                favoritos.remove(x);
                aux=false;
                break;
            }
        }
        if(aux)
            favoritos.add(pelicula);

        saveLista((ArrayList<T>) favoritos,"listaFavoritos");
        saveLista((ArrayList<T>) objetos,caso);
    }
    public void descargar(CanalOb objeto){
        DownloadManager downloadManager=(DownloadManager)context.getSystemService(context.DOWNLOAD_SERVICE);
        Uri uri =Uri.parse(objeto.getLink());
        DownloadManager.Request request =new DownloadManager.Request(uri);
        String nombre=objeto.getNombre()+(objeto.getCapitulo().isEmpty()?"":"-"+objeto.getCapitulo());
        request.setTitle(nombre);
        request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_MOVIES,nombre+".mp4");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        downloadManager.enqueue(request);
    }

    public String getNewLink(String opcion,String link){
        if(opcion.endsWith("720p")){
            link =link.replace("=m37","=m22");
        }else if(opcion.endsWith("480p")){
            link =link.contains("=m22")?link.replace("=m22","=m18"):link.replace("=m37","=m18");
        }
        return link;
    }
    /*public static MediaInfo buildMediaInfo(String title, String url, String mimeType, String imgUrl, String bigImageUrl) {
        MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);

        movieMetadata.putString(MediaMetadata.KEY_TITLE, title);
        movieMetadata.addImage(new WebImage(Uri.parse(imgUrl)));
        movieMetadata.addImage(new WebImage(Uri.parse(bigImageUrl)));
        return new MediaInfo.Builder(url)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType(mimeType)
                .setMetadata(movieMetadata)
                .build();
    }
    public static MediaInfo buildMediaInfo(CanalOb canalOb) {
        return buildMediaInfo(canalOb.getNombre(),canalOb.getLink(),"application/x-mpegURL",canalOb.getImagen(),canalOb.getImagen());
    }

    private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_METADATA_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = Funciones.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        //returns an authorized Credential object.
        return credential;
    }
    private void cargarDatos() throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        FileList result = service.files().list()
                .setPageSize(10)
                .setFields("nextPageToken, files(id, name)")
                .execute();
    }*/
}
