package com.example.agroapp;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class RecomendacionesActivity extends AppCompatActivity {
    
    private TextView tvRecomendaciones;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recomendaciones);
        
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Recomendaciones Nutricionales");
        
        tvRecomendaciones = findViewById(R.id.tvRecomendaciones);
        
        cargarRecomendaciones();
    }
    
    private void cargarRecomendaciones() {
        StringBuilder recomendaciones = new StringBuilder();
        
        recomendaciones.append("RECOMENDACIONES NUTRICIONALES PARA GANADO\n\n");
        
        recomendaciones.append("═══════════════════════════════════\n\n");
        
        recomendaciones.append("1. GANADO BOVINO DE CARNE\n\n");
        recomendaciones.append("• Forraje: 2-3% del peso corporal diario\n");
        recomendaciones.append("• Concentrado: 0.5-1% del peso corporal\n");
        recomendaciones.append("• Agua: 30-50 litros por día\n");
        recomendaciones.append("• Sal mineral: 30-50g por día\n\n");
        
        recomendaciones.append("2. GANADO LECHERO\n\n");
        recomendaciones.append("• Forraje verde: 40-50 kg por día\n");
        recomendaciones.append("• Concentrado: 1 kg por cada 2.5 litros de leche\n");
        recomendaciones.append("• Agua: 60-80 litros por día\n");
        recomendaciones.append("• Sal mineral: 50-80g por día\n\n");
        
        recomendaciones.append("3. TERNEROS (0-6 MESES)\n\n");
        recomendaciones.append("• Calostro: Primeras 6 horas de vida\n");
        recomendaciones.append("• Leche: 4-6 litros diarios\n");
        recomendaciones.append("• Concentrado iniciador: A partir del día 7\n");
        recomendaciones.append("• Forraje: Introducir gradualmente\n\n");
        
        recomendaciones.append("4. MANEJO SANITARIO\n\n");
        recomendaciones.append("• Desparasitación: Cada 3-4 meses\n");
        recomendaciones.append("• Vacunas: Según calendario oficial\n");
        recomendaciones.append("• Vitaminas: Aplicar cada 2-3 meses\n");
        recomendaciones.append("• Revisión veterinaria: Cada 6 meses\n\n");
        
        recomendaciones.append("5. CONDICIONES AMBIENTALES\n\n");
        recomendaciones.append("• Sombra adecuada en época de calor\n");
        recomendaciones.append("• Agua limpia y fresca disponible\n");
        recomendaciones.append("• Espacio mínimo: 10-15 m² por animal\n");
        recomendaciones.append("• Ventilación apropiada en corrales\n\n");
        
        recomendaciones.append("6. ALIMENTACIÓN POR ETAPA\n\n");
        recomendaciones.append("• Gestación: Incrementar 20% nutrientes\n");
        recomendaciones.append("• Lactancia: Máxima calidad nutritiva\n");
        recomendaciones.append("• Engorda: Alto contenido energético\n");
        recomendaciones.append("• Mantenimiento: Dieta balanceada básica\n\n");
        
        recomendaciones.append("═══════════════════════════════════\n\n");
        
        recomendaciones.append("NOTA: Estas son recomendaciones generales. ");
        recomendaciones.append("Consulte con un veterinario o zootecnista para ");
        recomendaciones.append("un plan nutricional específico según las características ");
        recomendaciones.append("de su ganado y condiciones locales.");
        
        tvRecomendaciones.setText(recomendaciones.toString());
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
