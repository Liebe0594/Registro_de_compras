package com.example.myapplication.model;

import java.util.Date;public class Transaccion {
    private long id;
    private Date fecha;
    private String nombre;
    private double precio;
    private String tipo; // "ingreso" o "egreso"

    // Constructor
    public Transaccion(Date fecha, String nombre, double precio, String tipo) {
        this.id = System.currentTimeMillis();
        this.fecha = fecha;
        this.nombre = nombre;
        this.precio = precio;
        this.tipo = tipo;
    }

    // Getters
    public String getNombre() { return nombre; }
    public double getPrecio() { return precio; }
    public String getTipo() { return tipo; }

    @Override
    public String toString() {
        return "Transacción (" + tipo + "): " + nombre + " - $" + String.format("%.2f", precio);
    }
}
