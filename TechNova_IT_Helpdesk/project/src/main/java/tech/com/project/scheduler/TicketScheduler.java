package tech.com.project.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.com.project.model.Ticket;
import tech.com.project.model.TicketStatus;
import tech.com.project.repository.TicketRepository;
import tech.com.project.repository.TicketStatusRepository;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class TicketScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(TicketScheduler.class);

    private final TicketRepository ticketRepository;
    private final TicketStatusRepository statusRepository;

    public TicketScheduler(
            TicketRepository ticketRepository,
            TicketStatusRepository statusRepository) {
        this.ticketRepository = ticketRepository;
        this.statusRepository = statusRepository;
    }

    // Se ejecuta diariamente a las 2:00 a. m.
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void autoCloseResolvedTickets() {

        log.info("Iniciando cierre automático de tickets resueltos.");

        LocalDateTime thresholdDate =
                LocalDateTime.now().minusDays(1);

        TicketStatus resolvedStatus;
        resolvedStatus = statusRepository
                .findByName("RESUELTA")
                .orElseThrow(() -> new IllegalStateException(
                        "Estado RESUELTA no encontrado."));

        TicketStatus closedStatus = statusRepository
                .findByName("CERRADA")
                .orElseThrow(() -> new IllegalStateException(
                        "Estado CERRADA no encontrado."));

        List<Ticket> ticketsToClose =
                ticketRepository.findByStatusAndUpdatedAtBefore(
                        resolvedStatus, thresholdDate);

        for (Ticket ticket : ticketsToClose) {
            ticket.setStatus(closedStatus);

            log.info(
                    "Ticket ID {} cerrado automáticamente por inactividad.",
                    ticket.getId());
        }

        ticketRepository.saveAll(ticketsToClose);

        log.info(
                "Tarea finalizada. Total de tickets cerrados: {}",
                ticketsToClose.size());
    }
}
