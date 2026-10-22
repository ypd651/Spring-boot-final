package tech.com.project.service;

import tech.com.project.dto.request.SlaPauseRequestDto;
import tech.com.project.dto.response.SlaPauseResponseDto;
import tech.com.project.dto.response.SlaStatusResponseDto;

import java.util.List;

public interface SlaPauseService {

    SlaStatusResponseDto pause(Integer ticketId, SlaPauseRequestDto request);

    /** Reanuda el SLA y extiende el dueDate del ticket por el tiempo que estuvo en pausa. */
    SlaStatusResponseDto resume(Integer ticketId);

    SlaStatusResponseDto getStatus(Integer ticketId);

    List<SlaPauseResponseDto> getPauses(Integer ticketId);
}
