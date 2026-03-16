package com.example.microserviciodiario.servicios;

import com.example.microserviciodiario.dto.ArchivoMultimediaDTO;
import com.example.microserviciodiario.modelos.ArchivoMultimedia;
import com.example.microserviciodiario.modelos.Diario;
import com.example.microserviciodiario.repositorios.ArchivoMultimediaRepository;
import com.example.microserviciodiario.repositorios.DiarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
public class ArchivoMultimediaService {

    @Autowired
    private ArchivoMultimediaRepository archivoRepository;

    @Autowired
    private DiarioRepository diarioRepository;

    @Autowired
    private CloudStorageService cloudStorageService;

    @Transactional
    public ArchivoMultimedia guardarArchivo(UUID diarioId, ArchivoMultimediaDTO dto, String emailUsuario) {
        // 1. Validar Diario y Dueño
        Diario diario = diarioRepository.findById(diarioId)
                .orElseThrow(() -> new RuntimeException("Diario no encontrado"));

        if (!diario.getUsuario().getEmail().equals(emailUsuario)) {
            throw new RuntimeException("No tienes permiso en este diario");
        }

        // 2. Guardar
        ArchivoMultimedia archivo = new ArchivoMultimedia();
        archivo.setUrl(dto.getUrl());
        archivo.setTipo(dto.getTipo());
        archivo.setFechaSubida(LocalDateTime.now());
        archivo.setDiario(diario);

        return archivoRepository.save(archivo);
    }

    public List<ArchivoMultimedia> listarPorDiario(UUID diarioId, String emailUsuario) {
        Diario diario = diarioRepository.findById(diarioId)
                .orElseThrow(() -> new RuntimeException("Diario no encontrado"));

        if (!diario.getUsuario().getEmail().equals(emailUsuario)) {
            throw new RuntimeException("No tienes permiso para ver este diario");
        }
        return archivoRepository.findByDiarioId(diarioId);
    }

    @Transactional
    public ArchivoMultimedia uploadYGuardarArchivo(UUID diarioId, MultipartFile file, String emailUsuario) throws IOException {
        // 1. Validar Diario y Dueño
        Diario diario = diarioRepository.findById(diarioId)
                .orElseThrow(() -> new RuntimeException("Diario no encontrado"));

        if (!diario.getUsuario().getEmail().equals(emailUsuario)) {
            throw new RuntimeException("No tienes permiso en este diario");
        }

        // 2. Subir Archivo a Cloud Storage
        String url = cloudStorageService.uploadFile(file, "diarios/" + diarioId.toString());

        // 3. Determinar Tipo (Si es imagen, etc.)
        String contentType = file.getContentType();
        String tipo = (contentType != null && contentType.startsWith("image")) ? "imagen" : "documento";

        // 4. Guardar
        ArchivoMultimedia archivo = new ArchivoMultimedia();
        archivo.setUrl(url);
        archivo.setTipo(tipo);
        archivo.setFechaSubida(LocalDateTime.now());
        archivo.setDiario(diario);

        return archivoRepository.save(archivo);
    }
}