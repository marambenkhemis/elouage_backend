package com.elouage.trajet;

import java.time.LocalDateTime;

// une ligne de la file de depart (US16)
public record FileDepartResponse(
        int rang, Long trajetId, Long louagisteId, String louagisteNom, String matricule,
        LocalDateTime heureCheckIn) {

    public static FileDepartResponse from(Trajet t) {
        String prenom = t.getLouagiste().getPrenom() == null ? "" : t.getLouagiste().getPrenom();
        String nom = t.getLouagiste().getNom() == null ? "" : t.getLouagiste().getNom();
        return new FileDepartResponse(t.getRang(), t.getId(), t.getLouagiste().getId(),
                (prenom + " " + nom).trim(), t.getLouagiste().getMatricule(), t.getHeureCheckIn());
    }
}
