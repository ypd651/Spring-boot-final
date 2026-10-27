package tech.com.project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.com.project.dto.request.SlaPauseRequestDto;
import tech.com.project.dto.response.SlaPauseResponseDto;
import tech.com.project.dto.response.SlaStatusResponseDto;
import tech.com.project.model.Comment;
import tech.com.project.model.SlaPause;
import tech.com.project.model.Ticket;
import tech.com.project.model.UserAccount;
import tech.com.project.repository.CommentRepository;
import tech.com.project.repository.SlaPauseRepository;
import tech.com.project.repository.TicketRepository;
import tech.com.project.service.SlaPauseService;
import tech.com.project.service.support.TicketAccessSupport;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlaPauseServiceImpl implements SlaPauseService {

    private final SlaPauseRepository pauseRepository;
    private final TicketRepository ticketRepository;
    private final CommentRepository commentRepository;
    private final TicketAccessSupport access;

    @Override
    @Transactional
    public SlaStatusResponseDto pause(Integer ticketId, SlaPauseRequestDto request) {
        Ticket ticket = access.findTicket(ticketId);
        access.assertAdminOrAssignedTechnician(ticket);

        String status = ticket.getStatus().getName();
        if (!"ASIGNADA".equals(status) && !"EN_PROCESO".equals(status)) {
            throw new IllegalArgumentException("Solo se puede pausar el SLA de tickets ASIGNADA o EN_PROCESO. Estado actual: " + status);
        }
        if (pauseRepository.findFirstByTicketIdAndResumedAtIsNull(ticketId).isPresent()) {
            throw new IllegalArgumentException("El SLA de este ticket ya se encuentra en pausa.");
        }

        UserAccount actor = access.currentUser();
        String reason = request.getReason().trim();
        LocalDateTime now = LocalDateTime.now();

        pauseRepository.save(SlaPause.builder().ticket(ticket).pausedBy(actor).reason(reason).pausedAt(now).build());
        commentRepository.save(Comment.builder().ticket(ticket).author(actor).createdAt(now)
                .content("[SLA en pausa] " + reason).build());
        log.info("SLA pausado en ticket {} por {}", ticket.getCode(), actor.getEmail());
        return buildStatus(ticket);
    }

    @Override
    @Transactional
    public SlaStatusResponseDto resume(Integer ticketId) {
        Ticket ticket = access.findTicket(ticketId);
        access.assertAdminOrAssignedTechnician(ticket);

        SlaPause pause = pauseRepository.findFirstByTicketIdAndResumedAtIsNull(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("El SLA de este ticket no se encuentra en pausa."));

        UserAccount actor = access.currentUser();
        LocalDateTime now = LocalDateTime.now();
        pause.setResumedAt(now);
        pause.setResumedBy(actor);
        pauseRepository.save(pause);

        long pausedMinutes = Duration.between(pause.getPausedAt(), now).toMinutes();
        if (ticket.getDueDate() != null) {
            ticket.setDueDate(ticket.getDueDate().plus(Duration.between(pause.getPausedAt(), now)));
        }
        ticket.setUpdatedAt(now);
        ticketRepository.save(ticket);

        commentRepository.save(Comment.builder().ticket(ticket).author(actor).createdAt(now)
                .content("[SLA reanudado] Tiempo en pausa: " + pausedMinutes + " min. Nuevo vencimiento extendido.").build());
        log.info("SLA reanudado en ticket {} ({} min en pausa)", ticket.getCode(), pausedMinutes);
        return buildStatus(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public SlaStatusResponseDto getStatus(Integer ticketId) {
        Ticket ticket = access.findTicket(ticketId);
        access.assertCanView(ticket);
        return buildStatus(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SlaPauseResponseDto> getPauses(Integer ticketId) {
        Ticket ticket = access.findTicket(ticketId);
        access.assertCanView(ticket);
        LocalDateTime now = LocalDateTime.now();
        return pauseRepository.findByTicketIdWithUsers(ticketId).stream().map(p -> {
            LocalDateTime end = p.getResumedAt() != null ? p.getResumedAt() : now;
            return SlaPauseResponseDto.builder()
                    .id(p.getId())
                    .reason(p.getReason())
                    .pausedByName(p.getPausedBy().getFirstName() + " " + p.getPausedBy().getLastName())
                    .pausedAt(p.getPausedAt())
                    .resumedByName(p.getResumedBy() != null
                            ? p.getResumedBy().getFirstName() + " " + p.getResumedBy().getLastName() : null)
                    .resumedAt(p.getResumedAt())
                    .durationMinutes(Duration.between(p.getPausedAt(), end).toMinutes())
                    .active(p.getResumedAt() == null)
                    .build();
        }).collect(Collectors.toList());
    }

    private SlaStatusResponseDto buildStatus(Ticket ticket) {
        LocalDateTime now = LocalDateTime.now();
        Optional<SlaPause> active = pauseRepository.findFirstByTicketIdAndResumedAtIsNull(ticket.getId());
        long closedMinutes = pauseRepository.findByTicketIdAndResumedAtIsNotNull(ticket.getId()).stream()
                .mapToLong(p -> Duration.between(p.getPausedAt(), p.getResumedAt()).toMinutes()).sum();

        // Con pausa activa el reloj se congela: se mide contra el instante en que se pausó.
        LocalDateTime reference = active.map(SlaPause::getPausedAt).orElse(now);
        long remaining = ticket.getDueDate() != null ? Duration.between(reference, ticket.getDueDate()).toMinutes() : 0;
        boolean finished = "RESUELTA".equals(ticket.getStatus().getName()) || "CERRADA".equals(ticket.getStatus().getName());

        return SlaStatusResponseDto.builder()
                .ticketId(ticket.getId())
                .ticketCode(ticket.getCode())
                .statusName(ticket.getStatus().getName())
                .paused(active.isPresent())
                .pausedSince(active.map(SlaPause::getPausedAt).orElse(null))
                .pauseReason(active.map(SlaPause::getReason).orElse(null))
                .dueDate(ticket.getDueDate())
                .totalPausedMinutes(closedMinutes)
                .remainingMinutes(remaining)
                .breached(!finished && ticket.getDueDate() != null && remaining < 0)
                .build();
    }
}
