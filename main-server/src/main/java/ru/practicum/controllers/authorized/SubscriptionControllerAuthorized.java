package ru.practicum.controllers.authorized;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.SubscriptionDto;
import ru.practicum.dto.events.EventShortDto;
import ru.practicum.services.SubscriptionService;

import javax.validation.constraints.Positive;
import java.util.List;

@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/subscription")
public class SubscriptionControllerAuthorized {
    private final SubscriptionService subscriptionService;

    @PostMapping(value = "/user/{userId}/friend/{friendId}")
    public SubscriptionDto create(@PathVariable @Positive  long userId,
                                  @PathVariable @Positive long friendId) {
        log.info("Пользователь с id={} подписался на пользователя с id={}", userId, friendId);
        return subscriptionService.create(userId, friendId);
    }

    @DeleteMapping(value = "/{subscriptionId}")
    public void delete(@PathVariable @Positive long subscriptionId) {
        subscriptionService.delete(subscriptionId);
        log.info("Подписка с id={} удалена", subscriptionId);
    }

    @GetMapping(value = "/user/{userId}/events")
    public List<EventShortDto> getEvents(@PathVariable @Positive long userId) {
        log.info("Список событий пользователя с id={}", userId);
        return subscriptionService.getEvents(userId);
    }

    @GetMapping(value = "/user/{userId}")
    public List<SubscriptionDto> getSubscriptions(@PathVariable @Positive long userId) {
        log.info("Список подписок пользователя с id={}", userId);
        return subscriptionService.getSubscriptions(userId);
    }
}