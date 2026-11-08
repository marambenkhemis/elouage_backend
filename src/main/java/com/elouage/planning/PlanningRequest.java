package com.elouage.planning;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

//US10: chaque jour contient maintenant la ligne, aller / aller-retour et l'heure d'arrivee a la station
// ex. {"2026-11-09": {"ligneId": 1, "allerRetour": true, "heureArrivee": "07:30"}}
public record PlanningRequest(Long louagisteId, LocalDate dateDebutSemaine, Map<LocalDate, JourRequest> lignesParJour) {

    public record JourRequest(Long ligneId, boolean allerRetour, LocalTime heureArrivee) { }
}