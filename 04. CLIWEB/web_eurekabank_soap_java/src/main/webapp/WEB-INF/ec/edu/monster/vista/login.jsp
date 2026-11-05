<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%--
  Login view. Rendered by LoginController (GET/POST /login).
  Request attributes set by the controller:
    tipo     -> selected role: empleado (default) | cliente
    error    -> message to show after a failed login
    username -> value typed by the user: username (Empleado) or account number (Cliente);
                the password is never sent back
  Design: Figma "EurekaBank - Plantillas UI", frames "Web · Login" / "Web · Login (error)".
--%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Iniciar sesión | EurekaBank</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/eurekabank.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/login.css">
</head>
<body>
<main class="login-page">
    <section class="login-card" aria-labelledby="login-title">

        <div class="login-card__top">
            <span class="logo"><span class="logo__mark">EB</span>EurekaBank</span>
            <%-- Service badge: this client consumes the SOAP service written in Java --%>
            <span class="badge" title="Servicio consumido: SOAP / Java">
                <span class="badge__label">Servicio</span>
                <span class="badge__protocol--soap">SOAP</span>
                <span class="badge__platform--java">Java</span>
            </span>
        </div>

        <div class="login-card__title">
            <h1 id="login-title" class="text-heading-xl">Iniciar sesión</h1>
            <p class="text-body">Ingrese sus credenciales para continuar</p>
        </div>

        <c:if test="${not empty error}">
            <p class="alert alert--error" role="alert"><c:out value="${error}"/></p>
        </c:if>

        <form class="login-form" method="post" action="${pageContext.request.contextPath}/login">
            <%@ include file="partials/csrf.jspf" %>
            <c:set var="esCliente" value="${tipo eq 'cliente'}"/>
            <fieldset class="field login-role">
                <legend class="field__label">Tipo de acceso</legend>
                <div class="login-role__options">
                    <label class="login-role__option">
                        <input type="radio" name="tipo" value="empleado"${esCliente ? '' : ' checked'}>
                        <span>Empleado</span>
                    </label>
                    <label class="login-role__option">
                        <input type="radio" name="tipo" value="cliente"${esCliente ? ' checked' : ''}>
                        <span>Cliente</span>
                    </label>
                </div>
            </fieldset>

            <div class="field">
                <label id="username-label" class="field__label" for="username">${esCliente ? 'Número de cuenta' : 'Usuario'}</label>
                <input class="field__input" type="text" id="username" name="username"
                       value="${fn:escapeXml(username)}"
                       placeholder="${esCliente ? 'Ingrese su número de cuenta' : 'Ingrese su usuario'}"
                       autocomplete="username" required autofocus>
            </div>

            <div class="field">
                <label class="field__label" for="password">Clave</label>
                <input class="field__input" type="password" id="password" name="password"
                       placeholder="Ingrese su clave" autocomplete="current-password" required>
            </div>

            <button class="btn btn--primary btn--block" type="submit">Ingresar</button>
        </form>

        <%-- Optional enhancement: the labels are also right without JavaScript after a POST --%>
        <script>
            document.querySelectorAll('input[name="tipo"]').forEach(function (radio) {
                radio.addEventListener('change', function () {
                    var cliente = radio.value === 'cliente';
                    document.getElementById('username-label').textContent = cliente ? 'Número de cuenta' : 'Usuario';
                    document.getElementById('username').placeholder =
                        cliente ? 'Ingrese su número de cuenta' : 'Ingrese su usuario';
                });
            });
        </script>

    </section>
</main>
</body>
</html>
