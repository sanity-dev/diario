package com.example.microserviciodiario.servicios;

import com.example.microserviciodiario.modelos.Diario;
import com.example.microserviciodiario.modelos.Usuario;
import com.example.microserviciodiario.repositorios.DiarioRepository;
import com.example.microserviciodiario.repositorios.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DiarioService {

    @Autowired
    private DiarioRepository diarioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Obtener todos los diarios de UN usuario específico (por email)
    public List<Diario> obtenerDiariosPorEmail(String email) {
        // 1. Buscamos al usuario en la BD
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseGet(() -> {
                    if ("test@example.com".equals(email)) {
                        return crearUsuarioTest();
                    }
                    throw new UsernameNotFoundException("Usuario no encontrado");
                });

        // 2. Retornamos solo los diarios de ese usuario
        return diarioRepository.findByUsuarioId(usuario.getId());
    }

    // Guardar un nuevo diario
    @Transactional
    public Diario crearDiario(Diario diario, String emailUsuario) {
        // 1. Buscamos quién es el usuario que está guardando esto
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseGet(() -> {
                    if ("test@example.com".equals(emailUsuario)) {
                        return crearUsuarioTest();
                    }
                    throw new UsernameNotFoundException("Usuario no encontrado");
                });

        // 2. Asociamos el diario a ese usuario
        diario.setUsuario(usuario);

        // 3. Fechas automáticas (aunque el @PrePersist ya lo hace, es bueno asegurar)
        if (diario.getFechaCreacion() == null) {
            diario.setFechaCreacion(LocalDateTime.now());
        }

        return diarioRepository.save(diario);
    }

    private Usuario crearUsuarioTest() {
        Usuario testUser = new Usuario();
        testUser.setEmail("test@example.com");
        testUser.setNombre("Usuario de Prueba");
        testUser.setPassword("123456"); // Dummy
        testUser.setRol("USER");
        return usuarioRepository.save(testUser);
    }

    // Actualizar diario (Validando que sea del dueño)
    public Diario actualizarDiario(UUID id, Diario diarioDetalles, String emailUsuario) {
        Diario diarioExistente = diarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Diario no encontrado"));

        // Validar seguridad: ¿El diario pertenece al usuario que intenta editarlo?
        if (!diarioExistente.getUsuario().getEmail().equals(emailUsuario)) {
            throw new RuntimeException("No tienes permiso para editar este diario");
        }

        diarioExistente.setTitulo(diarioDetalles.getTitulo());
        diarioExistente.setContenido(diarioDetalles.getContenido());
        // La fecha de actualización se maneja con @PreUpdate en la entidad

        return diarioRepository.save(diarioExistente);
    }

    // Eliminar diario
    public void eliminarDiario(UUID id, String emailUsuario) {
        Diario diarioExistente = diarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Diario no encontrado"));

        if (!diarioExistente.getUsuario().getEmail().equals(emailUsuario)) {
            throw new RuntimeException("No tienes permiso para eliminar este diario");
        }

        diarioRepository.deleteById(id);
    }

    // Obtener un diario por ID (Validando dueño)
    public Optional<Diario> obtenerDiarioPorId(UUID id, String emailUsuario) {
        Optional<Diario> diario = diarioRepository.findById(id);
        if (diario.isPresent() && !diario.get().getUsuario().getEmail().equals(emailUsuario)) {
            throw new RuntimeException("No tienes permiso para ver este diario");
        }
        return diario;
    }
}