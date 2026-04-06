package com.example.microserviciodiario.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class RecuerdoDTO {
    private UUID id;
    private String url;
    private LocalDateTime fecha;
    private String origen; 

    public RecuerdoDTO(UUID id, String url, LocalDateTime fecha, String origen) {
        this.id = id;
        this.url = url;
        this.fecha = fecha;
        this.origen = origen;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }
}
