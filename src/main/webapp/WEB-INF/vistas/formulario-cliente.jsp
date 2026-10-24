<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="co.vaultpost.clientes.modelo.Cliente" %>
<%@ include file="/WEB-INF/vistas/escape.jspf" %>
<%
    Cliente cliente = (Cliente) request.getAttribute("cliente");
    boolean esNuevo = cliente.getId() == null || cliente.getId().isEmpty();
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title><%= esNuevo ? "Nuevo cliente" : "Editar cliente" %> · Vaultpost</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/vaultpost.css">
</head>
<body>
<header class="barra"><span class="logo">OM</span><strong>Orbital Media</strong><span class="suave">· Vaultpost</span></header>
<main class="contenido estrecho">
  <a class="volver" href="${pageContext.request.contextPath}/clientes">← Volver a clientes</a>
  <section class="tarjeta formulario">
    <h1><%= esNuevo ? "Nuevo cliente" : "Editar cliente" %></h1>

    <% if (request.getAttribute("error") != null) { %>
      <p class="aviso error" role="alert"><%= esc((String) request.getAttribute("error")) %></p>
    <% } %>

    <form method="post" action="${pageContext.request.contextPath}/clientes" novalidate>
      <input type="hidden" name="accion" value="guardar">
      <input type="hidden" name="id" value="<%= esNuevo ? "" : cliente.getId() %>">

      <label for="nombre">Nombre *</label>
      <input id="nombre" name="nombre" required minlength="2" maxlength="120"
             placeholder="Nombre del cliente" value="<%= esc(cliente.getNombre()) %>">

      <label for="logoUrl">Logo URL (opcional)</label>
      <input id="logoUrl" name="logoUrl" type="url" placeholder="https://..." value="<%= esc(cliente.getLogoUrl()) %>">

      <div class="botones">
        <a class="boton secundario" href="${pageContext.request.contextPath}/clientes">Cancelar</a>
        <button class="boton" type="submit"><%= esNuevo ? "Crear" : "Actualizar" %></button>
      </div>
    </form>
  </section>
</main>
</body>
</html>
