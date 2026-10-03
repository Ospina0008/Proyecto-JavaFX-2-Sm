# ⛏ Torneo Minecraft — Sistema de Inscripción (JavaFX)

Aplicación de **escritorio** (no web) tipo CRUD para inscribir jugadores a un torneo, con una interfaz temática inspirada en *Minecraft*. Construida con **JavaFX 21 + Maven** y empaquetada como **Fat JAR ejecutable**.

---

## ✨ Características

| Acción | Descripción |
|---|---|
| **ENVIAR** (verde creeper) | Crea un jugador nuevo con Nombre y Correo. |
| **RECORDAR** (oro) | Carga la fila seleccionada en los campos de texto (también con doble clic). |
| **ACTUALIZAR** (diamante) | Guarda los cambios del jugador recordado. Solo se activa tras RECORDAR. |
| **ELIMINAR** (rojo redstone) | Borra el jugador seleccionado, previa confirmación (también con `SUPR`). |
| **LIMPIAR** | Vacía el formulario y vuelve al modo "Nueva inscripción" (también con `ESC`). |

**UX**
- La ventana **nunca se recarga, parpadea ni cierra**: el `TableView` se actualiza dinámicamente mediante `ObservableList` y propiedades JavaFX.
- Validación en vivo: campos obligatorios, formato de correo y correos duplicados (el campo con error se resalta en rojo).
- Indicador de modo (*Nueva inscripción* / *Editando jugador #N*), barra de estado con mensajes y contador de jugadores.
- Búsqueda instantánea por nombre o correo y ordenamiento por columnas.
- Botones que se habilitan/deshabilitan según el contexto (evita errores del usuario).

**Restricciones cumplidas**
- 🧠 **Sin persistencia**: los datos viven solo en RAM en un `ArrayList<Jugador>` (se pierden al cerrar).
- 🎨 **Sin estilo por defecto**: tema CSS propio (obsidiana `#2b2b2b`, esquinas cuadradas, biseles tipo bloque).
- 🚀 **Ejecutable real**: clase `Launcher` separada + `maven-shade-plugin`.

---

## 📁 Estructura del proyecto

```
torneo-minecraft/
├── pom.xml
├── README.md
└── src/
    └── main/
        ├── java/                         ← SOLO código fuente Java
        │   └── com/torneo/
        │       ├── Launcher.java         ← Main-Class del JAR (no extiende Application)
        │       ├── Main.java             ← Application de JavaFX
        │       ├── model/
        │       │   └── Jugador.java      ← Modelo
        │       └── controller/
        │           └── TorneoController.java
        └── resources/                    ← Recursos no compilables (se copian al classpath)
            └── com/torneo/
                ├── view/
                │   └── torneo.fxml       ← Vista (enlaza el CSS con stylesheets="@../css/minecraft.css")
                └── css/
                    └── minecraft.css     ← Tema visual
```

---

## 🔧 Requisitos

- **JDK 17 o superior** (`java -version`)
- **Apache Maven 3.8+** (`mvn -v`)

> No es necesario instalar JavaFX: Maven descarga las librerías (incluidos los binarios nativos de tu sistema operativo).

---

## ▶️ Compilar y ejecutar

```bash
# 1. Compilar
mvn clean compile

# 2. Ejecutar en modo desarrollo
mvn javafx:run

# 3. Generar el ejecutable (Fat JAR)
mvn clean package

# 4. Ejecutar el JAR generado
java -jar target/torneo-minecraft-1.0.0-ejecutable.jar
```

En Windows también puedes hacer **doble clic** sobre `torneo-minecraft-1.0.0-ejecutable.jar` si `.jar` está asociado a Java.

> ⚠️ El Fat JAR incluye los binarios nativos de JavaFX **del sistema operativo donde se compiló**. Para distribuirlo en Windows, Linux o macOS, genéralo en cada plataforma.

---

## 🧩 ¿Por qué existe `Launcher.java`?

Si el `Main-Class` del manifiesto extiende `javafx.application.Application`, la JVM verifica que los módulos de JavaFX estén en el *module-path* y falla con:

```
Error: JavaFX runtime components are missing, and are required to run this application
```

`Launcher` es una clase normal que solo llama a `Main.main(args)`, lo que permite cargar JavaFX desde el *classpath* del Fat JAR.

---

## ⌨️ Atajos de teclado

| Tecla | Acción |
|---|---|
| `ENTER` (en Correo) | Enviar, o Actualizar si hay un jugador recordado |
| `ESC` | Limpiar formulario |
| Doble clic en fila | Recordar |
| `SUPR` (en tabla) | Eliminar |
