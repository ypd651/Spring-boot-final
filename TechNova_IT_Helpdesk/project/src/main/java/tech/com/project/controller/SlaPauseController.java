package tech.com.project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.com.project.dto.request.SlaPauseRequestDto;
import tech.com.project.dto.response.SlaPauseResponseDto;
import tech.com.project.dto.response.SlaStatusResponseDto;
import tech.com.project.service.SlaPauseService;

import java.util.List;

@RestController
@RequestMapping("/api/tickets/{ticketId}/sla")
@RequiredArgsConstructor
@Tag(name = "Pauar estadso", description = "Pausar y reanudar el reloj del SLA mientras se espera al usuario")
public class SlaPauseController {

    private final SlaPauseService slaPauseService;

    @GetMapping
    @Operation(summary = "Estado del SLA del ticket (pausado, vencimiento, minutos restantes)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<SlaStatusResponseDto> getStatus(@PathVariable Integer ticketId) {
        return ResponseEntity.ok(slaPauseService.getStatus(ticketId));
    }

    @PostMapping("/pause")
    @Operation(summary = "Pausar el SLA (ADMIN o técnico asignado; ticket ASIGNADA/EN_PROCESO)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<SlaStatusResponseDto> pause(@PathVariable Integer ticketId, @Valid @RequestBody SlaPauseRequestDto request) {
        return ResponseEntity.ok(slaPauseService.pause(ticketId, request));
    }

    @PostMapping("/resume")
    @Operation(summary = "Reanudar el SLA y extender la fecha límite (ADMIN o técnico asignado)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<SlaStatusResponseDto> resume(@PathVariable Integer ticketId) {
        return ResponseEntity.ok(slaPauseService.resume(ticketId));
    }

    @GetMapping("/pauses")
    @Operation(summary = "Historial de pausas del ticket", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<SlaPauseResponseDto>> getPauses(@PathVariable Integer ticketId) {
        return ResponseEntity.ok(slaPauseService.getPauses(ticketId));
    }
}
