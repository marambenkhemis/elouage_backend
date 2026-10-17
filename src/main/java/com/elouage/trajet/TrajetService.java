package com.elouage.trajet;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TrajetService {
    private final TrajetRepository trajetRepository;

    public TrajetService(TrajetRepository trajetRepository) {
        this.trajetRepository = trajetRepository;
    }

    public List<Trajet> getTrajets(Long ligneId, LocalDate date) {
        return trajetRepository.findByLigneIdAndDate(ligneId, date);
    }

    // TEMPORARY: all 8 seats free. The real version removes the reserved seats.
    public List<Integer> getPlacesLibres(Long trajetId) {
        return List.of(1, 2, 3, 4, 5, 6, 7, 8);
    }
}
