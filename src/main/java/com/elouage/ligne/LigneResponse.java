package com.elouage.ligne;

public record LigneResponse(Long id, String villeDestination) {

    public static LigneResponse from(Ligne l) {
        return new LigneResponse(l.getId(), l.getVilleDestination());
    }
}
