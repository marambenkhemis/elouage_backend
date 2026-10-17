package com.elouage.trajet;

import java.time.LocalDate;

public record TrajetResponse(
        Long id, LocalDate date, Long ligneId, String villeDestination, Long louagisteId, String louagisteNom,
        String matricule, boolean present, int rang, int placesLibres) {

    public static TrajetResponse from(Trajet t, int placesLibres) {
        String prenom = t.getLouagiste().getPrenom() == null ? "" : t.getLouagiste().getPrenom();
        String nom = t.getLouagiste().getNom() == null ? "" : t.getLouagiste().getNom();
        return new TrajetResponse(t.getId(), t.getDate(), t.getLigne().getId(), t.getLigne().getVilleDestination(),
                t.getLouagiste().getId(), (prenom + " " + nom).trim(), t.getLouagiste().getMatricule(),
                t.isPresent(), t.getRang(), placesLibres);
    }
}
