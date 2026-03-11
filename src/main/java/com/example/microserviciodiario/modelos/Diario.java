package com.example.microserviciodiario.modelos;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "diarios")
public class Diario {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 150)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @OneToMany(mappedBy = "diario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore // <--- AGREGAR ESTO
    private List<BitacoraEmocional> bitacoras;

    @OneToMany(mappedBy = "diario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore // <--- AGREGAR ESTO
    private List<ArchivoMultimedia> archivos;

    @OneToMany(mappedBy = "diario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<MensajeDiario> mensajes;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    // --- GETTERS Y SETTERS MANUALES ---
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public List<BitacoraEmocional> getBitacoras() { return bitacoras; }
    public void setBitacoras(List<BitacoraEmocional> bitacoras) { this.bitacoras = bitacoras; }

    public List<ArchivoMultimedia> getArchivos() { return archivos; }
    public void setArchivos(List<ArchivoMultimedia> archivos) { this.archivos = archivos; }

    public List<MensajeDiario> getMensajes() { return mensajes; }
    public void setMensajes(List<MensajeDiario> mensajes) { this.mensajes = mensajes; }
}