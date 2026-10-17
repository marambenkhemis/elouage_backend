package com.elouage.trajet;

import java.time.LocalDateTime;

public record CheckInResponse(Long trajetId, int rang, int louagistesDevant, LocalDateTime heureCheckIn) {
}
