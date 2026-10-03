package com.torneo;

/**
 * Punto de entrada del JAR ejecutable.
 * <p>
 * Esta clase NO extiende {@link javafx.application.Application}. Si el Main-Class
 * del manifiesto fuera una subclase de Application, la JVM exigiría los módulos
 * de JavaFX en el module-path y lanzaría:
 * "Error: JavaFX runtime components are missing".
 * Al delegar desde una clase normal, JavaFX se carga desde el classpath del Fat JAR.
 */
public class Launcher {
    public static void main(String[] args) {
        Main.main(args);
    }
}
