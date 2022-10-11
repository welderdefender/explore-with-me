package ru.practicum.services.implementations;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.RequestDto;
import ru.practicum.errors.exceptions.BadRequestException;
import ru.practicum.errors.exceptions.NotFoundException;
import ru.practicum.mappers.RequestMapper;
import ru.practicum.models.Event;
import ru.practicum.models.Request;
import ru.practicum.repositories.RequestRepository;
import ru.practicum.repositories.events.EventRepository;
import ru.practicum.services.RequestService;
import ru.practicum.states.EventState;
import ru.practicum.states.RequestState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;

    @Override
    @Transactional
    public RequestDto create(long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("События с таким id не существует!"));

        if (event.getOwner().getId().equals(userId)) {
            throw new BadRequestException("Создатель события не может в нем участвовать!");
        }
        if (event.getState() != EventState.PUBLISHED) {
            throw new BadRequestException("Событие еще не опубликовано!");
        }
        checkRequestLimit(event);
        Request request = Request.builder()
                .userId(userId)
                .eventId(eventId)
                .created(LocalDateTime.now())
                .status(RequestState.PENDING)
                .build();

        if (!event.getRequestModeration()) {
            request.setStatus(RequestState.CONFIRMED);
        }
        return RequestMapper.toParticipationRequestDto(requestRepository.save(request));
    }

    @Override
    public List<RequestDto> find(long userId) {
        return requestRepository.findByUserId(userId).stream()
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RequestDto> findRequests(long userId, long eventId) {
        getAndCheckEvent(userId, eventId);
        return requestRepository.findByEventId(eventId).stream()
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RequestDto remove(long userId, long requestId) {
        Request request = getAndCheckRequest(requestId);


        request.setStatus(RequestState.CANCELED);
        return RequestMapper.toParticipationRequestDto(requestRepository.save(request));
    }

    @Override
    @Transactional
    public RequestDto confirmRequest(long userId, long eventId, long requestId) {
        Event event = getAndCheckEvent(userId, eventId);
        Request request = getAndCheckRequest(requestId);

        if (!request.getEventId().equals(eventId)) {
            throw new BadRequestException("Запрос не соответствует событию!");
        }
        checkRequestLimit(event);
        request.setStatus(RequestState.CONFIRMED);
        requestRepository.save(request);

        if (event.getParticipantLimit() > 0) {
            long reqLimit = event.getParticipantLimit();
            long used = requestRepository.countApprovedRequests(eventId);
            if (used >= reqLimit) {
                requestRepository.setRejectedStatusToPendingRequests(eventId);
            }
        }
        return RequestMapper.toParticipationRequestDto(request);
    }

    @Override
    @Transactional
    public RequestDto declineRequest(long userId, long eventId, long requestId) {
        getAndCheckEvent(userId, eventId);
        Request request = getAndCheckRequest(requestId);

        if (!request.getEventId().equals(eventId)) {
            throw new BadRequestException("Запрос не соответствует событию!");
        }
        request.setStatus(RequestState.REJECTED);
        requestRepository.save(request);
        return RequestMapper.toParticipationRequestDto(request);
    }

    private Event getAndCheckEvent(long ownerId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("События с таким id не существует!"));

        if (!event.getOwner().getId().equals(ownerId)) {
            throw new BadRequestException("Нельзя получить доступ к чужому событию!");
        }
        return event;
    }

    private Request getAndCheckRequest(Long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Заявки с таким id не существует!"));
    }

    private void checkRequestLimit(Event event) {
        if (event.getParticipantLimit() > 0) {
            if (event.getParticipantLimit() <= requestRepository.countApprovedRequests(event.getId())) {
                throw new BadRequestException("Мест больше нет, но вы держитесь!");
            }
        }
    }
}