package co.vaultpost.clientes.web;

import co.vaultpost.clientes.dao.ClienteDAO;
import co.vaultpost.clientes.modelo.Cliente;
import co.vaultpost.clientes.util.ConexionBD;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Controlador web del módulo de clientes de Vaultpost.
 *
 * GET  /clientes                  → lista (filtro opcional ?q=)
 * GET  /clientes?accion=nuevo     → formulario vacío
 * GET  /clientes?accion=editar&id → formulario con los datos del cliente
 * POST /clientes  accion=guardar  → inserta o actualiza
 * POST /clientes  accion=eliminar → elimina
 */
@WebServlet(name = "ClienteServlet", urlPatterns = "/clientes")
public class ClienteServlet extends HttpServlet {

    private static final String VISTA_LISTA = "/WEB-INF/vistas/clientes.jsp";
    private static final String VISTA_FORMULARIO = "/WEB-INF/vistas/formulario-cliente.jsp";

    private ClienteDAO clienteDao;

    @Override
    public void init() {
        clienteDao = new ClienteDAO(ConexionBD.agenciaActual());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String accion = valor(req.getParameter("accion"));
        try {
            switch (accion) {
                case "nuevo" -> mostrarFormulario(req, resp, new Cliente());
                case "editar" -> {
                    Optional<Cliente> cliente = clienteDao.buscarPorId(valor(req.getParameter("id")));
                    if (cliente.isPresent()) {
                        mostrarFormulario(req, resp, cliente.get());
                    } else {
                        redirigir(req, resp, "El cliente no existe", true);
                    }
                }
                default -> {
                    String filtro = valor(req.getParameter("q"));
                    req.setAttribute("filtro", filtro);
                    req.setAttribute("clientes", clienteDao.listar(filtro));
                    req.getRequestDispatcher(VISTA_LISTA).forward(req, resp);
                }
            }
        } catch (SQLException e) {
            throw new ServletException("Error consultando clientes", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String accion = valor(req.getParameter("accion"));
        try {
            if ("eliminar".equals(accion)) {
                boolean eliminado = clienteDao.eliminar(valor(req.getParameter("id")));
                redirigir(req, resp, eliminado ? "Cliente eliminado" : "El cliente no existe", !eliminado);
                return;
            }
            Cliente cliente = new Cliente();
            cliente.setId(valor(req.getParameter("id")));
            cliente.setNombre(req.getParameter("nombre"));
            cliente.setLogoUrl(req.getParameter("logoUrl"));
            try {
                if (cliente.getId().isEmpty()) {
                    clienteDao.insertar(cliente);
                    redirigir(req, resp, "Cliente creado", false);
                } else {
                    clienteDao.actualizar(cliente);
                    redirigir(req, resp, "Cliente actualizado", false);
                }
            } catch (IllegalArgumentException | ClienteDAO.ClienteDuplicadoException e) {
                // Se vuelve a mostrar el formulario con los datos escritos y el error
                req.setAttribute("error", e.getMessage());
                mostrarFormulario(req, resp, cliente);
            }
        } catch (SQLException e) {
            throw new ServletException("Error guardando el cliente", e);
        }
    }

    private void mostrarFormulario(HttpServletRequest req, HttpServletResponse resp, Cliente cliente)
            throws ServletException, IOException {
        req.setAttribute("cliente", cliente);
        req.getRequestDispatcher(VISTA_FORMULARIO).forward(req, resp);
    }

    /** Patrón Post/Redirect/Get: evita reenviar el formulario al recargar la página. */
    private void redirigir(HttpServletRequest req, HttpServletResponse resp, String mensaje, boolean error)
            throws IOException {
        String parametro = error ? "error" : "ok";
        resp.sendRedirect(req.getContextPath() + "/clientes?" + parametro + "="
                + URLEncoder.encode(mensaje, StandardCharsets.UTF_8));
    }

    private static String valor(String parametro) {
        return parametro == null ? "" : parametro.trim();
    }
}
