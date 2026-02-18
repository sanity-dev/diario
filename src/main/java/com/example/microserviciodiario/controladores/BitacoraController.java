package com.example.microserviciodiario.controladores;

import com.example.microserviciodiario.dto.BitacoraRequestDTO;
import com.example.microserviciodiario.modelos.BitacoraEmocional;
import com.example.microserviciodiario.servicios.BitacoraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/diarios")
public class BitacoraController {

    @Autowired
    private BitacoraService bitacoraService;

    // POST: Agregar una emoción a un diario específico
    // URL: http://localhost:8081/api/diarios/{diarioId}/bitacoras
    @PostMapping("/{diarioId}/bitacoras")
    public ResponseEntity<BitacoraEmocional> crear(
            @PathVariable UUID diarioId,
            @RequestBody BitacoraRequestDTO dto,
            Authentication authentication) {

        String email = (authentication != null) ? authentication.getName() : "test@example.com";
        try {
            BitacoraEmocional nueva = bitacoraService.crearBitacora(diarioId, dto, email);
            return new ResponseEntity<>(nueva, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    // GET: Ver las emociones de un diario
    @GetMapping("/{diarioId}/bitacoras")
    public ResponseEntity<List<BitacoraEmocional>> listar(
            @PathVariable UUID diarioId,
            Authentication authentication) {

        String email = (authentication != null) ? authentication.getName() : "test@example.com";
        try {
            return ResponseEntity.ok(bitacoraService.listarPorDiario(diarioId, email));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}