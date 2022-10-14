package ru.practicum.controllers.authorized;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.RequestDto;
import ru.practicum.services.RequestService;

import javax.validation.constraints.Positive;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/users")
@Validated
public class RequestControllerAuthorized {
    private final RequestService requestService;

    public RequestControllerAuthorized(RequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping(value = "/{userId}/requests")
    public RequestDto create(@PathVariable @Positive long userId, @RequestParam(required = true) Long eventId) {
        log.info("Пользователь с id={} отправил запрос на участие в событии с id={}", userId, eventId);
        return requestService.create(userId, eventId);
    }

    @GetMapping(value = "/{userId}/requests")
    public List<RequestDto> find(@PathVariable @Positive long userId) {
        log.info("Пользователь с id={} отправил следующие запросы на участие:", userId);
        return requestService.find(userId);
    }

    @GetMapping(value = "/{userId}/events/{eventId}/requests")
    public List<RequestDto> findRequests(@PathVariable @Positive long userId, @PathVariable @Positive long eventId) {
        log.info("Получение информации о запросах на участие в событии с id={} пользователя с id={}", eventId, userId);
        return requestService.findRequests(userId, eventId);
    }


    @PatchMapping(value = "/{userId}/requests/{requestId}/cancel")
    public RequestDto remove(@PathVariable @Positive long userId, @PathVariable @Positive long requestId) {
        log.info("Пользователь с id={} отменил участие в событии с id={}", userId, requestId);
        return requestService.remove(userId, requestId);
    }

    @PatchMapping(value = "/{userId}/events/{eventId}/requests/{reqId}/confirm")
    public RequestDto confirmRequest(@PathVariable @Positive long userId, @PathVariable @Positive long eventId,
                                     @PathVariable @Positive long reqId) {
        log.info("Пользователь с id={} подтвердил участие в событии с id={} по заявке с id={}", userId, eventId, reqId);
        return requestService.confirmRequest(userId, eventId, reqId);
    }

    @PatchMapping(value = "/{userId}/events/{eventId}/requests/{reqId}/reject")
    public RequestDto declineRequest(@PathVariable @Positive long userId, @PathVariable @Positive long eventId,
                                     @PathVariable @Positive long reqId) {
        log.info("Пользователь с id={} отклонил участие в событии с id={} по заявке с id={}", userId, eventId, reqId);
        return requestService.declineRequest(userId, eventId, reqId);
    }
}