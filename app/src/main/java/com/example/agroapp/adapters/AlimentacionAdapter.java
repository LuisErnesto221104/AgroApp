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
import com.example.agroapp.models.Alimentacion;
import com.example.agroapp.models.Animal;
import java.util.List;

public class AlimentacionAdapter extends RecyclerView.Adapter<AlimentacionAdapter.AlimentacionViewHolder> {
    
    private Context context;
    private List<Alimentacion> alimentacionList;
    private AnimalDAO animalDAO;
    private OnAlimentacionClickListener listener;
    
    public interface OnAlimentacionClickListener {
        void onAlimentacionClick(Alimentacion alimentacion);
    }
    
    public AlimentacionAdapter(Context context, List<Alimentacion> alimentacionList, 
                              AnimalDAO animalDAO, OnAlimentacionClickListener listener) {
        this.context = context;
        this.alimentacionList = alimentacionList;
        this.animalDAO = animalDAO;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public AlimentacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_alimentacion, parent, false);
        return new AlimentacionViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull AlimentacionViewHolder holder, int position) {
        Alimentacion alimentacion = alimentacionList.get(position);
        
        Animal animal = animalDAO.obtenerAnimalPorId(alimentacion.getAnimalId());
        String nombreAnimal = animal != null ? animal.getNumeroArete() : "Animal no encontrado";
        
        holder.tvAnimal.setText("Animal: " + nombreAnimal);
        holder.tvTipoAlimento.setText("Tipo: " + alimentacion.getTipoAlimento());
        holder.tvCantidad.setText("Cantidad: " + alimentacion.getCantidad() + " " + alimentacion.getUnidad());
        holder.tvFecha.setText("Fecha: " + alimentacion.getFecha());
        
        holder.cardView.setOnClickListener(v -> listener.onAlimentacionClick(alimentacion));
    }
    
    @Override
    public int getItemCount() {
        return alimentacionList.size();
    }
    
    static class AlimentacionViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvAnimal, tvTipoAlimento, tvCantidad, tvFecha;
        
        public AlimentacionViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            tvAnimal = itemView.findViewById(R.id.tvAnimal);
            tvTipoAlimento = itemView.findViewById(R.id.tvTipoAlimento);
            tvCantidad = itemView.findViewById(R.id.tvCantidad);
            tvFecha = itemView.findViewById(R.id.tvFecha);
        }
    }
}
