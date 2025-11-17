package com.example.agroapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.agroapp.R;
import com.example.agroapp.models.HistorialClinico;
import java.util.List;

public class HistorialClinicoAdapter extends RecyclerView.Adapter<HistorialClinicoAdapter.HistorialViewHolder> {
    
    private Context context;
    private List<HistorialClinico> historialList;
    private OnHistorialClickListener listener;
    
    public interface OnHistorialClickListener {
        void onHistorialClick(HistorialClinico historial);
    }
    
    public HistorialClinicoAdapter(Context context, List<HistorialClinico> historialList, 
                                  OnHistorialClickListener listener) {
        this.context = context;
        this.historialList = historialList;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public HistorialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_historial_clinico, parent, false);
        return new HistorialViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull HistorialViewHolder holder, int position) {
        HistorialClinico historial = historialList.get(position);
        
        holder.tvFecha.setText("Fecha: " + historial.getFecha());
        holder.tvEnfermedad.setText("Enfermedad: " + historial.getEnfermedad());
        holder.tvEstado.setText("Estado: " + historial.getEstado());
        holder.tvTratamiento.setText("Tratamiento: " + 
            (historial.getTratamiento() != null ? historial.getTratamiento() : "Sin tratamiento"));
        
        holder.cardView.setOnClickListener(v -> listener.onHistorialClick(historial));
    }
    
    @Override
    public int getItemCount() {
        return historialList.size();
    }
    
    static class HistorialViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvFecha, tvEnfermedad, tvEstado, tvTratamiento;
        
        public HistorialViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvEnfermedad = itemView.findViewById(R.id.tvEnfermedad);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            tvTratamiento = itemView.findViewById(R.id.tvTratamiento);
        }
    }
}
