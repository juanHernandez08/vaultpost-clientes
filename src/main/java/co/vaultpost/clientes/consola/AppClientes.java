package co.vaultpost.clientes.consola;

import co.vaultpost.clientes.dao.ClienteDAO;
import co.vaultpost.clientes.modelo.Cliente;
import co.vaultpost.clientes.util.ConexionBD;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

/**
 * Aplicación de consola del módulo de clientes de Vaultpost.
 * Permite insertar, consultar, actualizar y eliminar clientes usando JDBC.
 */
public class AppClientes {

    private final ClienteDAO clienteDao = new ClienteDAO(ConexionBD.agenciaActual());
    private final Scanner entrada = new Scanner(System.in);

    public static void main(String[] args) {
        new AppClientes().iniciar();
    }

    private void iniciar() {
        System.out.println("=== Vaultpost · Módulo de clientes ===");
        int opcion;
        do {
            mostrarMenu();
            opcion = leerEntero("Opción: ");
            try {
                switch (opcion) {
                    case 1 -> listarClientes();
                    case 2 -> crearCliente();
                    case 3 -> actualizarCliente();
                    case 4 -> eliminarCliente();
                    case 0 -> System.out.println("Hasta pronto.");
                    default -> System.out.println("Opción no válida.");
                }
            } catch (IllegalArgumentException | ClienteDAO.ClienteDuplicadoException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (SQLException e) {
                System.out.println("Error de base de datos: " + e.getMessage());
            }
        } while (opcion != 0);
    }

    private void mostrarMenu() {
        System.out.println();
        System.out.println("1. Consultar clientes");
        System.out.println("2. Insertar cliente");
        System.out.println("3. Actualizar cliente");
        System.out.println("4. Eliminar cliente");
        System.out.println("0. Salir");
    }

    private void listarClientes() throws SQLException {
        String filtro = leerTexto("Filtrar por nombre (Enter para todos): ");
        List<Cliente> clientes = clienteDao.listar(filtro);
        if (clientes.isEmpty()) {
            System.out.println("No hay clientes que coincidan.");
            return;
        }
        System.out.printf("%-38s %-25s %s%n", "ID", "NOMBRE", "CUENTAS");
        clientes.forEach(System.out::println);
    }

    private void crearCliente() throws SQLException {
        Cliente nuevo = new Cliente(null, leerTexto("Nombre: "), leerTexto("URL del logo (opcional): "));
        String id = clienteDao.insertar(nuevo);
        System.out.println("Cliente creado con id " + id);
    }

    private void actualizarCliente() throws SQLException {
        String id = leerTexto("Id del cliente a actualizar: ");
        Cliente cliente = clienteDao.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe un cliente con ese id"));
        String nombre = leerTexto("Nuevo nombre [" + cliente.getNombre() + "]: ");
        if (!nombre.isBlank()) {
            cliente.setNombre(nombre);
        }
        cliente.setLogoUrl(leerTexto("Nueva URL del logo (vacío para quitarla): "));
        System.out.println(clienteDao.actualizar(cliente) ? "Cliente actualizado." : "No se actualizó.");
    }

    private void eliminarCliente() throws SQLException {
        String id = leerTexto("Id del cliente a eliminar: ");
        String confirmacion = leerTexto("Se eliminarán también sus cuentas y publicaciones. ¿Confirmar? (s/n): ");
        if (confirmacion.equalsIgnoreCase("s")) {
            System.out.println(clienteDao.eliminar(id) ? "Cliente eliminado." : "No existe un cliente con ese id.");
        }
    }

    private String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return entrada.hasNextLine() ? entrada.nextLine() : "";
    }

    private int leerEntero(String mensaje) {
        String valor = leerTexto(mensaje).trim();
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            return valor.isEmpty() ? 0 : -1;
        }
    }
}
