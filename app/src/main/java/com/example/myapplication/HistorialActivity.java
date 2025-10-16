package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout; // Importar LinearLayout
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.model.Compra;

import java.util.ArrayList;
import java.util.Locale;

public class HistorialActivity extends AppCompatActivity {

    private ListView listaHistorialView;
    private TextView textoVacioView;
    private ArrayList<Compra> historialDeCompras;

    // --- INICIO DE CAMBIOS ---
    private TextView textoTotalGeneralView;
    private LinearLayout layoutTotalGeneral; // Referencia al layout del total
    // --- FIN DE CAMBIOS ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        listaHistorialView = findViewById(R.id.lista_historial);
        textoVacioView = findViewById(R.id.texto_vacio);

        // --- INICIO DE CAMBIOS ---
        textoTotalGeneralView = findViewById(R.id.texto_total_general);
        layoutTotalGeneral = findViewById(R.id.layout_total_general);
        // --- FIN DE CAMBIOS ---

        // Recibimos la lista de compras desde MainActivity
        historialDeCompras = (ArrayList<Compra>) getIntent().getSerializableExtra("HISTORIAL_COMPRAS");

        if (historialDeCompras == null || historialDeCompras.isEmpty()) {
            listaHistorialView.setVisibility(View.GONE);
            textoVacioView.setVisibility(View.VISIBLE);
            layoutTotalGeneral.setVisibility(View.GONE); // Ocultamos el total si no hay compras
        } else {
            // Hacemos visibles los elementos correctos
            listaHistorialView.setVisibility(View.VISIBLE);
            textoVacioView.setVisibility(View.GONE);
            layoutTotalGeneral.setVisibility(View.VISIBLE); // Mostramos el total

            // Usamos un ArrayAdapter que mostrará el resultado del método toString() de Compra
            // NOTA: Usar un layout personalizado para los items del historial mejoraría mucho la apariencia.
            ArrayAdapter<Compra> adaptador = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, historialDeCompras);
            listaHistorialView.setAdapter(adaptador);

            // --- INICIO DE CAMBIOS: Calcular y mostrar el total general ---
            calcularYMostrarTotalGeneral();
            // --- FIN DE CAMBIOS ---

            // Configuramos el listener para que al tocar un item, se abra MapsActivity
            listaHistorialView.setOnItemClickListener((parent, view, position, id) -> {
                Compra compraSeleccionada = historialDeCompras.get(position);
                Intent intent = new Intent(HistorialActivity.this, MapsActivity.class);
                intent.putExtra("COMPRA_SELECCIONADA", compraSeleccionada);
                startActivity(intent);
            });
        }
    }

    // --- INICIO DE CAMBIOS: Nuevo método para calcular el total ---
    private void calcularYMostrarTotalGeneral() {
        double totalGeneral = 0.0;
        // Recorremos cada compra en la lista y sumamos su total
        for (Compra compra : historialDeCompras) {
            totalGeneral += compra.getTotal();
        }

        // Formateamos el resultado y lo asignamos al TextView
        textoTotalGeneralView.setText(String.format(Locale.US, "$%.2f", totalGeneral));
    }
    // --- FIN DE CAMBIOS ---
}
