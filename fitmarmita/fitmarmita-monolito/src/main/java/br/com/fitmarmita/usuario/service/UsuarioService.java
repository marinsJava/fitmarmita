package br.com.fitmarmita.usuario.service;

import br.com.fitmarmita.shared.exception.DuplicateResourceException;
import br.com.fitmarmita.shared.exception.NotFoundException;
import br.com.fitmarmita.usuario.dto.CriarUsuarioRequest;
import br.com.fitmarmita.usuario.entity.Usuario;
import br.com.fitmarmita.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Value("${fitmarmita.login.max-tentativas:5}")
    private int maxTentativas;

    @Value("${fitmarmita.login.minutos-bloqueio:15}")
    private int minutosBloqueio;

    @Transactional
    public Usuario criar(CriarUsuarioRequest request) {
        if (repository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException("Ja existe um usuario com o e-mail informado");
        }
        Usuario usuario = new Usuario();
        usuario.setNome(request.nome().trim());
        usuario.setEmail(request.email().trim().toLowerCase());
        usuario.setSenha(passwordEncoder.encode(request.senha()));
        usuario.setTelefone(request.telefone());
        usuario.setAtivo(true);
        usuario.setTentativasLogin(0);
        return repository.save(usuario);
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorEmail(String email) {
        return repository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("Usuario nao encontrado: " + email));
    }

    @Transactional
    public void registrarLoginComSucesso(Usuario usuario) {
        usuario.setTentativasLogin(0);
        usuario.setBloqueadoAte(null);
    }

    @Transactional
    public void registrarFalhaDeLogin(Usuario usuario) {
        int tentativas = usuario.getTentativasLogin() + 1;
        usuario.setTentativasLogin(tentativas);
        if (tentativas >= maxTentativas) {
            usuario.setBloqueadoAte(LocalDateTime.now().plusMinutes(minutosBloqueio));
            usuario.setTentativasLogin(0);
        }
    }
}
