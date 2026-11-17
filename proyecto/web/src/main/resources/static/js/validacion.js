// Validación en el navegador para formularios con el atributo data-validar.
// Solo mejora la experiencia: la web (CiudadanoForm) y la API vuelven a validar.
// Usa las reglas HTML5 del campo (required, pattern, min, max, step, maxlength) y muestra
// mensajes en español en el <small id="error-<nombre>"> que genera el componente 'campo'.
(function () {
  'use strict';

  // Reglas que HTML5 no cubre: texto solo con espacios y "esta fecha después de otra".
  function aplicarReglasExtra(campo, form) {
    var mensaje = '';
    if (campo.required && campo.type === 'text' && campo.value !== '' && campo.value.trim() === '') {
      mensaje = 'Este campo es obligatorio.';
    }
    var otroNombre = campo.dataset.despuesDe;
    if (!mensaje && otroNombre && campo.value) {
      var otro = form.elements[otroNombre];
      // Las fechas yyyy-MM-dd se pueden comparar como texto.
      if (otro && otro.value && campo.value <= otro.value) {
        mensaje = campo.dataset.mensajeDespuesDe || 'Fecha inválida.';
      }
    }
    campo.setCustomValidity(mensaje);
  }

  function mensajePara(campo) {
    var v = campo.validity;
    if (v.valid) return '';
    if (v.valueMissing) return campo.tagName === 'SELECT' ? 'Seleccione una opción.' : 'Este campo es obligatorio.';
    if (v.customError) return campo.validationMessage;
    if (v.badInput) return campo.type === 'date' ? 'Fecha inválida.' : 'Debe ser un número.';
    if (v.tooLong) return 'Máximo ' + campo.maxLength + ' caracteres.';
    return campo.dataset.mensaje || 'Valor inválido.';
  }

  function mostrar(campo) {
    var mensaje = mensajePara(campo);
    var error = document.getElementById('error-' + campo.name);
    if (error) error.textContent = mensaje;
    campo.setAttribute('aria-invalid', mensaje ? 'true' : 'false');
    var contenedor = campo.closest('.campo');
    if (contenedor) contenedor.classList.toggle('con-error', mensaje !== '');
    return mensaje === '';
  }

  function validar(campo, form) {
    aplicarReglasExtra(campo, form);
    return mostrar(campo);
  }

  document.querySelectorAll('form[data-validar]').forEach(function (form) {
    // Desactiva las burbujas del navegador; mostramos nuestros mensajes junto a cada campo.
    form.noValidate = true;

    var campos = Array.prototype.filter.call(
      form.querySelectorAll('input[name], select[name]'),
      function (campo) { return !campo.readOnly && campo.type !== 'hidden'; });
    var resumen = form.querySelector('[data-resumen-errores]');

    // Un campo se valida en vivo solo después de que el usuario salió de él (o falló el envío),
    // para no mostrar errores mientras apenas empieza a escribir.
    var tocados = new Set();
    campos.forEach(function (campo) {
      if (campo.closest('.campo.con-error')) tocados.add(campo); // errores que vinieron del servidor
    });

    campos.forEach(function (campo) {
      campo.addEventListener('blur', function () {
        tocados.add(campo);
        validar(campo, form);
      });
      campo.addEventListener(campo.tagName === 'SELECT' ? 'change' : 'input', function () {
        if (tocados.has(campo)) validar(campo, form);
        // Si cambia una fecha de la que depende otra, revalidar la dependiente.
        campos.forEach(function (otro) {
          if (otro.dataset.despuesDe === campo.name && tocados.has(otro)) validar(otro, form);
        });
      });
    });

    form.addEventListener('submit', function (evento) {
      var invalidos = campos.filter(function (campo) {
        tocados.add(campo);
        return !validar(campo, form);
      });
      if (invalidos.length > 0) {
        evento.preventDefault();
        if (resumen) {
          resumen.textContent = invalidos.length === 1
            ? 'Hay 1 campo por corregir.'
            : 'Hay ' + invalidos.length + ' campos por corregir.';
          resumen.hidden = false;
        }
        invalidos[0].focus();
        return;
      }
      if (resumen) resumen.hidden = true;
      // Evita registros dobles por doble clic.
      var boton = form.querySelector('button[type="submit"]');
      if (boton) {
        boton.dataset.textoOriginal = boton.textContent;
        boton.disabled = true;
        boton.textContent = 'Guardando…';
      }
    });

    // Si el usuario vuelve con "Atrás", el navegador puede restaurar la página con el botón deshabilitado.
    window.addEventListener('pageshow', function () {
      var boton = form.querySelector('button[type="submit"]');
      if (boton && boton.dataset.textoOriginal) {
        boton.disabled = false;
        boton.textContent = boton.dataset.textoOriginal;
      }
    });
  });
})();
