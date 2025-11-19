package com.example.myapplication.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Compra implements Serializable {
    private Date fecha;
    private String nombreTienda;
    private String direccionTienda;
    private double total;
    private List<DetalleCompra> detalles;
    private double latitud;
    private double longitud;

    // --- 1. CONSTRUCTOR VACÍO (OBLIGATORIO PARA FIREBASE) ---
    public Compra() {
    }

    // --- 2. CONSTRUCTOR COMPLETO ---
    public Compra(Date fecha, String nombreTienda, String direccionTienda, double total, List<DetalleCompra> detalles, double latitud, double longitud) {
        this.fecha = fecha;
        this.nombreTienda = nombreTienda;
        this.direccionTienda = direccionTienda;
        this.total = total;
        this.detalles = detalles;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    // --- 3. GETTERS Y SETTERS (OBLIGATORIOS) ---
    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    public String getNombreTienda() { return nombreTienda; }
    public void setNombreTienda(String nombreTienda) { this.nombreTienda = nombreTienda; }

    public String getDireccionTienda() { return direccionTienda; }
    public void setDireccionTienda(String direccionTienda) { this.direccionTienda = direccionTienda; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public List<DetalleCompra> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleCompra> detalles) { this.detalles = detalles; }

    public double getLatitud() { return latitud; }
    public void setLatitud(double latitud) { this.latitud = latitud; }

    public double getLongitud() { return longitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }

    // Método para mostrar en la lista (si usas ListView simple)
    @Override
    public String toString() {
        return nombreTienda + "\n" + fecha.toString() + "\nTotal: $" + String.format("%.2f", total);
    }
}


