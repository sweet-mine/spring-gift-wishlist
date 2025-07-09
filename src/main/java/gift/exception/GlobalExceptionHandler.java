package gift.exception;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

// 예외 관리 핸들러
@RestControllerAdvice
public class GlobalExceptionHandler {
    // NOT_FOUND 응답하는 예외처리 핸들러
    @ExceptionHandler({ProductNotFoundException.class, EmptyResultDataAccessException.class, UserNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(RuntimeException e) {
        return e.getMessage();
    }

    // INTERNAL_SERVER_ERROR 응답하는 예외처리 핸들러
    @ExceptionHandler({IncorrectResultSizeDataAccessException.class, DecryptFailedException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleInternalServerError(RuntimeException e) { return e.getMessage(); }

    // FORBIDDEN 응답하는 예외처리 핸들러
    @ExceptionHandler(InvalidProductNameException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleForbidden(RuntimeException e) { return e.getMessage(); }

    // BAD_REQUEST 응답하는 예외처리 핸들러
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequest(MethodArgumentNotValidException e) { return e.getBindingResult().getAllErrors().getFirst().getDefaultMessage(); }

    // CONFLICT 응답하는 예외처리 핸들러
    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleConflict(DuplicateKeyException e) { return e.getMessage(); }

    // UNPROCESSABLE_ENTITY 응답하는 예외처리 핸들러
    @ExceptionHandler(EncryptFailedException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public String handleUnprocessableEntity(EncryptFailedException e) { return e.getMessage(); }

    // UNAUTHORIZED 응답하는 예외처리 핸들러
    @ExceptionHandler(LoginFailedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String handleUnauthorized(LoginFailedException e) { return e.getMessage(); }
}
