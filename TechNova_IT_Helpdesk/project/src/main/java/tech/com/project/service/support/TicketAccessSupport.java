package tech.com.project.service.support;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import tech.com.project.exception.ResourceNotFoundException;
import tech.com.project.model.Ticket;
import tech.com.project.model.UserAccount;
import tech.com.project.repository.TicketAssignmentRepository;
import tech.com.project.repository.TicketRepository;
import tech.com.project.repository.UserAccountRepository;
import tech.com.project.security.services.UserDetailsImpl;

/**
 * Utilidad compartida por los nuevos módulos: obtiene el usuario autenticado y
 * valida los permisos sobre un ticket con las mismas reglas que TicketServiceImpl
 * (ADMIN todo, TECHNICIAN asignado o solicitante, USER solo solicitante).
 */
@Component
@RequiredArgsConstructor
public class TicketAccessSupport {

    private final TicketRepository ticketRepository;
    private final UserAccountRepository userAccountRepository;
    private final TicketAssignmentRepository ticketAssignmentRepository;

    public UserDetailsImpl currentDetails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserDetailsImpl details)) {
            throw new AccessDeniedException("No se encuentra una sesión de usuario válida o autenticada.");
        }
        return details;
    }

    public UserAccount currentUser() {
        Integer id = currentDetails().getId();
        return userAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("El usuario de la sesión actual no fue encontrado."));
    }

    public boolean isAdmin() {
        return currentDetails().getRoleNames().contains("ADMIN");
    }

    public boolean isTechnician() {
        return currentDetails().getRoleNames().contains("TECHNICIAN");
    }

    public Ticket findTicket(Integer ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket con ID " + ticketId + " no encontrado."));
    }

    public boolean isRequester(Ticket ticket) {
        Integer currentId = currentDetails().getId();
        return ticket.getRequester() != null && ticket.getRequester().getId().equals(currentId);
    }

    public boolean isAssignedTechnician(Ticket ticket) {
        Integer currentId = currentDetails().getId();
        return ticketAssignmentRepository.existsByTicketIdAndTechnicianIdAndUnassignedAtIsNull(ticket.getId(), currentId);
    }

    /** ADMIN: todo. TECHNICIAN: asignado o solicitante. USER: solo solicitante. */
    public void assertCanView(Ticket ticket) {
        if (isAdmin()) {
            return;
        }
        if (isTechnician()) {
            if (!isAssignedTechnician(ticket) && !isRequester(ticket)) {
                throw new AccessDeniedException("No cuenta con permisos sobre este ticket: no está asignado a usted ni es su solicitante.");
            }
            return;
        }
        if (!isRequester(ticket)) {
            throw new AccessDeniedException("No cuenta con permisos para acceder a un ticket ajeno.");
        }
    }

    /** Acción operativa: solo ADMIN o el técnico actualmente asignado al ticket. */
    public void assertAdminOrAssignedTechnician(Ticket ticket) {
        if (isAdmin()) {
            return;
        }
        if (isTechnician() && isAssignedTechnician(ticket)) {
            return;
        }
        throw new AccessDeniedException("Solo un ADMIN o el técnico asignado a este ticket puede realizar esta acción.");
    }
}
