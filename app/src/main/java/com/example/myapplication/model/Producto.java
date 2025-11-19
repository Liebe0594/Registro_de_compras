package com.example.myapplication.model;

import androidx.annotation.NonNull;
import java.io.Serializable;

// Implementamos Serializable para poder pasar productos entre pantallas si fuera necesario
public class Producto implements Serializable {

    private String nombre;
    private int cantidad;
    private double precioUnitario;
    private double descuento; // Asumimos que es porcentaje según tu código anterior

    // --- 1. CONSTRUCTOR VACÍO (OBLIGATORIO PARA FIREBASE) ---
    public Producto() {
        // Firebase necesita esto vacío para crear el objeto al descargar datos
    }

    // --- 2. CONSTRUCTOR CON DATOS ---
    public Producto(String nombre, int cantidad, double precioUnitario, double descuento) {
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.descuento = descuento;
    }

    // --- 3. GETTERS Y SETTERS (OBLIGATORIOS PARA FIREBASE) ---
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }

    public double getDescuento() { return descuento; }
    public void setDescuento(double descuento) { this.descuento = descuento; }


    // --- 4. MÉTODOS DE CÁLCULO ---

    public double getPrecioSubtotal() {
        return precioUnitario * cantidad;
    }

    // Tu método original con la lógica de porcentaje
    public double getPrecioTotal() {
        double precioConDescuento = precioUnitario * (1 - (descuento / 100.0));
        return precioConDescuento * cantidad;
    }

    // --- EL MÉTODO QUE FALTABA ---
    // Creamos este método porque MainActivity lo está buscando.
    // Simplemente llama a tu lógica original.
    public double calcularTotal() {
        return getPrecioTotal();
    }

    public double getAhorro() {
        return getPrecioSubtotal() - getPrecioTotal();
    }

    @NonNull
    @Override
    public String toString() {
        String texto = cantidad + "x " + nombre + " - $" + String.format("%.2f", getPrecioTotal());
        if (descuento > 0) {
            texto += " (Desc: " + descuento + "%)";
        }
        return texto;
    }
}
