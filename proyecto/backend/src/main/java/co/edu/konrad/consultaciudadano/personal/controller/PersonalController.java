package co.edu.konrad.consultaciudadano.personal.controller;

import co.edu.konrad.consultaciudadano.personal.dto.RegistroPersonalRequest;
import co.edu.konrad.consultaciudadano.personal.dto.RegistroPersonalResponse;
import co.edu.konrad.consultaciudadano.personal.service.PersonalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/personal")
@RequiredArgsConstructor
public class PersonalController {
    private final PersonalService service;

    @PostMapping("/registro")
    public ResponseEntity<RegistroPersonalResponse> registrar(@Valid @RequestBody RegistroPersonalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(request));
    }
}
