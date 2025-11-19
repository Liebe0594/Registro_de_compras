package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.model.DetalleCompra;
import java.util.List;
import java.util.Locale;

public class DetalleHistorialAdapter extends RecyclerView.Adapter<DetalleHistorialAdapter.ViewHolder> {

    private List<DetalleCompra> listaDetalles;
    private OnItemClickListener listener; // Listener para el clic

    // Interfaz para comunicar el clic
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public DetalleHistorialAdapter(List<DetalleCompra> listaDetalles, OnItemClickListener listener) {
        this.listaDetalles = listaDetalles;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_detalle_producto, parent, false);
        return new ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DetalleCompra detalle = listaDetalles.get(position);
        holder.bind(detalle);
    }

    @Override
    public int getItemCount() {
        return listaDetalles.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtCantidad, txtNombre, txtPrecioUnit, txtTotalLinea;

        public ViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            txtCantidad = itemView.findViewById(R.id.detalle_cantidad);
            txtNombre = itemView.findViewById(R.id.detalle_nombre);
            txtPrecioUnit = itemView.findViewById(R.id.detalle_precio_unit);
            txtTotalLinea = itemView.findViewById(R.id.detalle_total_linea);

            // Configurar clic
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });
        }

        public void bind(DetalleCompra detalle) {
            txtCantidad.setText(detalle.getCantidad() + " x");
            txtNombre.setText(detalle.getNombreProducto());
            txtPrecioUnit.setText(String.format(Locale.US, "$%.2f c/u", detalle.getPrecioUnitario()));
            txtTotalLinea.setText(String.format(Locale.US, "$%.2f", detalle.getTotalLinea()));
        }
    }
}

