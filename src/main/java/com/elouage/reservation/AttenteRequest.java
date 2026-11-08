package com.elouage.reservation;

import java.time.LocalDate;
import java.util.Set;

// US19 : un passager rejoint la liste d'attente d'une ligne pour un jour, avec ses plages horaires
public record AttenteRequest(Long passagerId, Long ligneId, LocalDate date, Set<PlageHoraire> plages) {
}
