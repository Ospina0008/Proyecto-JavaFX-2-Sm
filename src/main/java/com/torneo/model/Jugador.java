package com.torneo.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Modelo de un jugador inscrito en el torneo.
 * <p>
 * Usa propiedades JavaFX: cuando se modifica un valor (Actualizar),
 * la celda del TableView se repinta sola, sin recargar la ventana.
 */
public class Jugador {

    private static int secuencia = 1;

    private final IntegerProperty id;
    private final StringProperty nombre;
    private final StringProperty correo;

    public Jugador(String nombre, String correo) {
        this.id = new SimpleIntegerProperty(secuencia++);
        this.nombre = new SimpleStringProperty(nombre);
        this.correo = new SimpleStringProperty(correo);
    }

    // --- ID (solo lectura) ---
    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }

    // --- Nombre ---
    public String getNombre() { return nombre.get(); }
    public void setNombre(String valor) { nombre.set(valor); }
    public StringProperty nombreProperty() { return nombre; }

    // --- Correo ---
    public String getCorreo() { return correo.get(); }
    public void setCorreo(String valor) { correo.set(valor); }
    public StringProperty correoProperty() { return correo; }

    @Override
    public String toString() {
        return "Jugador{id=" + getId() + ", nombre='" + getNombre() + "', correo='" + getCorreo() + "'}";
    }
}
