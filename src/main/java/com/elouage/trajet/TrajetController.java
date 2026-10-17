package com.elouage.trajet;

import com.elouage.reservation.ReservationResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/trajets")
public class TrajetController {

    private final TrajetService trajetService;

    public TrajetController(TrajetService trajetService) {
        this.trajetService = trajetService;
    }

    // GET http://localhost:8081/api/trajets/1
    @GetMapping("/{id}")
    public TrajetResponse getTrajet(@PathVariable Long id) {
        return trajetService.getTrajetResponse(id);
    }

    // US15 + US16  POST http://localhost:8081/api/trajets/1/check-in?louagisteId=1
    @PostMapping("/{id}/check-in")
    public CheckInResponse faireCheckIn(@PathVariable Long id, @RequestParam Long louagisteId) {
        return trajetService.faireCheckIn(id, louagisteId);
    }

    // US16  GET http://localhost:8081/api/trajets/file?ligneId=1&date=2026-10-20
    @GetMapping("/file")
    public List<FileDepartResponse> getFile(
            @RequestParam Long ligneId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return trajetService.getFile(ligneId, date);
    }

    // US18 (liste a cocher)  GET http://localhost:8081/api/trajets/1/presents?louagisteId=1
    @GetMapping("/{id}/presents")
    public List<ReservationResponse> getReservationsPresentes(@PathVariable Long id, @RequestParam Long louagisteId) {
        return trajetService.getReservationsPresentes(id, louagisteId);
    }
}
