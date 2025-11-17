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
import com.example.agroapp.models.Gasto;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class GastoAdapter extends RecyclerView.Adapter<GastoAdapter.GastoViewHolder> {
    
    private Context context;
    private List<Gasto> gastosList;
    private AnimalDAO animalDAO;
    private OnGastoClickListener listener;
    
    public interface OnGastoClickListener {
        void onGastoClick(Gasto gasto);
    }
    
    public GastoAdapter(Context context, List<Gasto> gastosList, 
                       AnimalDAO animalDAO, OnGastoClickListener listener) {
        this.context = context;
        this.gastosList = gastosList;
        this.animalDAO = animalDAO;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public GastoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_gasto, parent, false);
        return new GastoViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull GastoViewHolder holder, int position) {
        Gasto gasto = gastosList.get(position);
        
        // Evitar consulta de BD en el hilo principal - Cargar animal de forma asíncrona o desde cache
        // Por ahora mostramos el ID, lo ideal sería tener el nombre en el modelo Gasto o usar un Map de cache
        String nombreAnimal = "ID: " + gasto.getAnimalId();
        
        // Verificar si hay datos en cache (esto debería mejorarse con un Map<Integer, String> de animalId -> nombre)
        if (animalDAO != null) {
            try {
                Animal animal = animalDAO.obtenerAnimalPorId(gasto.getAnimalId());
                if (animal != null) {
                    nombreAnimal = animal.getNumeroArete();
                }
            } catch (Exception e) {
                // Mantener nombreAnimal por defecto si hay error
            }
        }
        
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale.Builder().setLanguage("es").setRegion("MX").build());
        
        holder.tvAnimal.setText("Animal: " + nombreAnimal);
        holder.tvTipo.setText("Tipo: " + gasto.getTipo());
        holder.tvConcepto.setText("Concepto: " + gasto.getConcepto());
        holder.tvMonto.setText("Monto: " + formatter.format(gasto.getMonto()));
        holder.tvFecha.setText("Fecha: " + gasto.getFecha());
        
        holder.cardView.setOnClickListener(v -> listener.onGastoClick(gasto));
    }
    
    @Override
    public int getItemCount() {
        return gastosList.size();
    }
    
    static class GastoViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvAnimal, tvTipo, tvConcepto, tvMonto, tvFecha;
        
        public GastoViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            tvAnimal = itemView.findViewById(R.id.tvAnimal);
            tvTipo = itemView.findViewById(R.id.tvTipo);
            tvConcepto = itemView.findViewById(R.id.tvConcepto);
            tvMonto = itemView.findViewById(R.id.tvMonto);
            tvFecha = itemView.findViewById(R.id.tvFecha);
        }
    }
}
