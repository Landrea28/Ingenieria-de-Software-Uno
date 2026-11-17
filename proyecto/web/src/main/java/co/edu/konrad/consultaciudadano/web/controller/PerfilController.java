package co.edu.konrad.consultaciudadano.web.controller;

import co.edu.konrad.consultaciudadano.web.dto.PerfilDto;
import co.edu.konrad.consultaciudadano.web.dto.PermisoDto;
import co.edu.konrad.consultaciudadano.web.service.ApiConflictoException;
import co.edu.konrad.consultaciudadano.web.service.ApiNoDisponibleException;
import co.edu.konrad.consultaciudadano.web.service.ApiNoEncontradoException;
import co.edu.konrad.consultaciudadano.web.service.ApiValidacionException;
import co.edu.konrad.consultaciudadano.web.service.PerfilApiClient;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Pantalla de perfiles y permisos (HU09): consultar los perfiles, las opciones del sistema con sus
 * permisos y asignar permisos a un perfil. El catálogo de perfiles es fijo en el backend, por eso no
 * hay crear ni eliminar perfiles. Quien autoriza cada acción es la API, no esta pantalla.
 */
@Controller
@RequestMapping("/perfiles")
public class PerfilController {

    static final String MSG_PERMISO_ADMIN =
            "El perfil Administrador debe conservar el permiso «Administrar usuarios y perfiles».";

    /** Nombre de cada opción del sistema (tabla 'opcion' del backend), en el orden del menú. */
    private static final Map<String, String> OPCIONES = opciones();

    private final PerfilApiClient perfiles;

    public PerfilController(PerfilApiClient perfiles) {
        this.perfiles = perfiles;
    }

    @GetMapping
    public String ver(@RequestParam(required = false) Long perfil, Model model) {
        try {
            List<PerfilDto> catalogo = perfiles.perfiles();
            model.addAttribute("perfiles", catalogo);
            PerfilDto seleccionado = catalogo.stream().filter(p -> p.id().equals(perfil)).findFirst()
                    .orElse(catalogo.isEmpty() ? null : catalogo.get(0));
            if (seleccionado != null) {
                Set<Long> asignados = perfiles.permisosDe(seleccionado.id()).stream()
                        .map(PermisoDto::id).collect(Collectors.toSet());
                model.addAttribute("seleccionado", seleccionado);
                model.addAttribute("asignados", asignados);
                model.addAttribute("permisosPorOpcion", agrupar(perfiles.permisos()));
            }
        } catch (ApiNoDisponibleException e) {
            model.addAttribute("errorCarga", e.getMessage());
        }
        return "perfiles";
    }

    /** La lista marcada reemplaza a la anterior (PUT /api/perfiles/{id}/permisos). */
    @PostMapping("/{id:[0-9]{1,18}}/permisos")
    public String guardar(@PathVariable long id, @RequestParam(required = false) List<Long> permisosIds,
            RedirectAttributes redirect) {
        List<Long> ids = permisosIds == null ? List.of() : permisosIds.stream().distinct().toList();
        try {
            perfiles.reemplazarPermisos(id, ids);
            redirect.addFlashAttribute("mensajeExito", "Permisos guardados. El perfil quedó con "
                    + ids.size() + (ids.size() == 1 ? " permiso." : " permisos."));
        } catch (ApiConflictoException e) {
            redirect.addFlashAttribute("mensajeError", MSG_PERMISO_ADMIN + " No se guardaron los cambios.");
        } catch (ApiNoEncontradoException e) {
            redirect.addFlashAttribute("mensajeError",
                    "El perfil o alguno de los permisos ya no existe. Se recargó la lista; repita la selección.");
        } catch (ApiValidacionException e) {
            redirect.addFlashAttribute("mensajeError", "El servidor rechazó los datos: " + e.getMessage());
        } catch (ApiNoDisponibleException e) {
            redirect.addFlashAttribute("mensajeError", e.getMessage() + " Los permisos no se guardaron.");
        }
        redirect.addAttribute("perfil", id);
        return "redirect:/perfiles";
    }

    // Agrupa los permisos por la opción del sistema a la que pertenecen, en el orden de OPCIONES.
    private static Map<String, List<PermisoDto>> agrupar(List<PermisoDto> permisos) {
        Map<String, List<PermisoDto>> grupos = new LinkedHashMap<>();
        OPCIONES.values().forEach(nombre -> grupos.put(nombre, new ArrayList<>()));
        for (PermisoDto permiso : permisos) {
            grupos.computeIfAbsent(OPCIONES.getOrDefault(permiso.opcion(), "Otras opciones"),
                    nombre -> new ArrayList<>()).add(permiso);
        }
        grupos.values().removeIf(List::isEmpty);
        return grupos;
    }

    private static Map<String, String> opciones() {
        Map<String, String> opciones = new LinkedHashMap<>();
        opciones.put("CIUDADANOS", "Gestión de ciudadanos");
        opciones.put("ANTECEDENTES", "Consulta de antecedentes");
        opciones.put("REPORTES", "Reportes de ciudadanos");
        opciones.put("SEGURIDAD", "Gestión de seguridad");
        return opciones;
    }
}
