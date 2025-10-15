package com.example.myapplication;

import com.example.myapplication.model.Compra;
import com.example.myapplication.model.DetalleCompra;
import com.example.myapplication.model.Transaccion;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Producto> carritoDeCompras;
    private ArrayAdapter<Producto> adaptador;
    private ListView listViewCompras;

    private TextView textoSubtotal, textoAhorro, textoTotalFinal;
    private Button botonAgregarProducto, botonFinalizarCompra, botonVerHistorial, botonVerMapa;


    private ArrayList<Compra> historialDeCompras = new ArrayList<>();
    private ArrayList<Transaccion> libroDeTransacciones = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        carritoDeCompras = new ArrayList<>();
        listViewCompras = findViewById(R.id.lista_compras);
        textoSubtotal = findViewById(R.id.texto_subtotal);
        textoAhorro = findViewById(R.id.texto_ahorro);
        textoTotalFinal = findViewById(R.id.texto_total_final);
        botonAgregarProducto = findViewById(R.id.boton_agregar_producto);
        botonFinalizarCompra = findViewById(R.id.boton_finalizar_compra);
        botonVerHistorial = findViewById(R.id.boton_ver_historial);

        botonVerMapa = findViewById(R.id.boton_ver_mapa);
        botonVerMapa.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(intent);
        });


        adaptador = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, carritoDeCompras);
        listViewCompras.setAdapter(adaptador);

        botonAgregarProducto.setOnClickListener(v -> mostrarDialogoAgregarProducto());
        botonFinalizarCompra.setOnClickListener(v -> mostrarDialogoFinalizarCompra());

        botonVerHistorial.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistorialActivity.class);
            intent.putExtra("HISTORIAL_COMPRAS", historialDeCompras);
            startActivity(intent);
        });

        actualizarTotales();
    }

    private void mostrarDialogoFinalizarCompra() {
        if (carritoDeCompras.isEmpty()) {
            Toast.makeText(this, "El carrito está vacío. Añade productos primero.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View viewDialogo = LayoutInflater.from(this).inflate(R.layout.dialogo_finalizar_compra, null);
        builder.setView(viewDialogo);

        final EditText editNombreTienda = viewDialogo.findViewById(R.id.edit_nombre_tienda);
        final EditText editDireccionTienda = viewDialogo.findViewById(R.id.edit_direccion_tienda);

        builder.setPositiveButton("Guardar Compra", (dialog, which) -> {
            String nombreTienda = editNombreTienda.getText().toString().trim();
            String direccionTienda = editDireccionTienda.getText().toString().trim();

            if (TextUtils.isEmpty(nombreTienda) || TextUtils.isEmpty(direccionTienda)) {
                Toast.makeText(this, "El nombre y la dirección de la tienda son obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }
            procesarYGuardarCompra(nombreTienda, direccionTienda);
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.create().show();
    }

    private void procesarYGuardarCompra(String nombreTienda, String direccionTienda) {
        Date fechaActual = new Date();
        double totalCompra = 0;
        try {
            totalCompra = Double.parseDouble(textoTotalFinal.getText().toString().replace("$", ""));
        } catch (NumberFormatException e) {
            Log.e("FinalizarCompra", "Error al parsear el total final");
            Toast.makeText(this, "Error en el cálculo del total.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<DetalleCompra> detallesDeLaCompra = new ArrayList<>();
        for (Producto p : carritoDeCompras) {
            DetalleCompra detalle = new DetalleCompra(
                    p.getNombre(),
                    p.getCantidad(),
                    p.getPrecioUnitario(),
                    p.getDescuento(),
                    p.getPrecioTotal()
            );
            detallesDeLaCompra.add(detalle);
        }

        Compra nuevaCompra = new Compra(fechaActual, nombreTienda, direccionTienda, totalCompra, detallesDeLaCompra);
        historialDeCompras.add(nuevaCompra);

        Transaccion nuevaTransaccion = new Transaccion(fechaActual, "Compra en " + nombreTienda, totalCompra, "egreso");
        libroDeTransacciones.add(nuevaTransaccion);

        Log.d("COMPRA_FINALIZADA", nuevaCompra.toString() + " en " + nuevaCompra.getDireccionTienda());
        Log.d("TRANSACCION_CREADA", nuevaTransaccion.toString());

        Toast.makeText(this, "¡Compra en '" + nombreTienda + "' registrada!", Toast.LENGTH_LONG).show();
        carritoDeCompras.clear();
        adaptador.notifyDataSetChanged();
        actualizarTotales();
    }

    private void actualizarTotales() {
        double subtotal = 0.0, totalFinal = 0.0, ahorroTotal = 0.0;
        for (Producto producto : carritoDeCompras) {
            subtotal += producto.getPrecioSubtotal();
            totalFinal += producto.getPrecioTotal();
            ahorroTotal += producto.getAhorro();
        }
        textoSubtotal.setText(String.format(Locale.US, "$%.2f", subtotal));
        textoAhorro.setText(String.format(Locale.US, "-$%.2f", ahorroTotal));
        textoTotalFinal.setText(String.format(Locale.US, "$%.2f", totalFinal));
    }

    private void mostrarDialogoAgregarProducto() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View viewDialogo = LayoutInflater.from(this).inflate(R.layout.dialogo_agregar_producto, null);
        builder.setView(viewDialogo);

        final EditText editNombre = viewDialogo.findViewById(R.id.edit_nombre_producto);
        final EditText editCantidad = viewDialogo.findViewById(R.id.edit_cantidad_producto);
        final EditText editPrecio = viewDialogo.findViewById(R.id.edit_precio_producto);
        final EditText editDescuento = viewDialogo.findViewById(R.id.edit_descuento_producto);

        builder.setPositiveButton("Agregar", (dialog, which) -> {
            String nombre = editNombre.getText().toString().trim();
            String cantidadStr = editCantidad.getText().toString().trim();
            String precioStr = editPrecio.getText().toString().trim();
            String descuentoStr = editDescuento.getText().toString().trim();

            if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(cantidadStr) || TextUtils.isEmpty(precioStr)) {
                Toast.makeText(MainActivity.this, "Nombre, cantidad y precio son obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int cantidad = Integer.parseInt(cantidadStr);
                double precio = Double.parseDouble(precioStr);
                double descuento = TextUtils.isEmpty(descuentoStr) ? 0.0 : Double.parseDouble(descuentoStr);

                Producto nuevoProducto = new Producto(nombre, cantidad, precio, descuento);
                carritoDeCompras.add(nuevoProducto);

                adaptador.notifyDataSetChanged();
                actualizarTotales();
                Toast.makeText(MainActivity.this, "Producto añadido al carrito", Toast.LENGTH_SHORT).show();

            } catch (NumberFormatException e) {
                Toast.makeText(MainActivity.this, "Por favor, ingresa números válidos", Toast.LENGTH_LONG).show();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.create().show();
    }
}






