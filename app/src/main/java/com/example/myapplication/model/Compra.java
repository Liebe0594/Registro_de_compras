package com.example.myapplication.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Compra implements Serializable {
    private long id;
    private Date fecha;

    private String nombreTienda;
    private String direccionTienda;

    private double total;
    private List<DetalleCompra> detalles;

    // Nuevos campos para geolocalización
    private double latitud;
    private double longitud;

    // Constructor con latitud y longitud opcional
    public Compra(Date fecha, String nombreTienda, String direccionTienda, double total,
                  List<DetalleCompra> detalles, double latitud, double longitud) {
        this.id = System.currentTimeMillis();
        this.fecha = fecha;
        this.nombreTienda = nombreTienda;
        this.direccionTienda = direccionTienda;
        this.total = total;
        this.detalles = detalles;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    // Constructor sin lat/long (para compatibilidad)
    public Compra(Date fecha, String nombreTienda, String direccionTienda, double total,
                  List<DetalleCompra> detalles) {
        this(fecha, nombreTienda, direccionTienda, total, detalles, 0.0, 0.0);
    }

    // Getters y setters
    public long getId() { return id; }
    public Date getFecha() { return fecha; }
    public String getNombreTienda() { return nombreTienda; }
    public String getDireccionTienda() { return direccionTienda; }
    public double getTotal() { return total; }
    public List<DetalleCompra> getDetalles() { return detalles; }

    public double getLatitud() { return latitud; }
    public void setLatitud(double latitud) { this.latitud = latitud; }

    public double getLongitud() { return longitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }

    @Override
    public String toString() {
        android.text.format.DateFormat df = new android.text.format.DateFormat();
        String fechaFormateada = df.format("dd/MM/yyyy", this.fecha).toString();

        StringBuilder detallesStr = new StringBuilder();
        for (DetalleCompra detalle : detalles) {
            detallesStr.append(detalle.getNombre())
                    .append(" x").append(detalle.getCantidad())
                    .append(" - $").append(String.format(Locale.US, "%.2f", detalle.getTotal()))
                    .append("\n");
        }

        return "Tienda: " + nombreTienda +
                "\nDirección: " + direccionTienda +
                " (" + fechaFormateada + ")\nTotal: " + String.format(Locale.US, "$%.2f", total) +
                "\nProductos:\n" + detallesStr.toString();
    }

}

