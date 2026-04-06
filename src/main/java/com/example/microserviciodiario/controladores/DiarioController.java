package com.example.microserviciodiario.controladores;

import com.example.microserviciodiario.dto.DiarioRequestDTO; // <--- ¡ESTA ES LA LÍNEA QUE FALTABA!
import com.example.microserviciodiario.modelos.Diario;
import com.example.microserviciodiario.servicios.DiarioService;
import com.example.microserviciodiario.dto.MensajeDiarioDTO;
import com.example.microserviciodiario.dto.NuevoMensajeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@RestController
@RequestMapping("/api/diary")
public class DiarioController {

    @Autowired
    private DiarioService diarioService;

    // GET: Ver mis diarios
    @GetMapping
    public ResponseEntity<List<Diario>> misDiarios(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(diarioService.obtenerDiariosPorEmail(email));
    }

    // POST: Crear un diario
    @PostMapping
    public ResponseEntity<Diario> crear(@RequestBody DiarioRequestDTO diarioDTO, Authentication authentication) {
        String email = authentication.getName();

        // Convertimos el DTO a la Entidad Diario manualmente
        Diario nuevoDiario = new Diario();
        nuevoDiario.setTitulo(diarioDTO.getTitulo());
        nuevoDiario.setContenido(diarioDTO.getContenido());

        // El servicio se encarga de poner fecha y usuario
        Diario diarioGuardado = diarioService.crearDiario(nuevoDiario, email);

        return new ResponseEntity<>(diarioGuardado, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Diario> obtenerUno(@PathVariable UUID id, Authentication authentication) {
        try {
            String email = authentication.getName();
            return diarioService.obtenerDiarioPorId(id, email)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Diario> editar(@PathVariable UUID id, @RequestBody Diario diario,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            return ResponseEntity.ok(diarioService.actualizarDiario(id, diario, email));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 403 si no es suyo
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id, Authentication authentication) {
        try {
            String email = authentication.getName();
            diarioService.eliminarDiario(id, email);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/{id}/mensajes")
    public ResponseEntity<List<MensajeDiarioDTO>> obtenerMensajes(@PathVariable UUID id, Authentication authentication) {
        try {
            String email = authentication.getName();
            List<MensajeDiarioDTO> mensajes = diarioService.obtenerMensajesDeDiario(id, email);
            return ResponseEntity.ok(mensajes);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping("/{id}/mensajes")
    public ResponseEntity<MensajeDiarioDTO> agregarMensaje(@PathVariable UUID id, @RequestBody NuevoMensajeDTO nuevoMensajeDTO, Authentication authentication) {
        try {
            String email = authentication.getName();
            MensajeDiarioDTO mensaje = diarioService.agregarMensajeADiario(id, nuevoMensajeDTO, email);
            return new ResponseEntity<>(mensaje, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping("/{id}/mensajes/upload")
    public ResponseEntity<?> agregarMensajeImagen(
            @PathVariable UUID id, 
            @RequestParam("file") MultipartFile file, 
            @RequestParam(value = "usuarioId", required = false, defaultValue = "default") String usuarioId,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            MensajeDiarioDTO mensaje = diarioService.agregarMensajeImagenADiario(id, file, email, usuarioId);
            return new ResponseEntity<>(mensaje, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException | IOException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @GetMapping("/recuerdos")
    public ResponseEntity<List<com.example.microserviciodiario.dto.RecuerdoDTO>> obtenerRecuerdos(Authentication authentication) {
        try {
            String email = authentication.getName();
            List<com.example.microserviciodiario.dto.RecuerdoDTO> recuerdos = diarioService.obtenerRecuerdos(email);
            return ResponseEntity.ok(recuerdos);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}