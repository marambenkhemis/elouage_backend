package com.elouage.reservation;

import java.time.LocalTime;

// US19 : plage horaire choisie par le passager en liste d'attente (MargeDisponibilite du diagramme)
public enum PlageHoraire {
    MATIN(0, 12),
    APRES_MIDI(12, 18),
    SOIR(18, 24);

    private final int heureDebut;
    private final int heureFin;

    PlageHoraire(int heureDebut, int heureFin) {
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
    }

    // true si l'heure est dans la plage [debut, fin[
    public boolean contient(LocalTime heure) {
        return heure.getHour() >= heureDebut && heure.getHour() < heureFin;
    }
}
