package com.example.alanolivares.altv.Funciones;

public class Guia{
    String titulo;
    String calificacion;
    String descripcion;

    public String getTitulo() {
        return titulo;
    }

    public String getCalificacion() {
        return calificacion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Guia(String titulo, String calificacion, String descripcion) {
        this.titulo = titulo;
        this.calificacion = calificacion;
        this.descripcion = descripcion;
    }
}
