package ru.practicum.errors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.errors.exceptions.BadRequestException;
import ru.practicum.errors.exceptions.NotFoundException;
import ru.practicum.errors.exceptions.UnavailableStatisticsException;

import javax.validation.ConstraintViolationException;
import java.sql.SQLException;

@RestControllerAdvice
public class ErrorHandler {
    private ResponseEntity<ApiError> makeResponse(Exception e, HttpStatus httpStatus, String message) {
        ApiError error = new ApiError(e, httpStatus, message);
        return new ResponseEntity<>(error, httpStatus);
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> UnavailableStatisticsExceptionHandler(
            final UnavailableStatisticsException e) {
        return makeResponse(e, HttpStatus.INTERNAL_SERVER_ERROR,
                "Сервис статистики не работает");
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> constraintViolationExceptionHandler(final ConstraintViolationException e) {
        return makeResponse(e, HttpStatus.BAD_REQUEST,
                "Запрос составлен неверно");
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> methodArgumentNotValidExceptionHandler(final MethodArgumentNotValidException e) {
        return makeResponse(e, HttpStatus.BAD_REQUEST,
                "Запрос составлен неверно");
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> illegalArgumentExceptionHandler(final IllegalArgumentException e) {
        return makeResponse(e, HttpStatus.BAD_REQUEST,
                "Неверные параметры в операции");
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> NotFoundExceptionHandler(final NotFoundException e) {
        return makeResponse(e, HttpStatus.NOT_FOUND,
                "Запрашиваемый объект в базе данных не найден");
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> sqlExceptionHandler(final SQLException e) {
        return makeResponse(e, HttpStatus.FORBIDDEN,
                "Ошибка чтения/записи в базу данных");
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> BadRequestExceptionHandler(final BadRequestException e) {
        return makeResponse(e, HttpStatus.FORBIDDEN,
                "Недопустимое действие с объектом");
    }
}