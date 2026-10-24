<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, co.vaultpost.clientes.modelo.Cliente, java.time.format.DateTimeFormatter" %>
<%@ include file="/WEB-INF/vistas/escape.jspf" %>
<%
    // Datos que envía ClienteServlet
    List<Cliente> clientes = (List<Cliente>) request.getAttribute("clientes");
    DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Clientes · Vaultpost</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/vaultpost.css">
</head>
<body>
<header class="barra"><span class="logo">OM</span><strong>Orbital Media</strong><span class="suave">· Vaultpost</span></header>
<main class="contenido">
  <div class="titulo">
    <div><h1>Clientes</h1><p class="suave">Administrá los clientes de tu agencia</p></div>
    <a class="boton" href="${pageContext.request.contextPath}/clientes?accion=nuevo">+ Nuevo cliente</a>
  </div>

  <% if (request.getParameter("ok") != null) { %>
    <p class="aviso exito" role="status"><%= esc(request.getParameter("ok")) %></p>
  <% } else if (request.getParameter("error") != null) { %>
    <p class="aviso error" role="alert"><%= esc(request.getParameter("error")) %></p>
  <% } %>

  <form method="get" action="${pageContext.request.contextPath}/clientes" class="buscador">
    <label for="q" class="oculto">Buscar cliente</label>
    <input id="q" name="q" type="search" placeholder="Buscar cliente..." value="<%= esc((String) request.getAttribute("filtro")) %>">
    <button class="boton secundario" type="submit">Buscar</button>
  </form>

  <section class="tarjeta">
    <table>
      <thead><tr><th>Cliente</th><th>Cuentas</th><th>Creado</th><th class="derecha">Acciones</th></tr></thead>
      <tbody>
      <% if (clientes.isEmpty()) { %>
        <tr><td colspan="4" class="vacio">No hay clientes que coincidan con los filtros</td></tr>
      <% } %>
      <% for (Cliente c : clientes) { %>
        <tr>
          <td><strong><%= esc(c.getNombre()) %></strong></td>
          <td><%= c.getTotalCuentas() %></td>
          <td class="suave"><%= c.getFechaCreacion() == null ? "" : c.getFechaCreacion().format(formato) %></td>
          <td class="derecha"><div class="acciones">
            <a href="${pageContext.request.contextPath}/clientes?accion=editar&id=<%= c.getId() %>">Editar</a>
            <form method="post" action="${pageContext.request.contextPath}/clientes"
                  onsubmit="return confirm('¿Eliminar este cliente y todas sus cuentas y publicaciones?');">
              <input type="hidden" name="accion" value="eliminar">
              <input type="hidden" name="id" value="<%= c.getId() %>">
              <button type="submit" class="enlace peligro">Eliminar</button>
            </form>
          </div></td>
        </tr>
      <% } %>
      </tbody>
    </table>
  </section>
  <p class="suave">Total: <%= clientes.size() %> cliente(s)</p>
</main>
</body>
</html>
