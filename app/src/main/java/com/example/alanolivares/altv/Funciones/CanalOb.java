package com.example.alanolivares.altv.Funciones;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

public class CanalOb implements Parcelable {
    private String nombre;
    private String imagen;
    private String link;
    private String capitulo;
    private String calificacion;
    private String descripcion;
    private Boolean favo;
    private String fecha;
    private int numero;
    private int numeroCap;
    private String fechaCap;

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public void setNumeroCap(int numeroCap) {
        this.numeroCap = numeroCap;
    }

    public CanalOb(String nombre, String imagen, String link, String capitulo, String calificacion, String descripcion, Boolean favo, String fecha, int numero, int numeroCap, String fechaCap){
        this.nombre= nombre;
        this.imagen=imagen;
        this.link=link;
        this.capitulo=capitulo;
        this.calificacion=calificacion;
        this.descripcion=descripcion;
        this.favo=favo;
        this.fecha=fecha;
        this.numero=numero;
        this.numeroCap=numeroCap;
        this.fechaCap=fechaCap;
    }

    public int getNumeroCap() {
        return numeroCap;
    }

    public void setCapitulo(String capitulo) {
        this.capitulo = capitulo;
    }

    public void setCalificacion(String calificacion) {
        this.calificacion = calificacion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public void setFechaCap(String fechaCap) {
        this.fechaCap = fechaCap;
    }

    public void setFavo(Boolean favo) {
        this.favo = favo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCapitulo() {
        return capitulo;
    }

    public String getCalificacion() {
        return calificacion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Boolean getFavo() {
        return favo;
    }

    public String getFecha() {
        return fecha;
    }

    public int getNumero() {
        return numero;
    }

    public String getFechaCap() {
        return fechaCap;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @NonNull
    @Override
    public String toString() {
        return "Nombre: "+nombre+
                //"Imagen: "+imagen+
                //",Link: "+link+
                ",Cap: "+capitulo+
                ",Cal: "+calificacion+
                ",Desc: "+descripcion+
                ",Fav: "+favo+
                ",F. Agr: "+fecha+
                ",Num: "+numero+
                ",T. Cap: "+numeroCap+
                ",F. Cap:"+fechaCap;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    protected CanalOb(Parcel in) {
        this.nombre= in.readString();
        this.imagen=in.readString();
        this.link=in.readString();
        this.capitulo=in.readString();
        this.calificacion=in.readString();
        this.descripcion=in.readString();
        this.favo=in.readBoolean();
        this.fecha=in.readString();
        this.numero=in.readInt();
        this.numeroCap=in.readInt();
        this.fechaCap=in.readString();

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(nombre);
        dest.writeString(imagen);
        dest.writeString(link);
        dest.writeString(capitulo);
        dest.writeString(calificacion);
        dest.writeString(descripcion);
        dest.writeBoolean(favo);
        dest.writeString(fecha);
        dest.writeInt(numero);
        dest.writeInt(numeroCap);
        dest.writeString(fechaCap);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<CanalOb> CREATOR = new Parcelable.Creator<CanalOb>() {
        @RequiresApi(api = Build.VERSION_CODES.Q)
        @Override
        public CanalOb createFromParcel(Parcel in) {
            return new CanalOb(in);
        }

        @Override
        public CanalOb[] newArray(int size) {
            return new CanalOb[size];
        }
    };
}
