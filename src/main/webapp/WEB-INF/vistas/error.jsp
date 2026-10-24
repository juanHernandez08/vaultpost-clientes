<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="es">
<head><meta charset="UTF-8"><title>Error · Vaultpost</title><link rel="stylesheet" href="${pageContext.request.contextPath}/css/vaultpost.css"></head>
<body>
<main class="contenido estrecho">
  <section class="tarjeta formulario">
    <h1>Algo salió mal</h1>
    <p class="aviso error">No pudimos completar la operación. Verificá la conexión con la base de datos e intentá de nuevo.</p>
    <a class="boton" href="${pageContext.request.contextPath}/clientes">Volver a clientes</a>
  </section>
</main>
</body>
</html>
