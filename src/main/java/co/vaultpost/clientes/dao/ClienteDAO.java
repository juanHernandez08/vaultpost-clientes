package co.vaultpost.clientes.dao;

import co.vaultpost.clientes.modelo.Cliente;
import co.vaultpost.clientes.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Acceso a datos de la tabla clients mediante JDBC.
 * Todas las consultas filtran por company_id para que una agencia
 * nunca vea ni modifique clientes de otra (aislamiento multi-tenant).
 * Se usan PreparedStatement para evitar inyección SQL.
 */
public class ClienteDAO {

    private static final String SQL_LISTAR =
            "SELECT c.id, c.company_id, c.name, c.logo_url, c.created_at, COUNT(sa.id) AS total_cuentas "
          + "FROM clients c LEFT JOIN social_accounts sa ON sa.client_id = c.id "
          + "WHERE c.company_id = ? AND c.name LIKE ? "
          + "GROUP BY c.id, c.company_id, c.name, c.logo_url, c.created_at ORDER BY c.name";
    private static final String SQL_BUSCAR_POR_ID =
            "SELECT c.id, c.company_id, c.name, c.logo_url, c.created_at, "
          + "(SELECT COUNT(*) FROM social_accounts sa WHERE sa.client_id = c.id) AS total_cuentas "
          + "FROM clients c WHERE c.id = ? AND c.company_id = ?";
    private static final String SQL_INSERTAR =
            "INSERT INTO clients (id, company_id, name, logo_url) VALUES (?, ?, ?, ?)";
    private static final String SQL_ACTUALIZAR =
            "UPDATE clients SET name = ?, logo_url = ? WHERE id = ? AND company_id = ?";
    private static final String SQL_ELIMINAR =
            "DELETE FROM clients WHERE id = ? AND company_id = ?";

    private final String companyId;

    public ClienteDAO(String companyId) {
        this.companyId = companyId;
    }

    /** Consulta: lista los clientes de la agencia; filtro opcional por nombre. */
    public List<Cliente> listar(String filtroNombre) throws SQLException {
        List<Cliente> clientes = new ArrayList<>();
        String patron = "%" + (filtroNombre == null ? "" : filtroNombre.trim()) + "%";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(SQL_LISTAR)) {
            ps.setString(1, companyId);
            ps.setString(2, patron);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    clientes.add(mapear(rs));
                }
            }
        }
        return clientes;
    }

    /** Consulta: busca un cliente por su identificador. */
    public Optional<Cliente> buscarPorId(String id) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(SQL_BUSCAR_POR_ID)) {
            ps.setString(1, id);
            ps.setString(2, companyId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    /** Inserción: crea un cliente nuevo y devuelve el id generado. */
    public String insertar(Cliente cliente) throws SQLException {
        validar(cliente);
        String id = UUID.randomUUID().toString();
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(SQL_INSERTAR)) {
            ps.setString(1, id);
            ps.setString(2, companyId);
            ps.setString(3, cliente.getNombre().trim());
            ps.setString(4, vacioANulo(cliente.getLogoUrl()));
            ps.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new ClienteDuplicadoException(cliente.getNombre());
        }
        cliente.setId(id);
        return id;
    }

    /** Actualización: cambia el nombre y el logo. Devuelve true si el cliente existía. */
    public boolean actualizar(Cliente cliente) throws SQLException {
        validar(cliente);
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(SQL_ACTUALIZAR)) {
            ps.setString(1, cliente.getNombre().trim());
            ps.setString(2, vacioANulo(cliente.getLogoUrl()));
            ps.setString(3, cliente.getId());
            ps.setString(4, companyId);
            return ps.executeUpdate() == 1;
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new ClienteDuplicadoException(cliente.getNombre());
        }
    }

    /**
     * Eliminación: borra el cliente. Por la regla ON DELETE CASCADE de la base de datos
     * también se eliminan sus cuentas sociales y publicaciones.
     */
    public boolean eliminar(String id) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(SQL_ELIMINAR)) {
            ps.setString(1, id);
            ps.setString(2, companyId);
            return ps.executeUpdate() == 1;
        }
    }

    private void validar(Cliente cliente) {
        String nombre = cliente.getNombre() == null ? "" : cliente.getNombre().trim();
        if (nombre.length() < 2 || nombre.length() > 120) {
            throw new IllegalArgumentException("El nombre del cliente debe tener entre 2 y 120 caracteres");
        }
        String logo = cliente.getLogoUrl();
        if (logo != null && !logo.isBlank() && !logo.matches("^https?://.+")) {
            throw new IllegalArgumentException("La URL del logo debe empezar por http:// o https://");
        }
    }

    private static String vacioANulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private static Cliente mapear(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setId(rs.getString("id"));
        c.setCompanyId(rs.getString("company_id"));
        c.setNombre(rs.getString("name"));
        c.setLogoUrl(rs.getString("logo_url"));
        Timestamp creado = rs.getTimestamp("created_at");
        c.setFechaCreacion(creado == null ? null : creado.toLocalDateTime());
        c.setTotalCuentas(rs.getInt("total_cuentas"));
        return c;
    }

    /** Se lanza cuando la agencia ya tiene un cliente con el mismo nombre. */
    public static class ClienteDuplicadoException extends RuntimeException {
        public ClienteDuplicadoException(String nombre) {
            super("Ya existe un cliente llamado \"" + nombre + "\" en esta agencia");
        }
    }
}
