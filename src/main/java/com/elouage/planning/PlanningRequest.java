package com.elouage.planning;

import java.time.LocalDate;
import java.util.Map;

// une ligne par jour, ex. {"2026-10-19": 1, "2026-10-20": 2}
public record PlanningRequest(Long louagisteId, LocalDate dateDebutSemaine, Map<LocalDate, Long> lignesParJour) {
}
