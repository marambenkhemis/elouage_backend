package com.elouage.trajet;

import java.util.List;

// US18 : le front decide qui est absent(le delai de grace), le backend enregistre
public record MarquerAbsentsRequest(Long louagisteId, List<Long> reservationIds) { }
