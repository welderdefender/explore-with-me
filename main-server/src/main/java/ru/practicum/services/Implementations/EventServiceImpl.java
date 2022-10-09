package ru.practicum.services.Implementations;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.dto.events.*;
import ru.practicum.errors.exceptions.BadRequestException;
import ru.practicum.errors.exceptions.NotFoundException;
import ru.practicum.mappers.EventMapper;
import ru.practicum.models.Category;
import ru.practicum.models.Event;
import ru.practicum.models.User;
import ru.practicum.repositories.CategoryRepository;
import ru.practicum.repositories.RequestRepository;
import ru.practicum.repositories.UserRepository;
import ru.practicum.repositories.events.CombineEventFilters;
import ru.practicum.repositories.events.EventRepository;
import ru.practicum.services.EventService;
import ru.practicum.states.EventSortBy;
import ru.practicum.states.EventState;
import ru.practicum.statistics.service.StatisticService;
import ru.practicum.utilities.DateTime;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class EventServiceImpl implements EventService {
    private final CategoryRepository categoryRepository;
    private final RequestRepository requestRepository;
    private final StatisticService statisticService;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    public EventFullDto create(long ownerId, CreateEventDto CreateEventDto) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователя с таким id не существует!"));

        Category category = categoryRepository.findById(CreateEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Категории с таким id не существует!"));

        Event event = EventMapper.toEvent(CreateEventDto);
        event.setOwner(owner);
        event.setCategory(category);
        event = eventRepository.save(event);
        return EventMapper.toEventFullDto(event);
    }

    @Override
    public EventFullDto update(long userId, UpdateEventRequest updateEventRequest) {
        Event sourceEvent = getEventAndCheckOwner(userId, updateEventRequest.getEventId());

        if ((sourceEvent.getState() != EventState.PENDING)
                && (sourceEvent.getState() != EventState.CANCELED)) {
            throw new BadRequestException("Обновить можно только PENDING и CANCELED события!");
        }
        if (updateEventRequest.getEventDate() != null) {
            LocalDateTime newDateTime = DateTime.stringToDateTime(updateEventRequest.getEventDate());
            if (newDateTime.minusHours(2).isBefore(LocalDateTime.now())) {
                throw new BadRequestException("Некорректные временные рамки!");
            }
        }
        EventMapper.prepareToUpdate(updateEventRequest, sourceEvent);
        Event event = eventRepository.save(sourceEvent);
        return getFullDto(event);
    }

    @Override
    public List<EventShortDto> findUserEvents(long id, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<EventShortDto> events = eventRepository.findEventsByOwnerId(id, pageable).stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());

        fullFillDto(events);
        return events;
    }

    @Override
    public EventFullDto removeUserEvent(long userId, long eventId) {
        Event event = getEventAndCheckOwner(userId, eventId);

        if (event.getState() != EventState.PENDING) {
            throw new BadRequestException("Отменить можно только PENDING события!");
        }
        event.setState(EventState.CANCELED);
        eventRepository.save(event);
        return getFullDto(event);
    }

    @Override
    public EventFullDto update(long eventId, AdminUpdateEvent adminUpdateEvent) {
        Event initialEvent = eventRepository.findById(eventId).get();
        EventMapper.prepareToUpdate(adminUpdateEvent, initialEvent);
        Event event = eventRepository.save(initialEvent);
        return getFullDto(event);
    }

    @Override
    public EventFullDto findUserEventById(long userId, long eventId) {
        Event event = getEventAndCheckOwner(userId, eventId);
        return getFullDto(event);
    }

    @Override
    public List<EventFullDto> findFullEvents(CombineEventFilters combineEventFilters, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.extendedSearchByFilters(combineEventFilters, EventSortBy.EVENT_DATE,
                pageable);

        List<EventFullDto> eventsDto = events.stream()
                .map(EventMapper::toEventFullDto)
                .collect(Collectors.toList());
        fullFillDto(eventsDto);
        return eventsDto;
    }

    @Override
    public List<EventShortDto> findShortEvents(CombineEventFilters combineEventFilters, EventSortBy sort,
                                               Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.extendedSearchByFilters(combineEventFilters, sort, pageable);

        List<EventShortDto> eventShortDto = events.stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());
        fullFillDto(eventShortDto);

        if (sort == EventSortBy.VIEWS) {
            return eventShortDto.stream()
                    .sorted((o1, o2) -> (int) (o1.getViews() - o2.getViews()))
                    .collect(Collectors.toList());
        } else {
            return eventShortDto;
        }
    }

    @Override
    public EventFullDto findEventById(Long eventId) {
        Event event = getAndCheckEvent(eventId);
        return getFullDto(event);
    }

    @Override
    public <T extends EventShortDto> void fullFillDto(List<T> listDto) {
        Set<Long> events = listDto.stream().map(EventShortDto::getId).collect(Collectors.toSet());
        Map<Long, Long> statisticsCount = statisticService.getEventViewCount(events);

        listDto.forEach(dto -> {
            dto.setViews(statisticsCount.get(dto.getId()));
            dto.setConfirmedRequests(requestRepository.countApprovedRequests(dto.getId()));
        });
    }

    @Override
    public EventFullDto publish(long id) {
        Event event = getAndCheckEvent(id);
        LocalDateTime publishTime = LocalDateTime.now();

        if (event.getEventDate().minusHours(1).isBefore(publishTime)) {
            throw new BadRequestException("Некорректные временные рамки!");
        }
        if (event.getState() != EventState.PENDING) {
            throw new BadRequestException("Опубликовать можно только PENDING события!");
        }
        event.setPublished(publishTime);
        event.setState(EventState.PUBLISHED);
        eventRepository.save(event);
        return getFullDto(event);
    }

    @Override
    public EventFullDto decline(long eventId) {
        Event event = getAndCheckEvent(eventId);

        if (event.getState() == EventState.PUBLISHED) {
            throw new BadRequestException("Нельзя публиковать это событие!");
        }
        event.setState(EventState.CANCELED);
        eventRepository.save(event);
        return getFullDto(event);
    }

    private EventFullDto getFullDto(Event event) {
        EventFullDto eventFullDto = EventMapper.toEventFullDto(event);
        eventFullDto.setViews(statisticService.getEventViewCount(event.getId()));
        eventFullDto.setConfirmedRequests(requestRepository.countApprovedRequests(event.getId()));
        return eventFullDto;
    }

    private Event getAndCheckEvent(long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("События с таким id не существует!"));

    }

    private Event getEventAndCheckOwner(long userId, long eventId) {
        Event event = getAndCheckEvent(eventId);

        if (!event.getOwner().getId().equals(userId)) {
            throw new BadRequestException("Нельзя редактировать чужое событие!");
        }
        return event;
    }
}