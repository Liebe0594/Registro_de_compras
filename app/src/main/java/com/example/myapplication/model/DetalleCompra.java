package com.example.myapplication.model;

import java.io.Serializable;

public class DetalleCompra implements Serializable {
    private String nombre;
    private int cantidad;
    private double precio;
    private double descuento;
    private double total;

    public DetalleCompra(String nombre, int cantidad, double precio, double descuento, double total) {
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.precio = precio;
        this.descuento = descuento;
        this.total = total;
    }

    // Getters
    public String getNombre() { return nombre; }
    public int getCantidad() { return cantidad; }
    public double getPrecio() { return precio; }
    public double getDescuento() { return descuento; }
    public double getTotal() { return total; }
}
//Se crea la clase `DetalleCompra` para representar cada artículo dentro de una compra. Almacena el nombre del producto, cantidad, precio, descuento y total.
//Implementa `Serializable` para ser utilizada dentro de la lista de detalles de la clase `Compra`.

