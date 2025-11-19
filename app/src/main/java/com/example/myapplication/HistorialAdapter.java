package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.myapplication.model.Compra;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class HistorialAdapter extends ArrayAdapter<Compra> {

    private Context context;
    private ArrayList<Compra> listaCompras;

    public HistorialAdapter(@NonNull Context context, ArrayList<Compra> listaCompras) {
        super(context, 0, listaCompras);
        this.context = context;
        this.listaCompras = listaCompras;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Reutilizar la vista si es posible para mejorar rendimiento
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(context).inflate(R.layout.item_historial, parent, false);
        }

        Compra compraActual = listaCompras.get(position);

        // Vincular elementos del XML (item_historial.xml)
        TextView nombreTienda = listItem.findViewById(R.id.text_nombre_tienda);
        TextView direccion = listItem.findViewById(R.id.text_direccion);
        TextView total = listItem.findViewById(R.id.text_total);
        TextView fecha = listItem.findViewById(R.id.text_fecha);

        // Asignar Textos
        nombreTienda.setText("Tienda: " + compraActual.getNombreTienda());

        // Verificar si hay dirección, si no, poner "Sin dirección"
        if (compraActual.getDireccionTienda() != null && !compraActual.getDireccionTienda().isEmpty()) {
            direccion.setText("Dirección: " + compraActual.getDireccionTienda());
        } else {
            direccion.setText("Dirección: No registrada");
        }

        // Formatear Total
        total.setText(String.format(Locale.US, "Total: $%.2f", compraActual.getTotal()));

        // Formatear Fecha
        if (compraActual.getFecha() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            fecha.setText(sdf.format(compraActual.getFecha()));
        } else {
            fecha.setText("--/--/----");
        }

        return listItem;
    }
}
