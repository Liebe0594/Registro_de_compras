package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.model.Compra;

import java.util.ArrayList;

public class HistorialActivity extends AppCompatActivity {

    private ListView listaHistorialView;
    private TextView textoVacioView;
    private ArrayList<Compra> historialDeCompras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        listaHistorialView = findViewById(R.id.lista_historial);
        textoVacioView = findViewById(R.id.texto_vacio);

        // Recibimos la lista de compras desde MainActivity
        historialDeCompras = (ArrayList<Compra>) getIntent().getSerializableExtra("HISTORIAL_COMPRAS");

        if (historialDeCompras == null || historialDeCompras.isEmpty()) {
            listaHistorialView.setVisibility(View.GONE);
            textoVacioView.setVisibility(View.VISIBLE);
        } else {
            // Usamos un ArrayAdapter que mostrará el resultado del método toString() de Compra
            ArrayAdapter<Compra> adaptador = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_2, android.R.id.text1, historialDeCompras);
            listaHistorialView.setAdapter(adaptador);

            // Configuramos el listener para que al tocar un item, se abra MapsActivity
            listaHistorialView.setOnItemClickListener((parent, view, position, id) -> {
                Compra compraSeleccionada = historialDeCompras.get(position);
                Intent intent = new Intent(HistorialActivity.this, MapsActivity.class);
                intent.putExtra("COMPRA_SELECCIONADA", compraSeleccionada);
                intent.putExtra("HISTORIAL_COMPRAS", historialDeCompras);
                startActivity(intent);
            });
        }
    }
}
