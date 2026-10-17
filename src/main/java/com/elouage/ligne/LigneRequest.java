package com.elouage.ligne;

public record LigneRequest(
        Long stationDepartId,
        Long stationArriveeId,
        String villeDestination,
        String delegationDestination,
        double prix,
        int minimumLouagistes
) {
}
