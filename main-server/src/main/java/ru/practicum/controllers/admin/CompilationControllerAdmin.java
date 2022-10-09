package ru.practicum.controllers.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.CreateCompilationDto;
import ru.practicum.services.CompilationService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/admin/compilations")
public class CompilationControllerAdmin {
    private final CompilationService compilationService;

    public CompilationControllerAdmin(CompilationService compilationService) {
        this.compilationService = compilationService;
    }

    @PostMapping
    public CompilationDto create(@Valid @RequestBody CreateCompilationDto createCompilationDto) {
        log.info("Подборка {} создана!", createCompilationDto);
        return compilationService.create(createCompilationDto);
    }

    @PatchMapping(value = "/{compId}/events/{eventId}")
    public void createEvent(@PathVariable @Positive long compId,
                            @PathVariable @Positive long eventId) {
        log.info("Событие {} в подборке {} создано!", eventId, compId);
        compilationService.createEvent(compId, eventId);
    }

    @DeleteMapping(value = "/{compId}")
    public void deleteById(@PathVariable @Positive long compId) {
        compilationService.deleteById(compId);
        log.info("Подборка с id={} удалена!", compId);
    }

    @DeleteMapping(value = "/{compId}/events/{eventId}")
    public void deleteEvent(@PathVariable @Positive long compId,
                            @PathVariable @Positive long eventId) {
        compilationService.deleteEvent(compId, eventId);
        log.info("Событие {} в подборке {} удалено!", eventId, compId);
    }

    @PatchMapping(value = "/{compId}/pin")
    public void pin(@PathVariable @Positive long compId) {
        compilationService.pin(compId);
        log.info("Подборка с id={} закреплена!", compId);
    }

    @DeleteMapping(value = "/{compId}/pin")
    public void unpin(@PathVariable @Positive long compId) {
        compilationService.unpin(compId);
        log.info("Подборка с id={} откреплена!", compId);
    }
}