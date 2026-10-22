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
public class SlaPauseResponseDto {
    private Integer id;
    private String reason;
    private String pausedByName;
    private LocalDateTime pausedAt;
    private String resumedByName;
    private LocalDateTime resumedAt;
    private long durationMinutes;
    private boolean active;
}
