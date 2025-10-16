package com.example.myapplication;

import androidx.annotation.NonNull;

public class Producto {
    private String nombre;
    private int cantidad;
    private double precioUnitario;
    private double descuento; // En porcentaje (ej. 10 para 10%)

    public Producto(String nombre, int cantidad, double precioUnitario, double descuento) {
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.descuento = descuento;
    }
    // ... dentro de la clase Producto ...


    public double getPrecioSubtotal() {
        return precioUnitario * cantidad;
    }
    // ... dentro de la clase Producto ...// Método para calcular el ahorro total de este producto
    public double getAhorro() {
        return getPrecioSubtotal() - getPrecioTotal();
    }

// ... resto de la clase ...


// ... resto de la clase ...


    // Getters para acceder a los datos
    public String getNombre() {
        return nombre;
    }

    public int getCantidad() {
        return cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public double getDescuento() {
        return descuento;
    }

    // Método para calcular el precio total del producto
    public double getPrecioTotal() {
        double precioConDescuento = precioUnitario * (1 - (descuento / 100.0));
        return precioConDescuento * cantidad;
    }

    // Sobrescribimos el método toString() para que se muestre bien en el ListView
    @NonNull
    @Override
    public String toString() {
        String texto = cantidad + "x " + nombre + " - $" + String.format("%.2f", getPrecioTotal());
        if (descuento > 0) {
            texto += " (Descuento: " + descuento + "%)";
        }
        return texto;
    }
}

//Añadir modelo de datos para Producto
//
//Se crea la clase `Producto` para representar artículos en el carrito.
//
//- Almacena nombre, cantidad, precio y descuento.
//- Incluye métodos para calcular el subtotal, el ahorro y el total con descuento.
//- El método `toString()` está formateado para mostrarse correctamente en la `ListView`.
