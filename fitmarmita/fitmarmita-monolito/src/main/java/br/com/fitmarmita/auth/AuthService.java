package br.com.fitmarmita.auth;

import br.com.fitmarmita.auth.dto.LoginRequest;
import br.com.fitmarmita.shared.exception.BusinessException;
import br.com.fitmarmita.usuario.entity.Usuario;
import br.com.fitmarmita.usuario.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public String login(LoginRequest request) {
        Usuario usuario;
        try {
            usuario = usuarioService.buscarPorEmail(request.email());
        } catch (Exception e) {
            throw new BusinessException("E-mail ou senha invalidos");
        }

        if (usuario.estaBloqueado()) {
            throw new BusinessException("Usuario temporariamente bloqueado. Tente novamente mais tarde");
        }

        if (!passwordEncoder.matches(request.senha(), usuario.getSenha())) {
            usuarioService.registrarFalhaDeLogin(usuario);
            throw new BusinessException("E-mail ou senha invalidos");
        }

        usuarioService.registrarLoginComSucesso(usuario);
        return jwtService.gerarToken(usuario);
    }
}
