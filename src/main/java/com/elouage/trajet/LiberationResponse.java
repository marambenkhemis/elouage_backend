package com.elouage.trajet;

import com.elouage.reservation.ReservationResponse;
import java.util.List;

// US18 : places liberees + passagers en attente promus automatiquement dans ces places
public record LiberationResponse(Long trajetId, List<Integer> placesLiberees, List<ReservationResponse> remplacants) { }