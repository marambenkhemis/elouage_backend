package com.elouage.trajet;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TrajetRepository extends JpaRepository<Trajet, Long> {
    List<Trajet> findByLigneIdAndDate(Long ligneId, LocalDate date);

}
