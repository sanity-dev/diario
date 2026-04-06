package com.example.microserviciodiario.servicios;

import com.example.microserviciodiario.modelos.Usuario;
import com.example.microserviciodiario.repositorios.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseGet(() -> {
                    // Si el usuario no existe localmente pero el token JWT es válido, 
                    // creamos un usuario 'stub' local para mantener la integridad referencial.
                    Usuario nuevo = new Usuario();
                    nuevo.setEmail(email);
                    nuevo.setNombre("Usuario " + email.split("@")[0]); 
                    nuevo.setPassword(""); // Sin contraseña real, la autenticación la hace el Gateway/JWT
                    nuevo.setRol("USER");
                    return usuarioRepository.save(nuevo);
                });

        // Convertimos tu Usuario (Entidad) a un User (Seguridad)
        return new User(
                usuario.getEmail(),
                usuario.getPassword(),
                List.of(new SimpleGrantedAuthority(usuario.getRol()))
        );
    }
}