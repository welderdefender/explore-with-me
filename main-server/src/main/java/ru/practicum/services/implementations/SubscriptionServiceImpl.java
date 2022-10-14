package ru.practicum.services.implementations;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.SubscriptionDto;
import ru.practicum.dto.events.EventShortDto;
import ru.practicum.errors.exceptions.NotFoundException;
import ru.practicum.mappers.EventMapper;
import ru.practicum.mappers.SubscriptionMapper;
import ru.practicum.models.Subscription;
import ru.practicum.models.User;
import ru.practicum.repositories.SubscriptionRepository;
import ru.practicum.repositories.UserRepository;
import ru.practicum.services.EventService;
import ru.practicum.services.SubscriptionService;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class SubscriptionServiceImpl implements SubscriptionService {
    private final UserRepository userRepository;
    private final EventService eventService;
    private final SubscriptionRepository subscriptionRepository;

    @Override
    @Transactional
    public SubscriptionDto create(long userId, long friendId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователя с таким id не существует!"));

        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователя с таким id не существует!"));

        Subscription subscription = Subscription.builder()
                .user(user)
                .friend(friend)
                .build();

        return SubscriptionMapper.toSubscriptionDto(subscriptionRepository.save(subscription));
    }

    @Override
    @Transactional
    public void delete(long id) {
        subscriptionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Подписки с таким id не существует!"));

        subscriptionRepository.deleteById(id);
    }

    @Override
    public List<EventShortDto> getEvents(long userId) {
        List<EventShortDto> el = subscriptionRepository.findEventsByUserId(userId).stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());

        eventService.fullFillDto(el);
        return el;
    }

    @Override
    public List<SubscriptionDto> getSubscriptions(long userId) {
        return subscriptionRepository.findByUserId(userId).stream()
                .map(SubscriptionMapper::toSubscriptionDto)
                .collect(Collectors.toList());
    }
}