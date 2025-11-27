package com.example.agroapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.example.agroapp.dao.AnimalDAO;
import com.example.agroapp.database.DatabaseHelper;
import com.example.agroapp.models.Animal;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class RegistroAnimalActivity extends BaseActivity {
    
    private EditText etArete, etPrecioCompra, etObservaciones;
    private android.widget.Button btnFechaNacimiento, btnFechaAdquisicion;
    private Spinner spinnerRaza, spinnerSexo, spinnerEstado;
    private ImageView ivFotoAnimal;
    private Button btnSeleccionarFoto, btnTomarFoto, btnGuardar, btnCancelar;
    private AnimalDAO animalDAO;
    private String modo;
    private int animalId;
    private String fotoBase64 = "";
    private Calendar calendar;
    private final String[] fechaNacimiento = {""};
    private final String[] fechaIngreso = {""};
    private Uri photoUri;
    private String currentPhotoPath;
    
    private static final int PICK_IMAGE = 1;
    private static final int TAKE_PHOTO = 2;
    private static final int CAMERA_PERMISSION_CODE = 100;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_animal);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        animalDAO = new AnimalDAO(dbHelper);
        calendar = Calendar.getInstance();
        
        inicializarVistas();
        configurarSpinners();
        
        modo = getIntent().getStringExtra("modo");
        if (modo == null) modo = "nuevo";
        animalId = getIntent().getIntExtra("animalId", -1);
        
        if (modo.equals("editar") && animalId != -1) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Editar Animal");
            }
            cargarDatosAnimal();
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Registrar Animal");
            }
        }
        
        configurarListeners();
    }
    
    private void inicializarVistas() {
        etArete = findViewById(R.id.etArete);
        btnFechaNacimiento = findViewById(R.id.btnFechaNacimiento);
        btnFechaAdquisicion = findViewById(R.id.btnFechaAdquisicion);
        etPrecioCompra = findViewById(R.id.etPrecioCompra);
        etObservaciones = findViewById(R.id.etObservaciones);
        spinnerRaza = findViewById(R.id.spinnerRaza);
        spinnerSexo = findViewById(R.id.spinnerSexo);
        spinnerEstado = findViewById(R.id.spinnerEstado);
        ivFotoAnimal = findViewById(R.id.ivFotoAnimal);
        btnSeleccionarFoto = findViewById(R.id.btnSeleccionarFoto);
        btnTomarFoto = findViewById(R.id.btnTomarFoto);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnCancelar = findViewById(R.id.btnCancelar);
    }
    
    private void configurarSpinners() {
        // Razas
        String[] razas = {"Holstein", "Jersey", "Angus", "Hereford", "Brahman", "Charolais", 
                         "Simmental", "Criollo", "Mestizo", "Otra"};
        ArrayAdapter<String> razaAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_dropdown_item, razas);
        spinnerRaza.setAdapter(razaAdapter);
        
        // Sexo
        String[] sexos = {"Macho", "Hembra"};
        ArrayAdapter<String> sexoAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_dropdown_item, sexos);
        spinnerSexo.setAdapter(sexoAdapter);
        
        // Estado
        String[] estados = {"Sano", "Enfermo", "Vendido", "Muerto"};
        ArrayAdapter<String> estadoAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_dropdown_item, estados);
        spinnerEstado.setAdapter(estadoAdapter);
    }
    
    private void configurarListeners() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        fechaNacimiento[0] = sdf.format(calendar.getTime());
        fechaIngreso[0] = sdf.format(calendar.getTime());
        btnFechaNacimiento.setText(fechaNacimiento[0]);
        btnFechaAdquisicion.setText(fechaIngreso[0]);
        
        btnFechaNacimiento.setOnClickListener(v -> mostrarDatePicker(true));
        btnFechaAdquisicion.setOnClickListener(v -> mostrarDatePicker(false));
        
        btnSeleccionarFoto.setOnClickListener(v -> seleccionarFoto());
        btnTomarFoto.setOnClickListener(v -> verificarPermisosCamara());
        btnGuardar.setOnClickListener(v -> guardarAnimal());
        btnCancelar.setOnClickListener(v -> finish());
    }
    
    private void mostrarDatePicker(boolean esFechaNacimiento) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                String fecha = sdf.format(calendar.getTime());
                if (esFechaNacimiento) {
                    fechaNacimiento[0] = fecha;
                    btnFechaNacimiento.setText(fecha);
                } else {
                    fechaIngreso[0] = fecha;
                    btnFechaAdquisicion.setText(fecha);
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }
    
    private void seleccionarFoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }
    
    private void verificarPermisosCamara() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                new String[]{android.Manifest.permission.CAMERA}, 
                CAMERA_PERMISSION_CODE);
        } else {
            tomarFoto();
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                          @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                tomarFoto();
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void tomarFoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            File photoFile = crearArchivoImagen();
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider",
                    photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, TAKE_PHOTO);
            }
        } catch (IOException ex) {
            Toast.makeText(this, "Error al crear archivo de imagen", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo abrir la cámara", Toast.LENGTH_SHORT).show();
        }
    }
    
    private File crearArchivoImagen() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "ANIMAL_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK) {
            Bitmap bitmap = null;
            
            try {
                if (requestCode == PICK_IMAGE && data != null) {
                    // Imagen desde galería
                    Uri imageUri = data.getData();
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                } else if (requestCode == TAKE_PHOTO) {
                    // Imagen desde cámara
                    bitmap = BitmapFactory.decodeFile(currentPhotoPath);
                }
                
                if (bitmap != null) {
                    // Redimensionar bitmap para no ocupar tanto espacio
                    int maxSize = 800;
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    float ratio = Math.min((float) maxSize / width, (float) maxSize / height);
                    int newWidth = Math.round(width * ratio);
                    int newHeight = Math.round(height * ratio);
                    Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
                    
                    // Convertir a Base64
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                    byte[] imageBytes = baos.toByteArray();
                    fotoBase64 = android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT);
                    
                    // Mostrar en ImageView
                    ivFotoAnimal.setImageBitmap(resizedBitmap);
                    
                    Toast.makeText(this, "Foto guardada correctamente", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al cargar la foto", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void guardarAnimal() {
        String arete = etArete.getText().toString().trim();
        String raza = spinnerRaza.getSelectedItem().toString();
        String sexo = spinnerSexo.getSelectedItem().toString();
        String fechaNac = fechaNacimiento[0];
        String fechaIng = fechaIngreso[0];
        double precioCompra = 0;
        try {
            precioCompra = Double.parseDouble(etPrecioCompra.getText().toString());
        } catch (NumberFormatException e) {
            precioCompra = 0;
        }
        String estado = spinnerEstado.getSelectedItem().toString();
        String observaciones = etObservaciones.getText().toString().trim();
        
        if (arete.isEmpty()) {
            Toast.makeText(this, "El número de arete es obligatorio", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (arete.length() != 10) {
            Toast.makeText(this, "El número de arete debe tener exactamente 10 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (!arete.matches("\\d{10}")) {
            Toast.makeText(this, "El número de arete debe contener solo números (10 dígitos)", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Animal animal = new Animal();
        animal.setNumeroArete(arete);
        animal.setNombre(arete); // Usar arete como nombre
        animal.setRaza(raza);
        animal.setSexo(sexo);
        animal.setFechaNacimiento(fechaNac);
        animal.setFechaIngreso(fechaIng);
        animal.setPrecioCompra(precioCompra);
        animal.setEstado(estado);
        animal.setObservaciones(observaciones);
        animal.setFoto(fotoBase64);
        
        if (modo.equals("editar")) {
            animal.setId(animalId);
            animalDAO.actualizarAnimal(animal);
            Toast.makeText(this, "Animal actualizado correctamente", Toast.LENGTH_SHORT).show();
        } else {
            animalDAO.insertarAnimal(animal);
            Toast.makeText(this, "Animal registrado correctamente", Toast.LENGTH_SHORT).show();
        }
        
        finish();
    }
    
    private void cargarDatosAnimal() {
        Animal animal = animalDAO.obtenerAnimalPorId(animalId);
        if (animal != null) {
            etArete.setText(animal.getNumeroArete());
            fechaNacimiento[0] = animal.getFechaNacimiento();
            fechaIngreso[0] = animal.getFechaIngreso();
            btnFechaNacimiento.setText(animal.getFechaNacimiento());
            btnFechaAdquisicion.setText(animal.getFechaIngreso());
            etPrecioCompra.setText(String.valueOf(animal.getPrecioCompra()));
            etObservaciones.setText(animal.getObservaciones());
            
            // Seleccionar valores en spinners
            setSpinnerValue(spinnerRaza, animal.getRaza());
            setSpinnerValue(spinnerSexo, animal.getSexo());
            setSpinnerValue(spinnerEstado, animal.getEstado());
            
            // Bloquear cambio de estado si el animal está vendido o muerto
            if (animal.getEstado() != null && 
                (animal.getEstado().equalsIgnoreCase("vendido") || animal.getEstado().equalsIgnoreCase("muerto"))) {
                spinnerEstado.setEnabled(false);
                spinnerEstado.setAlpha(0.5f);
            }
            
            // Cargar foto si existe
            fotoBase64 = animal.getFoto() != null ? animal.getFoto() : "";
            if (fotoBase64 != null && !fotoBase64.isEmpty()) {
                try {
                    byte[] decodedString = android.util.Base64.decode(fotoBase64, android.util.Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    ivFotoAnimal.setImageBitmap(decodedByte);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private void setSpinnerValue(Spinner spinner, String value) {
        if (value != null) {
            ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
            int position = adapter.getPosition(value);
            if (position >= 0) {
                spinner.setSelection(position);
            }
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
