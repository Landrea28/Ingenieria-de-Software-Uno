package co.edu.konrad.consultaciudadano.common.error;

import jakarta.validation.ConstraintViolationException;
import co.edu.konrad.consultaciudadano.common.security.AuditoriaSeguridad;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final ObjectProvider<AuditoriaSeguridad> auditoria;

    @ExceptionHandler(NegocioException.class)
    public ResponseEntity<ErrorResponse> negocio(NegocioException error, HttpServletRequest request) {
        if (error.getEstadoHttp() == HttpStatus.UNAUTHORIZED) {
            var auditoriaSeguridad = auditoria.getIfAvailable();
            if (auditoriaSeguridad != null) {
                auditoriaSeguridad.noAutorizado(request);
            }
        } else if (error.getEstadoHttp() == HttpStatus.FORBIDDEN) {
            var auditoriaSeguridad = auditoria.getIfAvailable();
            if (auditoriaSeguridad != null) {
                auditoriaSeguridad.accesoDenegado(request);
            }
        }
        return ResponseEntity.status(error.getEstadoHttp())
                .body(ErrorResponse.crear(error.getCodigo(), error.getMessage(), List.of()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException error) {
        var detalles = error.getBindingResult().getFieldErrors().stream()
                .map(campo -> campo.getField() + ": " + campo.getDefaultMessage()).distinct().toList();
        return ResponseEntity.badRequest().body(ErrorResponse.crear(CodigoError.VALIDACION, "Datos invalidos", detalles));
    }

    @ExceptionHandler({ConstraintViolationException.class, HandlerMethodValidationException.class,
            HttpMessageNotReadableException.class, IllegalArgumentException.class,
            MethodArgumentTypeMismatchException.class, ServletRequestBindingException.class})
    public ResponseEntity<ErrorResponse> parametros(Exception error) {
        return ResponseEntity.badRequest().body(ErrorResponse.crear(CodigoError.VALIDACION,
                "Peticion invalida", List.of()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> recursoNoEncontrado(NoResourceFoundException error) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.crear(CodigoError.NO_ENCONTRADO, "Recurso no encontrado", List.of()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> desconocido(Exception error) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.crear(CodigoError.ERROR_INTERNO,
                        "Error inesperado del servidor", List.of()));
    }
}
