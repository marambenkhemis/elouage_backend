package com.elouage.reservation;

public record ReservationRequest(Long trajetId, Long passagerId, int numeroPlace) {
}
