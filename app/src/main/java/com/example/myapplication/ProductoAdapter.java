package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.model.Producto;
import java.util.ArrayList;
import java.util.Locale;

public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder> {

    private ArrayList<Producto> listaProductos;
    private OnItemClickListener listener; // Variable para el listener

    // Interfaz para comunicar el clic al MainActivity
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    // Constructor actualizado para recibir el listener
    public ProductoAdapter(ArrayList<Producto> listaProductos, OnItemClickListener listener) {
        this.listaProductos = listaProductos;
        this.listener = listener;
    }

    // Constructor antiguo (para compatibilidad si hiciera falta, aunque es mejor usar el de arriba)
    public ProductoAdapter(ArrayList<Producto> listaProductos) {
        this.listaProductos = listaProductos;
    }

    // Método para asignar listener después
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_producto, parent, false);
        return new ProductoViewHolder(view, listener); // Pasamos el listener al ViewHolder
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto producto = listaProductos.get(position);
        holder.bind(producto);
    }

    @Override
    public int getItemCount() {
        return listaProductos.size();
    }

    public static class ProductoViewHolder extends RecyclerView.ViewHolder {
        private TextView textoNombre, textoDetalles, textoTotal;

        public ProductoViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            textoNombre = itemView.findViewById(R.id.texto_nombre_producto);
            textoDetalles = itemView.findViewById(R.id.texto_detalles_producto);
            textoTotal = itemView.findViewById(R.id.texto_total_producto);

            // Configurar el clic en toda la tarjeta
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });
        }

        public void bind(Producto producto) {
            textoNombre.setText(producto.getNombre());

            String detalles = String.format(Locale.US, "%d x $%.2f",
                    producto.getCantidad(), producto.getPrecioUnitario());
            if (producto.getDescuento() > 0) {
                detalles += " (Desc: " + producto.getDescuento() + "%)";
            }
            textoDetalles.setText(detalles);

            String total = String.format(Locale.US, "$%.2f", producto.calcularTotal());
            textoTotal.setText(total);
        }
    }
}
