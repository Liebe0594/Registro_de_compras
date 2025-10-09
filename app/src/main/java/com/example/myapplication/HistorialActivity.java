package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
            // Usamos un layout de item más espacioso
            ArrayAdapter<Compra> adaptador = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_2, android.R.id.text1, historialDeCompras);
            listaHistorialView.setAdapter(adaptador);

            // Configuramos el listener para que al tocar un item, se abra el mapa
            listaHistorialView.setOnItemClickListener((parent, view, position, id) -> {
                Compra compraSeleccionada = historialDeCompras.get(position);
                abrirMapaConDireccion(compraSeleccionada.getDireccionTienda());
            });
        }
    }

    // Copiamos el mismo método de MainActivity para abrir el mapa
    private void abrirMapaConDireccion(String direccion) {
        if (direccion == null || direccion.trim().isEmpty()) {
            Toast.makeText(this, "No hay dirección para mostrar en el mapa.", Toast.LENGTH_SHORT).show();
            return;
        }

        String direccionCodificada = Uri.encode(direccion);
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + direccionCodificada);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Uri webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + direccionCodificada);
            Intent webIntent = new Intent(Intent.ACTION_VIEW, webUri);
            if (webIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(webIntent);
            } else {
                Toast.makeText(this, "No se encontró una aplicación de mapas o navegador.", Toast.LENGTH_LONG).show();
            }
        }
    }
}


