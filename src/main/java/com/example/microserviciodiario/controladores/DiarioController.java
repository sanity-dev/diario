package com.example.microserviciodiario.controladores;

import com.example.microserviciodiario.dto.DiarioRequestDTO; // <--- ¡ESTA ES LA LÍNEA QUE FALTABA!
import com.example.microserviciodiario.modelos.Diario;
import com.example.microserviciodiario.servicios.DiarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/diarios")
public class DiarioController {

    @Autowired
    private DiarioService diarioService;

    // GET: Ver mis diarios
    @GetMapping
    public ResponseEntity<List<Diario>> misDiarios(Authentication authentication) {
        String email = (authentication != null) ? authentication.getName() : "test@example.com";
        return ResponseEntity.ok(diarioService.obtenerDiariosPorEmail(email));
    }

    // POST: Crear un diario
    @PostMapping
    public ResponseEntity<Diario> crear(@RequestBody DiarioRequestDTO diarioDTO, Authentication authentication) {
        String email = (authentication != null) ? authentication.getName() : "test@example.com";

        // Convertimos el DTO a la Entidad Diario manualmente
        Diario nuevoDiario = new Diario();
        nuevoDiario.setTitulo(diarioDTO.getTitulo());
        nuevoDiario.setContenido(diarioDTO.getContenido());

        // El servicio se encarga de poner fecha y usuario
        Diario diarioGuardado = diarioService.crearDiario(nuevoDiario, email);

        return new ResponseEntity<>(diarioGuardado, HttpStatus.CREATED);
    }

    // GET: Ver un diario específico
    @GetMapping("/{id}")
    public ResponseEntity<Diario> obtenerUno(@PathVariable UUID id, Authentication authentication) {
        try {
            String email = (authentication != null) ? authentication.getName() : "test@example.com";
            return diarioService.obtenerDiarioPorId(id, email)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    // PUT: Editar
    @PutMapping("/{id}")
    public ResponseEntity<Diario> editar(@PathVariable UUID id, @RequestBody Diario diario,
            Authentication authentication) {
        try {
            String email = (authentication != null) ? authentication.getName() : "test@example.com";
            return ResponseEntity.ok(diarioService.actualizarDiario(id, diario, email));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 403 si no es suyo
        }
    }

    // DELETE: Borrar
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id, Authentication authentication) {
        try {
            String email = (authentication != null) ? authentication.getName() : "test@example.com";
            diarioService.eliminarDiario(id, email);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}