# Diagramas de Casos de Uso - AgroApp

## 📋 Descripción

Este directorio contiene los diagramas de casos de uso del sistema AgroApp en formato PlantUML, siguiendo los estándares UML 2.5.

## 📁 Estructura de Archivos

| Archivo | Descripción |
|---------|-------------|
| `CU-General-Sistema.puml` | Diagrama general con todos los actores y casos de uso del sistema |
| `CU-Autenticacion.puml` | CU-001: Autenticarse en el Sistema |
| `CU-Modulo-Gestion.puml` | CU-002: Gestionar Animales (CRUD) |
| `CU-Modulo-Sanitario.puml` | CU-003: Registrar Evento Sanitario y CU-004: Gestionar Historial Clínico |
| `CU-Modulo-Financiero.puml` | CU-005: Registrar Gastos e Inversiones |
| `CU-Modulo-Reportes.puml` | CU-006: Consultar Alimentación y CU-007: Generar Reportes |
| `CU-Notificaciones.puml` | CU-008: Gestionar Notificaciones |
| `CU-Relaciones-Dependencias.puml` | Diagrama detallado de relaciones y dependencias entre casos de uso |

## 🎯 Casos de Uso Documentados

### CU-001: Autenticarse en el Sistema
- Login de usuario
- Registro de nuevo usuario
- Gestión de sesión
- Timeout de seguridad (10 segundos)
- Reautenticación automática

### CU-002: Gestionar Animales (CRUD)
- Registrar animal con validación de arete (10 dígitos)
- Consultar animales con filtros por estado
- Actualizar información de animales
- Eliminar registros
- Captura de foto (cámara/galería)

### CU-003: Registrar Evento Sanitario
- Programar vacunaciones
- Programar desparasitaciones
- Programar vitaminas
- Marcar eventos como realizados
- Consultar eventos pendientes

### CU-004: Gestionar Historial Clínico
- Registrar enfermedades
- Registrar tratamientos
- Registrar síntomas
- Actualizar estado del tratamiento
- Consultar historial médico

### CU-005: Registrar Gastos e Inversiones
- Registrar gastos por categoría
- Asociar gastos a animales (opcional)
- Calcular inversión total por animal
- Calcular ganancias/pérdidas

### CU-006: Consultar Alimentación
- Registrar alimentación
- Controlar cantidades y tipos
- Historial por animal
- Múltiples unidades de medida

### CU-007: Generar Reportes
- Reporte general del hato
- Reporte individual por animal
- Reporte financiero
- Reporte sanitario
- Exportación a PDF

### CU-008: Gestionar Notificaciones
- Programar recordatorios
- Notificaciones push locales
- Badge de eventos próximos
- Recordatorio 1 día antes del evento

## 👥 Actores del Sistema

### Actor Principal
- **Productor Ganadero**: Usuario principal del sistema, pequeño productor de Michoacán

### Actores Secundarios
- **Usuario No Registrado**: Puede registrarse en el sistema

### Actores de Sistema
- **Base de Datos SQLite**: Persistencia local
- **Sistema de Notificaciones Android**: AlarmManager y NotificationManager
- **Sistema de Archivos Android**: Almacenamiento de PDFs
- **Cámara del Dispositivo**: Captura de fotos

## 🛠️ Cómo Visualizar los Diagramas

### Opción 1: PlantUML Online
1. Ir a [PlantUML Server](http://www.plantuml.com/plantuml/uml)
2. Copiar el contenido del archivo `.puml`
3. Pegar en el editor
4. Ver el diagrama generado

### Opción 2: Visual Studio Code
1. Instalar extensión "PlantUML"
2. Abrir archivo `.puml`
3. Usar `Alt+D` para preview

### Opción 3: IntelliJ IDEA / Android Studio
1. Instalar plugin "PlantUML Integration"
2. Abrir archivo `.puml`
3. Click derecho → "Show UML Diagram"

### Opción 4: Línea de comandos
```bash
# Instalar PlantUML
sudo apt-get install plantuml

# Generar imagen PNG
plantuml archivo.puml

# Generar imagen SVG
plantuml -tsvg archivo.puml
```

## 📊 Matriz de Dependencias

```
│ CU │ 001 │ 002 │ 003 │ 004 │ 005 │ 006 │ 007 │ 008 │
├────┼─────┼─────┼─────┼─────┼─────┼─────┼─────┼─────┤
│001 │  -  │     │     │     │     │     │     │     │
│002 │  I  │  -  │     │     │     │     │     │     │
│003 │  I  │  I  │  -  │     │     │     │     │     │
│004 │  I  │  I  │  E  │  -  │     │     │     │     │
│005 │  I  │  E  │     │     │  -  │     │     │     │
│006 │  I  │  I  │     │     │     │  -  │     │     │
│007 │  I  │  I  │     │     │  I  │     │  -  │     │
│008 │  I  │     │  E  │     │     │     │     │  -  │
└────┴─────┴─────┴─────┴─────┴─────┴─────┴─────┴─────┘

I = <<include>> (Dependencia obligatoria)
E = <<extend>>  (Extensión opcional)
```

## 🎨 Paleta de Colores

Los diagramas utilizan la paleta de colores de AgroApp:

| Color | Hex | Uso en Diagramas |
|-------|-----|------------------|
| Verde Bosque | `#41692a` | Actores, Autenticación |
| Tan | `#c78f52` | Bordes de paquetes |
| Beige Claro | `#faf4de` | Fondos de actores y casos de uso |
| Marrón Oscuro | `#6d3e14` | Bordes y flechas |
| Sage | `#98a287` | Notas |

## 📖 Estándares Aplicados

- **UML 2.5**: Notación estándar de la Object Management Group (OMG)
- **Relaciones `<<include>>`**: Dependencias obligatorias entre casos de uso
- **Relaciones `<<extend>>`**: Funcionalidades opcionales o condicionales
- **Estereotipos**: Clasificación de actores según su rol

## 📝 Notas

- Todos los casos de uso (excepto CU-001) requieren autenticación previa
- Los módulos sanitario, alimentación y financiero dependen del módulo de gestión de animales
- El módulo de reportes consolida información de múltiples módulos
- Las notificaciones están integradas con el calendario sanitario

---

**AgroApp** - Sistema de Gestión Ganadera para pequeños productores de Michoacán, México 🐄
