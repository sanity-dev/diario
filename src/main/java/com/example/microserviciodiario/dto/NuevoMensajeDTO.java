package com.example.microserviciodiario.dto;

public class NuevoMensajeDTO {
    private String contenido;
    private String tipo;

    public NuevoMensajeDTO() {}

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}
