package com.example.myapplication.model;

import java.io.Serializable;

public class DetalleCompra implements Serializable {
    private String nombreProducto;
    private int cantidad;
    private double precioUnitario;
    private double descuento;
    private double totalLinea;

    // --- 1. CONSTRUCTOR VACÍO (OBLIGATORIO) ---
    public DetalleCompra() {
    }

    // --- 2. CONSTRUCTOR COMPLETO ---
    public DetalleCompra(String nombreProducto, int cantidad, double precioUnitario, double descuento, double totalLinea) {
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.descuento = descuento;
        this.totalLinea = totalLinea;
    }

    // --- 3. GETTERS Y SETTERS ---
    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }

    public double getDescuento() { return descuento; }
    public void setDescuento(double descuento) { this.descuento = descuento; }

    public double getTotalLinea() { return totalLinea; }
    public void setTotalLinea(double totalLinea) { this.totalLinea = totalLinea; }
}
