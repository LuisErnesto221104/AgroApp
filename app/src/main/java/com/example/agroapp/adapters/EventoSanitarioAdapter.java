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
import com.example.agroapp.dao.AnimalDAO;
import com.example.agroapp.models.Animal;
import com.example.agroapp.models.EventoSanitario;
import java.util.List;

public class EventoSanitarioAdapter extends RecyclerView.Adapter<EventoSanitarioAdapter.EventoViewHolder> {
    
    private Context context;
    private List<EventoSanitario> eventosList;
    private AnimalDAO animalDAO;
    private OnEventoClickListener listener;
    
    public interface OnEventoClickListener {
        void onEventoClick(EventoSanitario evento);
    }
    
    public EventoSanitarioAdapter(Context context, List<EventoSanitario> eventosList, 
                                 AnimalDAO animalDAO, OnEventoClickListener listener) {
        this.context = context;
        this.eventosList = eventosList;
        this.animalDAO = animalDAO;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public EventoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_evento_sanitario, parent, false);
        return new EventoViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull EventoViewHolder holder, int position) {
        EventoSanitario evento = eventosList.get(position);
        
        Animal animal = animalDAO.obtenerAnimalPorId(evento.getAnimalId());
        String nombreAnimal = animal != null ? animal.getNumeroArete() : "Animal no encontrado";
        
        holder.tvAnimal.setText("Animal: " + nombreAnimal);
        holder.tvTipo.setText("Tipo: " + evento.getTipo());
        holder.tvFecha.setText("Fecha: " + evento.getFechaProgramada());
        holder.tvEstado.setText("Estado: " + evento.getEstado());
        holder.tvDescripcion.setText(evento.getDescripcion() != null ? evento.getDescripcion() : "Sin descripción");
        
        holder.cardView.setOnClickListener(v -> listener.onEventoClick(evento));
    }
    
    @Override
    public int getItemCount() {
        return eventosList.size();
    }
    
    static class EventoViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvAnimal, tvTipo, tvFecha, tvEstado, tvDescripcion;
        
        public EventoViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            tvAnimal = itemView.findViewById(R.id.tvAnimal);
            tvTipo = itemView.findViewById(R.id.tvTipo);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
        }
    }
}
