package br.com.fitmarmita.shared.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // ---- 404 — recurso não encontrado ----
    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException ex) {
        return montar(HttpStatus.NOT_FOUND, "Recurso não encontrado", ex.getMessage());
    }

    // ---- 400 — regra de negócio violada (carrinho vazio, transição de status inválida, etc.) ----
    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusiness(BusinessException ex) {
        return montar(HttpStatus.BAD_REQUEST, "Regra de negócio violada", ex.getMessage());
    }

    // ---- 400 — recurso duplicado (ex: tentar criar algo que já existe) ----
    @ExceptionHandler(DuplicateResourceException.class)
    public ProblemDetail handleDuplicate(DuplicateResourceException ex) {
        return montar(HttpStatus.BAD_REQUEST, "Recurso duplicado", ex.getMessage());
    }

    // ---- 400 — violação de integridade no banco (constraint, FK, unique) ----
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
        // nunca expor ex.getMessage() aqui — vaza detalhes do schema/SQL
        return montar(HttpStatus.BAD_REQUEST, "Operação inválida",
                "A operação viola uma restrição de integridade dos dados.");
    }

    // ---- 400 — parâmetro de URL com tipo errado (ex: /pedidos/abc, esperando Long) ----
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String detalhe = "O parâmetro '%s' tem valor inválido: '%s'".formatted(ex.getName(), ex.getValue());
        return montar(HttpStatus.BAD_REQUEST, "Parâmetro inválido", detalhe);
    }

    // ---- 400 — corpo da requisição inválido (@Valid falhou em algum campo do DTO) ----
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        List<String> erros = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .toList();

        String detalhe = String.join("; ", erros);

        ProblemDetail pd = montar(HttpStatus.BAD_REQUEST, "Dados inválidos", detalhe);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
    }

    // ---- 500 — rede de segurança para o não previsto ----
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        logger.error("Erro não tratado", ex);
        return montar(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
                "Ocorreu um erro inesperado. Tente novamente mais tarde.");
    }

    @ExceptionHandler(org.springframework.data.mapping.PropertyReferenceException.class)
    public ProblemDetail handleInvalidSortField(org.springframework.data.mapping.PropertyReferenceException ex) {
        return montar(HttpStatus.BAD_REQUEST, "Parâmetro de ordenação inválido", ex.getMessage());
    }

    // Helper para padronizar a montagem do ProblemDetail
    private ProblemDetail montar(HttpStatus status, String titulo, String detalhe) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detalhe);
        pd.setTitle(titulo);
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }
}