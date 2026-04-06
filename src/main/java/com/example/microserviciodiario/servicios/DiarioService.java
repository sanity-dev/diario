package com.example.microserviciodiario.servicios;

import com.example.microserviciodiario.modelos.Diario;
import com.example.microserviciodiario.modelos.Usuario;
import com.example.microserviciodiario.repositorios.DiarioRepository;
import com.example.microserviciodiario.repositorios.UsuarioRepository;
import com.example.microserviciodiario.repositorios.MensajeDiarioRepository;
import com.example.microserviciodiario.modelos.MensajeDiario;
import com.example.microserviciodiario.dto.MensajeDiarioDTO;
import com.example.microserviciodiario.dto.NuevoMensajeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
public class DiarioService {

    @Autowired
    private DiarioRepository diarioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MensajeDiarioRepository mensajeDiarioRepository;

    @Autowired
    private com.example.microserviciodiario.repositorios.ArchivoMultimediaRepository archivoMultimediaRepository;

    @Autowired
    private CloudStorageService cloudStorageService;

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

    // Obtener los mensajes de un diario
    public List<MensajeDiarioDTO> obtenerMensajesDeDiario(UUID diarioId, String emailUsuario) {
        Diario diarioExistente = diarioRepository.findById(diarioId)
                .orElseThrow(() -> new RuntimeException("Diario no encontrado"));

        if (!diarioExistente.getUsuario().getEmail().equals(emailUsuario)) {
            throw new RuntimeException("No tienes permiso para ver este diario");
        }

        List<MensajeDiario> mensajes = mensajeDiarioRepository.findByDiarioIdOrderByFechaEnvioAsc(diarioId);
        return mensajes.stream()
                .map(m -> new MensajeDiarioDTO(m.getId(), m.getContenido(), m.getTipo(), m.getFechaEnvio()))
                .collect(Collectors.toList());
    }

    // Agregar un mensaje a un diario
    @Transactional
    public MensajeDiarioDTO agregarMensajeADiario(UUID diarioId, NuevoMensajeDTO nuevoMensajeDTO, String emailUsuario) {
        Diario diarioExistente = diarioRepository.findById(diarioId)
                .orElseThrow(() -> new RuntimeException("Diario no encontrado"));

        if (!diarioExistente.getUsuario().getEmail().equals(emailUsuario)) {
            throw new RuntimeException("No tienes permiso para editar este diario");
        }

        MensajeDiario mensaje = new MensajeDiario();
        mensaje.setDiario(diarioExistente);
        mensaje.setContenido(nuevoMensajeDTO.getContenido());
        mensaje.setTipo(nuevoMensajeDTO.getTipo() != null ? nuevoMensajeDTO.getTipo() : "TEXTO");
        mensaje.setFechaEnvio(LocalDateTime.now());

        MensajeDiario guardado = mensajeDiarioRepository.save(mensaje);

        // Actualizar la fecha de modificación del diario
        diarioExistente.setFechaActualizacion(LocalDateTime.now());
        diarioRepository.save(diarioExistente);

        return new MensajeDiarioDTO(guardado.getId(), guardado.getContenido(), guardado.getTipo(), guardado.getFechaEnvio());
    }

    // Agregar un mensaje de tipo IMAGEN a un diario usando Cloud Storage
    @Transactional
    public MensajeDiarioDTO agregarMensajeImagenADiario(UUID diarioId, MultipartFile file, String emailUsuario, String usuarioIdStr) throws IOException {
        Diario diarioExistente = diarioRepository.findById(diarioId)
                .orElseThrow(() -> new RuntimeException("Diario no encontrado"));

        if (!diarioExistente.getUsuario().getEmail().equals(emailUsuario)) {
            throw new RuntimeException("No tienes permiso para editar este diario");
        }

        // Subir Archivo a Cloud Storage
        String url = cloudStorageService.uploadFile(file, "diarios/" + usuarioIdStr + "/mensajes");

        MensajeDiario mensaje = new MensajeDiario();
        mensaje.setDiario(diarioExistente);
        mensaje.setContenido(url);
        mensaje.setTipo("IMAGEN");
        mensaje.setFechaEnvio(LocalDateTime.now());

        MensajeDiario guardado = mensajeDiarioRepository.save(mensaje);

        // Actualizar la fecha de modificación del diario
        diarioExistente.setFechaActualizacion(LocalDateTime.now());
        diarioRepository.save(diarioExistente);

        return new MensajeDiarioDTO(guardado.getId(), guardado.getContenido(), guardado.getTipo(), guardado.getFechaEnvio());
    }

    // Obtener los recuerdos del usuario (imágenes enviadas por chat y archivos)
    public List<com.example.microserviciodiario.dto.RecuerdoDTO> obtenerRecuerdos(String emailUsuario) {
        // Validar que el usuario exista
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<com.example.microserviciodiario.dto.RecuerdoDTO> recuerdos = new java.util.ArrayList<>();

        // Obtener imágenes de MensajeDiario
        List<MensajeDiario> mensajes = mensajeDiarioRepository.findImagenesByUsuarioEmail(emailUsuario);
        for (MensajeDiario m : mensajes) {
            recuerdos.add(new com.example.microserviciodiario.dto.RecuerdoDTO(
                    m.getId(), m.getContenido(), m.getFechaEnvio(), "Mensaje de Diario"));
        }

        // Obtener imágenes de ArchivoMultimedia
        List<com.example.microserviciodiario.modelos.ArchivoMultimedia> archivos = archivoMultimediaRepository.findImagenesByUsuarioEmail(emailUsuario);
        for (com.example.microserviciodiario.modelos.ArchivoMultimedia a : archivos) {
            recuerdos.add(new com.example.microserviciodiario.dto.RecuerdoDTO(
                    a.getId(), a.getUrl(), a.getFechaSubida(), "Archivo Adjunto"));
        }

        // Ordenar por fecha descendente
        recuerdos.sort((r1, r2) -> r2.getFecha().compareTo(r1.getFecha()));

        return recuerdos;
    }
}