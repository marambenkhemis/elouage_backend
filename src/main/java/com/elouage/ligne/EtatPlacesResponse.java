package com.elouage.ligne;

import java.util.List;

public record EtatPlacesResponse(Long trajetId, int placesLibres, List<PlaceEtat> places) {
    public record PlaceEtat(int numero, boolean libre) { }
}
