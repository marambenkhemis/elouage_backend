package com.elouage.ligne;

import java.time.LocalDate;

public record TrajetResultatResponse(
        Long trajetId, LocalDate date, String villeDepart, String villeDestination,
        double prix, String louagisteNom, String matricule, int placesLibres) { }
