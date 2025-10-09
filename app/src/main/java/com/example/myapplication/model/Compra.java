package com.example.myapplication.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Compra implements Serializable {    private long id;
    private Date fecha;
    private String geolocalizacion;

    private String nombreTienda;
    private String direccionTienda;

    private double total;
    private List<DetalleCompra> detalles;

    public Compra(Date fecha, String nombreTienda, String direccionTienda, double total, List<DetalleCompra> detalles) {
        this.id = System.currentTimeMillis();
        this.fecha = fecha;
        this.nombreTienda = nombreTienda;
        this.direccionTienda = direccionTienda;
        this.geolocalizacion = direccionTienda;
        this.total = total;
        this.detalles = detalles;
    }

    // Getters
    public long getId() { return id; }
    public Date getFecha() { return fecha; }
    public String getNombreTienda() { return nombreTienda; }
    public String getDireccionTienda() { return direccionTienda; }
    public double getTotal() { return total; }
    public List<DetalleCompra> getDetalles() { return detalles; }

    @Override
    public String toString() {
        // Formateamos la fecha para que sea más legible en la lista
        android.text.format.DateFormat df = new android.text.format.DateFormat();
        String fechaFormateada = df.format("dd/MM/yyyy", this.fecha).toString();

        return "Tienda: " + nombreTienda + " (" + fechaFormateada + ")\nTotal: " + String.format(Locale.US, "$%.2f", total);
    }
}

