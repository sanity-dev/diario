package com.example.microserviciodiario.modelos;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "mensajes_diario")
public class MensajeDiario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(columnDefinition = "NVARCHAR(MAX)", nullable = false)
    private String contenido;

    @Column(length = 50, nullable = false)
    private String tipo = "TEXTO"; // "TEXTO" o "IMAGEN"

    @Column(name = "fecha_envio", nullable = false)
    private LocalDateTime fechaEnvio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diario_id", nullable = false)
    @JsonIgnore
    private Diario diario;

    @PrePersist
    protected void onCreate() {
        if (fechaEnvio == null) {
            fechaEnvio = LocalDateTime.now();
        }
    }

    // --- GETTERS Y SETTERS MANUALES ---
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(LocalDateTime fechaEnvio) { this.fechaEnvio = fechaEnvio; }

    public Diario getDiario() { return diario; }
    public void setDiario(Diario diario) { this.diario = diario; }
}
