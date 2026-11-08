package com.elouage.ligne;

// US21
public record LigneEtatResponse(
        Long id, String depart, String arrivee, int minimumLouagistes, int nbLouagistesInscrits, boolean sousMinimum) {

    public static LigneEtatResponse from(Ligne l, int nbLouagistesInscrits) {
        String depart = l.getStationDepart() == null ? null : l.getStationDepart().getVille();
        return new LigneEtatResponse(l.getId(), depart, l.getVilleDestination(),
                l.getMinimumLouagistes(), nbLouagistesInscrits, nbLouagistesInscrits < l.getMinimumLouagistes());
    }
}