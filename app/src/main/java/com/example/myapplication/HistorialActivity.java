package com.example.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.model.Compra;

import java.util.ArrayList;
import java.util.Locale;

public class HistorialActivity extends AppCompatActivity {

    private ListView listaHistorialView;
    private TextView textoVacioView;
    private ArrayList<Compra> historialDeCompras;
    private TextView textoTotalGeneralView;
    private LinearLayout layoutTotalGeneral;

    // --- INICIO DE CAMBIOS PARA BORRADO ---
    // Hacemos el adaptador una variable de la clase para poder actualizarlo
    private ArrayAdapter<Compra> adaptador;
    private boolean seHicieronCambios = false; // Flag para saber si se borró algo
    // --- FIN DE CAMBIOS PARA BORRADO ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        listaHistorialView = findViewById(R.id.lista_historial);
        textoVacioView = findViewById(R.id.texto_vacio);
        textoTotalGeneralView = findViewById(R.id.texto_total_general);
        layoutTotalGeneral = findViewById(R.id.layout_total_general);

        historialDeCompras = (ArrayList<Compra>) getIntent().getSerializableExtra("HISTORIAL_COMPRAS");

        // Si el historial es nulo, lo inicializamos para evitar errores
        if (historialDeCompras == null) {
            historialDeCompras = new ArrayList<>();
        }

        actualizarVista();

        // Usamos un ArrayAdapter que mostrará el resultado del método toString() de Compra
        adaptador = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, historialDeCompras);
        listaHistorialView.setAdapter(adaptador);

        // Configuramos el listener para que al tocar un item, se abra MapsActivity
        listaHistorialView.setOnItemClickListener((parent, view, position, id) -> {
            Compra compraSeleccionada = historialDeCompras.get(position);
            Intent intent = new Intent(HistorialActivity.this, MapsActivity.class);
            intent.putExtra("COMPRA_SELECCIONADA", compraSeleccionada);
            startActivity(intent);
        });

        // --- INICIO DE CAMBIOS: Listener para borrado con clic largo ---
        listaHistorialView.setOnItemLongClickListener((parent, view, position, id) -> {
            // Mostramos un diálogo de confirmación antes de borrar
            mostrarDialogoDeConfirmacion(position);
            return true; // Indicamos que hemos manejado el evento
        });
        // --- FIN DE CAMBIOS: Listener para borrado con clic largo ---
    }

    private void mostrarDialogoDeConfirmacion(final int position) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Compra")
                .setMessage("¿Estás seguro de que deseas eliminar esta compra del historial?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    // Eliminar la compra de la lista
                    historialDeCompras.remove(position);

                    // Notificar al adaptador que los datos han cambiado para que refresque la UI
                    adaptador.notifyDataSetChanged();

                    // Recalcular el total general
                    calcularYMostrarTotalGeneral();

                    // Comprobar si la lista ha quedado vacía para mostrar el mensaje correspondiente
                    actualizarVista();

                    // Marcamos que se han realizado cambios para devolver el resultado
                    seHicieronCambios = true;
                })
                .setNegativeButton("Cancelar", null) // No hacer nada si se cancela
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    private void calcularYMostrarTotalGeneral() {
        double totalGeneral = 0.0;
        for (Compra compra : historialDeCompras) {
            totalGeneral += compra.getTotal();
        }
        textoTotalGeneralView.setText(String.format(Locale.US, "$%.2f", totalGeneral));
    }

    // --- INICIO DE CAMBIOS: Nuevo método para actualizar la visibilidad de las vistas ---
    private void actualizarVista() {
        if (historialDeCompras.isEmpty()) {
            listaHistorialView.setVisibility(View.GONE);
            textoVacioView.setVisibility(View.VISIBLE);
            layoutTotalGeneral.setVisibility(View.GONE);
        } else {
            listaHistorialView.setVisibility(View.VISIBLE);
            textoVacioView.setVisibility(View.GONE);
            layoutTotalGeneral.setVisibility(View.VISIBLE);
            calcularYMostrarTotalGeneral(); // Recalculamos por si acaso
        }
    }
    // --- FIN DE CAMBIOS ---


    // --- INICIO DE CAMBIOS: Devolver la lista actualizada a MainActivity ---
    @Override
    public void onBackPressed() {
        // Si se realizaron cambios, devolvemos la lista actualizada
        if (seHicieronCambios) {
            Intent intent = new Intent();
            intent.putExtra("HISTORIAL_ACTUALIZADO", historialDeCompras);
            setResult(RESULT_OK, intent);
        } else {
            setResult(RESULT_CANCELED);
        }
        super.onBackPressed();
    }
    // --- FIN DE CAMBIOS ---
}

//Mostrar total general en el historial de compras
//
//Se añade la funcionalidad para calcular y mostrar el gasto total acumulado en la pantalla de `HistorialActivity`.
//
//- Se agregan un TextView y un Layout para el total.
//- Se implementa un método que suma el total de cada compra.
//- El layout del total se oculta si no hay compras en el historial.