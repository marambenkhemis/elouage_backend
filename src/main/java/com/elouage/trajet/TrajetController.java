package com.elouage.trajet;

import com.elouage.reservation.ReservationResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
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

    // US15  POST http://localhost:8081/api/trajets/1/check-in?louagisteId=1
    @PostMapping("/{id}/check-in")
    public TrajetResponse faireCheckIn(@PathVariable Long id, @RequestParam Long louagisteId) {
        return trajetService.faireCheckIn(id, louagisteId);
    }
    // US18 (liste a cocher)  GET http://localhost:8081/api/trajets/1/presents?louagisteId=1
    @GetMapping("/{id}/presents")
    public List<ReservationResponse> getReservationsPresentes(@PathVariable Long id, @RequestParam Long louagisteId) {
        return trajetService.getReservationsPresentes(id, louagisteId);
    }

    //(passagers actifs du trajet): GET http://localhost:8081/api/trajets/1/reservations?louagisteId=1
    @GetMapping("/{id}/reservations")
    public List<ReservationResponse> getReservations(@PathVariable Long id, @RequestParam Long louagisteId) {
        return trajetService.getReservationsActives(id, louagisteId);
    }

    // US27:  POST http://localhost:8081/api/trajets/1/retour?louagisteId=1
    @PostMapping("/{id}/retour")
    @ResponseStatus(HttpStatus.CREATED)
    public TrajetResponse declarerRetour(@PathVariable Long id, @RequestParam Long louagisteId) {
        return trajetService.declarerRetour(id, louagisteId);
    }

    // US18:  GET http://localhost:8081/api/trajets/1/non-presents?louagisteId=1
    @GetMapping("/{id}/non-presents")
    public List<ReservationResponse> getNonPresents(@PathVariable Long id, @RequestParam Long louagisteId) {
        return trajetService.getNonPresents(id, louagisteId);
    }

    // US18: POST http://localhost:8081/api/trajets/1/absents   {"louagisteId": 1, "reservationIds": [5, 6]}
    @PostMapping("/{id}/absents")
    public LiberationResponse marquerAbsents(@PathVariable Long id, @RequestBody MarquerAbsentsRequest request) {
        return trajetService.marquerAbsents(id, request);
    }

    // US20:  GET http://localhost:8081/api/trajets/1/attente?louagisteId=1
    @GetMapping("/{id}/attente")
    public List<ReservationResponse> getAttente(@PathVariable Long id, @RequestParam Long louagisteId) {
        return trajetService.getAttente(id, louagisteId);
    }

}
