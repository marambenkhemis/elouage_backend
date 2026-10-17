package com.elouage.planning;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface PlanningRepository extends JpaRepository<PlanningHebdomadaire, Long> {
    boolean existsByLouagisteIdAndDateDebutSemaine(Long louagisteId, LocalDate dateDebutSemaine);

    Optional<PlanningHebdomadaire> findByLouagisteIdAndDateDebutSemaine(Long louagisteId, LocalDate dateDebutSemaine);
}
