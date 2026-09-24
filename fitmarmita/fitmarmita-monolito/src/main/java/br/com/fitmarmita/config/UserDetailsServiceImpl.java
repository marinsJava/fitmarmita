package br.com.fitmarmita.config;

import br.com.fitmarmita.usuario.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioService usuarioService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var usuario = usuarioService.buscarPorEmail(email);
        return User.withUsername(usuario.getEmail())
                .password(usuario.getSenha())
                .authorities(AuthorityUtils.NO_AUTHORITIES)
                .accountLocked(usuario.estaBloqueado())
                .disabled(!Boolean.TRUE.equals(usuario.getAtivo()))
                .build();
    }
}
