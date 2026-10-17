package com.elouage.reservation;

import java.time.LocalDate;

public record ReservationResponse(
        Long id, Long trajetId, LocalDate dateTrajet, Long passagerId, String passagerNom,
        int numeroPlace, LocalDate dateReservation, StatutReservation statut) {

    public static ReservationResponse from(Reservation r) {
        String prenom = r.getPassager().getPrenom() == null ? "" : r.getPassager().getPrenom();
        String nom = r.getPassager().getNom() == null ? "" : r.getPassager().getNom();
        return new ReservationResponse(
                r.getId(), r.getTrajet().getId(), r.getTrajet().getDate(),
                r.getPassager().getId(), (prenom + " " + nom).trim(),
                r.getNumeroPlace(), r.getDateReservation(), r.getStatut());
    }
}
