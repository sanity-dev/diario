package com.example.microserviciodiario.repositorios;

import com.example.microserviciodiario.modelos.ArchivoMultimedia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArchivoMultimediaRepository extends JpaRepository<ArchivoMultimedia, UUID> {
    List<ArchivoMultimedia> findByDiarioId(UUID diarioId);

    @Query("SELECT a FROM ArchivoMultimedia a WHERE a.diario.usuario.email = :email AND a.tipo = 'imagen' ORDER BY a.fechaSubida DESC")
    List<ArchivoMultimedia> findImagenesByUsuarioEmail(@Param("email") String email);
}