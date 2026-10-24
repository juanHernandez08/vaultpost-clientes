package co.vaultpost.clientes.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Crea conexiones JDBC a la base de datos de Vaultpost.
 * Los datos de conexión se leen de db.properties (o de variables de entorno),
 * para no escribir usuario ni contraseña dentro del código.
 */
public final class ConexionBD {

    private static final Properties CONFIG = cargarConfiguracion();

    static {
        // En un servidor web (Tomcat) el driver se debe registrar explícitamente,
        // porque DriverManager no ve las librerías de WEB-INF/lib
        try {
            Class.forName(CONFIG.getProperty("db.driver", "org.mariadb.jdbc.Driver"));
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("No se encontró el driver JDBC en el classpath", e);
        }
    }

    private ConexionBD() {
        // Clase de utilidad: no se instancia
    }

    private static Properties cargarConfiguracion() {
        Properties props = new Properties();
        try (InputStream in = ConexionBD.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer db.properties", e);
        }
        // Las variables de entorno tienen prioridad sobre el archivo
        sobrescribirDesdeEntorno(props, "db.url", "VAULTPOST_DB_URL");
        sobrescribirDesdeEntorno(props, "db.user", "VAULTPOST_DB_USER");
        sobrescribirDesdeEntorno(props, "db.password", "VAULTPOST_DB_PASSWORD");
        return props;
    }

    private static void sobrescribirDesdeEntorno(Properties props, String clave, String variable) {
        String valor = System.getenv(variable);
        if (valor != null && !valor.isBlank()) {
            props.setProperty(clave, valor);
        }
    }

    /**
     * Abre una nueva conexión. Quien la pide debe cerrarla (try-with-resources).
     */
    public static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(
                CONFIG.getProperty("db.url"),
                CONFIG.getProperty("db.user"),
                CONFIG.getProperty("db.password", ""));
    }

    /** Identificador de la agencia con la que trabaja el módulo (multi-tenant). */
    public static String agenciaActual() {
        return CONFIG.getProperty("app.company.id");
    }
}
