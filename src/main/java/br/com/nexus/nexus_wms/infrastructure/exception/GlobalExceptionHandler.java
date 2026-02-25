package br.com.nexus.nexus_wms.infrastructure.exception;

import jakarta.persistence.OptimisticLockException;
import org.hibernate.StaleObjectStateException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.jspecify.annotations.Nullable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler producing RFC 7807 Problem Detail responses.
 *
 * <p>
 * Handles:
 * <ul>
 * <li><b>Optimistic Lock conflicts</b> → 409 Conflict</li>
 * <li><b>Bean Validation failures</b> → 422 Unprocessable Entity</li>
 * <li><b>Data integrity violations</b> → 409 Conflict</li>
 * <li><b>Uncaught exceptions</b> → 500 Internal Server Error</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

        private static final String TIMESTAMP_KEY = "timestamp";

        // ---- Optimistic Locking (JPA / Hibernate) --------------------------------

        @ExceptionHandler({
                        OptimisticLockException.class,
                        StaleObjectStateException.class,
                        ObjectOptimisticLockingFailureException.class
        })
        public ProblemDetail handleOptimisticLock(Exception ex) {
                ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                                HttpStatus.CONFLICT,
                                "O recurso foi modificado por outro usuário. Atualize os dados e tente novamente.");
                problem.setTitle("Conflito de Concorrência");
                problem.setType(URI.create("https://nexus-wms.com/errors/optimistic-lock"));
                problem.setProperty(TIMESTAMP_KEY, Instant.now());
                return problem;
        }

        // ---- Bean Validation (@Valid) --------------------------------------------

        @Override
        @Nullable
        protected ResponseEntity<Object> handleMethodArgumentNotValid(
                        MethodArgumentNotValidException ex,
                        HttpHeaders headers,
                        HttpStatusCode status,
                        WebRequest request) {

                Map<String, String> fieldErrors = new HashMap<>();
                for (FieldError error : ex.getBindingResult().getFieldErrors()) {
                        fieldErrors.put(error.getField(), error.getDefaultMessage());
                }

                ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                                HttpStatus.UNPROCESSABLE_CONTENT,
                                "A validação falhou para um ou mais campos.");
                problem.setTitle("Erro de Validação");
                problem.setType(URI.create("https://nexus-wms.com/errors/validation"));
                problem.setProperty(TIMESTAMP_KEY, Instant.now());
                problem.setProperty("fieldErrors", fieldErrors);

                return ResponseEntity
                                .status(HttpStatus.UNPROCESSABLE_CONTENT)
                                .body(problem);
        }

        // ---- Data Integrity (unique constraint violations, FK errors) ------------

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
                ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                                HttpStatus.CONFLICT,
                                "Operação viola uma restrição de integridade do banco de dados.");
                problem.setTitle("Violação de Integridade");
                problem.setType(URI.create("https://nexus-wms.com/errors/data-integrity"));
                problem.setProperty(TIMESTAMP_KEY, Instant.now());
                return problem;
        }

        // ---- Business Exceptions ------------------------------------------------

        @ExceptionHandler(ResourceNotFoundException.class)
        public ProblemDetail handleResourceNotFound(ResourceNotFoundException ex) {
                ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                                HttpStatus.NOT_FOUND,
                                ex.getMessage());
                problem.setTitle("Recurso não encontrado");
                problem.setType(URI.create("https://nexus-wms.com/errors/not-found"));
                problem.setProperty(TIMESTAMP_KEY, Instant.now());
                return problem;
        }

        @ExceptionHandler(BusinessException.class)
        public ProblemDetail handleBusinessException(BusinessException ex) {
                ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                                HttpStatus.BAD_REQUEST,
                                ex.getMessage());
                problem.setTitle("Erro de Negócio");
                problem.setType(URI.create("https://nexus-wms.com/errors/business-rule"));
                problem.setProperty(TIMESTAMP_KEY, Instant.now());
                return problem;
        }

        // ---- Catch-all for unexpected errors ------------------------------------

        @ExceptionHandler(Exception.class)
        public ProblemDetail handleGeneric(Exception ex) {
                ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "Ocorreu um erro interno inesperado. Tente novamente mais tarde.");
                problem.setTitle("Erro Interno");
                problem.setType(URI.create("https://nexus-wms.com/errors/internal"));
                problem.setProperty(TIMESTAMP_KEY, Instant.now());
                return problem;
        }
}
