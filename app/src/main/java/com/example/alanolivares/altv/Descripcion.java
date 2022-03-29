package com.example.alanolivares.altv;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

import com.example.alanolivares.altv.Funciones.CanalOb;
import com.example.alanolivares.altv.Funciones.Funciones;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class Descripcion extends AppCompatActivity{
    private TextView nomb;
    private ProgressBar progressBar;
    private ArrayAdapter<String> adapter;
    private ArrayList<CanalOb> lista_peliculas,lista_favoritos;
    private Button trailer;
    private Funciones func;
    private CanalOb pelicula;
    //private CastContext castContext;
    //private CastSession mCastSession;
    //private SessionManagerListener<CastSession> mSessionManagerListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(android.view.Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().requestFeature(android.view.Window.FEATURE_ACTIVITY_TRANSITIONS);
        setContentView(R.layout.activity_descripcion);
        Inicializar();
        //setupCastListener();
        //castContext = CastContext.getSharedInstance(this);
        //mCastSession = castContext.getSessionManager().getCurrentCastSession();
    }
    private void Inicializar(){
        func=new Funciones(this);
        TextView aviso=findViewById(R.id.infoPel);
        pelicula=getIntent().getParcelableExtra("objeto");
        getSupportActionBar().setTitle(pelicula.getNombre());
        TextView call=(TextView)findViewById(R.id.txtCalif);
        TextView dess=(TextView)findViewById(R.id.txtdes);
        ImageView imageView=(ImageView)findViewById(R.id.imagenPe);
        ImageButton desca=(ImageButton)findViewById(R.id.descarBoton);
        ImageButton play=(ImageButton)findViewById(R.id.playBoton);
        nomb=(TextView)findViewById(R.id.txtNombre);
        progressBar=(ProgressBar)findViewById(R.id.progressPeli);
        Picasso.get()
                .load(pelicula.getImagen())
                .error(R.mipmap.altvlog)
                .into(imageView);
        ViewCompat.setTransitionName(imageView, "transition-image");
        dess.setText(pelicula.getDescripcion());
        call.setText(pelicula.getCalificacion());
        nomb.setText(pelicula.getNombre());
        func.progress((String) nomb.getText(),progressBar);
        desca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resolucion(pelicula.getLink(),pelicula.getNombre(),1);
            }
        });
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if (mCastSession != null && mCastSession.isConnected()) {
                    loadRemoteMedia(0);
                } else {
                    resolucion(pelicula.getLink(),pelicula.getNombre(),0);
                }*/
                resolucion(pelicula.getLink(),pelicula.getNombre(),0);

            }
        });
        func.aviso(aviso,pelicula);
        trailer=(Button)findViewById(R.id.trailer);
        trailer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pas = new Intent(Descripcion.this, VideoTrailer.class);
                pas.putExtra("nombre", pelicula.getNombre());
                startActivity(pas);
            }
        });
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        lista_peliculas=func.listaObjeto("listaPeliculas");
        lista_favoritos=func.listaObjeto("listaFavoritos");
    }
    public void resolucion(final String lik, final String nom, final int viewId){
        ArrayList<String> listaopc=func.resoluciones(lik);
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(Descripcion.this);
        String opc;
        opc=(viewId==0)?"ver":"descargar";
        builderSingle.setTitle("Resolución para "+opc);
        builderSingle.setNegativeButton("Cerrar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        adapter= new ArrayAdapter<String>(this,android.R.layout.simple_list_item_single_choice,listaopc);
        builderSingle.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strName = adapter.getItem(which);
                String newLink=func.getNewLink(strName,lik);
                switch (viewId){
                    case 1:
                        func.descargar(pelicula);
                        break;
                    case 0:
                        Intent pas;
                        pas = new Intent(Descripcion.this, ExoPlayer1.class);
                        pas.putExtra("nombre", nom);
                        pas.putExtra("link", newLink);
                        startActivity(pas);
                        break;
                    default:
                        //item de la lista
                        break;
                }
            }
        });
        builderSingle.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflar el menú; Esto agrega elementos a la barra de acción si está presente.
        getMenuInflater().inflate(R.menu.menu_fav, menu);
        MenuItem item = menu.getItem(0);
        item.setIcon(pelicula.getFavo()?R.drawable.ic_favorite_black_24dp:R.drawable.ic_favorite_border_black_24dp);
        //CastButtonFactory.setUpMediaRouteButton(getApplicationContext(),menu,R.id.media_route_menu_item);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String nombre=nomb.getText().toString();
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.favorito_menu:
                func.saveFavoritos(lista_peliculas, lista_favoritos, nombre, item,"listaPeliculas");
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        func.progress((String) nomb.getText(),progressBar);
    }


    /*private void loadRemoteMedia(int position) {
        MediaInfo mSelectedMedia=func.buildMediaInfo(pelicula);
        if (mCastSession == null) {
            return;
        }
        RemoteMediaClient remoteMediaClient = mCastSession.getRemoteMediaClient();
        if (remoteMediaClient == null) {
            return;
        }
        remoteMediaClient.registerCallback(new RemoteMediaClient.Callback() {
            @Override
            public void onStatusUpdated() {
                Intent intent = new Intent(getApplicationContext(), ExpandedControlsActivity.class);
                startActivity(intent);
                remoteMediaClient.unregisterCallback(this);
            }
        });
        remoteMediaClient.load(new MediaLoadRequestData.Builder()
                .setMediaInfo(buildMediaInfo())
                .setAutoplay(true)
                .setCurrentTime(position).build());
    }*/
    /*private MediaInfo buildMediaInfo() {
        MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);

        movieMetadata.putString(MediaMetadata.KEY_TITLE, pelicula.getNombre());
        movieMetadata.addImage(new WebImage(Uri.parse(pelicula.getImagen())));
        movieMetadata.addImage(new WebImage(Uri.parse(pelicula.getImagen())));

        return new MediaInfo.Builder(pelicula.getLink())
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType("application/x-mpegURL")
                .setMetadata(movieMetadata)
                .build();
    }


    private void setupCastListener() {
        mSessionManagerListener = new SessionManagerListener<CastSession>() {

            @Override
            public void onSessionEnded(CastSession session, int error) {
                onApplicationDisconnected();
            }

            @Override
            public void onSessionResumed(CastSession session, boolean wasSuspended) {
                onApplicationConnected(session);
            }

            @Override
            public void onSessionResumeFailed(CastSession session, int error) {
                onApplicationDisconnected();
            }

            @Override
            public void onSessionStarted(CastSession session, String sessionId) {
                onApplicationConnected(session);
            }

            @Override
            public void onSessionStartFailed(CastSession session, int error) {
                onApplicationDisconnected();
            }

            @Override
            public void onSessionStarting(CastSession session) {}

            @Override
            public void onSessionEnding(CastSession session) {}

            @Override
            public void onSessionResuming(CastSession session, String sessionId) {}

            @Override
            public void onSessionSuspended(CastSession session, int reason) {}

            private void onApplicationConnected(CastSession castSession) {
                mCastSession = castSession;
                supportInvalidateOptionsMenu();
            }

            private void onApplicationDisconnected() {
                supportInvalidateOptionsMenu();
            }
        };
    }*/
}
