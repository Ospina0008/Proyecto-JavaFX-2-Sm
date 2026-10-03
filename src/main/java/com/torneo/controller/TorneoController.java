package com.torneo.controller;

import com.torneo.model.Jugador;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Controlador de la vista principal (torneo.fxml).
 * <p>
 * Almacenamiento: EXCLUSIVAMENTE en memoria RAM, en un {@link ArrayList}.
 * La ObservableList es solo un "envoltorio" sobre ese mismo ArrayList que
 * notifica al TableView de cada cambio, por lo que la tabla se actualiza
 * al instante sin recargar ni parpadear la ventana.
 */
public class TorneoController {

    private static final Pattern PATRON_CORREO =
            Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final int MAX_NOMBRE = 30;

    // ---------- Almacenamiento en RAM ----------
    private final List<Jugador> jugadores = new ArrayList<>();
    private final ObservableList<Jugador> jugadoresObs = FXCollections.observableList(jugadores);

    /** Jugador cargado con "Recordar" y pendiente de "Actualizar". */
    private final ObjectProperty<Jugador> enEdicion = new SimpleObjectProperty<>();

    // ---------- Componentes FXML ----------
    @FXML private TextField txtNombre;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtBuscar;

    @FXML private TableView<Jugador> tablaJugadores;
    @FXML private TableColumn<Jugador, Number> colId;
    @FXML private TableColumn<Jugador, String> colNombre;
    @FXML private TableColumn<Jugador, String> colCorreo;

    @FXML private Button btnEnviar;
    @FXML private Button btnActualizar;
    @FXML private Button btnRecordar;
    @FXML private Button btnEliminar;
    @FXML private Button btnLimpiar;

    @FXML private Label lblModo;
    @FXML private Label lblEstado;
    @FXML private Label lblContador;

    @FXML
    private void initialize() {
        configurarTabla();
        configurarEstadosDeBotones();
        configurarAtajos();
        actualizarContador();
        mostrarEstado("¡Bienvenido, aventurero! Inscribe a tu primer jugador.", false);
    }

    // =====================================================================
    //  Configuración
    // =====================================================================

    private void configurarTabla() {
        colId.setCellValueFactory(c -> c.getValue().idProperty());
        colNombre.setCellValueFactory(c -> c.getValue().nombreProperty());
        colCorreo.setCellValueFactory(c -> c.getValue().correoProperty());

        // Búsqueda en vivo + ordenamiento por columnas
        FilteredList<Jugador> filtrados = new FilteredList<>(jugadoresObs, j -> true);
        txtBuscar.textProperty().addListener((obs, viejo, texto) -> {
            String q = texto == null ? "" : texto.trim().toLowerCase();
            filtrados.setPredicate(j -> q.isEmpty()
                    || j.getNombre().toLowerCase().contains(q)
                    || j.getCorreo().toLowerCase().contains(q));
        });
        SortedList<Jugador> ordenados = new SortedList<>(filtrados);
        ordenados.comparatorProperty().bind(tablaJugadores.comparatorProperty());
        tablaJugadores.setItems(ordenados);

        Label vacio = new Label("No hay jugadores inscritos todavía.\nUsa el formulario para añadir el primero.");
        vacio.getStyleClass().add("tabla-vacia");
        tablaJugadores.setPlaceholder(vacio);

        // Doble clic en una fila = Recordar
        tablaJugadores.setRowFactory(tv -> {
            TableRow<Jugador> fila = new TableRow<>();
            fila.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2 && !fila.isEmpty()) {
                    onRecordar();
                }
            });
            return fila;
        });
    }

    private void configurarEstadosDeBotones() {
        var sinSeleccion = tablaJugadores.getSelectionModel().selectedItemProperty().isNull();
        btnRecordar.disableProperty().bind(sinSeleccion);
        btnEliminar.disableProperty().bind(sinSeleccion);
        btnActualizar.disableProperty().bind(enEdicion.isNull());

        enEdicion.addListener((obs, viejo, nuevo) -> {
            lblModo.getStyleClass().removeAll("modo-nuevo", "modo-edicion");
            if (nuevo == null) {
                lblModo.setText("MODO: NUEVA INSCRIPCIÓN");
                lblModo.getStyleClass().add("modo-nuevo");
            } else {
                lblModo.setText("MODO: EDITANDO JUGADOR #" + nuevo.getId());
                lblModo.getStyleClass().add("modo-edicion");
            }
        });

        // Al escribir, se quita el resaltado de error del campo
        txtNombre.textProperty().addListener((o, a, b) -> txtNombre.getStyleClass().remove("campo-error"));
        txtCorreo.textProperty().addListener((o, a, b) -> txtCorreo.getStyleClass().remove("campo-error"));
    }

    private void configurarAtajos() {
        // ENTER en el formulario: si hay jugador recordado actualiza, si no, envía
        txtNombre.setOnAction(e -> txtCorreo.requestFocus());
        txtCorreo.setOnAction(e -> {
            if (enEdicion.get() != null) onActualizar(); else onEnviar();
        });
        // ESC: limpiar formulario | SUPR en la tabla: eliminar
        txtNombre.addEventFilter(KeyEvent.KEY_PRESSED, e -> { if (e.getCode() == KeyCode.ESCAPE) onLimpiar(); });
        txtCorreo.addEventFilter(KeyEvent.KEY_PRESSED, e -> { if (e.getCode() == KeyCode.ESCAPE) onLimpiar(); });
        tablaJugadores.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.DELETE && !btnEliminar.isDisabled()) onEliminar();
        });
    }

    // =====================================================================
    //  Acciones CRUD
    // =====================================================================

    /** CREATE: inscribe un nuevo jugador. */
    @FXML
    private void onEnviar() {
        if (enEdicion.get() != null) {
            mostrarEstado("Estás editando a un jugador. Pulsa ACTUALIZAR o LIMPIAR.", true);
            return;
        }
        String nombre = txtNombre.getText().trim();
        String correo = txtCorreo.getText().trim();
        if (!validar(nombre, correo, null)) return;

        Jugador nuevo = new Jugador(nombre, correo);
        jugadoresObs.add(nuevo);              // se guarda en el ArrayList y la tabla se entera sola
        tablaJugadores.getSelectionModel().select(nuevo);
        tablaJugadores.scrollTo(nuevo);

        limpiarFormulario();
        actualizarContador();
        mostrarEstado("✔ " + nombre + " se unió al torneo.", false);
    }

    /** READ (al formulario): carga la fila seleccionada en los campos. */
    @FXML
    private void onRecordar() {
        Jugador sel = tablaJugadores.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarEstado("Selecciona un jugador de la tabla para recordarlo.", true);
            return;
        }
        enEdicion.set(sel);
        txtNombre.setText(sel.getNombre());
        txtCorreo.setText(sel.getCorreo());
        txtNombre.requestFocus();
        txtNombre.selectAll();
        mostrarEstado("Datos de " + sel.getNombre() + " cargados. Edita y pulsa ACTUALIZAR.", false);
    }

    /** UPDATE: aplica los cambios al jugador recordado. */
    @FXML
    private void onActualizar() {
        Jugador objetivo = enEdicion.get();
        if (objetivo == null) {
            mostrarEstado("Primero usa RECORDAR sobre un jugador.", true);
            return;
        }
        String nombre = txtNombre.getText().trim();
        String correo = txtCorreo.getText().trim();
        if (!validar(nombre, correo, objetivo)) return;

        // Las propiedades JavaFX notifican a la celda: la tabla se repinta sola
        objetivo.setNombre(nombre);
        objetivo.setCorreo(correo);

        tablaJugadores.getSelectionModel().select(objetivo);
        limpiarFormulario();
        mostrarEstado("✔ Jugador #" + objetivo.getId() + " actualizado.", false);
    }

    /** DELETE: elimina el jugador seleccionado (con confirmación). */
    @FXML
    private void onEliminar() {
        Jugador sel = tablaJugadores.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarEstado("Selecciona un jugador para eliminarlo.", true);
            return;
        }

        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.initOwner(tablaJugadores.getScene().getWindow());
        alerta.setTitle("Eliminar jugador");
        alerta.setHeaderText("¿Expulsar a " + sel.getNombre() + " del torneo?");
        alerta.setContentText("Esta acción no se puede deshacer.");
        alerta.getDialogPane().getStylesheets().addAll(tablaJugadores.getScene().getRoot().getStylesheets());
        alerta.getDialogPane().getStyleClass().add("dialogo-mc");

        Optional<ButtonType> r = alerta.showAndWait();
        if (r.isPresent() && r.get() == ButtonType.OK) {
            jugadoresObs.remove(sel);
            if (sel == enEdicion.get()) limpiarFormulario();
            actualizarContador();
            mostrarEstado("✖ " + sel.getNombre() + " fue eliminado del torneo.", false);
        }
    }

    @FXML
    private void onLimpiar() {
        limpiarFormulario();
        tablaJugadores.getSelectionModel().clearSelection();
        mostrarEstado("Formulario limpio. Listo para una nueva inscripción.", false);
    }

    // =====================================================================
    //  Utilidades
    // =====================================================================

    /**
     * Valida los campos. {@code actual} es el jugador en edición (para no
     * marcar su propio correo como duplicado) o null si es alta nueva.
     */
    private boolean validar(String nombre, String correo, Jugador actual) {
        if (nombre.isEmpty()) {
            return error(txtNombre, "El NOMBRE es obligatorio.");
        }
        if (nombre.length() > MAX_NOMBRE) {
            return error(txtNombre, "El NOMBRE no puede superar " + MAX_NOMBRE + " caracteres.");
        }
        if (correo.isEmpty()) {
            return error(txtCorreo, "El CORREO es obligatorio.");
        }
        if (!PATRON_CORREO.matcher(correo).matches()) {
            return error(txtCorreo, "El CORREO no tiene un formato válido (ej: steve@mc.com).");
        }
        boolean duplicado = jugadores.stream()
                .anyMatch(j -> j != actual && j.getCorreo().equalsIgnoreCase(correo));
        if (duplicado) {
            return error(txtCorreo, "Ese CORREO ya está inscrito en el torneo.");
        }
        return true;
    }

    private boolean error(TextField campo, String mensaje) {
        if (!campo.getStyleClass().contains("campo-error")) {
            campo.getStyleClass().add("campo-error");
        }
        campo.requestFocus();
        mostrarEstado("⚠ " + mensaje, true);
        return false;
    }

    private void limpiarFormulario() {
        txtNombre.clear();
        txtCorreo.clear();
        txtNombre.getStyleClass().remove("campo-error");
        txtCorreo.getStyleClass().remove("campo-error");
        enEdicion.set(null);
        txtNombre.requestFocus();
    }

    private void mostrarEstado(String mensaje, boolean esError) {
        lblEstado.setText(mensaje);
        lblEstado.getStyleClass().removeAll("estado-ok", "estado-error");
        lblEstado.getStyleClass().add(esError ? "estado-error" : "estado-ok");
    }

    private void actualizarContador() {
        lblContador.setText("JUGADORES: " + jugadores.size());
    }
}
