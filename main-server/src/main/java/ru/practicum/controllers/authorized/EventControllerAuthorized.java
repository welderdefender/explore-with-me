package ru.practicum.controllers.authorized;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.events.CreateEventDto;
import ru.practicum.dto.events.EventFullDto;
import ru.practicum.dto.events.EventShortDto;
import ru.practicum.dto.events.UpdateEventRequest;
import ru.practicum.services.EventService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/users")
public class EventControllerAuthorized {
    private final EventService eventService;

    public EventControllerAuthorized(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping(value = "/{userId}/events")
    public EventFullDto create(@PathVariable @Positive long userId, @Valid @RequestBody CreateEventDto createEventDto) {
        log.info("Пользователь с id={} создал событие {}", userId, createEventDto);
        return eventService.create(userId, createEventDto);
    }

    @PatchMapping(value = "/{userId}/events")
    public EventFullDto update(@PathVariable @Positive long userId,
                               @Valid @RequestBody UpdateEventRequest updateEventRequest) {
        log.info("Пользователь с id={} обновил событие {}", userId, updateEventRequest);
        return eventService.update(userId, updateEventRequest);
    }

    @GetMapping(value = "/{userId}/events")
    public List<EventShortDto> findUserEvents(@PathVariable @Positive long userId, @RequestParam(required = false,
            defaultValue = "0") @PositiveOrZero Integer from,
                                              @RequestParam(required = false, defaultValue = "10") @Positive Integer size) {
        log.info("Пользователь с id={} примет участие в следующих событиях: ", userId);
        return eventService.findUserEvents(userId, from, size);
    }

    @GetMapping(value = "/{userId}/events/{eventId}")
    public EventFullDto findUserEventById(@PathVariable @Positive long userId, @PathVariable @Positive long eventId) {
        log.info("Получение информации о событии с id={}, добавленном пользователем с id={}", eventId, userId);
        return eventService.findUserEventById(userId, eventId);
    }

    @PatchMapping(value = "/{userId}/events/{eventId}")
    public EventFullDto removeUserEvent(@PathVariable @Positive long userId, @PathVariable @Positive long eventId) {
        log.info("Пользователь с id={} отменил событие с id={}", userId, eventId);
        return eventService.removeUserEvent(userId, eventId);
    }
}