package com.elouage.reservation;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    // US13  POST http://localhost:8081/api/reservations
    // {"trajetId": 1, "passagerId": 3, "numeroPlace": 3}
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse reserver(@RequestBody ReservationRequest request) {
        return reservationService.reserver(request);
    }

    // Mes reservations   GET http://localhost:8081/api/reservations?passagerId=3
    @GetMapping
    public List<ReservationResponse> getReservations(@RequestParam Long passagerId) {
        return reservationService.getReservations(passagerId);
    }

    // US14  PUT http://localhost:8081/api/reservations/1/annulation?passagerId=3
    @PutMapping("/{id}/annulation")
    public ReservationResponse annuler(@PathVariable Long id, @RequestParam Long passagerId) {
        return reservationService.annuler(id, passagerId);
    }

    // US17  PUT http://localhost:8081/api/reservations/1/presence?passagerId=3
    @PutMapping("/{id}/presence")
    public ReservationResponse signalerPresence(@PathVariable Long id, @RequestParam Long passagerId) {
        return reservationService.signalerPresence(id, passagerId);
    }

    // US18 (un seul passager)  PUT http://localhost:8081/api/reservations/1/embarquement?louagisteId=1
    @PutMapping("/{id}/embarquement")
    public ReservationResponse confirmerEmbarquement(@PathVariable Long id, @RequestParam Long louagisteId) {
        return reservationService.confirmerEmbarquement(id, louagisteId);
    }

    // US18 (checklist complete)  POST http://localhost:8081/api/reservations/checklist
    // {"trajetId": 1, "louagisteId": 1, "reservationIds": [1, 2]}
    @PostMapping("/checklist")
    public List<ReservationResponse> validerChecklist(@RequestBody ChecklistRequest request) {
        return reservationService.validerChecklist(request);
    }
}
