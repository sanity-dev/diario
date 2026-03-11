package com.example.microserviciodiario.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class MensajeDiarioDTO {
    private UUID id;
    private String contenido;
    private String tipo;
    private LocalDateTime fechaEnvio;

    public MensajeDiarioDTO() {}

    public MensajeDiarioDTO(UUID id, String contenido, String tipo, LocalDateTime fechaEnvio) {
        this.id = id;
        this.contenido = contenido;
        this.tipo = tipo;
        this.fechaEnvio = fechaEnvio;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(LocalDateTime fechaEnvio) { this.fechaEnvio = fechaEnvio; }
}
