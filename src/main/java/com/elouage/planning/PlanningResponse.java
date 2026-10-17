package com.elouage.planning;

import com.elouage.trajet.Trajet;

import java.time.LocalDate;
import java.util.List;

public record PlanningResponse(
        Long id, Long louagisteId, LocalDate dateDebutSemaine, LocalDate dateCloture, List<JourPlanning> jours) {

    // un jour du planning = un trajet sur une ligne
    public record JourPlanning(Long trajetId, LocalDate date, Long ligneId, String villeDestination, boolean present) { }

    public static PlanningResponse from(PlanningHebdomadaire p) {
        List<JourPlanning> jours = p.getTrajets().stream()
                .map(PlanningResponse::jour)
                .toList();
        return new PlanningResponse(p.getId(), p.getLouagiste().getId(), p.getDateDebutSemaine(),
                p.getDateCloture(), jours);
    }

    private static JourPlanning jour(Trajet t) {
        return new JourPlanning(t.getId(), t.getDate(), t.getLigne().getId(),
                t.getLigne().getVilleDestination(), t.isPresent());
    }
}
