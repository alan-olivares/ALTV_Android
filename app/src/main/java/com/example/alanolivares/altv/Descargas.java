package com.example.alanolivares.altv;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;


public class Descargas extends Fragment {
    private static final int REQUEST_READ = 1;
    ArrayList<String> lista;
    ArrayAdapter<String> adapter;
    ListView listView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        view= inflater.inflate(R.layout.fragment_descargas,container,false);
        listView=view.findViewById(R.id.listviewDescargas);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Descargas");
        lista=new ArrayList<>();
        File directory = new File(Environment.getExternalStorageDirectory()+"/Android/data/com.example.alanolivares.altv/files/Movies");
        System.out.println(directory.toString());
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED){
            File[] fList = directory.listFiles();
            if(directory.listFiles().length>0){
                for (File file : fList){
                    if (file.isFile()){
                        lista.add(file.getName());
                    }
                }
            }else{
                Snackbar
                        .make(getActivity().findViewById(android.R.id.content), "Lista de descargas vacia",Snackbar.LENGTH_LONG)
                        .show();
            }
        }else{
            Snackbar
                    .make(getActivity().findViewById(android.R.id.content), "Se necesitan permisos para poder ver las descargas",Snackbar.LENGTH_LONG)
                    .show();
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        Collections.sort(lista);
        adapter= new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,lista);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent pas =new Intent(view.getContext(),ExoPlayer1.class);
                System.out.println(lista.get(position));
                pas.putExtra("link", lista.get(position));
                pas.putExtra("nombre", lista.get(position));
                startActivity(pas);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           final int pos, long id) {
                String[] opc = new String[] { "Eliminar"};
                AlertDialog opciones = new AlertDialog.Builder(
                        getContext())
                        .setCancelable(true)
                        .setItems(opc,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int selected) {
                                        if (selected == 0) {
                                            File file = new File(Environment.getExternalStorageDirectory()+"/Android/data/com.example.alanolivares.altv/files/Movies/"+lista.get(pos));
                                            try {
                                                file.getCanonicalFile().delete();
                                                lista.remove(pos);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            adapter.notifyDataSetChanged();
                                            if(lista.isEmpty()){
                                                Snackbar
                                                        .make(getActivity().findViewById(android.R.id.content), "Lista de descargas vacia",Snackbar.LENGTH_LONG)
                                                        .show();
                                            }
                                        }
                                    }
                                }).create();
                opciones.show();

                return true;
            }
        });

        return view;
    }


}

