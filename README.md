# Vaultpost · Módulo de clientes (Java)

Módulo de gestión de clientes del proyecto **Vaultpost**, la plataforma SaaS con la que las agencias de marketing digital administran las redes sociales de sus clientes. Implementa inserción, consulta, actualización y eliminación (CRUD) sobre la tabla `clients` de la base de datos del proyecto.

- **AA2-EV01:** acceso a datos con **JDBC** (`ClienteDAO`) y aplicación de consola (`AppClientes`).
- **AA2-EV02:** interfaz web con **Servlets** (`ClienteServlet`, métodos GET y POST) y **JSP**.

## Estructura

```
vaultpost-clientes/
├── pom.xml
├── sql/                                   ← script de la base de datos y datos de prueba
└── src/main/
    ├── java/co/vaultpost/clientes/
    │   ├── modelo/Cliente.java            ← entidad
    │   ├── dao/ClienteDAO.java            ← CRUD con JDBC y PreparedStatement
    │   ├── util/ConexionBD.java           ← conexión a la base de datos
    │   ├── consola/AppClientes.java       ← menú de consola
    │   └── web/ClienteServlet.java        ← controlador web (GET / POST)
    ├── resources/db.properties            ← datos de conexión
    └── webapp/
        ├── css/vaultpost.css
        ├── index.jsp
        └── WEB-INF/
            ├── web.xml
            └── vistas/ clientes.jsp · formulario-cliente.jsp · error.jsp · escape.jspf
```

## Requisitos

- Java 17 o superior, Maven 3.9
- MySQL 8 o MariaDB 10.6+
- Apache Tomcat 10.1 (para la versión web)

## Puesta en marcha

```bash
# 1. Base de datos
mysql -u root -p < sql/vaultpost_mysql.sql
mysql -u root -p < sql/vaultpost_datos_prueba.sql
mysql -u root -p -e "CREATE USER 'vaultpost_app'@'localhost' IDENTIFIED BY 'vaultpost_app';
                     GRANT SELECT, INSERT, UPDATE, DELETE ON vaultpost.* TO 'vaultpost_app'@'localhost';"

# 2. Versión de consola (JDBC)
mvn compile exec:java

# 3. Versión web (Servlets + JSP)
mvn package
cp target/vaultpost-clientes.war $CATALINA_HOME/webapps/
# Abrir http://localhost:8080/vaultpost-clientes/clientes
```

Los datos de conexión se cambian en `src/main/resources/db.properties` o con las variables de entorno `VAULTPOST_DB_URL`, `VAULTPOST_DB_USER` y `VAULTPOST_DB_PASSWORD`.

## Estándares de codificación aplicados

| Elemento | Convención | Ejemplo |
|---|---|---|
| Paquetes | minúsculas, dominio invertido | `co.vaultpost.clientes.dao` |
| Clases | PascalCase, sustantivos | `ClienteDAO`, `ConexionBD` |
| Métodos | camelCase, verbos | `listar()`, `buscarPorId()` |
| Variables | camelCase descriptivo | `clienteDao`, `filtroNombre` |
| Constantes | MAYÚSCULAS_CON_GUION | `SQL_INSERTAR`, `VISTA_LISTA` |

## Seguridad

- Consultas con `PreparedStatement` (sin concatenar datos del usuario) para evitar inyección SQL.
- Todas las consultas filtran por `company_id`: una agencia solo ve sus propios clientes.
- Los datos mostrados en las JSP se escapan para evitar XSS.
- Patrón Post/Redirect/Get después de guardar o eliminar.

## Autores

Juan Esteban Hernández y Dayana Andrea Gómez · Análisis y Desarrollo de Software · SENA · Ficha 3336144
