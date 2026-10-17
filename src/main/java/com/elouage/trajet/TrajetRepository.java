package com.elouage.trajet;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TrajetRepository extends JpaRepository<Trajet, Long> {
    List<Trajet> findByLigneIdAndDate(Long ligneId, LocalDate date);

    // US16 : la file de depart = louagistes presents, dans l'ordre d'arrivee
    List<Trajet> findByLigneIdAndDateAndPresentTrueOrderByRangAsc(Long ligneId, LocalDate date);

    // verrou en ecriture sur un trajet (reservation d'une place)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Trajet t where t.id = :id")
    Optional<Trajet> findByIdForUpdate(@Param("id") Long id);

    // verrou sur tous les trajets d'une ligne pour un jour : deux check-in simultanes n'auront jamais le meme rang
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Trajet t where t.ligne.id = :ligneId and t.date = :date order by t.id")
    List<Trajet> findAllByLigneAndDateForUpdate(@Param("ligneId") Long ligneId, @Param("date") LocalDate date);

    @Query("select t.ligne.id from Trajet t where t.id = :id")
    Optional<Long> findLigneIdById(@Param("id") Long id);

    @Query("select t.date from Trajet t where t.id = :id")
    Optional<LocalDate> findDateById(@Param("id") Long id);
}
