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
import com.example.agroapp.models.Animal;
import java.util.List;

public class AnimalAdapter extends RecyclerView.Adapter<AnimalAdapter.AnimalViewHolder> {
    
    private Context context;
    private List<Animal> animalesList;
    private OnAnimalClickListener listener;
    
    public interface OnAnimalClickListener {
        void onAnimalClick(Animal animal);
    }
    
    public AnimalAdapter(Context context, List<Animal> animalesList, OnAnimalClickListener listener) {
        this.context = context;
        this.animalesList = animalesList;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public AnimalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_animal, parent, false);
        return new AnimalViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull AnimalViewHolder holder, int position) {
        Animal animal = animalesList.get(position);
        
        holder.tvArete.setText("Arete: " + animal.getNumeroArete());
        holder.tvNombre.setText(animal.getNombre() != null && !animal.getNombre().isEmpty() ? animal.getNombre() : "Sin nombre");
        holder.tvRaza.setText("Raza: " + (animal.getRaza() != null ? animal.getRaza() : "-"));
        holder.tvEstado.setText("Estado: " + (animal.getEstado() != null ? animal.getEstado() : "-"));
        
        holder.cardView.setOnClickListener(v -> listener.onAnimalClick(animal));
    }
    
    @Override
    public int getItemCount() {
        return animalesList.size();
    }
    
    static class AnimalViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvArete, tvNombre, tvRaza, tvEstado;
        
        public AnimalViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            tvArete = itemView.findViewById(R.id.tvArete);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvRaza = itemView.findViewById(R.id.tvRaza);
            tvEstado = itemView.findViewById(R.id.tvEstado);
        }
    }
}
