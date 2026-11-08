package com.elouage.reservation;

import com.elouage.trajet.Trajet;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;


public record ReservationResponse(
        Long id, Long trajetId, LocalDate dateTrajet, Long passagerId, String passagerNom,
        int numeroPlace, LocalDate dateReservation, StatutReservation statut,
        Long ligneId, Set<PlageHoraire> margeDisponibilite, LocalTime heureArrivee)
{

    public static ReservationResponse from(Reservation r) {
        String prenom = r.getPassager().getPrenom() == null ? "" : r.getPassager().getPrenom();
        String nom = r.getPassager().getNom() == null ? "" : r.getPassager().getNom();
        // en liste d'attente (US19) il n'y a pas encore de trajet : on renvoie la ligne et le jour souhaites
        Trajet t = r.getTrajet();
        Long trajetId = t != null ? t.getId() : null;
        LocalDate dateTrajet = t != null ? t.getDate() : r.getDateSouhaitee();
        Long ligneId = t != null ? t.getLigne().getId() : (r.getLigne() != null ? r.getLigne().getId() : null);
        LocalTime heureArrivee = t != null ? t.getHeureArrivee() : null; //heure d'arrivée
        return new ReservationResponse(
                r.getId(), trajetId, dateTrajet,
                r.getPassager().getId(), (prenom + " " + nom).trim(),
                r.getNumeroPlace(), r.getDateReservation(), r.getStatut(),
                ligneId, new HashSet<>(r.getMargeDisponibilite()),
                heureArrivee);
    }
}
