package com.example.alanolivares.altv.Funciones;

import androidx.annotation.NonNull;

public class TiempoOb {
    String nombre;
    int tiempo;
    int tiempoFinal;

    public TiempoOb(String nombre, int tiempo,int tiempoFinal) {
        this.nombre = nombre;
        this.tiempo = tiempo;
        this.tiempoFinal = tiempoFinal;
    }

    public int getTiempoFinal() {
        return tiempoFinal;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getTiempo() {
        return tiempo;
    }

    public void setTiempo(int tiempo) {
        this.tiempo = tiempo;
    }

    @NonNull
    @Override
    public String toString() {
        return "Nombre: "+nombre+
                " Avance: "+tiempo+
                " Total: "+tiempoFinal;
    }
}


