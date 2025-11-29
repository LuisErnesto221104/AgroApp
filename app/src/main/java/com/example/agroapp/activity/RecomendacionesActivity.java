package com.example.agroapp.activity;

import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.agroapp.R;

public class RecomendacionesActivity extends BaseActivity {
    
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
        String htmlContent = 
            "<h2 style='color:#4CAF50; margin-bottom:8px;'>🐄 GANADO BOVINO DE CARNE</h2>" +
            "<p><b>• Forraje:</b> 2-3% del peso corporal diario<br/>" +
            "<b>• Concentrado:</b> 0.5-1% del peso corporal<br/>" +
            "<b>• Agua:</b> 30-50 litros por día<br/>" +
            "<b>• Sal mineral:</b> 30-50g por día</p>" +
            
            "<hr style='border:1px solid #E0E0E0; margin:12px 0;'/>" +
            
            "<h2 style='color:#2196F3; margin-bottom:8px;'>🥛 GANADO LECHERO</h2>" +
            "<p><b>• Forraje verde:</b> 40-50 kg por día<br/>" +
            "<b>• Concentrado:</b> 1 kg por cada 2.5 litros de leche<br/>" +
            "<b>• Agua:</b> 60-80 litros por día<br/>" +
            "<b>• Sal mineral:</b> 50-80g por día</p>" +
            
            "<hr style='border:1px solid #E0E0E0; margin:12px 0;'/>" +
            
            "<h2 style='color:#FF9800; margin-bottom:8px;'>🍼 TERNEROS (0-6 MESES)</h2>" +
            "<p><b>• Calostro:</b> Primeras 6 horas de vida<br/>" +
            "<b>• Leche:</b> 4-6 litros diarios<br/>" +
            "<b>• Concentrado iniciador:</b> A partir del día 7<br/>" +
            "<b>• Forraje:</b> Introducir gradualmente</p>" +
            
            "<hr style='border:1px solid #E0E0E0; margin:12px 0;'/>" +
            
            "<h2 style='color:#E91E63; margin-bottom:8px;'>💊 MANEJO SANITARIO</h2>" +
            "<p><b>• Desparasitación:</b> Cada 3-4 meses<br/>" +
            "<b>• Vacunas:</b> Según calendario oficial<br/>" +
            "<b>• Vitaminas:</b> Aplicar cada 2-3 meses<br/>" +
            "<b>• Revisión veterinaria:</b> Cada 6 meses</p>" +
            
            "<hr style='border:1px solid #E0E0E0; margin:12px 0;'/>" +
            
            "<h2 style='color:#009688; margin-bottom:8px;'>🌡️ CONDICIONES AMBIENTALES</h2>" +
            "<p><b>• Sombra:</b> Adecuada en época de calor<br/>" +
            "<b>• Agua:</b> Limpia y fresca disponible<br/>" +
            "<b>• Espacio:</b> 10-15 m² por animal<br/>" +
            "<b>• Ventilación:</b> Apropiada en corrales</p>" +
            
            "<hr style='border:1px solid #E0E0E0; margin:12px 0;'/>" +
            
            "<h2 style='color:#9C27B0; margin-bottom:8px;'>📊 ALIMENTACIÓN POR ETAPA</h2>" +
            "<p><b>• Gestación:</b> Incrementar 20% nutrientes<br/>" +
            "<b>• Lactancia:</b> Máxima calidad nutritiva<br/>" +
            "<b>• Engorda:</b> Alto contenido energético<br/>" +
            "<b>• Mantenimiento:</b> Dieta balanceada básica</p>" +
            
            "<hr style='border:1px solid #E0E0E0; margin:12px 0;'/>" +
            
            "<p style='background-color:#FFF3CD; padding:12px; border-radius:4px; color:#856404; margin-top:12px;'>" +
            "<b>⚠️ NOTA IMPORTANTE:</b><br/>" +
            "Estas son recomendaciones generales. Consulte con un veterinario o zootecnista " +
            "para un plan nutricional específico según las características de su ganado y condiciones locales.</p>";
        
        tvRecomendaciones.setText(Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_LEGACY));
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
