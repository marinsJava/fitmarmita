package br.com.fitmarmita.usuario.service;

import br.com.fitmarmita.shared.exception.BusinessException;
import br.com.fitmarmita.shared.exception.NotFoundException;
import br.com.fitmarmita.usuario.dto.CadastroUsuarioRequest;
import br.com.fitmarmita.usuario.dto.UsuarioResponse;
import br.com.fitmarmita.usuario.entity.Usuario;
import br.com.fitmarmita.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioResponse cadastrar(CadastroUsuarioRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email já cadastrado");
        }

        String hash = passwordEncoder.encode(request.senha());
        Usuario usuario = Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .senhaHash(hash)
                .telefone(request.telefone())
                .ativo(true)
                .build();

        usuarioRepository.save(usuario);
        return UsuarioResponse.from(usuario);
    }

    public Usuario obterPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
    }

    public List<Usuario> obterTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario alterar(Long id, Usuario usuarioAtualizado) {
        Usuario usuario = obterPorId(id);

        usuario.setNome(usuarioAtualizado.getNome());
        usuario.setEmail(usuarioAtualizado.getEmail());
        usuario.setTelefone(usuarioAtualizado.getTelefone());
        usuario.setAtivo(usuarioAtualizado.getAtivo());
        // senha alterada via fluxo próprio (com PasswordEncoder), não aqui

        return usuarioRepository.save(usuario);
    }

    public void excluir(Long id) {
        Usuario usuario = obterPorId(id);
        usuarioRepository.delete(usuario);
    }
}