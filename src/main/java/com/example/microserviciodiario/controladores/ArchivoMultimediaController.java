package com.example.microserviciodiario.controladores;

import com.example.microserviciodiario.dto.ArchivoMultimediaDTO;
import com.example.microserviciodiario.modelos.ArchivoMultimedia;
import com.example.microserviciodiario.servicios.ArchivoMultimediaService;
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
public class ArchivoMultimediaController {

    @Autowired
    private ArchivoMultimediaService archivoService;

    // POST: Agregar archivo a un diario
    @PostMapping("/{diarioId}/archivos")
    public ResponseEntity<ArchivoMultimedia> agregarArchivo(
            @PathVariable UUID diarioId,
            @RequestBody ArchivoMultimediaDTO dto,
            Authentication authentication) {

        try {
            String email = (authentication != null) ? authentication.getName() : "test@example.com";
            return new ResponseEntity<>(archivoService.guardarArchivo(diarioId, dto, email), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    // POST: Subir archivo directamente a GCS y asociarlo a un diario
    @PostMapping("/{diarioId}/archivos/upload")
    public ResponseEntity<?> uploadArchivo(
            @PathVariable UUID diarioId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        try {
            String email = (authentication != null) ? authentication.getName() : "test@example.com";
            ArchivoMultimedia guardado = archivoService.uploadYGuardarArchivo(diarioId, file, email);
            return new ResponseEntity<>(guardado, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException | IOException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    // GET: Ver archivos de un diario
    @GetMapping("/{diarioId}/archivos")
    public ResponseEntity<List<ArchivoMultimedia>> listarArchivos(
            @PathVariable UUID diarioId,
            Authentication authentication) {

        try {
            String email = (authentication != null) ? authentication.getName() : "test@example.com";
            return ResponseEntity.ok(archivoService.listarPorDiario(diarioId, email));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}