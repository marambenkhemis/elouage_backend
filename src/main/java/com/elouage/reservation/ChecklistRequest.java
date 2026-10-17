package com.elouage.reservation;

import java.util.List;

// US18 : les reservations cochees par le louagiste
public record ChecklistRequest(Long trajetId, Long louagisteId, List<Long> reservationIds) {
}
