package com.elouage.planning;

import com.elouage.trajet.Trajet;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record PlanningResponse(
        Long id, Long louagisteId, LocalDate dateDebutSemaine, LocalDate dateCloture, List<JourPlanning> jours) {

    // un jour du planning = un trajet sur une ligne (un jour aller-retour = 2 trajets, le 2e avec retour = true)
    public record JourPlanning(Long trajetId, LocalDate date, Long ligneId, String villeDepart,
                               String villeDestination, boolean present, boolean retour, LocalTime heureArrivee) { }

    public static PlanningResponse from(PlanningHebdomadaire p) {
        List<JourPlanning> jours = p.getTrajets().stream()
                .map(PlanningResponse::jour)
                .toList();
        return new PlanningResponse(p.getId(), p.getLouagiste().getId(), p.getDateDebutSemaine(),
                p.getDateCloture(), jours);
    }

    private static JourPlanning jour(Trajet t) {
        String villeDepart = t.getLigne().getStationDepart() == null ? null : t.getLigne().getStationDepart().getVille();
        return new JourPlanning(t.getId(), t.getDate(), t.getLigne().getId(), villeDepart,
                t.getLigne().getVilleDestination(), t.isPresent(), t.isRetour(), t.getHeureArrivee());
    }
}
