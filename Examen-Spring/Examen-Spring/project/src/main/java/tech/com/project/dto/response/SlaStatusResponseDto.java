package tech.com.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlaStatusResponseDto {
    private Integer ticketId;
    private String ticketCode;
    private String statusName;
    private boolean paused;
    private LocalDateTime pausedSince;
    private String pauseReason;
    private LocalDateTime dueDate;
    /** Minutos acumulados en pausas ya cerradas (ya sumados al dueDate). */
    private long totalPausedMinutes;
    /** Minutos restantes al vencimiento (negativo = vencido). Con pausa activa se congela. */
    private long remainingMinutes;
    private boolean breached;
}
