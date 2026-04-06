package com.example.microserviciodiario.repositorios;

import com.example.microserviciodiario.modelos.MensajeDiario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface MensajeDiarioRepository extends JpaRepository<MensajeDiario, UUID> {
    List<MensajeDiario> findByDiarioIdOrderByFechaEnvioAsc(UUID diarioId);

    @Query("SELECT m FROM MensajeDiario m WHERE m.diario.usuario.email = :email AND m.tipo = 'IMAGEN' ORDER BY m.fechaEnvio DESC")
    List<MensajeDiario> findImagenesByUsuarioEmail(@Param("email") String email);
}
