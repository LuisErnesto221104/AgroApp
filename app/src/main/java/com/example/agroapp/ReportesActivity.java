package com.example.agroapp;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.agroapp.dao.AnimalDAO;
import com.example.agroapp.dao.GastoDAO;
import com.example.agroapp.database.DatabaseHelper;
import com.example.agroapp.models.Animal;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportesActivity extends AppCompatActivity {
    
    private TextView tvTotalAnimales, tvAnimalesSanos, tvAnimalesEnfermos, 
                     tvAnimalesVendidos, tvTotalGastos, tvPromedioGastos;
    private Button btnGenerarPDF;
    private AnimalDAO animalDAO;
    private GastoDAO gastoDAO;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reportes);
        
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Reportes y Estadísticas");
        
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        animalDAO = new AnimalDAO(dbHelper);
        gastoDAO = new GastoDAO(dbHelper);
        
        inicializarVistas();
        cargarEstadisticas();
        
        btnGenerarPDF.setOnClickListener(v -> generarReportePDF());
    }
    
    private void inicializarVistas() {
        tvTotalAnimales = findViewById(R.id.tvTotalAnimales);
        tvAnimalesSanos = findViewById(R.id.tvAnimalesSanos);
        tvAnimalesEnfermos = findViewById(R.id.tvAnimalesEnfermos);
        tvAnimalesVendidos = findViewById(R.id.tvAnimalesVendidos);
        tvTotalGastos = findViewById(R.id.tvTotalGastos);
        tvPromedioGastos = findViewById(R.id.tvPromedioGastos);
        btnGenerarPDF = findViewById(R.id.btnGenerarPDF);
    }
    
    private void cargarEstadisticas() {
        List<Animal> todosLosAnimales = animalDAO.obtenerTodosLosAnimales();
        List<Animal> animalesSanos = animalDAO.obtenerAnimalesPorEstado("Sano");
        List<Animal> animalesEnfermos = animalDAO.obtenerAnimalesPorEstado("Enfermo");
        List<Animal> animalesVendidos = animalDAO.obtenerAnimalesPorEstado("Vendido");
        
        double totalGastos = gastoDAO.obtenerTotalGastos();
        double promedioGasto = todosLosAnimales.size() > 0 ? totalGastos / todosLosAnimales.size() : 0;
        
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale.Builder().setLanguage("es").setRegion("MX").build());
        
        tvTotalAnimales.setText("Total de Animales: " + todosLosAnimales.size());
        tvAnimalesSanos.setText("Animales Sanos: " + animalesSanos.size());
        tvAnimalesEnfermos.setText("Animales Enfermos: " + animalesEnfermos.size());
        tvAnimalesVendidos.setText("Animales Vendidos: " + animalesVendidos.size());
        tvTotalGastos.setText("Total Gastos: " + formatter.format(totalGastos));
        tvPromedioGastos.setText("Promedio por Animal: " + formatter.format(promedioGasto));
    }
    
    private void generarReportePDF() {
        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(16);
        
        int y = 50;
        page.getCanvas().drawText("Reporte AgroApp", 50, y, paint);
        
        paint.setTextSize(12);
        y += 40;
        page.getCanvas().drawText("Fecha: " + new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()), 50, y, paint);
        
        y += 30;
        List<Animal> todosLosAnimales = animalDAO.obtenerTodosLosAnimales();
        page.getCanvas().drawText("Total de Animales: " + todosLosAnimales.size(), 50, y, paint);
        
        y += 20;
        List<Animal> animalesSanos = animalDAO.obtenerAnimalesPorEstado("Sano");
        page.getCanvas().drawText("Animales Sanos: " + animalesSanos.size(), 50, y, paint);
        
        y += 20;
        List<Animal> animalesEnfermos = animalDAO.obtenerAnimalesPorEstado("Enfermo");
        page.getCanvas().drawText("Animales Enfermos: " + animalesEnfermos.size(), 50, y, paint);
        
        y += 20;
        List<Animal> animalesVendidos = animalDAO.obtenerAnimalesPorEstado("Vendido");
        page.getCanvas().drawText("Animales Vendidos: " + animalesVendidos.size(), 50, y, paint);
        
        y += 30;
        double totalGastos = gastoDAO.obtenerTotalGastos();
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale.Builder().setLanguage("es").setRegion("MX").build());
        page.getCanvas().drawText("Total Gastos: " + formatter.format(totalGastos), 50, y, paint);
        
        pdfDocument.finishPage(page);
        
        try {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            String fileName = "AgroApp_Reporte_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".pdf";
            File file = new File(downloadsDir, fileName);
            
            FileOutputStream fos = new FileOutputStream(file);
            pdfDocument.writeTo(fos);
            pdfDocument.close();
            fos.close();
            
            Toast.makeText(this, "PDF guardado en: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al generar PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
