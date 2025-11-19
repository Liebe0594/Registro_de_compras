package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
// import android.widget.ArrayAdapter; // YA NO SE USA EL ADAPTADOR SIMPLE
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.model.Compra;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Locale;

public class HistorialActivity extends AppCompatActivity {

    private ListView listaHistorialView;
    private TextView textoVacioView;
    private TextView textoTotalGeneralView;
    private LinearLayout layoutTotalGeneral;

    private ArrayList<Compra> historialDeCompras;
    private ArrayList<String> listaIdsCompras;

    // CAMBIO 1: Usamos nuestro adaptador personalizado
    private HistorialAdapter adaptador;

    // Variables de Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Vincular Vistas
        listaHistorialView = findViewById(R.id.lista_historial);
        textoVacioView = findViewById(R.id.texto_vacio);
        textoTotalGeneralView = findViewById(R.id.texto_total_general);
        layoutTotalGeneral = findViewById(R.id.layout_total_general);

        historialDeCompras = new ArrayList<>();
        listaIdsCompras = new ArrayList<>();

        // CAMBIO 2: Inicializar el adaptador personalizado
        adaptador = new HistorialAdapter(this, historialDeCompras);
        listaHistorialView.setAdapter(adaptador);

        // Cargar datos desde la nube
        cargarHistorialDesdeFirebase();


        listaHistorialView.setOnItemClickListener((parent, view, position, id) -> {
            Compra compraSeleccionada = historialDeCompras.get(position);
            String compraId = listaIdsCompras.get(position); // <--- OBTENEMOS EL ID

            Intent intent = new Intent(HistorialActivity.this, DetalleHistorialActivity.class);
            intent.putExtra("COMPRA_SELECCIONADA", compraSeleccionada);
            intent.putExtra("COMPRA_ID", compraId); // <--- PASAMOS EL ID
            startActivity(intent);
        });



        // CLICK LARGO: Mostrar Menú de Opciones (Editar / Eliminar)
        listaHistorialView.setOnItemLongClickListener((parent, view, position, id) -> {
            mostrarMenuOpciones(position);
            return true;
        });
    }

    // --- CARGA DE DATOS ---

    private void cargarHistorialDesdeFirebase() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("usuarios").document(userId).collection("compras")
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        historialDeCompras.clear();
                        listaIdsCompras.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Compra compra = document.toObject(Compra.class);
                                historialDeCompras.add(compra);
                                listaIdsCompras.add(document.getId());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        adaptador.notifyDataSetChanged();
                        actualizarVista();
                    } else {
                        Toast.makeText(HistorialActivity.this, "Error al cargar historial", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // --- MENÚS Y DIÁLOGOS ---

    private void mostrarMenuOpciones(int position) {
        String[] opciones = {"Editar Nombre Tienda", "Eliminar Compra"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Opciones de Compra");
        builder.setItems(opciones, (dialog, which) -> {
            if (which == 0) {
                mostrarDialogoEditar(position);
            } else if (which == 1) {
                mostrarDialogoConfirmarEliminar(position);
            }
        });
        builder.show();
    }

    private void mostrarDialogoEditar(int position) {
        Compra compraActual = historialDeCompras.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar Tienda");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(compraActual.getNombreTienda());
        builder.setView(input);

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String nuevoNombre = input.getText().toString().trim();
            if (!nuevoNombre.isEmpty()) {
                actualizarCompraEnFirebase(position, nuevoNombre);
            } else {
                Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void mostrarDialogoConfirmarEliminar(final int position) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Compra")
                .setMessage("¿Estás seguro? Esto borrará la compra de la nube permanentemente.")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    eliminarCompraDeFirebase(position);
                })
                .setNegativeButton("Cancelar", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // --- OPERACIONES EN FIREBASE ---

    private void actualizarCompraEnFirebase(int position, String nuevoNombre) {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();
        String documentoId = listaIdsCompras.get(position);

        db.collection("usuarios").document(userId).collection("compras").document(documentoId)
                .update("nombreTienda", nuevoNombre)
                .addOnSuccessListener(aVoid -> {
                    historialDeCompras.get(position).setNombreTienda(nuevoNombre);
                    adaptador.notifyDataSetChanged();
                    Toast.makeText(HistorialActivity.this, "Nombre actualizado", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(HistorialActivity.this, "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void eliminarCompraDeFirebase(int position) {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();
        String documentoId = listaIdsCompras.get(position);

        db.collection("usuarios").document(userId).collection("compras").document(documentoId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Compra eliminada", Toast.LENGTH_SHORT).show();
                    historialDeCompras.remove(position);
                    listaIdsCompras.remove(position);
                    adaptador.notifyDataSetChanged();
                    actualizarVista();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al eliminar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // --- UTILIDADES VISUALES ---

    private void calcularYMostrarTotalGeneral() {
        double totalGeneral = 0.0;
        for (Compra compra : historialDeCompras) {
            totalGeneral += compra.getTotal();
        }
        textoTotalGeneralView.setText(String.format(Locale.US, "$%.2f", totalGeneral));
    }

    private void actualizarVista() {
        if (historialDeCompras.isEmpty()) {
            listaHistorialView.setVisibility(View.GONE);
            textoVacioView.setVisibility(View.VISIBLE);
            layoutTotalGeneral.setVisibility(View.GONE);
        } else {
            listaHistorialView.setVisibility(View.VISIBLE);
            textoVacioView.setVisibility(View.GONE);
            layoutTotalGeneral.setVisibility(View.VISIBLE);
            calcularYMostrarTotalGeneral();
        }
    }
}


