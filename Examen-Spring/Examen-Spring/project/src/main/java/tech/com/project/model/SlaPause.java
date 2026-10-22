package tech.com.project.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sla_pause")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlaPause {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paused_by", nullable = false)
    private UserAccount pausedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resumed_by")
    private UserAccount resumedBy;

    @Column(name = "reason", nullable = false, length = 300)
    private String reason;

    @Column(name = "paused_at", nullable = false)
    private LocalDateTime pausedAt;

    /** Null mientras la pausa sigue activa. */
    @Column(name = "resumed_at")
    private LocalDateTime resumedAt;

    @PrePersist
    public void prePersist() {
        if (pausedAt == null) {
            pausedAt = LocalDateTime.now();
        }
    }
}
