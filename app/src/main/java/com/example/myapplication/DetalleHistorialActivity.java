package com.example.myapplication;import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.model.Compra;
import com.example.myapplication.model.DetalleCompra;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class DetalleHistorialActivity extends AppCompatActivity {

    private TextView txtTienda, txtFecha, txtTotal;
    private Button btnMapa;
    private RecyclerView recyclerDetalles;

    private Compra compraSeleccionada;
    private String compraId; // Necesitamos el ID del documento para actualizarlo
    private DetalleHistorialAdapter adapter;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_historial);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Recibir datos del Intent
        compraSeleccionada = (Compra) getIntent().getSerializableExtra("COMPRA_SELECCIONADA");
        // Intentamos obtener el ID si lo pasamos (si no, tendremos que buscarlo,
        // pero idealmente deberías pasar el ID desde HistorialActivity también si no viene en el objeto)
        // NOTA: El objeto 'Compra' debería tener un campo 'id' o similar si usas Firestore,
        // o pasarlo extra. Asumiremos que en HistorialActivity recuperamos el ID de alguna forma
        // o que tendremos que buscarlo.
        // TRUCO: Para simplificar, vamos a buscar la compra en HistorialActivity y pasar el ID en el intent.
        compraId = getIntent().getStringExtra("COMPRA_ID");

        if (compraSeleccionada == null) {
            Toast.makeText(this, "Error al cargar detalles", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Vincular vistas
        txtTienda = findViewById(R.id.txt_titulo_tienda);
        txtFecha = findViewById(R.id.txt_subtitulo_fecha);
        txtTotal = findViewById(R.id.txt_total_final_detalle);
        btnMapa = findViewById(R.id.btn_ver_mapa);
        recyclerDetalles = findViewById(R.id.recycler_detalles);

        configurarInterfaz();
    }

    private void configurarInterfaz() {
        txtTienda.setText(compraSeleccionada.getNombreTienda());

        if (compraSeleccionada.getFecha() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            txtFecha.setText(sdf.format(compraSeleccionada.getFecha()));
        }

        actualizarTotalVisual();

        // Configurar RecyclerView
        recyclerDetalles.setLayoutManager(new LinearLayoutManager(this));
        if (compraSeleccionada.getDetalles() != null) {
            // Pasamos el listener para editar (this::mostrarDialogoEditarProducto)
            adapter = new DetalleHistorialAdapter(compraSeleccionada.getDetalles(), this::mostrarDialogoEditarProducto);
            recyclerDetalles.setAdapter(adapter);
        }

        // Botón Mapa
        if (compraSeleccionada.getLatitud() == 0 && compraSeleccionada.getLongitud() == 0) {
            btnMapa.setVisibility(View.GONE);
        } else {
            btnMapa.setOnClickListener(v -> {
                Intent intent = new Intent(DetalleHistorialActivity.this, MapsActivity.class);
                intent.putExtra("COMPRA_SELECCIONADA", compraSeleccionada);
                startActivity(intent);
            });
        }
    }

    private void actualizarTotalVisual() {
        txtTotal.setText(String.format(Locale.US, "$%.2f", compraSeleccionada.getTotal()));
    }

    // --- LÓGICA DE EDICIÓN ---

    private void mostrarDialogoEditarProducto(int position) {
        DetalleCompra detalle = compraSeleccionada.getDetalles().get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Reutilizamos el layout de agregar producto porque tiene los mismos campos
        View viewDialogo = LayoutInflater.from(this).inflate(R.layout.dialogo_agregar_producto, null);
        builder.setView(viewDialogo);
        builder.setTitle("Editar Producto Histórico");

        final EditText editNombre = viewDialogo.findViewById(R.id.edit_nombre_producto);
        final EditText editCantidad = viewDialogo.findViewById(R.id.edit_cantidad_producto);
        final EditText editPrecio = viewDialogo.findViewById(R.id.edit_precio_producto);
        final EditText editDescuento = viewDialogo.findViewById(R.id.edit_descuento_producto);

        // Rellenar datos actuales
        editNombre.setText(detalle.getNombreProducto());
        editCantidad.setText(String.valueOf(detalle.getCantidad()));
        editPrecio.setText(String.valueOf(detalle.getPrecioUnitario()));
        editDescuento.setText(String.valueOf(detalle.getDescuento()));

        builder.setPositiveButton("Guardar Cambios", (dialog, which) -> {
            String nombre = editNombre.getText().toString().trim();
            String cantidadStr = editCantidad.getText().toString().trim();
            String precioStr = editPrecio.getText().toString().trim();
            String descuentoStr = editDescuento.getText().toString().trim();

            if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(cantidadStr) || TextUtils.isEmpty(precioStr)) {
                Toast.makeText(this, "Campos vacíos", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int cantidad = Integer.parseInt(cantidadStr);
                double precio = Double.parseDouble(precioStr);
                double descuento = TextUtils.isEmpty(descuentoStr) ? 0.0 : Double.parseDouble(descuentoStr);

                // 1. Actualizar el objeto localmente
                detalle.setNombreProducto(nombre);
                detalle.setCantidad(cantidad);
                detalle.setPrecioUnitario(precio);
                detalle.setDescuento(descuento);

                // Recalcular el total de la línea
                double subtotalLinea = cantidad * precio;
                double descuentoMonto = subtotalLinea * (descuento / 100);
                detalle.setTotalLinea(subtotalLinea - descuentoMonto);

                // 2. Recalcular el TOTAL GENERAL de la compra
                recalcularTotalCompra();

                // 3. Guardar en Firebase
                guardarCambiosEnFirebase();

                // 4. Actualizar vista
                adapter.notifyItemChanged(position);
                actualizarTotalVisual();

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Números inválidos", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void recalcularTotalCompra() {
        double nuevoTotal = 0;
        for (DetalleCompra d : compraSeleccionada.getDetalles()) {
            nuevoTotal += d.getTotalLinea();
        }
        compraSeleccionada.setTotal(nuevoTotal);
    }

    private void guardarCambiosEnFirebase() {
        if (compraId == null || mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Error: No se puede actualizar en la nube (Falta ID)", Toast.LENGTH_LONG).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        db.collection("usuarios").document(userId).collection("compras").document(compraId)
                .update(
                        "detalles", compraSeleccionada.getDetalles(), // Actualizamos solo la lista de productos
                        "total", compraSeleccionada.getTotal()        // Y el total global
                )
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Actualizado en la nube correctamente", Toast.LENGTH_SHORT).show();


                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}

