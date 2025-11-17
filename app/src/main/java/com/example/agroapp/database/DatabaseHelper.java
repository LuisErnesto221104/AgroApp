package com.example.agroapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    
    private static final String DATABASE_NAME = "AgroApp.db";
    private static final int DATABASE_VERSION = 1;
    
    // Tabla Usuarios
    public static final String TABLE_USUARIOS = "usuarios";
    public static final String COL_USUARIO_ID = "id";
    public static final String COL_USUARIO_USERNAME = "username";
    public static final String COL_USUARIO_PASSWORD = "password";
    public static final String COL_USUARIO_NOMBRE = "nombre";
    
    // Tabla Animales
    public static final String TABLE_ANIMALES = "animales";
    public static final String COL_ANIMAL_ID = "id";
    public static final String COL_ANIMAL_ARETE = "numero_arete";
    public static final String COL_ANIMAL_NOMBRE = "nombre";
    public static final String COL_ANIMAL_RAZA = "raza";
    public static final String COL_ANIMAL_SEXO = "sexo";
    public static final String COL_ANIMAL_FECHA_NACIMIENTO = "fecha_nacimiento";
    public static final String COL_ANIMAL_FECHA_INGRESO = "fecha_ingreso";
    public static final String COL_ANIMAL_FECHA_SALIDA = "fecha_salida";
    public static final String COL_ANIMAL_PRECIO_COMPRA = "precio_compra";
    public static final String COL_ANIMAL_PRECIO_VENTA = "precio_venta";
    public static final String COL_ANIMAL_FOTO = "foto";
    public static final String COL_ANIMAL_ESTADO = "estado";
    public static final String COL_ANIMAL_OBSERVACIONES = "observaciones";
    
    // Tabla Calendario Sanitario
    public static final String TABLE_CALENDARIO_SANITARIO = "calendario_sanitario";
    public static final String COL_CALENDARIO_ID = "id";
    public static final String COL_CALENDARIO_ANIMAL_ID = "animal_id";
    public static final String COL_CALENDARIO_TIPO = "tipo";
    public static final String COL_CALENDARIO_FECHA_PROGRAMADA = "fecha_programada";
    public static final String COL_CALENDARIO_FECHA_REALIZADA = "fecha_realizada";
    public static final String COL_CALENDARIO_DESCRIPCION = "descripcion";
    public static final String COL_CALENDARIO_RECORDATORIO = "recordatorio";
    public static final String COL_CALENDARIO_ESTADO = "estado";
    
    // Tabla Historial Clínico
    public static final String TABLE_HISTORIAL_CLINICO = "historial_clinico";
    public static final String COL_HISTORIAL_ID = "id";
    public static final String COL_HISTORIAL_ANIMAL_ID = "animal_id";
    public static final String COL_HISTORIAL_FECHA = "fecha";
    public static final String COL_HISTORIAL_ENFERMEDAD = "enfermedad";
    public static final String COL_HISTORIAL_SINTOMAS = "sintomas";
    public static final String COL_HISTORIAL_TRATAMIENTO = "tratamiento";
    public static final String COL_HISTORIAL_ESTADO = "estado";
    public static final String COL_HISTORIAL_OBSERVACIONES = "observaciones";
    
    // Tabla Gastos
    public static final String TABLE_GASTOS = "gastos";
    public static final String COL_GASTO_ID = "id";
    public static final String COL_GASTO_ANIMAL_ID = "animal_id";
    public static final String COL_GASTO_TIPO = "tipo";
    public static final String COL_GASTO_CONCEPTO = "concepto";
    public static final String COL_GASTO_MONTO = "monto";
    public static final String COL_GASTO_FECHA = "fecha";
    public static final String COL_GASTO_OBSERVACIONES = "observaciones";
    
    // Tabla Alimentación
    public static final String TABLE_ALIMENTACION = "alimentacion";
    public static final String COL_ALIMENTACION_ID = "id";
    public static final String COL_ALIMENTACION_ANIMAL_ID = "animal_id";
    public static final String COL_ALIMENTACION_TIPO_ALIMENTO = "tipo_alimento";
    public static final String COL_ALIMENTACION_CANTIDAD = "cantidad";
    public static final String COL_ALIMENTACION_UNIDAD = "unidad";
    public static final String COL_ALIMENTACION_FECHA = "fecha";
    public static final String COL_ALIMENTACION_OBSERVACIONES = "observaciones";
    
    private static DatabaseHelper instance;
    
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }
    
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crear tabla Usuarios
        String createUsuarios = "CREATE TABLE " + TABLE_USUARIOS + " (" +
                COL_USUARIO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USUARIO_USERNAME + " TEXT UNIQUE NOT NULL, " +
                COL_USUARIO_PASSWORD + " TEXT NOT NULL, " +
                COL_USUARIO_NOMBRE + " TEXT NOT NULL)";
        db.execSQL(createUsuarios);
        
        // Crear tabla Animales
        String createAnimales = "CREATE TABLE " + TABLE_ANIMALES + " (" +
                COL_ANIMAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_ANIMAL_ARETE + " TEXT UNIQUE NOT NULL, " +
                COL_ANIMAL_NOMBRE + " TEXT, " +
                COL_ANIMAL_RAZA + " TEXT, " +
                COL_ANIMAL_SEXO + " TEXT, " +
                COL_ANIMAL_FECHA_NACIMIENTO + " TEXT, " +
                COL_ANIMAL_FECHA_INGRESO + " TEXT, " +
                COL_ANIMAL_FECHA_SALIDA + " TEXT, " +
                COL_ANIMAL_PRECIO_COMPRA + " REAL, " +
                COL_ANIMAL_PRECIO_VENTA + " REAL, " +
                COL_ANIMAL_FOTO + " TEXT, " +
                COL_ANIMAL_ESTADO + " TEXT, " +
                COL_ANIMAL_OBSERVACIONES + " TEXT)";
        db.execSQL(createAnimales);
        
        // Crear tabla Calendario Sanitario
        String createCalendario = "CREATE TABLE " + TABLE_CALENDARIO_SANITARIO + " (" +
                COL_CALENDARIO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_CALENDARIO_ANIMAL_ID + " INTEGER, " +
                COL_CALENDARIO_TIPO + " TEXT, " +
                COL_CALENDARIO_FECHA_PROGRAMADA + " TEXT, " +
                COL_CALENDARIO_FECHA_REALIZADA + " TEXT, " +
                COL_CALENDARIO_DESCRIPCION + " TEXT, " +
                COL_CALENDARIO_RECORDATORIO + " INTEGER, " +
                COL_CALENDARIO_ESTADO + " TEXT, " +
                "FOREIGN KEY(" + COL_CALENDARIO_ANIMAL_ID + ") REFERENCES " + 
                TABLE_ANIMALES + "(" + COL_ANIMAL_ID + ") ON DELETE CASCADE)";
        db.execSQL(createCalendario);
        
        // Crear tabla Historial Clínico
        String createHistorial = "CREATE TABLE " + TABLE_HISTORIAL_CLINICO + " (" +
                COL_HISTORIAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_HISTORIAL_ANIMAL_ID + " INTEGER, " +
                COL_HISTORIAL_FECHA + " TEXT, " +
                COL_HISTORIAL_ENFERMEDAD + " TEXT, " +
                COL_HISTORIAL_SINTOMAS + " TEXT, " +
                COL_HISTORIAL_TRATAMIENTO + " TEXT, " +
                COL_HISTORIAL_ESTADO + " TEXT, " +
                COL_HISTORIAL_OBSERVACIONES + " TEXT, " +
                "FOREIGN KEY(" + COL_HISTORIAL_ANIMAL_ID + ") REFERENCES " + 
                TABLE_ANIMALES + "(" + COL_ANIMAL_ID + ") ON DELETE CASCADE)";
        db.execSQL(createHistorial);
        
        // Crear tabla Gastos
        String createGastos = "CREATE TABLE " + TABLE_GASTOS + " (" +
                COL_GASTO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_GASTO_ANIMAL_ID + " INTEGER, " +
                COL_GASTO_TIPO + " TEXT, " +
                COL_GASTO_CONCEPTO + " TEXT, " +
                COL_GASTO_MONTO + " REAL, " +
                COL_GASTO_FECHA + " TEXT, " +
                COL_GASTO_OBSERVACIONES + " TEXT, " +
                "FOREIGN KEY(" + COL_GASTO_ANIMAL_ID + ") REFERENCES " + 
                TABLE_ANIMALES + "(" + COL_ANIMAL_ID + ") ON DELETE CASCADE)";
        db.execSQL(createGastos);
        
        // Crear tabla Alimentación
        String createAlimentacion = "CREATE TABLE " + TABLE_ALIMENTACION + " (" +
                COL_ALIMENTACION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_ALIMENTACION_ANIMAL_ID + " INTEGER, " +
                COL_ALIMENTACION_TIPO_ALIMENTO + " TEXT, " +
                COL_ALIMENTACION_CANTIDAD + " REAL, " +
                COL_ALIMENTACION_UNIDAD + " TEXT, " +
                COL_ALIMENTACION_FECHA + " TEXT, " +
                COL_ALIMENTACION_OBSERVACIONES + " TEXT, " +
                "FOREIGN KEY(" + COL_ALIMENTACION_ANIMAL_ID + ") REFERENCES " + 
                TABLE_ANIMALES + "(" + COL_ANIMAL_ID + ") ON DELETE CASCADE)";
        db.execSQL(createAlimentacion);
        
        // Insertar usuario por defecto
        db.execSQL("INSERT INTO " + TABLE_USUARIOS + " (" + 
                COL_USUARIO_USERNAME + ", " + COL_USUARIO_PASSWORD + ", " + COL_USUARIO_NOMBRE + 
                ") VALUES ('admin', 'admin123', 'Administrador')");
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALIMENTACION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GASTOS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORIAL_CLINICO);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CALENDARIO_SANITARIO);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ANIMALES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USUARIOS);
        onCreate(db);
    }
    
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }
}
