<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%--
  "Consultar movimientos" view. Rendered by MovimientosController (GET /movimientos).
  Request attributes:
    cuenta    -> queried account (Empleado: typed value kept in the form)
    errores   -> field -> message (blank account)
    error     -> server or connection message, shown as is
    historial -> HistorialCuenta (cuenta + movimientos) after a successful query
  Empleado: search form (GET, read-only). Cliente: only their own account, no form.
  Every dynamic value is escaped (c:out / fn:escapeXml).
--%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="activeNav" value="movimientos"/>
<c:set var="esEmpleado" value="${sessionScope.tipoUsuario eq 'Empleado'}"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Consultar movimientos | EurekaBank</title>
    <link rel="stylesheet" href="${ctx}/css/eurekabank.css">
    <link rel="stylesheet" href="${ctx}/css/menu.css">
    <link rel="stylesheet" href="${ctx}/css/operacion.css">
</head>
<body>
<%@ include file="partials/header.jspf" %>

<div class="app-body">
    <%@ include file="partials/sidebar.jspf" %>

    <main class="content">
        <div class="content__title">
            <h1 class="text-heading-xl">Consultar movimientos</h1>
            <p class="text-body">Ver saldo e historial de una cuenta</p>
        </div>

        <c:if test="${esEmpleado}">
            <form class="op-card" method="get" action="${ctx}/movimientos">
                <div class="field">
                    <label class="field__label" for="cuenta">Número de cuenta</label>
                    <input class="field__input${not empty errores.cuenta ? ' field__input--error' : ''}"
                           type="text" id="cuenta" name="cuenta" maxlength="30"
                           value="${fn:escapeXml(cuenta)}" placeholder="Ingrese el número de cuenta"
                           autocomplete="off" required
                           <c:if test="${not empty errores.cuenta}">aria-invalid="true" aria-describedby="cuenta-error"</c:if>>
                    <c:if test="${not empty errores.cuenta}">
                        <span id="cuenta-error" class="field__message field__message--error"><c:out value="${errores.cuenta}"/></span>
                    </c:if>
                </div>
                <div class="op-actions">
                    <a class="btn btn--secondary" href="${ctx}/menu">Cancelar</a>
                    <button class="btn btn--primary" type="submit">Consultar</button>
                </div>
            </form>
        </c:if>

        <c:if test="${not empty error}">
            <p class="alert alert--error" role="alert"><c:out value="${error}"/></p>
        </c:if>

        <c:if test="${not empty historial}">
            <section class="op-card history" aria-labelledby="cuenta-title">
                <h2 id="cuenta-title" class="receipt__title">Cuenta</h2>
                <dl class="receipt">
                    <div class="receipt__row"><dt>Número</dt><dd><c:out value="${historial.cuenta.numero}"/></dd></div>
                    <div class="receipt__row"><dt>Titular</dt><dd><c:out value="${historial.cuenta.titular}"/></dd></div>
                    <div class="receipt__row"><dt>Moneda</dt><dd><c:out value="${historial.cuenta.moneda}"/></dd></div>
                    <div class="receipt__row"><dt>Saldo</dt><dd><c:out value="${historial.cuenta.saldoFormateado}"/></dd></div>
                    <div class="receipt__row"><dt>Estado</dt><dd><c:out value="${historial.cuenta.estado}"/></dd></div>
                </dl>

                <h2 class="receipt__title">Movimientos</h2>
                <c:choose>
                    <c:when test="${historial.sinMovimientos}">
                        <p class="alert alert--info" role="status">La cuenta no tiene movimientos</p>
                    </c:when>
                    <c:otherwise>
                        <div class="history__scroll">
                            <table class="history__table">
                                <thead>
                                <tr>
                                    <th scope="col">N.º</th>
                                    <th scope="col">Fecha</th>
                                    <th scope="col">Tipo</th>
                                    <th scope="col">Acción</th>
                                    <th scope="col" class="history__amount">Importe</th>
                                    <th scope="col">Cuenta referencia</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach var="m" items="${historial.movimientos}">
                                    <tr>
                                        <td><c:out value="${m.numero}"/></td>
                                        <td><c:out value="${m.fecha}"/></td>
                                        <td><c:out value="${m.tipo}"/></td>
                                        <td><span class="history__action${m.ingreso ? ' history__action--in' : ' history__action--out'}"><c:out value="${m.accion}"/></span></td>
                                        <td class="history__amount"><c:out value="${m.importeFormateado}"/></td>
                                        <td><c:out value="${m.referencia}"/></td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:otherwise>
                </c:choose>

                <div class="op-actions">
                    <a class="btn btn--primary" href="${ctx}/menu">Volver al menú</a>
                </div>
            </section>
        </c:if>
    </main>
</div>
</body>
</html>
