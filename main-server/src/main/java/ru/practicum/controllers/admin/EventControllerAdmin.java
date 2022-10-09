package ru.practicum.controllers.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.events.AdminUpdateEvent;
import ru.practicum.dto.events.EventFullDto;
import ru.practicum.repositories.events.CombineEventFilters;
import ru.practicum.services.EventService;
import ru.practicum.states.EventState;
import ru.practicum.utilities.DateTime;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/admin/events")
public class EventControllerAdmin {
    private final EventService eventService;

    public EventControllerAdmin(EventService eventService) {
        this.eventService = eventService;
    }

    @PutMapping(value = "/{eventId}")
    public EventFullDto update(@PathVariable @Positive long eventId, @RequestBody AdminUpdateEvent adminUpdateEvent) {
        log.info("Событие с id={} обновлено!", eventId);
        return eventService.update(eventId, adminUpdateEvent);
    }

    @PatchMapping(value = "/{eventId}/publish")
    public EventFullDto publish(@PathVariable @Positive long eventId) {
        log.info("Событие с id={} опубликовано!", eventId);
        return eventService.publish(eventId);
    }

    @PatchMapping(value = "/{eventId}/reject")
    public EventFullDto decline(@PathVariable @Positive long eventId) {
        log.info("Событие с id={} отклонено!", eventId);
        return eventService.decline(eventId);
    }

    @GetMapping
    public List<EventFullDto> findFullEvents(
            @RequestParam(required = false) Long[] users, @RequestParam(required = false) EventState[] states,
            @RequestParam(required = false) Long[] categories, @RequestParam(required = false) String rangeStart,
            @RequestParam(required = false) String rangeEnd,
            @RequestParam(required = false, defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(required = false, defaultValue = "10") @Positive Integer size) {

        CombineEventFilters combineEventFilters = CombineEventFilters.builder()
                .users(users)
                .states(states)
                .categories(categories)
                .rangeStart(DateTime.stringToDateTime(rangeStart))
                .rangeEnd(DateTime.stringToDateTime(rangeEnd))
                .build();
        log.info("По запрашиваемым критериям найдены следующие события");
        return eventService.findFullEvents(combineEventFilters, from, size);
    }
}