package com.elouage.ligne;

import java.time.LocalDate;

public record TrajetResultatResponse(
        Long trajetId, LocalDate date,
        String villeDepart, String stationDepart,
        String villeDestination, String stationArrivee,
        double prix, String louagisteNom, String matricule, int placesLibres) { }
