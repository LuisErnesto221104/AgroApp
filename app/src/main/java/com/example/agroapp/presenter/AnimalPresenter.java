package com.example.agroapp.presenter;

import android.graphics.Bitmap;
import android.util.Base64;
import com.example.agroapp.dao.AnimalDAO;
import com.example.agroapp.models.Animal;
import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AnimalPresenter {
    private AnimalDAO animalDAO;
    private AnimalView view;
    private ExecutorService executorService;
    
    public interface AnimalView {
        void mostrarError(String mensaje);
        void mostrarExito(String mensaje);
        void cerrarActividad();
        void ejecutarEnUIThread(Runnable runnable);
    }
    
    public AnimalPresenter(AnimalDAO animalDAO, AnimalView view) {
        this.animalDAO = animalDAO;
        this.view = view;
        this.executorService = Executors.newSingleThreadExecutor();
    }
    
    public boolean validarArete(String arete) {
        if (arete == null || arete.trim().isEmpty()) {
            view.mostrarError("El número de arete es obligatorio");
            return false;
        }
        
        if (arete.length() != 10) {
            view.mostrarError("El número de arete debe tener exactamente 10 caracteres");
            return false;
        }
        
        if (!arete.matches("\\d{10}")) {
            view.mostrarError("El número de arete debe contener solo números (10 dígitos)");
            return false;
        }
        
        return true;
    }
    
    public boolean validarPrecio(double precio, String nombreCampo) {
        if (precio < 0) {
            view.mostrarError(nombreCampo + " debe ser 0 o positivo");
            return false;
        }
        return true;
    }
    
    public boolean validarFechasCoherentes(String fechaNacimiento, String fechaIngreso) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date fechaNac = sdf.parse(fechaNacimiento);
            Date fechaIng = sdf.parse(fechaIngreso);
            
            if (fechaNac != null && fechaIng != null && fechaNac.after(fechaIng)) {
                view.mostrarError("Fecha de nacimiento debe ser anterior a fecha de ingreso");
                return false;
            }
            return true;
        } catch (ParseException e) {
            view.mostrarError("Error en formato de fechas");
            return false;
        }
    }
    
    public String procesarImagen(Bitmap bitmap) {
        if (bitmap == null) return "";
        
        int maxSize = 800;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float ratio = Math.min((float) maxSize / width, (float) maxSize / height);
        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);
        
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] imageBytes = baos.toByteArray();
        
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }
    
    public void guardarAnimal(Animal animal, boolean modoEdicion) {
        executorService.execute(() -> {
            try {
                long resultado;
                if (modoEdicion) {
                    resultado = animalDAO.actualizarAnimal(animal);
                    if (resultado > 0) {
                        view.ejecutarEnUIThread(() -> {
                            view.mostrarExito("Animal actualizado correctamente");
                            view.cerrarActividad();
                        });
                    } else {
                        view.ejecutarEnUIThread(() -> 
                            view.mostrarError("Error al actualizar animal"));
                    }
                } else {
                    resultado = animalDAO.insertarAnimal(animal);
                    if (resultado == -1) {
                        view.ejecutarEnUIThread(() -> 
                            view.mostrarError("Error: El número de arete ya existe"));
                    } else {
                        view.ejecutarEnUIThread(() -> {
                            view.mostrarExito("Animal registrado correctamente");
                            view.cerrarActividad();
                        });
                    }
                }
            } catch (Exception e) {
                view.ejecutarEnUIThread(() -> 
                    view.mostrarError("Error al guardar: " + e.getMessage()));
            }
        });
    }
    
    public void cargarAnimal(int animalId, CargarAnimalCallback callback) {
        executorService.execute(() -> {
            Animal animal = animalDAO.obtenerAnimalPorId(animalId);
            view.ejecutarEnUIThread(() -> callback.onAnimalCargado(animal));
        });
    }
    
    /**
     * Carga un animal por su número de arete (identificador visible para el usuario)
     * @param arete Número de arete SINIGA
     * @param callback Callback con el animal cargado
     */
    public void cargarAnimalPorArete(String arete, CargarAnimalCallback callback) {
        executorService.execute(() -> {
            Animal animal = animalDAO.obtenerAnimalPorArete(arete);
            view.ejecutarEnUIThread(() -> callback.onAnimalCargado(animal));
        });
    }
    
    public interface CargarAnimalCallback {
        void onAnimalCargado(Animal animal);
    }
    
    public void destruir() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
