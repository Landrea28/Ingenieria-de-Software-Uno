// Estado de carga: mientras el servidor consulta la API, muestra una barra de progreso y bloquea
// clics repetidos. Se activa con data-cargando en formularios (al enviar) y enlaces (al hacer clic).
(function () {
  'use strict';

  function mostrarCarga() {
    document.body.classList.add('cargando');
    var main = document.querySelector('main');
    if (main) main.setAttribute('aria-busy', 'true');
  }

  document.addEventListener('submit', function (evento) {
    // validacion.js cancela el envío si hay errores; en ese caso no se muestra la carga.
    if (evento.target.matches('form[data-cargando], form[data-validar]') && !evento.defaultPrevented) {
      mostrarCarga();
    }
  });

  document.addEventListener('click', function (evento) {
    var enlace = evento.target.closest('a[data-cargando]');
    // Ctrl/Cmd+clic abre otra pestaña: esta página no queda cargando.
    if (enlace && !evento.ctrlKey && !evento.metaKey && !evento.shiftKey) mostrarCarga();
  });

  // Al volver con "Atrás", el navegador puede restaurar la página con la barra activa.
  window.addEventListener('pageshow', function () {
    document.body.classList.remove('cargando');
    var main = document.querySelector('main');
    if (main) main.removeAttribute('aria-busy');
  });
})();
